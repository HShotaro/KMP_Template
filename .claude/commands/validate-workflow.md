# ワークフロー整合性検証

ワークフロー・スキル・サブエージェント・CLAUDE.md の間の矛盾と重複を調査する。

## 使い方

```
/validate-workflow
```

引数不要。プロジェクトルートで実行する。

---

## 検証手順

### ステップ 1: ファイル一覧の収集

以下のファイルをすべて Read する:

**CLAUDE.md（定義リスト）**
- `CLAUDE.md`

**ワークフロー（`.claude/commands/`）**
- Glob で `.claude/commands/*.md` を列挙してすべて Read する

**スキル（`.claude/skills/`）**
- Glob で `.claude/skills/*/SKILL.md` を列挙してすべて Read する

**サブエージェント（`.claude/agents/`）**
- Glob で `.claude/agents/*.md` を列挙してすべて Read する

**ドキュメント（`docs/`）**
- `docs/workflow-overview.md`
- `docs/workflow-reference.md`

---

### ステップ 2: 整合性チェック

以下の観点で矛盾を検出する。

#### A. CLAUDE.md ↔ 実ファイルの整合性

- CLAUDE.md のワークフロー一覧に記載されているコマンドが `.claude/commands/` に存在するか
- CLAUDE.md のスキル一覧に記載されているディレクトリが `.claude/skills/` に存在するか
- CLAUDE.md のエージェント一覧に記載されているファイルが `.claude/agents/` に存在するか
- 逆に、実ファイルが存在するのに CLAUDE.md の一覧に載っていないものがないか

#### B. ワークフロー内のクロスリファレンス

- ワークフローが参照しているエージェント名（例: `explorer`, `planner`）が `.claude/agents/` に存在するか
- ワークフローが参照しているスキルパス（例: `.claude/skills/commit/SKILL.md`）が存在するか

#### C. エージェント内のクロスリファレンス

- エージェントが参照しているスキルのテンプレートパス（例: `.claude/skills/new-screen/template.md`）が存在するか

#### D. ドキュメントの整合性

- `docs/workflow-overview.md` のコマンド一覧が実際のワークフローファイルと一致しているか
- `docs/workflow-reference.md` のエージェント・スキル一覧が実際のファイルと一致しているか

---

### ステップ 3: 重複チェック

以下の観点で重複を検出する。

#### A. ワークフロー間の重複ロジック

- 複数のワークフローで同一フェーズ（例: フェーズ レビュー・フェーズ コミット）の記述が実質同じ内容になっている箇所
- 同じエージェント起動パターンが複数ワークフローで繰り返されている箇所

#### B. エージェント間の重複定義

- 複数のエージェントが同じ調査内容・出力フォーマットを定義していないか

#### C. CLAUDE.md とドキュメントの重複

- CLAUDE.md に書かれた内容が `docs/workflow-overview.md` または `docs/workflow-reference.md` にほぼ同じ形で重複していないか

---

### ステップ 5（オプション）: 機能仕様書と実装の整合性チェック

ステップ 1〜3 の完了後、以下の確認をユーザーに行う:

> 「機能仕様書（docs/feature/）と KMP 実装の整合性チェックも実行しますか？
> 対象: UiState プロパティ名 / ViewModelInterface メソッド名
> 追加コスト: 約 20〜30 回の Grep / Read が発生します。」

「はい」の場合、**Agent ツールで `explorer` エージェントをサブエージェントとして起動する**（メインコンテキストの汚染防止）:

```
タスク: docs/feature/ 配下の機能仕様書に記載された UiState プロパティ名・
        ViewModelInterface メソッド名が KMP 実装と一致しているかを検証する。

手順:
  1. .claude/skills/feature-specs/SKILL.md を Read して機能一覧（対象 spec ファイル）を把握する
  2. 各機能の spec ファイル（ios.md または android.md）を Read し、
     「### XxxUiState」テーブルのプロパティ名と
     「### XxxViewModelInterface」テーブルのメソッド名を抽出する
  3. Grep で shared/ui-model/src/commonMain/ から対応する ViewModel.kt を特定し、
     data class XxxUiState の val/var プロパティ名と
     interface XxxViewModelInterface の fun メソッド名を抽出する
  4. 差分を以下の観点で報告する:
     - spec に記載があるが実装に存在しない（spec が古い可能性）
     - 実装に存在するが spec に記載がない（spec が未更新の可能性）
  5. computed property（spec で "(computed)" と記載されているもの）は
     クラス本体ではなく get() として定義されるため別途 Grep で確認する

注意:
  - メソッド名の引数名は照合しない（名前のみ照合）
  - Android 固有の VideoPlayerUiState など KMP 外の UiState は対象外
  - 一致していれば ✅、乖離があれば ⚠️ で報告する
```

サブエージェント完了後、結果をステップ 4 の出力に「### 機能仕様書の整合性」セクションとして追記する。

---

### ステップ 4: 結果の出力

検証結果をコンソールに出力する（`.steering/` への保存は不要）。

```
## 検証結果

### 矛盾
- [深刻度: 高/中/低] <問題の説明>
  - 箇所: <ファイル名と該当行>
  - 修正案: <推奨される対処>

### 重複
- [重複度: 高/中/低] <重複の説明>
  - 箇所: <ファイル名と該当行>
  - 修正案: <推奨される対処>

### 機能仕様書の整合性（ステップ5を実行した場合のみ）
- [⚠️ spec が古い] XxxUiState.`propertyName` — spec に記載あり、実装に存在しない
  - 仕様書: docs/feature/xxx/ios.md
  - 実装: shared/ui-model/.../XxxViewModel.kt
- [⚠️ spec 漏れ] XxxViewModelInterface.`methodName()` — 実装に存在するが spec に未記載
  - 仕様書: docs/feature/xxx/android.md
  - 実装: shared/ui-model/.../XxxViewModel.kt
- [✅] HomeUiState — spec と実装が一致

### 問題なし
- <問題のなかった観点を列挙>

### サマリー
矛盾: X件（高X / 中X / 低X）
重複: X件（高X / 中X / 低X）
仕様書乖離: X件（ステップ5実行時のみ）
```

問題が0件の場合は「矛盾・重複なし。ワークフロー定義は整合しています。」と出力する。
