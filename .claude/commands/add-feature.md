# 機能追加ワークフロー

「探索→計画→並列実装→テスト→レビュー→コミット」の6フェーズで機能を追加する。
新規画面の追加・既存画面への機能追加どちらにも使用する。
サブエージェントの並列実行でコンテキスト消費を最小化しながら開発速度を上げる。

## 使い方

```
/add-feature <機能名と概要>
```

例: `/add-feature アーティスト詳細画面の追加`

---

## フェーズ 1: タスクノート初期化

`$ARGUMENTS` が空の場合は「どのような機能を追加しますか？（例: アーティスト詳細画面の追加）」と確認してから進む。

1. `.steering/tasks/` 以下に既存のディレクトリがあれば一覧を表示し、「過去のタスクログを削除しますか？」と確認する。「はい」の場合は `.steering/tasks/` 以下のディレクトリをすべて削除する。
2. タスクディレクトリパスを決定する
   - 形式: `.steering/tasks/YYYY-MM-DD_<kebab-case-name>/`
2. TodoWrite でタスクを登録する:
   - [ ] 並列探索: KMP側・iOS側の調査
   - [ ] 計画: planner エージェントによる計画立案・ユーザー承認
   - [ ] 並列実装: KMP側・iOS側・Android側の同時実装
   - [ ] テスト: ユニットテストの確認・追加
   - [ ] レビュー: reviewer エージェントによるレビュー
   - [ ] コミット

---

## フェーズ 2: 並列探索（explorer × 3）

**Agent ツールで3つの `explorer` エージェントを同時に起動する。**

**エージェント A** — KMP / Kotlin 側:
```
タスク: $ARGUMENTS（KMP/Kotlin側）
出力先: .steering/tasks/<dir>/exploration-kmp.md
調査: 類似ViewModel/UiStateパターン・ドメインエンティティ・リポジトリ有無・
      AppComponent/IosViewModelProvider現状・テストパターン・FakeRepository有無
```

**エージェント B** — iOS / Swift 側:
```
タスク: $ARGUMENTS（iOS側）
出力先: .steering/tasks/<dir>/exploration-ios.md
調査: 類似iOSViewModelラッパー・類似Screenパターン・AppDestination追加箇所・
      ローカライズキー命名パターン
```

**エージェント C** — 影響範囲:
```
タスク: $ARGUMENTS（影響範囲調査）
出力先: .steering/tasks/<dir>/exploration-impact.md
調査: 追加による既存機能への副作用（DI競合）・MockAppComponent対応要否・
      Android（composeApp）への波及・同一パターンの横断確認
```

全エージェント完了後、3ファイルを Read してフェーズ3へ。

---

## フェーズ 3: 計画（planner エージェント → ユーザー承認）

Agent ツールで `planner` エージェントを起動する:

```
探索ファイル:
  - .steering/tasks/<dir>/exploration-kmp.md
  - .steering/tasks/<dir>/exploration-ios.md
  - .steering/tasks/<dir>/exploration-impact.md
出力先: .steering/tasks/<dir>/plan.md
機能: $ARGUMENTS
必須: plan.md に XxxUiState / XxxViewModelInterface の完全な型定義を含めること
      （implementer-ios が KMP コンパイル待ちなしに実装できるようにするための共有契約）
```

完了後 `plan.md` を Read してユーザーに提示する。
**ユーザー承認を得てからフェーズ3.5へ進む。**（plan.md はフェーズ4・5のエージェントの唯一の仕様書になる）

---

## フェーズ 3.5: 機能仕様書の作成（feature-doc スキル）

`docs/feature/<feature-name>/ios.md` および `docs/feature/<feature-name>/android.md` の存在を確認する。

- **ファイルが存在しない場合**: `.claude/skills/feature-doc/SKILL.md` の手順に従い新規作成する。`plan.md` の UiState・ViewModelInterface 定義を情報源にする
- **ファイルが既に存在する場合**: 内容を Read してユーザーに提示し、「仕様書が既に存在します。上書き更新しますか？それともスキップしますか？」と確認してから進む

---

## フェーズ 4: 並列実装（implementer-kmp × implementer-ios × implementer-android）

**Agent ツールで3つの実装エージェントを同時に起動する。**

**エージェント A** — `implementer-kmp`:
```
plan.md: .steering/tasks/<dir>/plan.md
出力先: .steering/tasks/<dir>/implementation-kmp.md
担当: KMP側（Domain / Data / ViewModel / DI配線 / ユニットテスト）
```

**エージェント B** — `implementer-ios`:
```
plan.md: .steering/tasks/<dir>/plan.md
出力先: .steering/tasks/<dir>/implementation-ios.md
担当: iOS側（.claude/skills/new-screen/SKILL.md のパターンを使い、IosXxxViewModel / XxxScreen / AppDestination を実装）
※ plan.md のインターフェース仕様を参照して実装すること
```

**エージェント C** — `implementer-android`:
```
plan.md: .steering/tasks/<dir>/plan.md
出力先: .steering/tasks/<dir>/implementation-android.md
担当: Android側（Compose Screen / MainScreen へのナビゲーション追加）
※ plan.md のインターフェース仕様を参照して実装すること
```

全エージェント完了後、3つの完了ファイル一覧を確認してフェーズ5へ。

---

## フェーズ 5: テスト確認

ユーザーに以下のコマンドの実行を依頼する:

```
./gradlew :shared:ui-model:test
```

失敗があれば結果を共有してもらい、メインエージェントが直接修正する（KMP/iOS の区別なく対処）。修正後に再度実行を依頼する。
テスト完了後、結果（合格/失敗件数・失敗内容の要約）を `.steering/tasks/<dir>/test-result.md` に書き出す。

---

## フェーズ 6〜8: レビュー・コミット・PR

[`docs/workflow-overview.md` の共通フェーズ定義](../../../docs/workflow-overview.md#共通フェーズ定義フェーズ-68)に従って実行する。

- **ブランチ prefix**: `feature/`（例: `feature/artist-detail-screen`）
- **commit type**: `feat`
