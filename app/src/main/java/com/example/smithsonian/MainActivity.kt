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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.painterResource
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
    FAVORITES
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
                    val buttonColors = listOf(Color.Red, Color.Green, Color.Blue)
                    val buttonTexts = listOf("Search", "Term", "Favorite")
                    val destinations = listOf(
                        Screens.SEARCH.name,
                        Screens.TERMS.name,
                        Screens.FAVORITES.name
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Main Screen",
                            fontSize = 30.sp
                        )
                        for (i in buttonColors.indices) {
                            GenerateClickableRectangle(
                                text = buttonTexts[i],
                                color = buttonColors[i],
                                onClick = { navController.navigate(destinations[i]) }
                            )
                        }
                    }
                }
                // Page where you can search for objects within different categories
                composable(Screens.SEARCH.name) {
                    Column {
                        var tempSearch = true
                        var tempCategory by remember { mutableStateOf("All") }
                        var tempKeyword by remember { mutableStateOf("") }
                        // Buttons for choosing category
                        Row {
                            Button(
                                onClick = {
                                    navController.navigate(Screens.MAIN.name)
                                }
                            ){
                                Text(text ="Back")
                            }
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
                // Page where you can search for objects by terms
                composable(Screens.TERMS.name) {
                    val buttonColors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Gray)
                    val buttonTexts = listOf("culture", "data_source", "date", "place", "topic")
                    val destinations = Screens.SUBTERMS.name

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Terms Screen",
                            fontSize = 30.sp
                        )
                        for (i in buttonColors.indices) {
                            GenerateClickableRectangle(
                                text = buttonTexts[i],
                                color = buttonColors[i],
                                onClick = {
                                    selectedTerm.value = buttonTexts[i]
                                    navController.navigate(Screens.SUBTERMS.name)
                                }
                            )
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

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            GenerateBackButton(navController)
                            GenerateHomeButton(navController)
                        }

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
                    // Display
                    Column {
                        Text(status)
                        DisplayObjects(objectList, trigger, dbman)
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

            }
        }
    }
}

// Composable to generate a clickable rectangle with text
@Composable
fun GenerateClickableRectangle(text: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.Black,
            fontSize = 25.sp
        )
    }
}

// Composable for Back button
@Composable
fun GenerateBackButton(navController: NavController) {
    Box(
        modifier = Modifier
            .width(90.dp)
            .height(45.dp)
            .background(Color.Gray)
            .clickable {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Back",
            color = Color.White,
            fontSize = 18.sp
        )
    }
}

// Composable for Home button
@Composable
fun GenerateHomeButton(navController: NavController) {
    Box(
        modifier = Modifier
            .width(90.dp)
            .height(45.dp)
            .background(Color.Gray)
            .clickable {
                navController.navigate(Screens.MAIN.name)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Home",
            color = Color.White,
            fontSize = 18.sp
        )
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