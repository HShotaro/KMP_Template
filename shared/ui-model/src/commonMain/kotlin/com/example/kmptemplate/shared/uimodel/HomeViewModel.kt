package com.example.kmptemplate.shared.uimodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kmptemplate.shared.domain.entity.Post
import com.example.kmptemplate.shared.domain.model.SharedSendable
import com.example.kmptemplate.shared.domain.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

data class HomeUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : SharedSendable() {
    constructor() : this(emptyList(), false, null)
}

interface HomeViewModelInterface {
    fun loadPosts()
}

@Inject
class HomeViewModel(
    private val postRepository: PostRepository
) : ViewModel(), HomeViewModelInterface {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun observeUiState(onChange: (HomeUiState) -> Unit) {
        uiState.onEach { onChange(it) }.launchIn(viewModelScope)
    }

    fun dispose() {
        viewModelScope.run { }
    }

    override fun loadPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            postRepository.getPosts()
                .onSuccess { posts -> _uiState.update { it.copy(posts = posts, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
        }
    }
}
