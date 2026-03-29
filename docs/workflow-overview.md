# ワークフロー概要

Claude Code のカスタムスラッシュコマンドによる開発ワークフロー。
「探索→計画→実装→テスト→レビュー→コミット」の共通フェーズ構成で、タスク種別に応じたコマンドを使い分ける。

---

## コマンド一覧

| コマンド | 用途 | コミット type |
|---------|------|-------------|
| `/add-feature <機能名>` | 機能追加（新規画面・既存画面への機能追加、KMP・iOS・Android 並列実装） | `feat` |
| `/fix-feature <修正内容>` | 既存機能の修正 | - |
| `/bugfix <症状>` | バグ修正（根本原因調査から） | `fix` |
| `/validate-workflow` | ワークフロー定義の整合性検証 | - |
| `/create-issue <issue概要>` | GitHub Issue の作成 | - |

---

## 共通フェーズ構成

```
フェーズ1: タスクノート初期化
  └─ .steering/tasks/YYYY-MM-DD_<name>/ を作成し TodoWrite でタスク登録

フェーズ2: 探索 / 調査
  └─ explorer または bug-investigator エージェントが exploration*.md を出力

フェーズ3: 計画（ユーザー承認必須）
  └─ planner エージェントが plan.md を出力 → ユーザー承認後に次フェーズへ

フェーズ4: 実装
  └─ 複数側の変更: 該当する implementer-kmp / implementer-ios / implementer-android を並列起動
  └─ 1つの側のみの変更: メインエージェントが直接修正

フェーズ5: テスト確認
  └─ ユーザーに ./gradlew :shared:ui-model:test の実行を依頼し、結果ログを受け取る。失敗があれば修正して再依頼

フェーズ6: レビュー
  └─ reviewer エージェント（サブエージェント）が review.md を出力（コンテキスト汚染防止）
  └─ 重要度「高」は必ず修正。その後 simplify スキルでコード簡略化

フェーズ7: コミット
  └─ commit スキルの規約に従ってコミット

フェーズ8: PR作成（任意）
  └─ ユーザー確認後、pr スキルで GitHub PR を作成
```

---

## ワークフロー別の特徴

### `/add-feature` — 機能追加

新規画面の追加・既存画面への機能追加どちらにも使用する。
並列探索・並列実装でコンテキスト消費を最小化しながら開発速度を上げる。

```
フェーズ1: products-requirement.md を作成 → gen-feature-doc スキルで ios.md / android.md / testcase.md の雛形を生成
フェーズ2: explorer × 3（KMP側 / iOS側 / 影響範囲）を同時起動
フェーズ3: planner が plan.md にインターフェース仕様（UiState・ViewModelInterface の完全な型定義）を含める
           → iOS・Android エージェントが KMP ビルド待ちなしで並列実装できるようにするための共有契約
フェーズ4: implementer-kmp × implementer-ios × implementer-android を同時起動
フェーズ6後: update-feature-doc スキルでレビュー承認済みの実装コードを見て ios.md / android.md / testcase.md を差分更新
```

### `/fix-feature` — 既存機能修正

最小変更の原則。既存の動作に影響しない範囲で修正する。

```
フェーズ2: explorer × 2（実装調査 / 影響範囲調査）を同時起動
フェーズ4: 変更範囲に応じて並列実装 or メインエージェントによる直接修正
```

### `/bugfix` — バグ修正

根本原因の特定を優先し、最小変更で修正する。

```
フェーズ2: bug-investigator（根本原因調査）+ explorer（横断影響調査）を同時起動
フェーズ4: メインエージェントが直接修正（バグ修正は根本原因への集中が重要なため並列実装は使用しない）
フェーズ5: このバグをカバーするテストケースを追加（再発防止）
```

---

## 共通フェーズ定義

全ワークフロー共通。ワークフロー固有の差分（ブランチ prefix・commit type・TodoWrite 項目）は各コマンドファイルに記載。

### フェーズ 1: タスクノート初期化

1. `.steering/tasks/` 以下に既存のディレクトリがあれば一覧を表示し、「過去のタスクログを削除しますか？」と確認する。「はい」の場合は `.steering/tasks/` 以下のディレクトリをすべて削除する。
2. タスクディレクトリパスを決定する
   - 形式: `.steering/tasks/YYYY-MM-DD_<kebab-case-name>/`
3. TodoWrite でタスクを登録する（**タスク内容はワークフロー固有**。各コマンドファイル参照）

---

### フェーズ 5: テスト確認

`.claude/skills/test-specs/SKILL.md` を参照してテスト実装パターンを確認すること。

ユーザーに以下のコマンドの実行を依頼し、結果ログを共有してもらう:

```
./gradlew :shared:ui-model:test
```

#### テスト失敗時の分岐

受け取ったログに失敗があれば、**失敗の種別**を判定してから対応する:

**パターン A — 実装ミス**（修正内容のコードミス・型エラー・想定内の修正漏れ）:
→ フェーズ4に戻って修正し、再度テスト実行を依頼する

**パターン B — 想定外の失敗**（調査時の根本原因とは別の箇所が壊れた・全く別の依存関係でクラッシュする等）:
→ フェーズ2の調査エージェントを「新たな失敗ログ」を加えて再起動し、根本原因を再調査する。調査完了後、フェーズ3（再計画）→ フェーズ4（再実装）→ フェーズ5（テスト）の順で再実行する。
（`/bugfix` ワークフローの場合は `bug-investigator` エージェントを再起動すること）

テスト完了後（すべて合格）、結果（合格/失敗件数・失敗内容の要約）を `.steering/tasks/<dir>/test-result.md` に書き出す。

---

### フェーズ 6: レビュー

**Agent ツールで `reviewer` エージェントを起動する**（コンテキスト汚染防止のためサブエージェントとして実行）:

```
出力先: .steering/tasks/<dir>/review.md
```

完了後 `review.md` を Read して結果を確認する。

- 重要度「高」・ルール違反があれば必ず修正してから次へ進む
- 重要度「中・低」はユーザーに報告して判断を委ねる

その後、`.claude/skills/simplify/SKILL.md` の手順でコードを簡略化する。

簡略化完了後、`.claude/skills/update-feature-doc/SKILL.md` の手順で機能仕様書を更新する（UiState・ViewModelInterface・非自明な設計に変更がない場合はスキップしてよい）。

### フェーズ 7: コミット

`git branch --show-current` で現在のブランチを確認する。
`develop` ブランチにいる場合は作業ブランチへの切り替えを提案する（**ブランチ prefix はワークフロー固有**）。
「はい」の場合はブランチ名を提案してユーザー確認後、`git checkout -b <prefix>/<名前>` で作成する。

`.claude/skills/commit/SKILL.md` の手順でコミットする（**commit type はワークフロー固有**）。

コミット完了後、コミットハッシュと変更概要を `.steering/tasks/<dir>/result.md` に書き出す。

### フェーズ 8: PR 作成（任意）

コミット完了後、「プッシュしてプルリクエストを作成しますか？」とユーザーに確認する。
「はい」の場合は `.claude/skills/pr/SKILL.md` の手順で PR を作成する。

---

## 作業ログの保存先

ワークフロー実行中の中間成果物は `.steering/tasks/` に保存される。

```
.steering/tasks/YYYY-MM-DD_<name>/
├── exploration.md        # 探索・調査結果（explorer / bug-investigator が出力）
├── exploration-kmp.md    # KMP側探索結果（add-feature の並列探索時）
├── exploration-ios.md    # iOS側探索結果（add-feature の並列探索時）
├── exploration-impl.md   # 実装調査結果（fix-feature の並列探索時）
├── exploration-impact.md    # 影響範囲調査結果（全ワークフローの並列探索時）
├── exploration-hypothesis.md # 仮説検証結果（bugfix の仮説検証ループ時）
├── plan.md                  # 実装計画（planner が出力）
├── implementation-kmp.md    # KMP実装完了ファイル一覧（implementer-kmp が出力）
├── implementation-ios.md    # iOS実装完了ファイル一覧（implementer-ios が出力）
├── implementation-android.md # Android実装完了ファイル一覧（implementer-android が出力）
├── implementation.md        # 実装完了ファイル一覧（メインエージェント直接修正時）
├── test-result.md           # テスト結果サマリー
├── review.md                # レビュー結果（reviewer エージェントが出力）
└── result.md                # コミットハッシュと変更概要
```

詳細は [`.steering/README.md`](../.steering/README.md) を参照。

---

## 関連ドキュメント

- [workflow-reference.md](workflow-reference.md) — エージェント・スキルの詳細リファレンス
