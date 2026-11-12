package dev.marcosfarias.pokedex.blackbox

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import dev.marcosfarias.pokedex.model.Pokemon
import dev.marcosfarias.pokedex.repository.PokemonService
import io.mockk.every
import io.mockk.mockk
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module
import org.koin.dsl.single
import retrofit2.Call
import retrofit2.Callback
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class PokemonErrorScenarioTest {

    @Test
    fun pokedexRemainsVisibleWhenPokemonRequestFails() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        val context = instrumentation.targetContext
        val packageName = context.packageName
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)

        requireNotNull(intent)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)

        assertTrue(device.wait(Until.hasObject(By.text("Pokedex")), 5_000))

        val failedService = mockk<PokemonService>()
        val failedCall = mockk<Call<List<Pokemon>>>()
        every { failedService.get() } returns failedCall
        every { failedCall.enqueue(any()) } answers {
            firstArg<Callback<List<Pokemon>>>().onFailure(
                failedCall,
                IOException("Simulated network failure")
            )
        }

        val testModule = module {
            single<PokemonService>(override = true) { failedService }
        }
        loadKoinModules(testModule)

        try {
            device.findObject(By.text("Pokedex")).click()
            assertTrue(
                "Pokédex crashed or failed to display after a network error",
                device.wait(
                    Until.hasObject(By.res(packageName, "recyclerView")),
                    5_000
                )
            )
        } finally {
            unloadKoinModules(testModule)
        }
    }
}
