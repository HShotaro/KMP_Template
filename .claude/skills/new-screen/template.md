# new-screen テンプレート

## 1. Kotlin ViewModel

```kotlin
// shared/ui-model/src/commonMain/kotlin/com/hshotaro/music/kmp/shared/uimodel/XxxViewModel.kt
package com.hshotaro.music.kmp.shared.uimodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.hshotaro.music.kmp.shared.domain.model.SharedSendable
import me.tatarka.inject.annotations.Inject

data class XxxUiState(
    val isLoading: Boolean = false,
    val error: String? = null
) : SharedSendable() {
    constructor() : this(false, null)  // Swift 向け no-arg コンストラクタ必須
}

interface XxxViewModelInterface {
    fun loadData()
}

open class XxxViewModel @Inject constructor(
    // private val xxxRepository: XxxRepository
) : ViewModel(), XxxViewModelInterface {

    private val _uiState = MutableStateFlow(XxxUiState())
    val uiState: StateFlow<XxxUiState> = _uiState.asStateFlow()

    fun observeUiState(onChange: (XxxUiState) -> Unit) {
        uiState.onEach { onChange(it) }.launchIn(viewModelScope)
    }

    fun dispose() { viewModelScope.cancel() }

    override fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // repository.fetch()
            //     .onSuccess { result -> _uiState.update { it.copy(isLoading = false, data = result) } }
            //     .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
```

## 2. DI 配線

```kotlin
// shared/ui-model/src/commonMain/.../di/AppComponent.kt に追加
abstract val xxxViewModel: XxxViewModel

// IosViewModelProvider に追加
val xxxViewModel: XxxViewModel get() = component.xxxViewModel
```

## 3. iOS ViewModel ラッパー

```swift
// iosApp/iosApp/viewModel/IosXxxViewModel.swift
import Shared
import HSMacro

@KmpObservableViewModel
@MainActor
class IosXxxViewModel: ObservableObject {
    // KMP 側にない @Published プロパティは @KmpObserveIgnore を付ける
    // @KmpObserveIgnore
    // @Published var localState: String = ""
}

@KmpForwardAll
extension IosXxxViewModel: @MainActor XxxViewModelInterface {
    func loadData()
}
```

## 4. iOS Screen

```swift
// iosApp/iosApp/View/Screen/XxxScreen.swift
import SwiftUI
import Shared

struct XxxScreen: View {
    @StateObject private var viewModel = IosXxxViewModel()

    var body: some View {
        Group {
            if viewModel.uiState.isLoading {
                ProgressView()
            } else {
                content
            }
        }
        .onAppear { viewModel.loadData() }
    }

    private var content: some View {
        Text("XxxScreen")
    }
}
```

## 5. AppDestination の更新

```swift
// iosApp/iosApp/Route/AppDestination.swift
// enum に追加
case xxx(String)

// preferredTab に追加
case .xxx: return .home  // 適切なタブを指定

// AppDestinationView の switch に追加
case .xxx(let id):
    XxxScreen(id: id)
```

## 6. ユニットテスト

```kotlin
// shared/ui-model/src/commonTest/kotlin/.../XxxViewModelTest.kt
class XxxViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun loadData_success_updatesState() = runTest {
        val viewModel = XxxViewModel(/* FakeXxxRepository() */)
        viewModel.loadData()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }
}
```
