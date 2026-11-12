package dev.marcosfarias.pokedex.blackbox

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PokemonSearchScenarioTest {

    @Test
    fun homeScreenShowsSearchPrompt() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        val packageName = instrumentation.targetContext.packageName
        val intent = instrumentation.targetContext.packageManager
            .getLaunchIntentForPackage(packageName)

        requireNotNull(intent)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        instrumentation.targetContext.startActivity(intent)

        assertTrue(
            "Search prompt is not visible on the home screen",
            device.wait(
                Until.hasObject(By.textContains("Search Pokemon")),
                5_000
            )
        )
    }
}
