---
name: arch-patterns
description: アーキテクチャパターン早見表。planner・implementer-kmp・implementer-ios・explorer が実装・計画前に参照する。
---

# arch-patterns スキル

実装・計画前にこのプロジェクトのアーキテクチャパターンを確認する。

## 実行手順

1. `template.md` の早見表でパターンを確認する
2. 担当領域に応じて必要なドキュメントを Read する:
   - iOS interop / HSMacro の詳細 → `docs/ios/macro.md`
   - iOS ルーティング / AppDestination の詳細 → `docs/ios/route.md`

## 確認すべき主要なルール

### モジュール依存方向（違反厳禁）
```
:composeApp → :shared:ui-model → :shared:domain
                               → :shared:data
                               → :shared:core
                               → :shared:network
:shared:testing  (commonTest のみ参照)
```
- `ui-model` から `data` の実装クラスを直接 import しない
- `iosApp/` は `Shared.xcframework` 経由でのみ KMP にアクセスする

### ViewModel / UiState パターン（必須制約）
- `UiState` は `SharedSendable` を継承する
- `UiState` に `constructor() : this(...)` の no-arg コンストラクタを必ず付ける（Swift側 `XxxUiState()` 呼び出しに必須）
- `XxxViewModelInterface` を定義し、iOS から呼ぶメソッドを列挙する
- ViewModel に `observeUiState(onChange:)` と `dispose()` を実装する
- `@Inject` アノテーションを付ける
- エラーハンドリングは `onFailure { _uiState.update { it.copy(error = ...) } }` パターン

### DI（AppComponent / MockAppComponent）
- `AppComponent` に `abstract val xxxViewModel: XxxViewModel` を追加する
- `IosViewModelProvider` に `val xxxViewModel get() = component.xxxViewModel` を追加する
- `MockAppComponent` にも `override val xxxViewModel = XxxViewModel(mockRepo)` を追加する（Mock Mode 対応必須）

### iOS ViewModel ラッパー（HSMacro パターン）
- `@KmpObservableViewModel` — KMP観察・init・deinit のボイラープレートを自動生成
- `@KmpForwardAll` — extension 内の全 func に KMP デリゲートを一括生成
- `@KmpObserveIgnore` — iOS ローカルな `@Published` プロパティを observation から除外
- シングルトン ViewModel（LoginViewModel など）は `deinit {}` を明示して自動生成を opt-out

### iOS ルーティング（AppDestination）
- 新画面は `AppDestination` に case を追加し `preferredTab` と `AppDestinationView` も更新する
- 各タブの ViewModel は `@KmpObserveIgnore @Published var navigationPath: [AppDestination] = []` を持つ
- プッシュは `NavigationLink(value:)` または `viewModel.navigationPath.append(...)` で行う

### テストパターン
- `StandardTestDispatcher` + `Dispatchers.setMain` + `advanceUntilIdle()` を使う
- Fake は `:shared:testing` に配置し、`testImplementation(project(":shared:testing"))` で参照する
- iOS テストは `@MainActor` をクラスでなくメソッドに付け、KMP 型はローカル変数として生成する（Swift 6 concurrency 対応）

## コード例は `.claude/skills/arch-patterns/template.md` を参照
