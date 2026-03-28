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

1. `.steering/tasks/` 以下に既存のディレクトリがあれば一覧を表示し、「過去のタスクログを削除しますか？」と確認する。「はい」の場合は `.steering/tasks/` 以下のディレクトリをすべて削除する。
2. タスクディレクトリパスを決定する
   - 形式: `.steering/tasks/YYYY-MM-DD_<kebab-case-name>/`
2. TodoWrite でタスクを登録する:
   - [ ] 調査: バグの根本原因の特定
   - [ ] 計画: 修正方針の決定・ユーザー承認
   - [ ] 実装: 最小変更での修正
   - [ ] テスト: 修正の検証・テスト追加
   - [ ] レビュー: 実装の観点レビュー
   - [ ] コミット

---

## フェーズ 2: 並列調査（bug-investigator × explorer）

**Agent ツールで2つのエージェントを同時に起動する。**

**エージェント A** — `bug-investigator`:
```
バグの症状: $ARGUMENTS
出力先: .steering/tasks/<dir>/exploration.md
```

**エージェント B** — `explorer`:
```
タスク: $ARGUMENTS（横断影響調査）
出力先: .steering/tasks/<dir>/exploration-impact.md
調査: 同一バグが他のViewModel/Repositoryに横断的に存在しないか・
      MockAppComponent への影響・
      Android（composeApp）への波及
```

両エージェント完了後、`exploration.md` と `exploration-impact.md` を Read してフェーズ3へ。

---

## フェーズ 3: 計画（planner エージェント → ユーザー承認）

Agent ツールで `planner` エージェントを起動する:

```
探索ファイル:
  - .steering/tasks/<dir>/exploration.md
  - .steering/tasks/<dir>/exploration-impact.md
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

---

## フェーズ 5: テスト確認

このバグをカバーするテストケースを追加する（再発防止のためにテストで保護する）。

ユーザーに以下のコマンドの実行を依頼し、結果ログを共有してもらう:

```
./gradlew :shared:ui-model:test
```

受け取ったログに失敗があれば修正する。修正後は再度実行を依頼し、結果ログを受け取る。
テスト完了後、結果（合格/失敗件数・失敗内容の要約）を `.steering/tasks/<dir>/test-result.md` に書き出す。

---

## フェーズ 5.5: 機能仕様書の更新確認（feature-doc スキル）

バグ修正で設計上の契約（UiState・ViewModelInterface・非自明な振る舞い）が変わった場合のみ実行する。内部ロジックのみの修正はスキップしてよい。

変更が仕様に影響すると判断した場合、`docs/feature/<feature-name>/ios.md` または `docs/feature/<feature-name>/android.md` の存在を確認し、「仕様書を更新しますか？」とユーザーに確認してから `.claude/skills/feature-doc/SKILL.md` の手順で更新する。

---

## フェーズ 6〜8: レビュー・コミット・PR

[`docs/workflow-overview.md` の共通フェーズ定義](../../../docs/workflow-overview.md#共通フェーズ定義フェーズ-68)に従って実行する。

- **ブランチ prefix**: `fix/`（例: `fix/home-screen-empty-list`）
- **commit type**: `fix`
