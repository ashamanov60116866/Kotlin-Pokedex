package dev.marcosfarias.pokedex.api

import dev.marcosfarias.pokedex.repository.PokemonService
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PokeApiServiceTest {

    private fun createService(server: MockWebServer): PokemonService =
        Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PokemonService::class.java)

    @Test
    fun getPokemonList_returnsNotFoundFor404() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(404))
        server.start()

        try {
            val response = createService(server).get().execute()
            assertEquals(404, response.code())
            assertFalse(response.isSuccessful)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun getPokemonList_returnsServerErrorFor500() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(500))
        server.start()

        try {
            val response = createService(server).get().execute()
            assertEquals(500, response.code())
            assertFalse(response.isSuccessful)
        } finally {
            server.shutdown()
        }
    }
}
