package dev.marcosfarias.pokedex.blackbox

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
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

        requireNotNull(intent)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)

        assertTrue(device.wait(Until.hasObject(By.text("Pokedex")), 5_000))
        device.findObject(By.text("Pokedex")).click()

        assertTrue(
            "Pokemon list did not open",
            device.wait(Until.hasObject(By.res(packageName, "recyclerView")), 5_000)
        )
        val pokemonList = device.findObject(By.res(packageName, "recyclerView"))
        val pokemonCards = pokemonList.children
        assertTrue("Pokemon list is empty", pokemonCards.isNotEmpty())
        pokemonCards.first().click()

        assertTrue(
            "Pokemon details screen did not open",
            device.wait(Until.hasObject(By.res(packageName, "textViewName")), 10_000)
        )
    }
}
