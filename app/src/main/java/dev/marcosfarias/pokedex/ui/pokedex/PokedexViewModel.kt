package dev.marcosfarias.pokedex.ui.pokedex

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.marcosfarias.pokedex.database.dao.PokemonDAO
import dev.marcosfarias.pokedex.model.Pokemon
import dev.marcosfarias.pokedex.repository.PokemonService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.concurrent.thread

class PokedexViewModel(
    private val pokemonDAO: PokemonDAO,
    private val pokemonService: PokemonService
) : ViewModel() {

    private val _hasNetworkError = MutableLiveData(false)
    val hasNetworkError: LiveData<Boolean> = _hasNetworkError

    init {
        initNetworkRequest()
    }

    private fun initNetworkRequest() {
        pokemonService.get().enqueue(object : Callback<List<Pokemon>> {
            override fun onResponse(
                call: Call<List<Pokemon>>,
                response: Response<List<Pokemon>>
            ) {
                val pokemons = response.body()
                if (response.isSuccessful && pokemons != null) {
                    _hasNetworkError.postValue(false)
                    thread(name = "pokemon-database-write") {
                        pokemonDAO.add(pokemons)
                    }
                } else {
                    _hasNetworkError.postValue(true)
                }
            }

            override fun onFailure(call: Call<List<Pokemon>>, throwable: Throwable) {
                _hasNetworkError.postValue(true)
            }
        })
    }

    fun getListPokemon() = pokemonDAO.all()
}
