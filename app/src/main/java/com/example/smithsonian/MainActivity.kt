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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smithsonian.ui.theme.SmithsonianTheme

enum class Screens {
    MAINMENU,
    SECONDMENU,
    ITEMS,
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(navController, startDestination = Screens.MAINMENU.name) {
                // Main menu with major categories
                composable(Screens.MAINMENU.name) {
                    Column {
                        Text("Main Menu")
                        Button(
                            onClick = {
                                navController.navigate(Screens.SECONDMENU.name)
                            }
                        ) {
                            Text("Second Menu")
                        }
                    }
                }
                // Secondary menu with subcategories
                composable(Screens.SECONDMENU.name) {
                    Column {
                        Text("Second Menu")
                        Button(
                            onClick = {
                                navController.navigate(Screens.MAINMENU.name)
                            }
                        ) {
                            Text("Main Menu")
                        }
                        Button(
                            onClick = {
                                navController.navigate(Screens.ITEMS.name)
                            }
                        ) {
                            Text("Items Page")
                        }
                    }
                }
                // Items page for showing artifacts
                composable(Screens.ITEMS.name) {
                    Column {
                        Text("Items Page")
                        Button(
                            onClick = {
                                navController.navigate(Screens.SECONDMENU.name)
                            }
                        ) {
                            Text("Second Menu")
                        }
                    }
                }
            }
        }
    }
}