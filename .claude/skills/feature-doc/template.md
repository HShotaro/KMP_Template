# feature-doc テンプレート

## iOS 仕様書テンプレート（`docs/feature/<feature-name>/ios.md`）

```markdown
# XxxScreen 仕様 (iOS)

## 概要

<!-- 画面の目的・主要な操作を 1〜2 文で説明する -->
<!-- 例: ジャンル別チャート楽曲をグリッド表示するホーム画面。タブバーでジャンルを切り替え、タブ間をスワイプで移動できる。 -->

## ViewModel

**KMP:** `XxxViewModel` (`shared/ui-model/`)
**iOS ラッパー:** `IosXxxViewModel` (`iosApp/iosApp/viewModel/IosXxxViewModel.swift`)

### XxxUiState

| プロパティ | 型 | 説明 |
|---|---|---|
| `isLoading` | `Boolean` | ロード中フラグ |
| `error` | `String?` | エラーメッセージ |
<!-- 他のプロパティを追加。computed property には (computed) を付ける -->

### XxxViewModelInterface

| メソッド | 説明 |
|---|---|
| `loadData()` | <!-- 振る舞いの契約: 「〜の場合はスキップ」「成功時に〜を再実行」など --> |
<!-- 他のメソッドを追加 -->

### IosXxxViewModel の設計ポイント

<!-- コードを読むだけでは分からない設計意図のみ記載する -->
<!-- 例: -->
- `navigationPath` は KMP UiState 外で管理する iOS 固有プロパティ（`@KmpObserveIgnore`）
<!-- 双方向バインディングが必要な場合: -->
<!-- - `selectedXxxId` は XxxComponent / View との双方向バインディング用 computed property。`get` で `uiState.selectedXxxId` を返し、`set` で `selectXxx()` を呼ぶ -->
<!-- iOS 固有の複数プロパティがある場合: -->
<!-- 以下はすべて KMP UiState 外で管理する iOS 固有の UI 状態（`@KmpObserveIgnore`）: -->
<!-- - `isSheetPresented` — シートの表示フラグ -->
<!-- - `inputText` — フォームの一時状態 -->
<!-- `submitXxx()` は iOS 固有メソッド。KMP `createXxx()` を呼んだ後、フォームのリセットとシートのクローズを行う -->

## ライフサイクル

| イベント | 処理 |
|---|---|
| `.onAppear` | <!-- 条件付きの場合は条件も記載。例: `isLoading` でない場合のみ `loadData()` を呼び出す（重複呼び出し防止） --> |
<!-- 他のライフサイクルイベントがあれば追加 -->

## ナビゲーション

- `NavigationStack(path: $viewModel.navigationPath)` — Xxx タブの NavStack
- `.appNavigationDestination()` modifier で遷移を一元管理
<!-- ディープリンクがある場合: -->
<!-- - `.deepLinkNavigation(for: .xxx, path: ...)` — ディープリンク対応 -->

<!-- 遷移先がある場合: -->
| 操作 | AppDestination |
|---|---|
| <!-- アイテムタップ --> | `AppDestination.xxx(String)` |

<!-- 非自明な設計決定がある場合のみ追加 -->
## 非自明な設計決定

<!-- 例: -->
<!-- - **〜の理由**: ScrollView とジェスチャーが競合するため、ドラッグハンドル部分にのみ付与する -->
<!-- - **〜の制約**: XxxManager は `@EnvironmentObject` として iOSApp から注入される。play() 呼び出し後 `isVisible = true` となり、ContentView が `.fullScreenCover` で表示する -->
```

---

## Android 仕様書テンプレート（`docs/feature/<feature-name>/android.md`）

```markdown
# XxxScreen 仕様 (Android)

## 概要

<!-- 画面の目的・主要な操作を 1〜2 文で説明する -->

<!-- iOS と画面構成が大きく異なる場合のみ iOS との差分を記載する -->
<!-- > iOS 仕様は **[`ios.md`](ios.md)** を参照。 -->

## ViewModel

**KMP:** `XxxViewModel` (`shared/ui-model/`)

<!-- Android 固有の ViewModel パラメータがある場合のみ記載 -->
<!-- 例: `VideoPlayerViewModel` は `MainScreen` からパラメータとして渡される -->

### XxxUiState

| プロパティ | 型 | 説明 |
|---|---|---|
| `isLoading` | `Boolean` | ロード中フラグ |
| `error` | `String?` | エラーメッセージ |
<!-- 他のプロパティを追加。iOS と同じ UiState を使う場合も表として再掲する（Android 仕様書として完結させる） -->

### XxxViewModelInterface

| メソッド | 説明 |
|---|---|
| `loadData()` | <!-- 振る舞いの契約 --> |

<!-- 非自明な同期ロジックがある場合のみ追加 -->
## タブとページの同期（HorizontalPager を使う場合）

<!-- 例: `PrimaryScrollableTabRow` と `HorizontalPager` は双方向に同期する。 -->
<!-- - ページスワイプ → snapshotFlow で変化を検知 → viewModel.selectXxx() 呼び出し -->
<!-- - タブタップ → LaunchedEffect で pagerState.animateScrollToPage() 呼び出し -->

<!-- グリッドレイアウトにタブレット対応がある場合のみ追加 -->
## グリッドカラム数（タブレット対応がある場合）

| 条件 | カラム数 |
|---|---|
| `screenWidthDp >= 600`（タブレット） | 3 列 |
| `screenWidthDp < 600`（スマートフォン） | 2 列 |

## 楽曲・アイテムタップ

<!-- onXxxClick が MainScreen から渡される場合のみ記載 -->
<!-- 例: `onSongClick: (List<PlayableSong>, Int) -> Unit` は `MainScreen` から渡され、`playerViewModel.play(songs, index)` を呼び出す -->

## ライフサイクル

| イベント | 処理 |
|---|---|
| `LaunchedEffect(Unit)` | <!-- 条件付きの場合は条件も記載 --> |

<!-- 非自明な設計決定がある場合のみ追加 -->
## 非自明な設計決定

<!-- 例: -->
<!-- - **再生位置のポーリング**: ExoPlayer は位置変化の Listener を持たないため、再生中は viewModelScope で 500ms ごとにポーリングする -->
```

---

## 記載判断チートシート

| 情報 | 記載する？ | 理由 |
|---|---|---|
| UiState のプロパティ一覧 | ✅ | インターフェース契約 |
| `@KmpObserveIgnore` が付いたプロパティとその理由 | ✅ | 非自明な設計意図 |
| computed property のバインディング目的 | ✅ | 非自明な設計意図 |
| 部分失敗時のエラー表示ロジック | ✅ | 非自明な振る舞い |
| 画面またぎの相互作用（EnvironmentObject 経由の呼び出しなど） | ✅ | コードを追わないと分からない |
| ライフサイクルの重複呼び出し防止条件 | ✅ | 非自明な理由がある |
| タブレット用グリッド列数の閾値 | ✅ | 設計上の決定値 |
| View 階層ツリー（SwiftUI/Compose 構造） | ❌ | コードを読めばわかる |
| 具体的な Swift/Kotlin コードスニペット | ❌ | コードを読めばわかる |
| LazyVGrid のカラム幅パラメータ | ❌ | コードを読めばわかる |
| アクセシビリティ識別子 / テストタグ | ❌ | コードを読めばわかる |
| DI 取得ボイラープレート | ❌ | arch-patterns に記載済み |
| ルーティング定義コード | ❌ | コードを読めばわかる |
| ファイルパス | ❌ | Glob で調べられる |
| 実装順序 | ❌ | 実装済みなら不要 |
