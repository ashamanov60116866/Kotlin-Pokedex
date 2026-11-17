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

/**
 * Black-box error scenario: force the device proxy to an unreachable local port.
 * No repository, service or dependency is mocked in this test.
 */
@RunWith(AllureAndroidJUnit4::class)
class PokemonErrorScenarioTest {

    @Test
    fun pokedexShowsNetworkErrorWhenApiCannotBeReached() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        val context = instrumentation.targetContext
        val packageName = context.packageName
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)

        requireNotNull(intent) { "Launcher activity was not found" }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

        try {
            device.executeShellCommand("settings put global http_proxy 127.0.0.1:9")
            context.startActivity(intent)

            assertTrue(device.wait(Until.hasObject(By.text("Pokedex")), 5_000))
            device.findObject(By.text("Pokedex")).click()

            assertTrue(
                "Network error message was not displayed",
                device.wait(Until.hasObject(By.res(packageName, "networkError")), 15_000)
            )
        } finally {
            device.executeShellCommand("settings put global http_proxy :0")
        }
    }
}
