package com.example.kmptemplate.shared.uimodel

import com.example.kmptemplate.shared.testing.FakePostRepository
import com.example.kmptemplate.shared.testing.defaultPosts
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class HomeViewModelTest {

    private val repo = FakePostRepository()
    private val viewModel = HomeViewModel(repo)

    @Test
    fun initialState_isEmpty() {
        assertEquals(emptyList(), viewModel.uiState.value.posts)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun loadPosts_populatesState() = runTest {
        viewModel.loadPosts()
        assertEquals(defaultPosts, viewModel.uiState.value.posts)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
