# 既存機能修正ワークフロー

「探索→計画→実装→テスト→レビュー→コミット」の6フェーズで既存機能を修正する。
変更が KMP・iOS 両方に及ぶ場合は並列実装を選択できる。

## 使い方

```
/fix-feature <対象機能と修正内容>
```

例: `/fix-feature 検索画面のページネーションが2ページ目以降取得できない問題を修正`

---

## フェーズ 1: タスクノート初期化

`$ARGUMENTS` が空の場合は「どの機能をどのように修正しますか？（例: 検索画面のページネーションが2ページ目以降取得できない問題を修正）」と確認してから進む。

1. `.steering/tasks/` 以下に既存のディレクトリがあれば一覧を表示し、「過去のタスクログを削除しますか？」と確認する。「はい」の場合は `.steering/tasks/` 以下のディレクトリをすべて削除する。
2. タスクディレクトリパスを決定する
   - 形式: `.steering/tasks/YYYY-MM-DD_<kebab-case-name>/`
2. TodoWrite でタスクを登録する:
   - [ ] 探索: 対象機能の現状調査
   - [ ] 計画: 修正計画の立案・ユーザー承認
   - [ ] 実装: コードの修正
   - [ ] テスト: テストの確認・更新
   - [ ] レビュー: 実装の観点レビュー
   - [ ] コミット

---

## フェーズ 2: 並列探索（explorer × 2）

**Agent ツールで2つの `explorer` エージェントを同時に起動する。**

**エージェント A** — 実装調査:
```
タスク: $ARGUMENTS（実装調査）
出力先: .steering/tasks/<dir>/exploration-impl.md
調査: 対象機能のファイル構成（ViewModel / Repository / iOS Screen）・
      現在の実装フロー（UI → ViewModel → Repository のコールチェーン）・
      類似パターン（同一問題が他のViewModel/Repositoryにないか）・
      関連するテストと FakeRepository の有無
```

**エージェント B** — 影響範囲調査:
```
タスク: $ARGUMENTS（影響範囲調査）
出力先: .steering/tasks/<dir>/exploration-impact.md
調査: 修正によるiOS側インターフェースへの波及・
      Android（composeApp）への波及・
      MockAppComponent対応要否・
      同一問題の横断的存在確認
```

両エージェント完了後、`exploration-impl.md` と `exploration-impact.md` を Read してフェーズ3へ。

---

## フェーズ 3: 計画（planner エージェント → ユーザー承認）

Agent ツールで `planner` エージェントを起動する:

```
探索ファイル:
  - .steering/tasks/<dir>/exploration-impl.md
  - .steering/tasks/<dir>/exploration-impact.md
出力先: .steering/tasks/<dir>/plan.md
機能: $ARGUMENTS
方針: 最小変更の原則。既存の動作に影響しない範囲で修正する。
```

完了後 `plan.md` を Read してユーザーに提示する。
**ユーザー承認を得てからフェーズ4へ進む。**

---

## フェーズ 4: 実装

`plan.md` を参照して修正する。

**変更が複数側に及ぶ場合**: Agent ツールで該当する実装エージェントを並列起動する:
- KMP側: `implementer-kmp`（出力先: `.steering/tasks/<dir>/implementation-kmp.md`）
- iOS側: `implementer-ios`（出力先: `.steering/tasks/<dir>/implementation-ios.md`）
- Android側: `implementer-android`（出力先: `.steering/tasks/<dir>/implementation-android.md`）

**変更が1つの側のみの場合**: メインエージェントが直接修正し、変更ファイル一覧を `.steering/tasks/<dir>/implementation.md` に書き出す。

各ファイル完了ごとに TodoWrite で進捗を更新する。

---

## フェーズ 5: テスト確認

ユーザーに以下のコマンドの実行を依頼し、結果ログを共有してもらう:

```
./gradlew :shared:ui-model:test
```

受け取ったログに失敗があれば修正する。修正後は再度実行を依頼し、結果ログを受け取る。カバーできていないケースがあればテストを追加する。
テスト完了後、結果（合格/失敗件数・失敗内容の要約）を `.steering/tasks/<dir>/test-result.md` に書き出す。

---

## フェーズ 5.5: 機能仕様書の更新確認（feature-doc スキル）

UiState・ViewModelInterface・非自明な設計に変更があった場合のみ実行する。

`docs/feature/<feature-name>/ios.md` または `docs/feature/<feature-name>/android.md` が存在するか確認する。

- **ファイルが存在し、変更が仕様に影響する場合**: 「仕様書を更新しますか？」とユーザーに確認し、「はい」であれば `.claude/skills/feature-doc/SKILL.md` の手順に従い変更されたセクションのみ更新する
- **ファイルが存在しない場合**: 「仕様書が存在しません。新規作成しますか？」とユーザーに確認する
- **変更がインターフェースや設計に影響しない場合（内部ロジックのみの修正など）**: スキップしてよい

---

## フェーズ 6〜8: レビュー・コミット・PR

[`docs/workflow-overview.md` の共通フェーズ定義](../../../docs/workflow-overview.md#共通フェーズ定義フェーズ-68)に従って実行する。

- **ブランチ prefix**: `feature/`（例: `feature/fix-search-pagination`）
- **commit type**: 変更内容に応じて `fix` / `feat` / `refactor` から選択
