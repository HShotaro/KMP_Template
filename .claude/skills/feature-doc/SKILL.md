---
name: feature-doc
description: 新機能の仕様書を docs/feature/ 配下に作成・更新する。add-feature ではフェーズ3（計画）完了後に作成。bugfix/fix-feature では変更内容に応じて更新。
---

# feature-doc スキル

`docs/feature/` 配下の機能仕様書を作成・更新し、`feature-specs` インデックスに登録する。

## 実行タイミング

| ワークフロー | タイミング | 操作 |
|---|---|---|
| `/add-feature` | フェーズ3（計画）承認後・フェーズ4（実装）開始前 | **新規作成**。`plan.md` の UiState・ViewModelInterface 定義を元に仕様書を作成する。実装エージェントが参照できるようにする |
| `/fix-feature` | フェーズ4（実装）完了後 | UiState・ViewModelInterface・非自明な設計が変わった場合のみ**更新**。変更がない修正はスキップ |
| `/bugfix` | フェーズ4（実装）完了後 | 原則スキップ。バグ修正で設計上の契約（インターフェース・非自明な振る舞い）が変わった場合のみ更新 |

## 実行手順

### 1. 対象ファイルの確認

画面 + ViewModel を持つ機能かを確認する。以下のいずれかが存在しない場合はスキップしてよい:
- KMP ViewModel（`XxxViewModel.kt`）
- iOS Screen または Android Screen

### 2. ディレクトリとファイルの決定

```
docs/feature/<feature-name>/
├── ios.md      ← iOS 画面が存在する場合
└── android.md  ← Android 画面が存在する場合
```

`<feature-name>` はケバブケースで命名する（例: `music-video`, `playlist`, `artist-detail`）。

### 3. 仕様書を作成・更新する

`template.md` の構造に従い内容を埋める。

**add-feature（新規作成）の場合**: `plan.md` の UiState・ViewModelInterface 定義を主な情報源にする。非自明な設計決定は `plan.md` の設計方針セクションから転記する。

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

### 4. feature-specs/SKILL.md のインデックスを更新する

`.claude/skills/feature-specs/SKILL.md` のテーブルに新しいエントリを追加する（更新の場合は既存行を修正する）:

```
| <機能名> (iOS) | `docs/feature/<feature-name>/ios.md` | <Screen名>・<ViewModel名>・<主要なUI要素> 関連（iOS） |
| <機能名> (Android) | `docs/feature/<feature-name>/android.md` | <Screen名>・<ViewModel名>・<主要なUI要素> 関連（Android） |
```

## コード例は `.claude/skills/feature-doc/template.md` を参照
