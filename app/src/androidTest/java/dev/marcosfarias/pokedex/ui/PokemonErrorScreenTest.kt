package dev.marcosfarias.pokedex.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import dev.marcosfarias.pokedex.MainActivity
import dev.marcosfarias.pokedex.R
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AllureAndroidJUnit4::class)
class PokemonErrorScreenTest {

    @Test
    fun pokedexDisplaysErrorMessageWhenNetworkIsUnavailable() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)

        try {
            device.executeShellCommand("settings put global http_proxy 127.0.0.1:9")
            ActivityScenario.launch(MainActivity::class.java).use {
                onView(withText("Pokedex")).check(matches(isDisplayed()))
                onView(withText("Pokedex")).perform(click())
                onView(withId(R.id.networkError)).check(matches(isDisplayed()))
            }
        } finally {
            device.executeShellCommand("settings put global http_proxy :0")
        }
    }
}
