package com.example.smithsonian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smithsonian.ui.theme.SmithsonianTheme

enum class Screens {
    MAIN,
    CATEGORY,
    SEARCH,
    TERMS,
    FAVORITES
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(navController, startDestination = Screens.MAIN.name) {
                // Main menu with major categories
                composable(Screens.MAIN.name) {
                    Column {
                        Text("Main Menu")
                        Button(
                            onClick = {
                                navController.navigate(Screens.CATEGORY.name)
                            }
                        ) {
                            Text("Category")
                        }
                    }
                }
                // Page where you can search for objects within specific categories
                composable(Screens.CATEGORY.name) {

                }
                // Page where you can search for objects by terms
                composable(Screens.TERMS.name) {

                }
                // Page where you can search for items from all available objects
                composable(Screens.SEARCH.name) {

                }
                // Page where you can see objects that were added to favorites
                composable(Screens.FAVORITES.name) {

                }
            }
        }
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