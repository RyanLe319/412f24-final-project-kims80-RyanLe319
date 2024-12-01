package com.example.smithsonian

import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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

            LaunchedEffect(true) {
                scope.launch(Dispatchers.IO) {

                }
            }

            NavHost(navController, startDestination = Screens.MAIN.name) {
                // Main menu with major options
                composable(Screens.MAIN.name) {

                }
                // Page where you can search for objects within different categories
                composable(Screens.SEARCH.name) {

                }
                // Page where you can search for objects by terms
                composable(Screens.TERMS.name) {

                }
                // Page where you can select sub terms to search
                composable(Screens.SUBTERMS.name) {

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