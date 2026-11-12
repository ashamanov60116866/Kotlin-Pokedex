package dev.marcosfarias.pokedex.api

import dev.marcosfarias.pokedex.model.Pokemon
import dev.marcosfarias.pokedex.repository.PokemonService
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PokeApiClientTest {

    @Test
    fun getPokemonList_requestsConfiguredEndpointAndParsesResponse() {
        val server = MockWebServer()
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""[{"id":"25","name":"pikachu"}]""")
        )
        server.start()

        try {
            val service = Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PokemonService::class.java)

            val response = service.get().execute()

            assertEquals(200, response.code())
            assertNotNull(response.body())
            assertEquals("pikachu", response.body()?.first()?.name)

            val request = server.takeRequest()
            assertEquals("GET", request.method)
            assertEquals("/pokemon.json", request.path)
        } finally {
            server.shutdown()
        }
    }
}
