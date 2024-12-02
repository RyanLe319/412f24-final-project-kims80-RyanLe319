package com.example.smithsonian

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells

import androidx.compose.material3.Button
import androidx.compose.material3.Label
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import androidx.compose.ui.res.painterResource

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Screens {
    MAIN,
    SEARCH,
    TERMS,
    SUBTERMS,
    FAVORITES
}
val batchSize = 1000

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

            LaunchedEffect(trigger.value) {
                Log.d("%%%", "Launched!!!")
                if(!objectList.isEmpty() && !trigger.value) {
                    Log.d("^^^", "Adding more to list")
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
                        Log.d("&&&", "New list size ${objectList.size}")
                        Log.d("###", "Trigger set to true")
                        trigger.value = true
                        status = "Results:"
                    }
                }
            }

            NavHost(navController, startDestination = Screens.SEARCH.name) {

                // Main menu with major options
                composable(Screens.MAIN.name) {
                    MainScreen(navController = navController)
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
                        DisplayObjects(objectList, trigger)
                    }
                }
                // Page where you can search for objects by terms
                composable(Screens.TERMS.name) {
                    // Terms screen content
                }
                // Page where you can select sub terms to search
                composable(Screens.SUBTERMS.name) {
                    // Subterms screen content
                }
                // Page where you can see objects that were added to favorites
                composable(Screens.FAVORITES.name) {
                    // Favorites screen content
                }
            }
        }
    }
}


// Main Screen with 3 buttons
@Composable
fun MainScreen(navController: NavController) {
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

        // Create buttons dynamically from the lists
        for (i in buttonColors.indices) {
            GenerateClickableRectangle(
                text = buttonTexts[i],
                color = buttonColors[i],
                onClick = { navController.navigate(destinations[i]) }
            )
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


// Composable to display list of objects in a lazy staggered grid

@Composable
fun DisplayObjects(objectList: SnapshotStateList<SmithsonianObject>,
                   trigger: MutableState<Boolean>
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(3)
    ) {
        items(objectList.size) {index ->
            if(index == objectList.size - 1 && trigger.value) {
                trigger.value = false
                Log.d("###", "Trigger set to false")
            }
            Column {
                AsyncImage(
                    model = objectList[index].image,
                    contentDescription = objectList[index].title,
                    placeholder = painterResource(R.drawable.placeholder)
                )
                Text(objectList[index].title)
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