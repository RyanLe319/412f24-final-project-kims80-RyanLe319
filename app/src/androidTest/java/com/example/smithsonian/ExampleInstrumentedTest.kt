import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import com.example.smithsonian.MainActivity
import org.junit.Rule
import org.junit.Test

class ExampleInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSearchButtonNavigation() {
        // Launch the MainActivity composable
        composeTestRule.setContent {
            MainActivity()
        }

        // Find the "Search" button by its test tag
        composeTestRule.onNodeWithTag("search_button")
            .assertIsDisplayed() // Ensure the button is displayed
            .performClick() // Simulate a click action

        // Verify that the navigation occurs (navigate to the "Search" screen)
        composeTestRule.onNodeWithText("Search") // Check for the presence of the "Search" screen
            .assertIsDisplayed()
    }
}
