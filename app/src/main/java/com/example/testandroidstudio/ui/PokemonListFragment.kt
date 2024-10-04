package com.example.testandroidstudio.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testandroidstudio.R
import com.example.testandroidstudio.utility.Constants
import com.example.testandroidstudio.utility.PokemonUiState
import com.example.testandroidstudio.viewModel.PokemonListViewModel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class PokemonListFragment : Fragment() {
    private val viewModel: PokemonListViewModel by activityViewModels()
    private lateinit var adapter: PokemonListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var titleTextView: TextView
    private lateinit var searchEditText: TextInputEditText
    private lateinit var navigationLayout: LinearLayout
    private lateinit var nextPageButton: Button
    private lateinit var previousPageButton: Button
    private lateinit var loadingImageView: ImageView

    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(viewModel.pokemonList.value == null) {
            viewModel.loadPokemonList()
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_pokemon_list_view, container, false)

        recyclerView = view.findViewById(R.id.pokemonRecyclerView)
        searchEditText = view.findViewById(R.id.searchEditText)
        navigationLayout = view.findViewById(R.id.navigationLayout)
        nextPageButton = view.findViewById(R.id.nextButton)
        previousPageButton = view.findViewById(R.id.previousButton)
        titleTextView = view.findViewById(R.id.titleTextView)
        loadingImageView = view.findViewById(R.id.loadingImageView)

        adapter = PokemonListAdapter()

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.pokemonUiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PokemonUiState.Loading -> {
                    showLoading(true)
                }
                is PokemonUiState.Success -> {
                    showLoading(false)
                }
                is PokemonUiState.Error -> {
                    showLoading(false)
                    showDialog(state.message)
                }
            }
        }

        setupSearch()
        setupRecyclerView()

        val titleText = "PokemonBox"
        val spannableString = SpannableString(titleText)
        spannableString.setSpan(StyleSpan(Typeface.NORMAL), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(StyleSpan(Typeface.BOLD), 7, titleText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        titleTextView.text = spannableString

        navigationLayout.visibility = View.GONE

        viewModel.currentPokemonList.observe(viewLifecycleOwner) { pokemonList ->
            adapter.submitList(pokemonList)
        }

        nextPageButton.setOnClickListener {
            if (!isLoading){
                println("Next page button clicked")
                navigationLayout.visibility = View.GONE
                viewModel.incrementPage()
            }
        }

        previousPageButton.setOnClickListener {
            println("Previous page button clicked")
            navigationLayout.visibility = View.GONE
            viewModel.decrementPage()
        }

        return view
    }

    private fun setupRecyclerView() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastVisibleItemPosition >= Constants.PAGE_SIZE - 1 && !isLoading ) {
                        navigationLayout.visibility = View.VISIBLE

                        //Hide previous button if the current page is the first one
                        if(viewModel.currentPage.value == 0) {
                            previousPageButton.visibility = View.INVISIBLE
                            previousPageButton.isEnabled = false
                        } else {
                            previousPageButton.visibility = View.VISIBLE
                            previousPageButton.isEnabled = true
                        }

                        //Download new element only if the current page is the last one
                        if(viewModel.currentPage.value == viewModel.maxPage.value) {
                            println("Loading next page")
                            isLoading = true
                            nextPageButton.isEnabled = false
                            viewModel.loadMorePokemon().invokeOnCompletion {
                                isLoading = false
                                nextPageButton.isEnabled = true
                            }
                        } else {
                            nextPageButton.visibility = View.VISIBLE
                            nextPageButton.isEnabled = true
                        }

                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                //Hide the navigation layout when scrolling up
                if (dy < 0 && navigationLayout.visibility == View.VISIBLE) {
                    navigationLayout.visibility = View.GONE
                }
            }
        })
    }

    private fun setupSearch() {
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchEditText.text.toString())
                true
            } else {
                false
            }
        }
    }

    private fun performSearch(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.searchPokemon(query).join()
                val results = viewModel.searchResults.value

                if (results != null) {
                    val bundle = Bundle().apply {
                        putString("name", results.name)
                        putStringArray("type", results.types?.toTypedArray())
                        putString("imageUrl", results.imageUrl)
                        putString("description", results.description)
                    }

                    if(viewModel.pokemonUiState.value is PokemonUiState.Success) {
                        findNavController().navigate(R.id.action_pokemonListView_to_searchResult, bundle)
                    }

                } else {
                    showDialog("No results found")
                }
            } catch (e: Exception) {
                println(e)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            loadingImageView.visibility = View.VISIBLE
        } else {
            loadingImageView.visibility = View.GONE
        }
    }

    private fun showDialog(message: String) {
        view?.let { view ->
            if (view.isAttachedToWindow) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage(message)
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                builder.create().show()
            }
        }
    }
}