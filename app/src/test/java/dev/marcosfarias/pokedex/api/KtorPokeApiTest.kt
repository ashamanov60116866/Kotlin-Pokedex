package dev.marcosfarias.pokedex.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.qameta.allure.kotlin.junit4.AllureRunner
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AllureRunner::class)
class KtorPokeApiTest {

    private fun createApi(
        expectedPath: String,
        status: HttpStatusCode,
        responseBody: String
    ): Pair<KtorPokeApi, HttpClient> {
        val engine = MockEngine { request ->
            assertEquals(expectedPath, request.url.encodedPath)
            respond(
                content = responseBody,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = HttpClient(engine)
        return KtorPokeApi(client) to client
    }

    @Test
    fun pokemonEndpoint_returnsPokemonForValidId() = runBlocking {
        val (api, client) = createApi(
            "/api/v2/pokemon/25",
            HttpStatusCode.OK,
            """{"id":25,"name":"pikachu"}"""
        )
        try {
            val response = api.pokemon("25")
            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("pikachu"))
        } finally {
            client.close()
        }
    }

    @Test
    fun pokemonEndpoint_returnsNotFoundForUnknownPokemon() = runBlocking {
        val (api, client) = createApi(
            "/api/v2/pokemon/unknown-pokemon",
            HttpStatusCode.NotFound,
            """{"detail":"Not found."}"""
        )
        try {
            assertEquals(HttpStatusCode.NotFound, api.pokemon("unknown-pokemon").status)
        } finally {
            client.close()
        }
    }

    @Test
    fun typeEndpoint_returnsTypeForValidName() = runBlocking {
        val (api, client) = createApi(
            "/api/v2/type/fire",
            HttpStatusCode.OK,
            """{"id":10,"name":"fire"}"""
        )
        try {
            val response = api.type("fire")
            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("fire"))
        } finally {
            client.close()
        }
    }

    @Test
    fun typeEndpoint_returnsNotFoundForUnknownType() = runBlocking {
        val (api, client) = createApi(
            "/api/v2/type/not-a-type",
            HttpStatusCode.NotFound,
            """{"detail":"Not found."}"""
        )
        try {
            assertEquals(HttpStatusCode.NotFound, api.type("not-a-type").status)
        } finally {
            client.close()
        }
    }

    @Test
    fun abilityEndpoint_returnsAbilityForValidName() = runBlocking {
        val (api, client) = createApi(
            "/api/v2/ability/overgrow",
            HttpStatusCode.OK,
            """{"id":65,"name":"overgrow"}"""
        )
        try {
            val response = api.ability("overgrow")
            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("overgrow"))
        } finally {
            client.close()
        }
    }

    @Test
    fun abilityEndpoint_returnsServerErrorWhenApiFails() = runBlocking {
        val (api, client) = createApi(
            "/api/v2/ability/overgrow",
            HttpStatusCode.InternalServerError,
            """{"detail":"Internal server error"}"""
        )
        try {
            assertEquals(HttpStatusCode.InternalServerError, api.ability("overgrow").status)
        } finally {
            client.close()
        }
    }
}
