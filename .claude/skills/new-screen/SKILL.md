# new-screen スキル

新しい画面に必要なファイル群をこのプロジェクトのパターンに従って生成する。

## 使い方

`/new-screen <ScreenName>` または ワークフローから呼び出す。

例: `new-screen Artist` → `ArtistScreen` と `ArtistViewModel` を生成

## 生成するファイル一覧

1. **Kotlin ViewModel** — `shared/ui-model/src/commonMain/.../XxxViewModel.kt`
   - `XxxUiState` (SharedSendable 継承 + no-arg コンストラクタ)
   - `XxxViewModelInterface`
   - `XxxViewModel` (@Inject コンストラクタ)

2. **DI 配線** — `AppComponent.kt` + `IosViewModelProvider`

3. **iOS ViewModel ラッパー** — `iosApp/iosApp/viewModel/IosXxxViewModel.swift`
   - `@KmpObservableViewModel` + `@KmpForwardAll` パターン

4. **iOS Screen** — `iosApp/iosApp/View/Screen/XxxScreen.swift`

5. **AppDestination の更新** — `iosApp/iosApp/Route/AppDestination.swift`

6. **ユニットテスト** — `shared/ui-model/src/commonTest/.../XxxViewModelTest.kt`

## 生成手順

1. `<Xxx>` を指定した ScreenName に置換する
2. 各ファイルを生成・既存ファイルを更新する
3. `UiState` のフィールドはドメインエンティティに合わせて調整する
4. リポジトリが必要な場合は `:shared:domain` にインターフェース、`:shared:data` に実装を追加する

## コード例は `.claude/skills/new-screen/template.md` を参照
