# arch-patterns テンプレート

## ViewModel / UiState の完全パターン

```kotlin
// shared/ui-model/src/commonMain/.../XxxViewModel.kt
data class XxxUiState(
    val isLoading: Boolean = false,
    val items: List<XxxItem> = emptyList(),
    val error: String? = null
) : SharedSendable() {
    constructor() : this(false, emptyList(), null)  // Swift 向け必須
}

interface XxxViewModelInterface {
    fun loadData()
    fun onItemSelected(id: String)
}

open class XxxViewModel @Inject constructor(
    private val xxxRepository: XxxRepository
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
            xxxRepository.fetch()
                .onSuccess { items -> _uiState.update { it.copy(isLoading = false, items = items) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
```

## DI 配線

```kotlin
// shared/ui-model/.../di/AppComponent.kt
abstract val xxxViewModel: XxxViewModel

// shared/ui-model/.../di/IosViewModelProvider.kt
val xxxViewModel: XxxViewModel get() = component.xxxViewModel

// shared/ui-model/.../di/MockAppComponent.kt
private val xxxRepo = object : XxxRepository {
    override suspend fun fetch() = Result.success(listOf(/* mock data */))
}
override val xxxViewModel: XxxViewModel = XxxViewModel(xxxRepo)
```

## iOS ViewModel ラッパー（標準パターン）

```swift
// iosApp/iosApp/viewModel/IosXxxViewModel.swift
import Shared
import HSMacro

// 自動生成: viewModel / uiState / setupKmpObservations / init() / deinit { viewModel.dispose() }
@KmpObservableViewModel
@MainActor
class IosXxxViewModel: ObservableObject {
    // iOS ローカルな Published プロパティには @KmpObserveIgnore を付ける
    @KmpObserveIgnore
    @Published var localState: String = ""
}

@KmpForwardAll
extension IosXxxViewModel: @MainActor XxxViewModelInterface {
    func loadData()               // ボディなしで書く（{} があると @KmpForward が効かない）
    func onItemSelected(id: String)
}
```

## iOS ViewModel ラッパー（シングルトン opt-out パターン）

```swift
@KmpObservableViewModel
class IosLoginViewModel: ObservableObject {
    deinit {}  // 明示することで dispose() 自動生成を opt-out（シングルトンのため解放しない）
}
```

## AppDestination への追加

```swift
// iosApp/iosApp/Route/AppDestination.swift
enum AppDestination: Hashable, Codable {
    // 既存 case ...
    case xxx(String)  // 追加
}

// preferredTab に追加
var preferredTab: TabItem {
    switch self {
    // ...
    case .xxx: return .home  // 適切なタブを指定
    }
}

// AppDestinationView の switch に追加
case .xxx(let id):
    XxxScreen(id: id)
```

## ViewModel に navigationPath を追加する場合（タブルート画面）

```swift
// IosXxxViewModel に追加
@KmpObserveIgnore
@Published var navigationPath: [AppDestination] = []
```

## KMP ユニットテストパターン

```kotlin
class XxxViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() { Dispatchers.setMain(testDispatcher) }
    @AfterTest fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun loadData_success_updatesState() = runTest {
        val fakeRepo = FakeXxxRepository().apply {
            fetchResult = Result.success(listOf(/* test data */))
        }
        val viewModel = XxxViewModel(fakeRepo)
        viewModel.loadData()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
        assertEquals(1, viewModel.uiState.value.items.size)
    }
}
```

## iOS ユニットテストパターン（Swift 6 対応）

```swift
// @MainActor はクラスでなくメソッドに付ける
final class XxxTests: XCTestCase {
    @MainActor
    func testSomething() async {
        let vm = XxxViewModel()  // KMP 型はローカル変数として生成（actor boundary を越えない）
        // ...
    }
}
```

## 既存 FakeRepository 一覧（:shared:testing）

| クラス | 制御できるプロパティ |
|--------|-------------------|
| `FakeLoginRepository` | `saveTokenResult` |
| `FakeHomeRepository` | `genresResult`, `songsResult` |
| `FakeSearchRepository` | `searchResult` |
| `FakeLibraryRepository` | `recentlyPlayedResult`, `songsResult`, `albumsResult` |
| `FakePlaylistRepository` | `playlistsResult`, `createResult`, `addTracksResult` |
