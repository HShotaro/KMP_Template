# バグ修正ワークフロー

「調査→計画→実装→テスト→レビュー→コミット」の6フェーズでバグを修正する。
根本原因の特定を優先し、最小変更で修正する。

## 使い方

```
/bugfix <バグの症状と再現手順>
```

例: `/bugfix HomeScreenで初回起動時に楽曲一覧が空のまま表示される。onAppearで loadSongs()を呼んでいるが反映されない`

---

## フェーズ 1: タスクノート初期化

`$ARGUMENTS` が空の場合は「バグの症状と再現手順を教えてください（例: HomeScreen で初回起動時に楽曲一覧が空のまま表示される）」と確認してから進む。

[`docs/workflow-overview.md` の共通フェーズ定義（フェーズ1）](../../docs/workflow-overview.md#共通フェーズ定義) の手順1〜2を実行する。続いて以下のタスクを TodoWrite で登録する:
- [ ] 調査: バグの根本原因の特定
- [ ] 計画: 修正方針の決定・ユーザー承認
- [ ] 実装: 最小変更での修正
- [ ] 動作確認: ユーザーによる実機確認
- [ ] テスト: 修正の検証・テスト追加
- [ ] レビュー: 実装の観点レビュー
- [ ] コミット

---

## フェーズ 2: 並列調査（bug-investigator × explorer）＋ 仮説検証ループ

### iteration 1: 初回並列調査

**Agent ツールで2つのエージェントを同時に起動する。**

**エージェント A** — `bug-investigator`:
```
バグの症状: $ARGUMENTS
出力先: .steering/tasks/<dir>/exploration.md
根本原因の仮説を複数列挙し、各仮説の確信度（高/中/低）を明記すること
```

**エージェント B** — `explorer`:
```
タスク: $ARGUMENTS（横断影響調査）
出力先: .steering/tasks/<dir>/exploration-impact.md
調査: 同一バグが他のViewModel/Repositoryに横断的に存在しないか・
      MockAppComponent への影響・
      Android（composeApp）への波及
```

### iteration 2: 仮説検証（不確定な場合のみ）

`exploration.md` を Read して、確信度「高」の仮説が1つに絞り込まれているかを確認する。

- **絞り込まれている場合**: フェーズ3へ進む
- **複数の仮説が残っている（確信度「高」が複数 or 最高が「中」）場合**:
  `explorer` エージェントを再起動し、各仮説を裏付ける・否定するコードを具体的に特定させる:
  ```
  タスク: 以下の仮説A/Bのどちらが正しいかを、コードを読んで確定させる
    仮説: [exploration.md の仮説リストをここに貼る]
  出力先: .steering/tasks/<dir>/exploration-hypothesis.md
  ```
  完了後 `exploration-hypothesis.md` を Read し、根本原因を1つに確定する（最大 iteration 3 まで。それ以上は仮説を列挙したままフェーズ3へ進む）。

両エージェント（+ 仮説検証）完了後、フェーズ3へ。

---

## フェーズ 3: 計画（planner エージェント → ユーザー承認）

Agent ツールで `planner` エージェントを起動する:

```
探索ファイル:
  - .steering/tasks/<dir>/exploration.md
  - .steering/tasks/<dir>/exploration-impact.md
  - .steering/tasks/<dir>/exploration-hypothesis.md（存在する場合）
出力先: .steering/tasks/<dir>/plan.md
方針: 最小変更の原則。根本原因のみを修正し、周辺コードには手を加えない。
```

完了後 `plan.md` を Read してユーザーに提示する。
**ユーザー承認を得てからフェーズ4へ進む。**

---

## フェーズ 4: 実装

`plan.md` の修正方針に従い、メインエージェントが直接修正する。
（バグ修正は根本原因への集中が重要なため、並列実装は使用しない）

修正完了後、変更ファイル一覧を `.steering/tasks/<dir>/implementation.md` に書き出す。

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
2. 発覚した症状を `.steering/tasks/<dir>/exploration.md` の末尾に以下の形式で追記する:
   ```
   ## 動作確認フィードバック（再計画用）
   - 試みた修正: [plan.md の修正内容の要約]
   - 発覚した症状: [ユーザーが報告した症状]
   - 仮定が外れた可能性: [なぜ修正が効かなかったか]
   ```
3. フェーズ3（計画）へ戻り、`planner` エージェントを以下のプロンプトで再起動する:
   ```
   探索ファイル:
     - .steering/tasks/<dir>/exploration.md（末尾の動作確認フィードバックを含む）
     - .steering/tasks/<dir>/exploration-impact.md
   出力先: .steering/tasks/<dir>/plan.md
   方針: 最小変更の原則。前回の修正方針が動作確認で否定されたため、
         exploration.md 末尾のフィードバックを踏まえて修正方針を再検討すること。
   ```

**「微修正」を選んだ場合:**

フェーズ4の先頭に戻り、特定した問題箇所を直接修正する。修正後、再度動作確認をユーザーに依頼する。

---

## フェーズ 5: テスト確認

このバグをカバーするテストケースを追加する（再発防止のためにテストで保護する）。

[`docs/workflow-overview.md` の共通フェーズ定義（フェーズ5）](../../docs/workflow-overview.md#共通フェーズ定義) に従って実行する。

---

## フェーズ 6: レビュー

[`docs/workflow-overview.md` の共通フェーズ定義（フェーズ6）](../../docs/workflow-overview.md#共通フェーズ定義)に従ってレビューを実施する。

## フェーズ 7〜8: コミット・PR

[`docs/workflow-overview.md` の共通フェーズ定義（フェーズ7〜8）](../../docs/workflow-overview.md#共通フェーズ定義)に従って実行する。

- **ブランチ prefix**: `fix/`（例: `fix/home-screen-empty-list`）
- **commit type**: `fix`
