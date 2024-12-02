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
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smithsonian.ui.theme.SmithsonianTheme
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()
            val navController = rememberNavController()
            val batchSize = 1000

            NavHost(navController, startDestination = Screens.MAIN.name) {
                // Main menu with major options
                composable(Screens.MAIN.name) {
                    MainScreen(navController = navController)
                }
                // Page where you can search for objects within different categories
                composable(Screens.SEARCH.name) {
                    // Search screen content
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

// Composable to display a lazy staggered grid with given object list
@Composable
fun DisplayObjects(objectList: SnapshotStateList<SmithsonianObject>) {

}

// Composable to display a list of term options for a term category
@Composable
fun DisplayTermOptions(termList: List<String>) {

}

// Composable to display a dialogue for each Smithsonian object
@Composable
fun DisplayDialogue(onDismissRequest: () -> Unit, obj: SmithsonianObject) {

}