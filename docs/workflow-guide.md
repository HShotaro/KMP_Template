# Claude Code ワークフローガイド

このプロジェクトでは **Claude Code**（Anthropic の AI コーディングアシスタント CLI）を使った開発ワークフローを採用しています。
スラッシュコマンドを起点に、探索・計画・実装・テスト・レビュー・コミットの各フェーズを自動化します。

---

## 前提条件

- [Claude Code](https://claude.ai/code) がインストールされていること
- プロジェクトルートで `claude` コマンドが実行できること

---

## ワークフロー一覧

タスクの種別に応じて3つのコマンドを使い分けます。

| コマンド | 用途 | ブランチ prefix | commit type |
|---------|------|----------------|-------------|
| `/add-feature <機能名>` | 新規画面・既存画面への機能追加 | `feature/` | `feat` |
| `/fix-feature <修正内容>` | 既存機能の修正・リファクタリング | `fix/` | `fix` / `refactor` など |
| `/bugfix <症状>` | 根本原因が不明なバグの調査・修正 | `fix/` | `fix` |

### コマンドの選び方

```
原因が不明でコードを調査する必要がある
  → /bugfix

原因・スコープが明確な修正（UI調整・文言変更・リファクタなど）
  → /fix-feature

新しい画面や機能を追加する
  → /add-feature
```

---

## 共通フェーズ構成

どのワークフローも以下の8フェーズで進みます。

```
フェーズ1  タスクノート初期化
フェーズ2  探索 / 調査         ← サブエージェントが並列実行
フェーズ3  計画               ← ユーザー承認が必須
フェーズ4  実装               ← サブエージェントが並列実行（規模に応じて）
フェーズ5  テスト確認
フェーズ6  レビュー
フェーズ7  コミット
フェーズ8  PR 作成（任意）
```

### フェーズ1: タスクノート初期化

`.steering/tasks/YYYY-MM-DD_<name>/` ディレクトリを作成し、作業ログの保存先を確保します。

### フェーズ2: 探索 / 調査

専用の**サブエージェント**が既存コードを調査し、結果を Markdown ファイルに書き出します。
ワークフローごとに起動するエージェントが異なります（後述）。

### フェーズ3: 計画（ユーザー承認必須）

`planner` エージェントが `plan.md` に実装計画を出力します。
**ユーザーが承認してからフェーズ4へ進みます。** `plan.md` はこの後の実装エージェントが参照する唯一の仕様書になります。

### フェーズ4: 実装

`plan.md` に従って実装します。変更範囲が KMP・iOS・Android の複数側に及ぶ場合は、専用の実装エージェントを並列起動してコンテキスト消費を最小化します。

実装完了後、ユーザーに実機 / シミュレーターでの動作確認を依頼します。問題があればフェーズ3に戻ります。

### フェーズ5: テスト確認

以下のコマンドを実行してもらい、結果ログを共有します。

```bash
./gradlew :shared:ui-model:test
```

失敗した場合は原因を判定し、実装ミスならフェーズ4へ、想定外の失敗なら再調査します。

### フェーズ6: レビュー

`reviewer` エージェントが `review.md` を出力します（メインエージェントのコンテキスト汚染防止のためサブエージェントとして実行）。
重要度「高」の指摘は必ず修正します。その後 `simplify` スキルでコードを簡略化します。

機能仕様書（`ios.md` / `android.md` / `testcase.md`）に変更が必要な場合は `update-feature-doc` スキルで更新します。

### フェーズ7: コミット

`develop` ブランチにいる場合は作業ブランチへの切り替えを提案します。
`commit` スキルの規約に従ってコミットします。

### フェーズ8: PR 作成（任意）

ユーザー確認後、`pr` スキルで GitHub PR を作成します（base: `develop`）。

---

## ワークフロー別の特徴

### `/add-feature` — 機能追加

並列探索・並列実装を最大活用して開発速度を上げます。

```
フェーズ1: products-requirement.md を確認 → gen-feature-doc スキルで
          ios.md / android.md / testcase.md の雛形を生成

フェーズ2: explorer × 3 を同時起動
          - エージェントA: KMP/Kotlin側（ViewModel・Repository パターン調査）
          - エージェントB: iOS/Swift側（Screen・ViewModel ラッパーパターン調査）
          - エージェントC: 影響範囲（DI競合・MockAppComponent 対応要否）

フェーズ3: plan.md に UiState / ViewModelInterface の完全な型定義を含める
          → iOS・Android エージェントが KMP ビルド待ちなしで並列実装できる共有契約

フェーズ4: implementer-kmp × implementer-ios × implementer-android を同時起動
```

### `/fix-feature` — 既存機能修正・リファクタリング

最小変更の原則。既存の動作に影響しない範囲で修正します。
リファクタリングにも使用できます（振る舞いを変えない場合は仕様書更新をスキップ）。

```
フェーズ2: explorer × 2 を同時起動
          - エージェントA: 対象機能の実装調査（コールチェーン・テスト有無）
          - エージェントB: 影響範囲調査（iOS/Android 波及・横断的問題の有無）

フェーズ4: 変更が複数側に及ぶ → 実装エージェントを並列起動
          変更が1つの側のみ  → メインエージェントが直接修正
```

### `/bugfix` — バグ修正

根本原因の特定を優先します。確信度が低い場合は仮説検証ループを回します。

```
フェーズ2: bug-investigator + explorer を同時起動
          - bug-investigator: 根本原因の仮説を複数列挙（確信度付き）
          - explorer: 同一バグの横断的存在確認

          確信度「高」が1つに絞り込まれない場合:
          → explorer を再起動して仮説を検証（最大3 iteration）

フェーズ4: メインエージェントが直接修正（並列実装は使用しない）

フェーズ5: このバグをカバーするテストを追加（再発防止）
```

---

## サブエージェント一覧

ワークフローが `Agent` ツールで起動する専用エージェントです。各エージェントはメインエージェントのコンテキストを消費せずに処理を完結し、`.steering/tasks/` に結果を書き出します。

| エージェント | 役割 | 主な出力ファイル |
|------------|------|----------------|
| `explorer` | コードベース探索（既存パターン・DI配線・テスト構造の調査） | `exploration*.md` |
| `bug-investigator` | バグの根本原因調査（仮説列挙・コールチェーン追跡） | `exploration.md` |
| `planner` | 実装計画の立案（インターフェース定義を含む） | `plan.md` |
| `implementer-kmp` | KMP/Kotlin 側の実装（Domain / Data / ViewModel / DI / テスト） | `implementation-kmp.md` |
| `implementer-ios` | iOS/Swift 側の実装（IosViewModel / Screen / AppDestination） | `implementation-ios.md` |
| `implementer-android` | Android/Compose 側の実装（Screen / ナビゲーション） | `implementation-android.md` |
| `reviewer` | 実装レビュー（ルール準拠・コード品質） | `review.md` |

---

## スキル一覧

ワークフローの特定フェーズや手動操作で呼び出す手順定義です。

| スキル | 用途 | 呼び出し方 |
|--------|------|----------|
| `commit` | コミットメッセージ生成・コミット実行 | フェーズ7で自動呼び出し |
| `pr` | GitHub PR 作成（base: `develop`） | フェーズ8で自動呼び出し |
| `gen-feature-doc` | `products-requirement.md` から `ios.md` / `android.md` / `testcase.md` の雛形生成 | `/add-feature` フェーズ1で自動呼び出し |
| `update-feature-doc` | 機能仕様書の差分更新 | フェーズ6で自動呼び出し（変更があれば） |
| `simplify` | コードの再利用性・品質・効率の改善 | フェーズ6で自動呼び出し |
| `review` | 手動レビュー | `/review` |
| `new-screen` | 新画面のボイラープレート生成 | `/new-screen` |
| `create-issue` | GitHub Issue の作成 | `/create-issue <概要>` |
| `arch-patterns` | アーキテクチャパターン早見表 | エージェントが参照 |
| `test-specs` | KMP・iOS ユニットテスト仕様ガイド | エージェントが参照 |
| `feature-specs` | 機能ドキュメントへのインデックス | エージェントが参照 |

---

## 作業ログの保存先

ワークフロー実行中の中間成果物は `.steering/tasks/` に保存されます。

```
.steering/tasks/YYYY-MM-DD_<name>/
├── exploration.md           # 調査結果
├── exploration-kmp.md       # KMP側探索結果（add-feature）
├── exploration-ios.md       # iOS側探索結果（add-feature）
├── exploration-impl.md      # 実装調査結果（fix-feature）
├── exploration-impact.md    # 影響範囲調査結果
├── exploration-hypothesis.md # 仮説検証結果（bugfix）
├── plan.md                  # 実装計画
├── implementation-kmp.md    # KMP実装完了ファイル一覧
├── implementation-ios.md    # iOS実装完了ファイル一覧
├── implementation-android.md # Android実装完了ファイル一覧
├── implementation.md        # 実装完了ファイル一覧（直接修正時）
├── test-result.md           # テスト結果サマリー
├── review.md                # レビュー結果
└── result.md                # コミットハッシュと変更概要
```

---

## ドキュメント体系

機能開発に関わるドキュメントは `docs/feature/<feature-name>/` 以下に配置されます。

| ファイル | 内容 | リファクタで変わるか |
|---------|------|-------------------|
| `products-requirement.md` | 振る舞い・仕様（What） | 変わらない |
| `ios.md` / `android.md` | インターフェース定義（How） | インターフェースが変わる場合のみ |
| `testcase.md` | テストケース一覧 | 振る舞いが変わらない限り変わらない |

---

## 関連ドキュメント

- [`docs/workflow-overview.md`](workflow-overview.md) — 共通フェーズの詳細定義
- [`docs/workflow-reference.md`](workflow-reference.md) — エージェント・スキルの詳細仕様
