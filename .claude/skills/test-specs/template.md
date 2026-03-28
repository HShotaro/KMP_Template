# test-specs テンプレート

## KMP ユニットテスト（新規 ViewModel）

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

    @Test
    fun loadData_failure_setsError() = runTest {
        val fakeRepo = FakeXxxRepository().apply {
            fetchResult = Result.failure(Exception("network error"))
        }
        val viewModel = XxxViewModel(fakeRepo)
        viewModel.loadData()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
```

## FakeRepository の新規作成パターン

```kotlin
// shared/testing/src/commonMain/kotlin/.../testing/FakeXxxRepository.kt
class FakeXxxRepository : XxxRepository {
    var fetchResult: Result<List<XxxItem>> = Result.success(emptyList())
    var fetchCallCount: Int = 0

    override suspend fun fetchItems(): Result<List<XxxItem>> {
        fetchCallCount++
        return fetchResult
    }
}
```

## iOS ユニットテスト（Swift 6 対応）

`@MainActor` はクラスでなくメソッドに付け、KMP 型はローカル変数として生成する。

```swift
// NG: クラスに @MainActor + stored property → actor boundary crossing エラー
@MainActor
final class MyTests: XCTestCase {
    var manager: MusicPlayerManager!
}

// OK: メソッドに @MainActor + local variable
final class MyTests: XCTestCase {
    @MainActor
    func testSomething() async {
        let vm = PlayerViewModel()
        let manager = MusicPlayerManager(playerViewModel: vm)
        // 同一 @MainActor コンテキスト内で生成・使用
        // ...
    }
}
```
