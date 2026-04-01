# 既存機能修正ワークフロー

「探索→計画→実装→テスト→レビュー→コミット」の6フェーズで既存機能を修正する。
変更が KMP・iOS 両方に及ぶ場合は並列実装を選択できる。

原因が既知・スコープが明確な修正に使用する（UIの見た目・文言調整、仕様変更に伴う動作変更、リファクタリングなど）。
原因が不明でコードを調査して根本原因を特定する必要があるバグには `/bugfix` を使用すること。

## 使い方

```
/fix-feature <対象機能と修正内容>
```

例:
- `/fix-feature 検索画面の検索結果カードのレイアウトを調整`
- `/fix-feature 楽曲詳細画面のタイトル文言を仕様変更に合わせて更新`
- `/fix-feature HomeViewModelの不要なAPI呼び出しをリファクタリング`

---

## フェーズ 1: タスクノート初期化

`$ARGUMENTS` が空の場合は「どの機能をどのように修正しますか？（例: 検索画面のページネーションが2ページ目以降取得できない問題を修正）」と確認してから進む。

[`docs/workflow-overview.md` の共通フェーズ定義（フェーズ1）](../../docs/workflow-overview.md#共通フェーズ定義) の手順1〜2を実行する。続いて以下のタスクを TodoWrite で登録する:
- [ ] 探索: 対象機能の現状調査
- [ ] 計画: 修正計画の立案・ユーザー承認
- [ ] 実装: コードの修正
- [ ] 動作確認: ユーザーによる実機確認
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

### 動作確認（ユーザー確認必須）

実装完了後、以下の文言でユーザーに動作確認を依頼する:

> 実装が完了しました。実機またはシミュレーターで動作確認をお願いします。
> - 問題なし → 「問題なし」とお知らせください。フェーズ5（テスト確認）へ進みます。
> - 問題あり → 「問題あり：[症状]」とお知らせください。実装を元に戻してフェーズ3（計画）へ戻ります。

**問題ありの場合の対応:**

1. ユーザーが報告した症状をコードと照合し、**修正が必要な箇所と理由**（なぜ現在の実装が動作しないか）を分析してユーザーに伝える。
2. 以下の質問をユーザーに提示する:

   > 上記を踏まえて、どちらで対応しますか？
   > - **再計画**: 計画を見直す必要がある → フェーズ3（計画）に戻ります
   > - **微修正**: 現在の計画の範囲内で実装を直せる → フェーズ4で続けます

**「再計画」を選んだ場合:**

1. `git diff --name-only HEAD` で変更ファイルを確認し、`git restore $(git diff --name-only HEAD)` で元に戻す
2. 発覚した症状を `exploration-impl.md` の末尾に以下の形式で追記する:
   ```
   ## 動作確認フィードバック（再計画用）
   - 試みた修正: [plan.md の修正内容の要約]
   - 発覚した症状: [ユーザーが報告した症状]
   - 想定と異なった点: [なぜ修正が効かなかったか]
   ```
3. フェーズ3（計画）へ戻り、`planner` エージェントを以下のプロンプトで再起動する:
   ```
   探索ファイル:
     - .steering/tasks/<dir>/exploration-impl.md（末尾の動作確認フィードバックを含む）
     - .steering/tasks/<dir>/exploration-impact.md
   出力先: .steering/tasks/<dir>/plan.md
   機能: $ARGUMENTS
   方針: 最小変更の原則。前回の修正方針が動作確認で否定されたため、
         exploration-impl.md 末尾のフィードバックを踏まえて修正方針を再検討すること。
   ```

**「微修正」を選んだ場合:**

フェーズ4の先頭に戻り、特定した問題箇所を直接修正する。修正後、再度動作確認をユーザーに依頼する。

---

## フェーズ 5: テスト確認

カバーできていないケースがあればテストを追加する。

[`docs/workflow-overview.md` の共通フェーズ定義（フェーズ5）](../../docs/workflow-overview.md#共通フェーズ定義) に従って実行する。

---

## フェーズ 6〜8: レビュー・コミット・PR

[`docs/workflow-overview.md` の共通フェーズ定義](../../docs/workflow-overview.md#共通フェーズ定義)に従って実行する。

- **ブランチ prefix**: `fix/`（例: `fix/search-result-card-layout`）
- **commit type**: 変更内容に応じて `fix` / `feat` / `refactor` から選択
