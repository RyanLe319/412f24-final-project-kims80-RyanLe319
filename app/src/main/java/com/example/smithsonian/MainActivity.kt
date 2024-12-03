package com.example.smithsonian

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Screens {
    MAIN,
    SEARCH,
    TERMS,
    SUBTERMS,
    ITEMS,
    FAVORITES,
    COLORS
}
val batchSize = 1000


class MyDatabaseManager(context: Context) : SQLiteOpenHelper(context, "MyDb", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("""
            CREATE TABLE IF NOT EXISTS SMITHSONIAN_OBJECTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT, 
                title TEXT UNIQUE,  -- Enforce uniqueness on the 'title' column
                imageUrl TEXT
            )
        """)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    // Check if an object already exists in the database
    fun isObjectExists(title: String): Boolean {
        val query = "SELECT 1 FROM SMITHSONIAN_OBJECTS WHERE title = ?"
        val cursor = readableDatabase.rawQuery(query, arrayOf(title))
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    // Insert a SmithsonianObject only if it doesn't already exist
    fun insertObject(smithsonianObject: SmithsonianObject) {
        if (!isObjectExists(smithsonianObject.title)) { // Check for duplicates
            val query = "INSERT INTO SMITHSONIAN_OBJECTS (title, imageUrl) VALUES(?, ?)"
            val statement = writableDatabase.compileStatement(query)
            statement.bindString(1, smithsonianObject.title)
            statement.bindString(2, smithsonianObject.image)
            statement.executeInsert()
        } else {
            Log.d("DatabaseManager", "Object with title '${smithsonianObject.title}' already exists.")
        }
    }

    fun clearDatabase() {
        val db = writableDatabase
        db.execSQL("DELETE FROM SMITHSONIAN_OBJECTS") // Deletes all rows in the table
        db.close()
    }

    fun deleteObject(title: String){
        if(isObjectExists(title)){
            val query = "DELETE FROM SMITHSONIAN_OBJECTS WHERE title = ?"
            val statement = writableDatabase.compileStatement(query)
            statement.bindString(1, title)
            statement.executeUpdateDelete()
        }
    }


}


// Font stuff
val font = FontFamily(
    Font(R.font.minionpro_regular, FontWeight.Normal),
    Font(R.font.minionpro_medium, FontWeight.Medium),
    Font(R.font.minionpro_bold, FontWeight.Bold),
    Font(R.font.minionpro_italic, style = FontStyle.Italic)
)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val scope = rememberCoroutineScope()
            val navController = rememberNavController()
            var keyword by remember { mutableStateOf("") }
            var searchAll by remember { mutableStateOf(true) }
            var category by remember { mutableStateOf("") }
            val objectList = remember { mutableStateListOf<SmithsonianObject>() }
            val currentRow = remember { mutableIntStateOf(0) }
            val trigger = remember { mutableStateOf(true) }
            var status by remember { mutableStateOf("Waiting for search") }
            var selectedTerm: MutableState<String> =  remember { mutableStateOf("") }
            val dbman = MyDatabaseManager(this)

            // Color stuff
            val currentColors = remember {
                mutableStateListOf(
                    R.color.Tertiary1,
                    R.color.Primary2,
                    R.color.Primary1,
                    R.color.Tertiary2,
                    R.color.Primary3
                )
            }
            val backgroundColor = colorResource(currentColors[0])
            val textColor = colorResource(currentColors[1])
            val iconColor = colorResource(currentColors[2])
            val uiColor = colorResource(currentColors[3])
            val topColor = colorResource(currentColors[4])

            // This LaunchedEffect triggers everytime the trigger is set to false
            // It will add more objects to the current objectList and update the status
            // It will consider the current values of keyword, searchAll, category, and currentRow
            // After it is done, trigger will be set to true again
            LaunchedEffect(trigger.value) {

                if(!objectList.isEmpty() && !trigger.value) {
                    scope.launch(Dispatchers.IO) {
                        status = "Loading..."
                        if(searchAll) {
                            var result: List<SmithsonianObject>
                            do {
                                result = SmithsonianApi.searchGeneral(
                                    keyword = keyword,
                                    start = currentRow.intValue,
                                    rows = batchSize
                                )
                                currentRow.intValue += batchSize
                            } while (result.isEmpty())
                            withContext(Dispatchers.Main) {
                                objectList.addAll(result)
                            }
                        }
                        else {
                            var result: List<SmithsonianObject>
                            do {
                                result = SmithsonianApi.searchCategory(
                                    keyword = keyword,
                                    category = category,
                                    start = currentRow.intValue,
                                    rows = batchSize
                                )
                                currentRow.intValue += batchSize
                            } while (result.isEmpty())
                            withContext(Dispatchers.Main) {
                                objectList.addAll(result)
                            }
                        }
                        trigger.value = true
                        status = "Results:"
                    }
                }
            }

            NavHost(navController, startDestination = Screens.MAIN.name) {
                // Main menu with major options
                composable(Screens.MAIN.name) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor)
                    ) {
                        val buttonTexts = listOf("Search", "Term", "Favorite")
                        val destinations = listOf(
                            Screens.SEARCH.name,
                            Screens.TERMS.name,
                            Screens.FAVORITES.name
                        )
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            TopBar(false, false, topColor, iconColor, textColor, navController)
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .weight(1f),
                                verticalArrangement = Arrangement.SpaceEvenly,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Smithsonian Institution Open Access",
                                    fontSize = 64.sp,
                                    color = textColor,
                                    fontFamily = font,
                                    fontWeight = FontWeight.Bold
                                )
                                for (i in buttonTexts.indices) {
                                    GenerateClickableRectangle(
                                        text = buttonTexts[i],
                                        buttonColor = uiColor,
                                        textColor = textColor,
                                        font = font,
                                        onClick = { navController.navigate(destinations[i]) }
                                    )
                                }
                            }
                        }
                    }
                }
                // Page where you can search for objects within different categories
                composable(Screens.SEARCH.name) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            TopBar(true, false, topColor, iconColor, textColor, navController)
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                var tempSearch = true
                                var tempCategory by remember { mutableStateOf("All") }
                                var tempKeyword by remember { mutableStateOf("") }
                                // Buttons for choosing category
                                Row {
                                    Button(
                                        onClick = {
                                            tempSearch = true
                                            tempCategory = "All"
                                        }
                                    ) {
                                        Text("All")
                                    }
                                    Button(
                                        onClick = {
                                            tempSearch = false
                                            tempCategory = "art_design"
                                        }
                                    ) {
                                        Text("Art Design")
                                    }
                                }
                                Row {
                                    Button(
                                        onClick = {
                                            tempSearch = false
                                            tempCategory = "history_culture"
                                        }
                                    ) {
                                        Text("History Culture")
                                    }
                                    Button(
                                        onClick = {
                                            tempSearch = false
                                            tempCategory = "science_technology"
                                        }
                                    ) {
                                        Text("Science Technology")
                                    }
                                }
                                // Text field for entering keyword and search button
                                Row {
                                    TextField(
                                        value = tempKeyword,
                                        onValueChange = {
                                            tempKeyword = it
                                        },
                                        label = {Text("Category: $tempCategory")},
                                        placeholder = {Text("Enter search keyword")}
                                    )
                                    Button(
                                        onClick = {
                                            searchAll = tempSearch
                                            category = tempCategory
                                            keyword = tempKeyword
                                            currentRow.intValue = 0
                                            objectList.clear()
                                            // Make sure there is at least one object in the object list for new search
                                            scope.launch(Dispatchers.IO) {
                                                status = "Loading..."
                                                if(searchAll) {
                                                    var result: List<SmithsonianObject>
                                                    do {
                                                        result = SmithsonianApi.searchGeneral(
                                                            keyword = keyword,
                                                            start = currentRow.intValue,
                                                            rows = batchSize
                                                        )
                                                        currentRow.intValue += batchSize
                                                    } while (result.isEmpty())
                                                    withContext(Dispatchers.Main) {
                                                        objectList.addAll(result)
                                                    }
                                                }
                                                else {
                                                    var result: List<SmithsonianObject>
                                                    do {
                                                        result = SmithsonianApi.searchCategory(
                                                            keyword = keyword,
                                                            category = category,
                                                            start = currentRow.intValue,
                                                            rows = batchSize
                                                        )
                                                        currentRow.intValue += batchSize
                                                    } while (result.isEmpty())
                                                    withContext(Dispatchers.Main) {
                                                        objectList.addAll(result)
                                                    }
                                                }
                                                status = "Results:"
                                            }
                                        }
                                    ) {
                                        Text("Search")
                                    }
                                }
                                Text(status)
                                // Display of items searched
                                DisplayObjects(objectList, trigger, dbman)
                            }
                        }
                    }
                }
                // Page where you can search for objects by terms
                composable(Screens.TERMS.name) {
                    val buttonTexts = listOf("culture", "data_source", "date", "place", "topic")
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            TopBar(true, false, topColor, iconColor, textColor, navController)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceEvenly,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Choose a term category",
                                    fontSize = 64.sp,
                                    color = textColor,
                                    fontFamily = font,
                                    fontWeight = FontWeight.Bold
                                )
                                for (i in buttonTexts.indices) {
                                    GenerateClickableRectangle(
                                        text = buttonTexts[i],
                                        buttonColor = uiColor,
                                        textColor = textColor,
                                        font = font,
                                        onClick = {
                                            selectedTerm.value = buttonTexts[i]
                                            navController.navigate(Screens.SUBTERMS.name)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                // Page where you can select sub terms to search
                composable(Screens.SUBTERMS.name) {
                    val termsList = remember { mutableStateListOf<String>() }
                    val isLoading = remember { mutableStateOf(true) }

                    LaunchedEffect(true) {
                        isLoading.value = true
                        scope.launch(Dispatchers.IO) {
                            try {
                                val fetchedTerms = SmithsonianApi.searchTerms(selectedTerm.value)
                                withContext(Dispatchers.Main) {
                                    termsList.clear()
                                    termsList.addAll(fetchedTerms)
                                }
                            } catch (e: Exception) {
                                termsList.clear()
                            } finally {
                                isLoading.value = false
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            TopBar(true, true, topColor, iconColor, textColor, navController)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (isLoading.value) {
                                    Text(text = "Loading...")
                                }
                                else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = 16.dp)
                                    ) {
                                        items(termsList) { term ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(Color.LightGray)
                                                    .border(2.dp, Color.Gray, RoundedCornerShape(16.dp))
                                            ) {
                                                Text(
                                                    text = term,
                                                    fontSize = 20.sp,
                                                    modifier = Modifier
                                                        .padding(8.dp)
                                                        .clickable {
                                                            keyword = term
                                                            searchAll = true
                                                            currentRow.intValue = 0
                                                            objectList.clear()
                                                            navController.navigate(Screens.ITEMS.name)
                                                        }
                                                        .fillParentMaxWidth()
                                                        .padding(5.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(5.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // Page to display the term search objects
                composable(Screens.ITEMS.name) {
                    // Add some initial objects
                    LaunchedEffect(true) {
                        scope.launch(Dispatchers.IO) {
                            status = "Loading..."
                            var result: List<SmithsonianObject>
                            do {
                                result = SmithsonianApi.searchGeneral(
                                    keyword = keyword,
                                    start = currentRow.intValue,
                                    rows = batchSize
                                )
                                currentRow.intValue += batchSize
                            } while (result.isEmpty())
                            withContext(Dispatchers.Main) {
                                objectList.addAll(result)
                            }
                            status = "Results:"
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            TopBar(true, true, topColor, iconColor, textColor, navController)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(16.dp)
                            ) {
                                Text(status)
                                DisplayObjects(objectList, trigger, dbman)
                            }
                        }
                    }
                }
                // Page where you can see objects that were added to favorites
                composable(Screens.FAVORITES.name) {
                    val favoritesList = remember { mutableStateListOf<SmithsonianObject>() }
                    val isLoading = remember { mutableStateOf(true) }

                    // Fetch data from the database when the Favorites screen is displayed
                    LaunchedEffect(Unit) {
                        isLoading.value = true
                        scope.launch(Dispatchers.IO) {
                            try {
                                val cursor = dbman.readableDatabase.rawQuery("SELECT * FROM SMITHSONIAN_OBJECTS", null)
                                val fetchedFavorites = mutableListOf<SmithsonianObject>()
                                while (cursor.moveToNext()) {
                                    val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                                    val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                                    val imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("imageUrl"))
                                    fetchedFavorites.add(SmithsonianObject(id = id.toString(), title = title, image = imageUrl))
                                }
                                cursor.close()

                                withContext(Dispatchers.Main) {
                                    favoritesList.clear()
                                    favoritesList.addAll(fetchedFavorites)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                isLoading.value = false
                            }
                        }
                    }

                    Column {
                        Text(
                            text = "Favorites",
                            fontSize = 30.sp,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                        if (isLoading.value) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Loading...")
                            }
                        } else if (favoritesList.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No Favorites Found")
                            }
                        } else {
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(3),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                items(favoritesList.size) { index ->
                                    val favorite = favoritesList[index]

                                    Column(
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        AsyncImage(
                                            model = favorite.image,
                                            contentDescription = favorite.title,
                                            placeholder = painterResource(R.drawable.placeholder),
                                            modifier = Modifier
                                                .fillMaxWidth()

                                        )
                                        Text(
                                            text = favorite.title,

                                        )
                                        Button(onClick = {

                                            dbman.deleteObject(favorite.title)
                                            favoritesList.removeAt(index)
                                        }) {
                                            Text("Delete")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }


                // Page where you can choose app color scheme
                composable(Screens.COLORS.name) {
                }
            }
        }
    }
}

// Composable to generate a clickable rectangle with text
@Composable
fun GenerateClickableRectangle(text: String, buttonColor: Color, textColor: Color, font: FontFamily, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(buttonColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 25.sp,
            fontFamily = font,
            fontWeight = FontWeight.Medium
        )
    }
}

// Composable for Back button
@Composable
fun GenerateBackButton(navController: NavController, iconColor: Color) {
    IconButton(
        onClick = {
            if(navController.previousBackStackEntry != null) {
                navController.popBackStack()
            }
        },
        modifier = Modifier.fillMaxHeight()
    ) {
        Icon(
            painter = painterResource(R.drawable.back_button),
            contentDescription = "Back Button",
            tint = iconColor,
            modifier = Modifier.fillMaxHeight()
        )
    }
}

// Composable for Home button
@Composable
fun GenerateHomeButton(navController: NavController, iconColor: Color) {
    IconButton(
        onClick = {
                navController.navigate(Screens.MAIN.name)
        },
        modifier = Modifier.fillMaxHeight()
    ) {
        Icon(
            painter = painterResource(R.drawable.home_button),
            contentDescription = "Home Button",
            tint = iconColor,
            modifier = Modifier.fillMaxHeight()
        )
    }
}

// Top bar for each page
@Composable
fun TopBar(back: Boolean, home: Boolean, topColor: Color, iconColor: Color, textColor: Color, navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(topColor)
            .height(60.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.width(100.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                if(back) {
                    GenerateBackButton(navController, iconColor)
                }
            }
            Row(
                modifier = Modifier.width(150.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.smithsonianlogo),
                    contentDescription = "Smithsonian Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.fillMaxHeight()
                        .padding(10.dp)

                )
                Text("Smithsonian Institute", fontFamily = font, fontSize = 16.sp, color = textColor)
            }
            Row(
                modifier = Modifier.width(100.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if(home) {
                    GenerateHomeButton(navController, iconColor)
                }
            }
        }
    }
}

@Composable
fun DisplayObjects(
    objectList: SnapshotStateList<SmithsonianObject>,
    trigger: MutableState<Boolean>,
    dbman: MyDatabaseManager
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(3)
    ) {
        items(objectList.size) { index ->
            if (index == objectList.size - 1 && trigger.value) {
                trigger.value = false
            }

            Column {
                AsyncImage(
                    model = objectList[index].image,
                    contentDescription = objectList[index].title,
                    placeholder = painterResource(R.drawable.placeholder)
                )
                Text(objectList[index].title)

                Button(onClick = {
                    val objectToFavorite = SmithsonianObject(
                        id = "",
                        title = objectList[index].title,
                        image = objectList[index].image
                    )
                    dbman.insertObject(objectToFavorite)
                }) {
                    Text("Favorite")
                }
            }
        }
    }
}


// Composable to display a list of term options for a term category
@Composable
fun DisplayTermOptions(termList: List<String>) {

}

// Composable to display a dialogue for each Smithsonian object
@Composable
fun DisplayDialogue(onDismissRequest: () -> Unit, obj: SmithsonianObject) {

}