# レビューチェックリスト

## チェックリスト A: ルール準拠

### アーキテクチャ・モジュール境界
- [ ] モジュール依存が `:composeApp → :shared:ui-model → :shared:domain` の方向に従っているか
- [ ] `:shared:domain` にインターフェースが、`:shared:data` に実装があるか
- [ ] `:shared:ui-model` が `:shared:data` の実装クラスを直接 import していないか

### ViewModel パターン
- [ ] ViewModel は `ViewModel()` を継承しているか
- [ ] UiState は `StateFlow<XxxUiState>` で公開されているか
- [ ] UiState は `SharedSendable` を継承しているか
- [ ] UiState に Swift 向けの no-arg セカンダリコンストラクタ `constructor() : this(...)` があるか
- [ ] `XxxViewModelInterface` が定義されており、iOS 呼び出しメソッドが列挙されているか
- [ ] `observeUiState(onChange:)` と `dispose()` メソッドがあるか
- [ ] `@Inject` アノテーションが付いているか

### DI
- [ ] `AppComponent` に新しい ViewModel のプロパティが追加されているか
- [ ] `IosViewModelProvider` にも対応するプロパティが追加されているか
- [ ] `MockAppComponent` に Mock 用のバインドが追加されているか（Mock Mode が必要な場合）

### iOS interop
- [ ] iOS ViewModel ラッパーが `@KmpObservableViewModel` を使っているか
- [ ] `@KmpForwardAll` で `XxxViewModelInterface` に準拠しているか
- [ ] KMP 側に対応しない `@Published` プロパティに `@KmpObserveIgnore` が付いているか
- [ ] シングルトン ViewModel の場合、`deinit {}` で dispose の opt-out をしているか

### iOS ナビゲーション
- [ ] 新しい画面は `AppDestination` に case が追加されているか
- [ ] `AppDestinationView` の switch に遷移先 View が登録されているか
- [ ] `preferredTab` が適切に設定されているか

### ローカライズ
- [ ] UI に表示する文字列がハードコードされていないか
- [ ] `LocalizedStringKey+App.swift` に `static var` として定義されているか（`static let` 不可）
- [ ] `Localizable.xcstrings` に `ja` / `en` 両方のエントリがあるか

### テスト
- [ ] ViewModel の変更に対するユニットテストが `commonTest` にあるか
- [ ] 新しい Repository が必要な場合、`FakeXxxRepository` が `:shared:testing` に追加されているか
- [ ] テストが `StandardTestDispatcher` + `advanceUntilIdle()` パターンを使っているか

### Swift 6 concurrency
- [ ] `@MainActor` クラスの stored property として KMP 型を持っていないか（`nonisolated(unsafe)` かローカル変数パターンを使うこと）
- [ ] `LocalizedStringKey` の定義が `static let` ではなく `static var` の computed property になっているか

---

## チェックリスト B: コードレビュー（品質・正確性）

### 正確性・安全性
- ロジックのバグや境界値の見落とし
- KMP `StateFlow` / `MutableStateFlow` の更新が競合状態を起こしていないか
- Kotlin/Native で問題になる Sendable 非準拠の型が Swift 境界を越えていないか
- iOS: Swift 6 concurrency ルール違反（`@MainActor` の使い方、actor boundary crossing）

### コード品質
- 過剰な抽象化や不要なボイラープレートがないか
- エラーハンドリングが `onFailure { _uiState.update { it.copy(error = ...) } }` パターンに沿っているか
