# simplify スキル

選択中のコードまたは変更差分をこのプロジェクトのパターンを活かして簡略化する。

## 実行手順

1. `git diff HEAD` で変更内容を確認する
2. 変更されたファイルを Read して以下の観点で簡略化できる箇所を探す
3. 動作を変えない範囲で修正を適用する

## 簡略化の観点

### Kotlin / KMP
- `if/else` チェーンを `when` 式へ
- `?.let { }` / `?: return` のアーリーリターンパターン
- `copy()` の連鎖を1回の `update { it.copy(..., ...) }` にまとめる
- `flow.collect { }` より `flow.onEach { }.launchIn(viewModelScope)` の統一パターン
- `Result.onSuccess / onFailure` チェーンの活用

### Swift / SwiftUI
- `if viewModel.uiState.isLoading` などの条件分岐を `Group` + `switch` で整理
- `@ViewBuilder` を使って複数の View 条件分岐を整理
- `NavigationLink(value:)` の宣言型パターンへの統一
- `@KmpForwardAll` が使える場合は個別の `@KmpForward` をまとめる
- `[weak self]` キャプチャリストの一貫した使用

### 不要なコードの除去
- 使われていない `@Published` プロパティ
- 重複した `loadData` 呼び出し
- 空の `init() {}` や `deinit {}` ブロック（ただし意図的な opt-out は除く）
- コメントアウトされたコード

### ボイラープレートの削減
- iOS ViewModel のボイラープレートは `@KmpObservableViewModel` + `@KmpForwardAll` で置き換えられるか確認
- 同じ `Task { @MainActor [weak self] in self?.xxx = value }` パターンは `setupKmpObservations()` に集約

## 制約（変更しないもの）
- `deinit {}` の明示的な空実装はシングルトン opt-out の可能性があるため削除しない
- `nonisolated(unsafe)` は意図的な Swift 6 concurrency 回避のため削除しない
- `constructor() : this(...)` の no-arg コンストラクタは Swift 向けに必要なため削除しない

## コード例は `.claude/skills/simplify/template.md` を参照
