---
name: update-feature-doc
description: 機能仕様書を docs/feature/ 配下に更新する。全ワークフローでレビュー承認後・コミット前に実行（条件付き）。
---

# update-feature-doc スキル

`docs/feature/` 配下の機能仕様書を更新し、`feature-specs` インデックスに登録する。

## 実行タイミング

| ワークフロー | タイミング | 操作 |
|---|---|---|
| `/add-feature` | フェーズ6（レビュー）承認後・フェーズ7（コミット）前 | **差分更新**。フェーズ1で `gen-feature-doc` が作成した雛形を、確定した実装コードを見て更新する |
| `/fix-feature` | フェーズ6（レビュー）承認後・フェーズ7（コミット）前 | UiState・ViewModelInterface・非自明な設計が変わった場合のみ**更新**。変更がない修正はスキップ |
| `/bugfix` | フェーズ6（レビュー）承認後・フェーズ7（コミット）前 | 原則スキップ。バグ修正で設計上の契約（インターフェース・非自明な振る舞い）が変わった場合のみ更新 |

**`gen-feature-doc` との役割分担**:
- `gen-feature-doc`（フェーズ1）: `products-requirement.md` から技術ドキュメントの**雛形**を作成する（explorer・planner が参照できるようにする）
- `update-feature-doc`（レビュー承認後）: 確定した実装コードを見て技術ドキュメントを**更新**する

## 実行手順

### 1. 対象ファイルの確認

画面 + ViewModel を持つ機能かを確認する。以下のいずれかが存在しない場合はスキップしてよい:
- KMP ViewModel（`XxxViewModel.kt`）
- iOS Screen または Android Screen

### 2. ディレクトリとファイルの決定

```
docs/feature/<feature-name>/
├── products-requirement.md  ← プロダクト要件（目的・ユーザーストーリー・UX フロー）
├── ios.md                   ← iOS 技術仕様（iOS 画面が存在する場合）
├── android.md               ← Android 技術仕様（Android 画面が存在する場合）
└── testcase.md              ← テストケース（KMP ユニット・iOS ユニット・iOS UITest・Android UITest）
```

`<feature-name>` はケバブケースで命名する（例: `music-video`, `playlist`, `artist-detail`）。

### 3. 仕様書を更新する

`template.md` の構造に従い内容を埋める。

**add-feature（差分更新）の場合**: 確定した実装コード（KMP ViewModel・iOS/Android Screen）を主な情報源にする。非自明な設計決定は `plan.md` の設計方針セクションも参照する。

**fix-feature / bugfix（更新）の場合**: 変更されたセクションのみ更新する。変更がないセクションは触らない。

#### 記載するもの

| セクション | 内容 |
|---|---|
| 概要 | 画面の目的・主要な操作を 1〜2 文で説明 |
| UiState | プロパティ名・型・意味を表形式で列挙 |
| ViewModelInterface | メソッド名と振る舞いの契約（「〜の場合はスキップ」「成功時に〜を再実行」など） |
| iOS ViewModel 設計ポイント | `@KmpObserveIgnore` を付けた iOS 固有プロパティと理由、computed property のバインディング目的など、**コードを読むだけでは分からない設計意図** |
| ライフサイクル | `.onAppear` / `LaunchedEffect` の条件（重複防止ロジックなど）を表形式で記載 |
| ナビゲーション | NavStack の構造、`AppDestination` の遷移先テーブル |
| 非自明な設計決定 | 非自明な相互作用・制約・理由（例: ジェスチャーの競合回避、部分失敗時の表示ロジック） |

#### 記載しないもの

- 具体的な Swift / Kotlin コードスニペット（コードはソースを読めばわかる）
- View 階層ツリー（SwiftUI/Compose の画面構成）
- アクセシビリティ識別子 / テストタグの一覧
- DI 取得ボイラープレート（`viewModel(factory = ...)` 等）
- ルーティング定義コード（`composable("route") { ... }` 等）
- 実装順序
- ファイルパス（コードを読めばわかる）

### 4. testcase.md を更新する

`testcase.md` の以下のセクションに変更内容を反映する:
- **KMP ユニットテスト**: ViewModel テストケース
- **iOS ユニットテスト** (`iosAppTests`): iOS 固有クラスのテストケース
- **iOS UITest** (`iosAppUITests`): XCUITest ファイル名・検証概要（詳細テストケース一覧があれば記載）
- **Android Compose UITest** (`composeApp/androidTest`): テストファイル名・検証概要

### 5. docs/testing/test.md を更新する

新規 feature または未参照 ViewModel が追加された場合: ViewModel テストへの参照行を追加する。

### 6. docs/testing/uitest.md を更新する

新規 UITest ファイルが追加された場合: iOS / Android テストファイル一覧テーブルに行を追加する。
新規 accessibilityIdentifier / testTag が追加された場合: それぞれの一覧テーブルに追加する。

### 7. feature-specs/SKILL.md のインデックスを更新する

`.claude/skills/feature-specs/SKILL.md` のテーブルに新しいエントリを追加する（既存行がある場合は修正する）:

```
| <機能名> (iOS) | `docs/feature/<feature-name>/ios.md` | <Screen名>・<ViewModel名>・<主要なUI要素> 関連（iOS） |
| <機能名> (Android) | `docs/feature/<feature-name>/android.md` | <Screen名>・<ViewModel名>・<主要なUI要素> 関連（Android） |
```

## コード例は `.claude/skills/gen-feature-doc/template.md` を参照
