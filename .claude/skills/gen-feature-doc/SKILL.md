---
name: gen-feature-doc
description: products-requirement.md を元に各画面の ios.md・android.md・testcase.md を作成または差分更新し、test.md・uitest.md を必要に応じて更新する。
---

# gen-feature-doc スキル

`docs/feature/<feature-name>/products-requirement.md` を入力として、同ディレクトリの `ios.md` / `android.md` / `testcase.md` を作成または差分更新する。
また `docs/testing/test.md` と `docs/testing/uitest.md` を必要に応じて更新する。

## 実行タイミング

| ワークフロー | タイミング | 操作 |
|---|---|---|
| `/add-feature` | **フェーズ1（タスクノート初期化後）**。`products-requirement.md` を作成したら即実行 | ios.md / android.md / testcase.md の雛形を作成。後続の explorer・planner が参照できるようにする |
| 手動 | プロダクト要件が変更され、技術ドキュメントを更新したいとき | 差分更新 |

**`update-feature-doc` との使い分け**:
- `gen-feature-doc`（フェーズ1）: `products-requirement.md` から技術ドキュメントの**雛形**を作成する（explorer・planner が参照できるようにする）
- `update-feature-doc`（レビュー承認後）: 確定した実装コードを見て技術ドキュメントを**更新**する

## 実行手順

### 1. 対象 feature の確認

ユーザーから feature 名（例: `home`, `search`, `artist-detail`）を受け取る。
`docs/feature/<feature-name>/products-requirement.md` が存在することを確認する。

### 2. 入力ドキュメントを読む

以下を Read する:
- `docs/feature/<feature-name>/products-requirement.md` — 生成の主な情報源
- `docs/feature/<feature-name>/ios.md`（存在する場合）— 既存の技術仕様（ViewModel/UiState/Interface）を引き継ぐ
- `docs/feature/<feature-name>/android.md`（存在する場合）— 同上
- `docs/feature/<feature-name>/testcase.md`（存在する場合）— 既存テストケースを引き継ぐ
- `.claude/skills/arch-patterns/SKILL.md` — UiState・ViewModel・DI パターンの参照

### 3. ios.md を作成または差分更新する

テンプレート構造・記載判断チートシートは **`.claude/skills/gen-feature-doc/template.md`** を参照。

- **ファイルが存在しない場合**: テンプレートに従って新規作成する
- **ファイルが存在する場合**: `products-requirement.md` との差分のみを更新する（追加・変更・削除）。変更のないセクションは触らない

### 4. android.md を作成または差分更新する

テンプレート構造は **`.claude/skills/gen-feature-doc/template.md`** を参照。iOS 固有セクションは Android 固有内容に置き換える。ios.md と同じ方針（新規作成または差分更新）で処理する。

### 5. testcase.md を作成または差分更新する

テンプレート構造は **`.claude/skills/gen-feature-doc/template.md`** を参照。

- **ファイルが存在しない場合**: テンプレートに従って新規作成する
- **ファイルが存在する場合**: `products-requirement.md` との差分のみを反映する。要件追加 → テストケース追加、要件変更 → 該当ケース更新、要件削除 → 対応ケースを削除。変更のないテストケースは触らない

KMP ユニットテストは「UX フロー」「非機能要件」から正常系・異常系・エッジケースを導出する。
iOS / Android UITest は画面の主要な状態（ロード・コンテンツ・エラー・空状態）を網羅するケースを導出する。

### 6. docs/testing/test.md を更新する

新規 feature の場合は ViewModel テストへの参照行を追加する。追記フォーマットは **`template.md`** を参照。
既存 feature の再生成の場合は参照行の存在を確認するのみ。

### 7. docs/testing/uitest.md を更新する

新規 feature の場合は iOS / Android テストファイル一覧テーブルへの追加行と、accessibilityIdentifier / testTag 一覧への追加行を更新する。追記フォーマットは **`template.md`** を参照。

### 8. feature-specs/SKILL.md のインデックスを更新する

`.claude/skills/feature-specs/SKILL.md` のテーブルに新しいエントリを追加する（既存行がある場合は修正する）。

### 9. 完了報告

生成・更新したファイルの一覧をユーザーに報告する。

## 注意事項

- 既存ファイルがある場合は**差分更新**（変更のないセクションは触らない）。ファイルが存在しない場合のみ新規作成する。
- 既存の ViewModel/UiState/Interface 定義が正確な場合（実装済みコードと整合している場合）は、products-requirement.md から導出した内容より既存定義を優先する。
- 要件削除に伴う技術仕様・テストケースの削除は、`products-requirement.md` と既存ファイルを突き合わせて該当箇所を特定してから行う。
