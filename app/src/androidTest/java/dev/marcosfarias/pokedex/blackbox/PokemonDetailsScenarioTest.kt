
package dev.marcosfarias.pokedex.blackbox

import android.content.Intent
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AllureAndroidJUnit4::class)
class PokemonDetailsScenarioTest {

    @Test
    fun userCanOpenPokemonDetailsFromList() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        val context = instrumentation.targetContext
        val packageName = context.packageName

        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        requireNotNull(intent) { "Launcher activity was not found" }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)

        assertTrue(
            "Pokedex button did not appear",
            device.wait(Until.hasObject(By.text("Pokedex")), 10_000)
        )

        device.findObject(By.text("Pokedex")).click()

        assertTrue(
            "Pokemon list did not open",
            device.wait(
                Until.hasObject(By.res(packageName, "recyclerView")),
                10_000
            )
        )

        // RecyclerView appears before asynchronous API/Room loading finishes.
        // Wait until at least one Pokemon card is actually displayed.
        val deadline = SystemClock.uptimeMillis() + 30_000
        var pokemonCards = emptyList<androidx.test.uiautomator.UiObject2>()

        while (SystemClock.uptimeMillis() < deadline) {
            val pokemonList =
                device.findObject(By.res(packageName, "recyclerView"))

            pokemonCards = pokemonList?.children ?: emptyList()

            if (pokemonCards.isNotEmpty()) {
                break
            }

            SystemClock.sleep(500)
        }

        assertTrue(
            "Pokemon list is still empty after waiting 30 seconds. " +
                "Check API availability and Room database population.",
            pokemonCards.isNotEmpty()
        )

        pokemonCards.first().click()

        assertTrue(
            "Pokemon details screen did not open",
            device.wait(
                Until.hasObject(By.res(packageName, "textViewName")),
                10_000
            )
        )
    }
}
