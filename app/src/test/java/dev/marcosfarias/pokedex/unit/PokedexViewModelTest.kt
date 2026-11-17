package dev.marcosfarias.pokedex.unit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import dev.marcosfarias.pokedex.database.dao.PokemonDAO
import dev.marcosfarias.pokedex.model.Pokemon
import dev.marcosfarias.pokedex.repository.PokemonService
import dev.marcosfarias.pokedex.ui.pokedex.PokedexViewModel
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.slot
import io.mockk.verify
import io.qameta.allure.kotlin.junit4.AllureRunner
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AllureRunner::class)
class PokedexViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var dao: PokemonDAO
    private lateinit var service: PokemonService
    private lateinit var call: Call<List<Pokemon>>
    private lateinit var callbackSlot: io.mockk.CapturingSlot<Callback<List<Pokemon>>>

    @Before
    fun setUp() {
        dao = mockk()
        service = mockk()
        call = mockk()
        callbackSlot = slot()

        every { service.get() } returns call
        every { call.enqueue(capture(callbackSlot)) } just Runs
        every { dao.all() } returns MutableLiveData(emptyList())
        every { dao.add(any()) } just Runs
    }

    @Test
    fun viewModel_initializationStartsNetworkRequest() {
        PokedexViewModel(dao, service)

        verify(exactly = 1) { service.get() }
        verify(exactly = 1) { call.enqueue(any()) }
    }

    @Test
    fun getListPokemon_returnsDaoLiveData() {
        val expected = listOf(Pokemon(id = "25", name = "pikachu"))
        every { dao.all() } returns MutableLiveData(expected)

        val viewModel = PokedexViewModel(dao, service)

        assertEquals(expected, viewModel.getListPokemon().value)
    }

    @Test
    fun successfulResponse_savesPokemonAndDoesNotExposeError() {
        val expected = listOf(Pokemon(id = "25", name = "pikachu"))
        val saved = CountDownLatch(1)
        every { dao.add(expected) } answers {
            saved.countDown()
        }

        val viewModel = PokedexViewModel(dao, service)
        callbackSlot.captured.onResponse(call, Response.success(expected))

        assertEquals(true, saved.await(2, TimeUnit.SECONDS))
        assertEquals(false, viewModel.hasNetworkError.value)
        verify(exactly = 1) { dao.add(expected) }
    }

    @Test
    fun networkFailure_exposesErrorAndDoesNotSavePokemon() {
        val viewModel = PokedexViewModel(dao, service)

        callbackSlot.captured.onFailure(call, IOException("Network error"))

        assertEquals(true, viewModel.hasNetworkError.value)
        verify(exactly = 0) { dao.add(any()) }
    }

    @Test
    fun serverErrorResponse_exposesErrorAndDoesNotSavePokemon() {
        val viewModel = PokedexViewModel(dao, service)
        val errorBody = """{"error":"server error"}"""
            .toResponseBody("application/json".toMediaType())

        callbackSlot.captured.onResponse(
            call,
            Response.error<List<Pokemon>>(500, errorBody)
        )

        assertEquals(true, viewModel.hasNetworkError.value)
        verify(exactly = 0) { dao.add(any()) }
    }
}
