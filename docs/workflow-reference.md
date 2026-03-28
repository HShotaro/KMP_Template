# ワークフロー リファレンス

ワークフローを構成するサブエージェントとスキルの一覧。詳細仕様は各ファイルを参照。

---

## サブエージェント（`.claude/agents/`）

ワークフローが Agent ツールで起動する専用エージェント。各エージェントはメインエージェントのコンテキストを消費せずに処理を完結し、`.steering/tasks/` に結果を書き出す。

| エージェント | 役割 | モデル | 出力ファイル | 詳細 |
|------------|------|--------|------------|------|
| `explorer` | コードベース探索 | haiku | `exploration*.md` | [explorer.md](../.claude/agents/explorer.md) |
| `bug-investigator` | バグの根本原因調査 | sonnet | `exploration.md` | [bug-investigator.md](../.claude/agents/bug-investigator.md) |
| `planner` | 実装計画の立案 | sonnet | `plan.md` | [planner.md](../.claude/agents/planner.md) |
| `implementer-kmp` | KMP/Kotlin 側の実装 | sonnet | `implementation-kmp.md` | [implementer-kmp.md](../.claude/agents/implementer-kmp.md) |
| `implementer-ios` | iOS/Swift 側の実装 | sonnet | `implementation-ios.md` | [implementer-ios.md](../.claude/agents/implementer-ios.md) |
| `implementer-android` | Android/Compose 側の実装 | sonnet | `implementation-android.md` | [implementer-android.md](../.claude/agents/implementer-android.md) |
| `reviewer` | 実装レビュー（ルール準拠 + コード品質） | sonnet | `review.md` | [reviewer.md](../.claude/agents/reviewer.md) |

> `reviewer` エージェントと `review` スキルの使い分け:
> - **reviewer エージェント** — ワークフローのフェーズ6で使用。コンテキスト保護が目的。
> - **review スキル** — ユーザーが `/review` で手動実行する基本レビュー。メインエージェントのコンテキスト内で実行。

---

## スキル（`.claude/skills/`）

ワークフローの特定フェーズで使用される手順定義。

| スキル | 用途 | 詳細 |
|--------|------|------|
| `commit` | コミットメッセージ生成・コミット実行 | [SKILL.md](../.claude/skills/commit/SKILL.md) |
| `pr` | GitHub PR 作成（base: `develop`） | [SKILL.md](../.claude/skills/pr/SKILL.md) |
| `new-screen` | 新画面のボイラープレート生成 | [SKILL.md](../.claude/skills/new-screen/SKILL.md) |
| `review` | 手動レビュー用チェックリスト | [SKILL.md](../.claude/skills/review/SKILL.md) |
| `simplify` | コードの再利用性・品質・効率の改善 | [SKILL.md](../.claude/skills/simplify/SKILL.md) |
| `arch-patterns` | アーキテクチャパターン早見表 | [SKILL.md](../.claude/skills/arch-patterns/SKILL.md) |
| `feature-specs` | 機能ドキュメントへのインデックス | [SKILL.md](../.claude/skills/feature-specs/SKILL.md) |
| `feature-doc` | `docs/feature/` 配下の機能仕様書の作成・更新 | [SKILL.md](../.claude/skills/feature-doc/SKILL.md) |
| `test-specs` | KMP・iOS ユニットテスト仕様ガイド | [SKILL.md](../.claude/skills/test-specs/SKILL.md) |

---

## 関連ドキュメント

- [workflow-overview.md](workflow-overview.md) — ワークフローの概要・フェーズ構成
