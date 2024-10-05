package com.example.testandroidstudio.ui

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testandroidstudio.R
import com.example.testandroidstudio.databinding.FragmentPokemonListViewBinding
import com.example.testandroidstudio.model.Pokemon
import com.example.testandroidstudio.utility.Constants
import com.example.testandroidstudio.utility.PokemonUiState
import com.example.testandroidstudio.viewModel.PokemonListViewModel
import kotlinx.coroutines.launch

class PokemonListFragment : Fragment() {
    //region Properties
    private val viewModel: PokemonListViewModel by activityViewModels()
    private lateinit var adapter: PokemonListAdapter
    private lateinit var binding: FragmentPokemonListViewBinding
    private var isLoading = false
    //endregion Properties

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPokemonListViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
    }
    //endregion Lifecycle methods

    //region UI Setup
    private fun setupUI() {
        setupRecyclerView()
        setupSearch()
        setupNavigationButtons()
        setupTitle()
    }

    private fun setupRecyclerView() {
        adapter = PokemonListAdapter()
        binding.pokemonRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PokemonListFragment.adapter
            addOnScrollListener(createScrollListener())
        }
    }

    private fun setupSearch() {
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(binding.searchEditText.text.toString())
                true
            } else {
                false
            }
        }
    }

    private fun setupNavigationButtons() {
        binding.navigationLayout.visibility = View.GONE
        binding.nextButton.setOnClickListener { onNextPageClicked() }
        binding.previousButton.setOnClickListener { onPreviousPageClicked() }
    }

    private fun setupTitle() {
        val titleText = "PokemonBox"
        val spannableString = SpannableString(titleText).apply {
            setSpan(StyleSpan(Typeface.NORMAL), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StyleSpan(Typeface.BOLD), 7, titleText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.titleTextView.text = spannableString
    }
    //endregion UI Setup

    //region ViewModel and Observers
    private fun initializeViewModel() {
        if (viewModel.pokemonList.value == null) {
            viewModel.loadPokemonList()
        }
    }

    private fun setupObservers() {
        viewModel.pokemonUiState.observe(viewLifecycleOwner, ::handleUiState)
        viewModel.currentPokemonList.observe(viewLifecycleOwner) { pokemonList ->
            adapter.submitList(pokemonList)
        }
    }

    private fun handleUiState(state: PokemonUiState) {
        when (state) {
            is PokemonUiState.Loading -> showLoading(true)
            is PokemonUiState.Success -> showLoading(false)
            is PokemonUiState.Error -> {
                showLoading(false)
                showDialog(state.message)
            }
        }
    }
    //endregion ViewModel and Observers

    //region User Actions
    private fun onNextPageClicked() {
        if (!isLoading) {
            binding.navigationLayout.visibility = View.GONE
            viewModel.incrementPage()
        }
    }

    private fun onPreviousPageClicked() {
        binding.navigationLayout.visibility = View.GONE
        viewModel.decrementPage()
    }

    private fun performSearch(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.searchPokemon(query).join()
                val results = viewModel.searchResults.value
                if (results != null) {
                    navigateToSearchResult(results)
                } else {
                    showDialog("No results found")
                }
            } catch (e: Exception) {
                println(e)
            }
        }
    }
    //endregion User Actions

    //region Helper Methods
    private fun createScrollListener(): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    handleScrollIdle(recyclerView)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy < 0 && binding.navigationLayout.visibility == View.VISIBLE) {
                    binding.navigationLayout.visibility = View.GONE
                }
            }
        }
    }

    private fun handleScrollIdle(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        if (lastVisibleItemPosition >= Constants.PAGE_SIZE - 1 && !isLoading) {
            updateNavigationVisibility()
        }
    }

    private fun updateNavigationVisibility() {
        binding.navigationLayout.visibility = View.VISIBLE
        updatePreviousButtonVisibility()
        updateNextButtonVisibility()
    }

    private fun updatePreviousButtonVisibility() {
        binding.previousButton.apply {
            visibility = if (viewModel.currentPage.value == 0) View.INVISIBLE else View.VISIBLE
            isEnabled = viewModel.currentPage.value != 0
        }
    }

    private fun updateNextButtonVisibility() {
        if (viewModel.currentPage.value == viewModel.maxPage.value) {
            loadMorePokemon()
        } else {
            binding.nextButton.apply {
                visibility = View.VISIBLE
                isEnabled = true
            }
        }
    }

    private fun loadMorePokemon() {
        isLoading = true
        binding.nextButton.isEnabled = false
        viewModel.loadMorePokemon().invokeOnCompletion {
            isLoading = false
            binding.nextButton.isEnabled = true
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingImageView.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showDialog(message: String) {
        view?.let { view ->
            if (view.isAttachedToWindow) {
                AlertDialog.Builder(requireContext())
                    .setMessage(message)
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            }
        }
    }

    private fun navigateToSearchResult(results: Pokemon) {
        val bundle = Bundle().apply {
            putString("name", results.name)
            putStringArray("type", results.types?.toTypedArray())
            putString("imageUrl", results.imageUrl)
            putString("description", results.description)
        }
        if (viewModel.pokemonUiState.value is PokemonUiState.Success) {
            findNavController().navigate(R.id.action_pokemonListView_to_searchResult, bundle)
        }
    }
    //endregion Helper Methods
}