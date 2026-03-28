# simplify テンプレート

## Kotlin 簡略化例

### StateFlow の update をまとめる
```kotlin
// Before
_uiState.update { it.copy(isLoading = false) }
_uiState.update { it.copy(items = result) }

// After
_uiState.update { it.copy(isLoading = false, items = result) }
```

### onSuccess / onFailure チェーン
```kotlin
// Before
val result = repository.fetch()
if (result.isSuccess) {
    _uiState.update { it.copy(isLoading = false, items = result.getOrNull()!!) }
} else {
    _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
}

// After
repository.fetch()
    .onSuccess { items -> _uiState.update { it.copy(isLoading = false, items = items) } }
    .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
```

### flow の収集パターン
```kotlin
// Before
viewModelScope.launch {
    repository.observeItems().collect { items ->
        _uiState.update { it.copy(items = items) }
    }
}

// After
repository.observeItems()
    .onEach { items -> _uiState.update { it.copy(items = items) } }
    .launchIn(viewModelScope)
```

## Swift 簡略化例

### View の条件分岐
```swift
// Before
if viewModel.uiState.isLoading {
    ProgressView()
} else if viewModel.uiState.error != nil {
    ErrorView()
} else {
    ContentView()
}

// After
Group {
    switch (viewModel.uiState.isLoading, viewModel.uiState.error) {
    case (true, _): ProgressView()
    case (_, .some(let e)): ErrorView(message: e)
    default: ContentView()
    }
}
```

### @KmpForwardAll でボイラープレート削減
```swift
// Before
extension IosArtistViewModel: ArtistViewModelInterface {
    func loadData() { viewModel.loadData() }
    func onArtistSelected(id: String) { viewModel.onArtistSelected(id: id) }
}

// After
@KmpForwardAll
extension IosArtistViewModel: @MainActor ArtistViewModelInterface {
    func loadData()
    func onArtistSelected(id: String)
}
```
