import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.testandroidstudio.model.Pokemon
import com.example.testandroidstudio.repository.IPokemonRepository
import com.example.testandroidstudio.viewModel.PokemonListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val pokemonRepository: IPokemonRepository = mock(IPokemonRepository::class.java)
    private lateinit var viewModel: PokemonListViewModel

    @Before
    fun setup() {
        viewModel = PokemonListViewModel(pokemonRepository)
    }

    @Test
    fun `test loadPokemonList should update pokemonList`() = runTest {
        val fakePokemonList = listOf(
            Pokemon(id = 1, name = "Pikachu", types = listOf("Electric"), imageUrl = "https://pokeapi.co/media/sprites/pokemon/25.png", description = "This is Pikachu"),
            Pokemon(id = 2, name = "Charmander", types = listOf("Fire"), imageUrl = "https://pokeapi.co/media/sprites/pokemon/4.png", description = "This is Charmander")
        )
        whenever(pokemonRepository.getPokemonList(anyOrNull())).thenReturn(fakePokemonList)

        viewModel.loadPokemonList().join()

        assertEquals(fakePokemonList, viewModel.pokemonList.getOrAwaitValue())
    }

    @Test
    fun `test searchPokemon should update searchResults`() = runTest {
        val fakePokemon = Pokemon(id = 1, name = "Pikachu", types = listOf("Electric"), imageUrl = "https://pokeapi.co/media/sprites/pokemon/25.png", description = "This is Pikachu")
        whenever(pokemonRepository.searchPokemon("Pikachu")).thenReturn(fakePokemon)

        viewModel.searchPokemon("Pikachu").join()

        assertEquals(fakePokemon, viewModel.searchResults.getOrAwaitValue())
    }

    @Test
    fun `test loadMorePokemon should append new pokemon to current list`() = runTest {
        val initialPokemonList = listOf(Pokemon(id = 1, name = "Bulbasaur", types = listOf("Grass"), imageUrl = "https://pokeapi.co/media/sprites/pokemon/1.png", description = "This is Bulbasaur"))
        val newPokemonList = listOf(Pokemon(id = 2, name = "Ivysaur", types = listOf("Grass"), imageUrl = "https://pokeapi.co/media/sprites/pokemon/2.png", description = "This is Ivysaur"))

        whenever(pokemonRepository.getPokemonList(anyOrNull())).thenReturn(initialPokemonList)
        viewModel.loadPokemonList().join()

        whenever(pokemonRepository.getPokemonList(anyOrNull())).thenReturn(newPokemonList)
        viewModel.loadMorePokemon().join()

        val combinedList = initialPokemonList + newPokemonList
        assertEquals(combinedList, viewModel.pokemonList.getOrAwaitValue())
    }

    @Test
    fun `test incrementPage should update current page and change currentPokemonList`() = runTest {
        val fakePokemonList = List(40) { index ->
            Pokemon(id = index, name = "Pokemon$index", types = listOf("Type"), imageUrl = "", description = "")
        }
        whenever(pokemonRepository.getPokemonList(anyOrNull())).thenReturn(fakePokemonList)

        viewModel.loadPokemonList().join()
        viewModel.incrementPage()

        assertEquals(1, viewModel.currentPage.getOrAwaitValue())

        val expectedList = fakePokemonList.subList(20, 40)
        assertEquals(expectedList, viewModel.currentPokemonList.getOrAwaitValue())
    }

    @Test
    fun `test decrementPage should update current page and change currentPokemonList`() = runTest {
        val fakePokemonList = List(40) { index ->
            Pokemon(id = index, name = "Pokemon$index", types = listOf("Type"), imageUrl = "", description = "")
        }

        whenever(pokemonRepository.getPokemonList(anyOrNull())).thenReturn(fakePokemonList)

        viewModel.loadPokemonList().join()
        viewModel.incrementPage()
        viewModel.decrementPage()

        assertEquals(0, viewModel.currentPage.getOrAwaitValue())

        val expectedList = fakePokemonList.subList(0, 20)
        assertEquals(expectedList, viewModel.currentPokemonList.getOrAwaitValue())
    }

    private fun <T> LiveData<T>.getOrAwaitValue(): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = Observer<T> { o ->
            data = o
            latch.countDown()
        }
        this@getOrAwaitValue.removeObserver(observer)
        this.observeForever(observer)

        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw TimeoutException("LiveData value was never set.")
            }
        } finally {
            this.removeObserver(observer)
        }

        return data as T
    }

}
