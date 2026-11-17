
package dev.marcosfarias.pokedex.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import dev.marcosfarias.pokedex.MainActivity
import dev.marcosfarias.pokedex.R
import dev.marcosfarias.pokedex.database.dao.PokemonDAO
import dev.marcosfarias.pokedex.model.Pokemon
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(AllureAndroidJUnit4::class)
class PokemonDetailsScreenTest {

    private val testPokemon = Pokemon(
        id = "test-999",
        name = "Testmon",
        typeofpokemon = listOf("Grass"),
        hp = 45,
        attack = 49,
        defense = 49
    )

    @Test
    fun selectingPokemonOpensDashboardDetails() {
        // Получаем настоящий DAO из Koin.
        val pokemonDAO = GlobalContext.get().get<PokemonDAO>()

        // Заполняем Room до запуска Activity.
        runDbOperation {
            pokemonDAO.add(listOf(testPokemon))
        }

        // Запускаем Activity только после подготовки данных.
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        var idlingResource: RecyclerViewItemsIdlingResource? = null

        try {
            // Переходим на экран Pokedex.
            onView(withText("Pokedex"))
                .perform(click())

            onView(withId(R.id.recyclerView))
                .check(matches(isDisplayed()))

            lateinit var recyclerView: RecyclerView

            scenario.onActivity { activity ->
                recyclerView = activity.findViewById(R.id.recyclerView)
            }

            // Ожидаем появления элементов, отслеживая актуальный адаптер.
            idlingResource = RecyclerViewItemsIdlingResource(recyclerView)
            IdlingRegistry.getInstance().register(idlingResource)

            // Ищем именно тестового покемона, а не элемент с позицией 0.
            onView(withId(R.id.recyclerView))
                .perform(
                    RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                        hasDescendant(withText("Testmon")),
                        click()
                    )
                )

            // Проверяем экран деталей и данные выбранного покемона.
            onView(withId(R.id.textViewName))
                .check(matches(isDisplayed()))
                .check(matches(withText("Testmon")))

            onView(withId(R.id.textViewID))
                .check(matches(isDisplayed()))
                .check(matches(withText("test-999")))

        } finally {
            idlingResource?.let {
                IdlingRegistry.getInstance().unregister(it)
                it.unregister()
            }

            scenario.close()

            // Удаляем тестовую запись, не затрагивая остальные данные.
            runDbOperation {
                pokemonDAO.delete(testPokemon)
            }
        }
    }

    private fun runDbOperation(block: () -> Unit) {
        val executor = Executors.newSingleThreadExecutor()

        try {
            executor.submit {
                block()
            }.get(10, TimeUnit.SECONDS)
        } finally {
            executor.shutdownNow()
        }
    }
}

/**
 * IdlingResource, который отслеживает замену RecyclerView.adapter.
 * Это важно, потому что PokedexFragment создаёт новый PokemonAdapter
 * при каждом обновлении LiveData.
 */
class RecyclerViewItemsIdlingResource(
    private val recyclerView: RecyclerView
) : IdlingResource {

    private var resourceCallback: IdlingResource.ResourceCallback? = null

    private var observedAdapter: RecyclerView.Adapter<*>? = null

    private val observer = object : RecyclerView.AdapterDataObserver() {

        override fun onChanged() {
            notifyIfIdle()
        }

        override fun onItemRangeInserted(
            positionStart: Int,
            itemCount: Int
        ) {
            notifyIfIdle()
        }

        override fun onItemRangeChanged(
            positionStart: Int,
            itemCount: Int
        ) {
            notifyIfIdle()
        }
    }

    private val layoutChangeListener =
        View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            attachCurrentAdapter()
            notifyIfIdle()
        }

    init {
        recyclerView.addOnLayoutChangeListener(layoutChangeListener)
        attachCurrentAdapter()
    }

    override fun getName(): String =
        "RecyclerViewItemsIdlingResource"

    override fun isIdleNow(): Boolean {
        attachCurrentAdapter()
        return observedAdapter?.itemCount?.let { it > 0 } ?: false
    }

    override fun registerIdleTransitionCallback(
        callback: IdlingResource.ResourceCallback
    ) {
        resourceCallback = callback
        notifyIfIdle()
    }

    private fun attachCurrentAdapter() {
        val currentAdapter = recyclerView.adapter ?: return

        if (currentAdapter !== observedAdapter) {
            observedAdapter?.unregisterAdapterDataObserver(observer)

            observedAdapter = currentAdapter
            currentAdapter.registerAdapterDataObserver(observer)
        }
    }

    private fun notifyIfIdle() {
        attachCurrentAdapter()

        if (isIdleNow) {
            resourceCallback?.onTransitionToIdle()
        }
    }

    fun unregister() {
        recyclerView.removeOnLayoutChangeListener(layoutChangeListener)
        observedAdapter?.unregisterAdapterDataObserver(observer)
        observedAdapter = null
        resourceCallback = null
    }
}