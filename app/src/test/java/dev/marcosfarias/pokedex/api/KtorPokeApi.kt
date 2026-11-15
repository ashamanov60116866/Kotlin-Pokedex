package dev.marcosfarias.pokedex.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse

/**
 * Small Ktor client used by API contract tests against the public PokéAPI.
 * The production application continues to use its existing Retrofit service.
 */
internal class KtorPokeApi(
    private val client: HttpClient,
    private val baseUrl: String = "https://pokeapi.co/api/v2/"
) {
    suspend fun pokemon(idOrName: String): HttpResponse =
        client.get("$baseUrl/pokemon/$idOrName")

    suspend fun type(nameOrId: String): HttpResponse =
        client.get("$baseUrl/type/$nameOrId")

    suspend fun ability(nameOrId: String): HttpResponse =
        client.get("$baseUrl/ability/$nameOrId")
}
