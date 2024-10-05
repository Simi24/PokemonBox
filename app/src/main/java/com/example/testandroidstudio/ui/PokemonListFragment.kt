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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testandroidstudio.R
import com.example.testandroidstudio.databinding.FragmentPokemonListViewBinding
import com.example.testandroidstudio.model.Pokemon
import com.example.testandroidstudio.repository.PokemonRepository
import com.example.testandroidstudio.utility.Constants
import com.example.testandroidstudio.utility.PokemonUiState
import com.example.testandroidstudio.utility.ResourceHelper
import com.example.testandroidstudio.viewModel.PokemonListViewModel
import kotlinx.coroutines.launch

class PokemonListFragment : Fragment() {
    //region Properties
    private val viewModel: PokemonListViewModel by activityViewModels() {
        val pokemonRepository = PokemonRepository()
        val resourceHelper = ResourceHelper(requireContext())
        PokemonListViewModelFactory(pokemonRepository, resourceHelper)
    }
    private lateinit var adapter: PokemonListAdapter
    private lateinit var binding: FragmentPokemonListViewBinding
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
        setUpLoadingImageAnimation()
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
            viewModel.initializePokemonList()
        }
    }

    private fun setupObservers() {
        viewModel.pokemonUiState.observe(viewLifecycleOwner, ::handleUiState)
        viewModel.currentPokemonList.observe(viewLifecycleOwner) { pokemonList ->
            adapter.submitList(pokemonList)
        }
        viewModel.canLoadNextPage.observe(viewLifecycleOwner) { canLoadNextPage ->
            binding.nextButton.isEnabled = canLoadNextPage
        }
    }

    //endregion ViewModel and Observers

    //region User Actions
    private fun onNextPageClicked() {
        binding.navigationLayout.visibility = View.GONE
        println(binding.nextButton.isEnabled)
        viewModel.goToNextPage()
    }

    private fun onPreviousPageClicked() {
        binding.navigationLayout.visibility = View.GONE
        viewModel.goToPreviousPage()
    }

    private fun performSearch(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.searchPokemon(query).join()
                val results = viewModel.searchResults.value
                if (results != null) {
                    navigateToSearchResult(results)
                } else {
                    //Do nothing
                }
            } catch (e: Exception) {
                println(e)
            }
        }
    }
    //endregion User Actions

    //region Helper Methods
    private fun handleUiState(state: PokemonUiState) {
        when (state) {
            is PokemonUiState.LoadingFirstPage -> {
                handleLoadingState(true)
            }
            is PokemonUiState.Success -> {
                handleLoadingState(false)
            }
            is PokemonUiState.Error -> {
                handleLoadingState(false)
                showErrorWithDelay(state.message)
            }
        }
    }

    private fun handleLoadingState(isLoading: Boolean) {
        if(isLoading) {
            binding.searchInputLayout.isEnabled = false
            binding.pokemonRecyclerView.suppressLayout(true)
            showLoading(true)
        } else {
            showLoading(false)
            binding.pokemonRecyclerView.suppressLayout(false)
            binding.searchInputLayout.isEnabled = true
        }
    }

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
        if (lastVisibleItemPosition >= Constants.PAGE_SIZE - 1) {
            updateNavigationVisibility()
            handleLoadMorePokemon()
        }
    }

    private fun handleLoadMorePokemon() {
        viewModel.loadMorePokemonIfNeeded()
    }

    private fun updateNavigationVisibility() {
        binding.navigationLayout.visibility = View.VISIBLE
        binding.nextButton.visibility = View.VISIBLE
        updatePreviousButtonVisibility()
    }

    private fun updatePreviousButtonVisibility() {
        binding.previousButton.apply {
            visibility = if (viewModel.currentPage.value == 0) View.INVISIBLE else View.VISIBLE
            isEnabled = viewModel.currentPage.value != 0
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingImageView.visibility = if (isLoading) View.VISIBLE else View.GONE
        setUpLoadingImageAnimation()
    }

    private fun setUpLoadingImageAnimation() {
        if (binding.loadingImageView.visibility == View.VISIBLE) {
            val rotateAnimation = android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.rotate)
            binding.loadingImageView.startAnimation(rotateAnimation)
        } else {
            binding.loadingImageView.clearAnimation()
        }
    }

    private fun showErrorWithDelay(message: String) {
        view?.postDelayed({
            if (isAdded && !isDetached && !isRemoving) {
                showDialog(message)
            }
        }, 300)
    }

    private fun showDialog(message: String) {
        view?.let { view ->
            if (view.isAttachedToWindow) {
                AlertDialog.Builder(requireContext())
                    .setMessage(message)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        handleLoadingState(false)
                        if(message.contains("Error loading Pokemon list")) {
                            viewModel.initializePokemonList()
                        }
                    }
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

    class PokemonListViewModelFactory(
        private val pokemonRepository: PokemonRepository,
        private val resourceHelper: ResourceHelper
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PokemonListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PokemonListViewModel(resourceHelper, pokemonRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}