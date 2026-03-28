# pr スキル

現在のブランチの変更を解析し、GitHub PR を作成する。

## 実行手順

1. `git log develop..HEAD --oneline` でコミット一覧を確認する
2. `git diff develop...HEAD` で変更差分を確認する
3. PR タイトル・本文を生成してユーザーに確認を求める
4. 承認後に `gh pr create` で PR を作成する

## PR フォーマット

### タイトル
```
<type>: <概要>（日本語、60文字以内）
```
type は `commit` スキルと同じ（feat / fix / refactor / test / docs / chore / style）

### 本文テンプレート

```markdown
## 概要

<!-- この PR で何をしたか、なぜそうしたかを簡潔に -->

## 変更内容

<!-- 変更したファイル・モジュールと変更の要点 -->
-

## テスト

<!-- テスト方法・確認した動作 -->
- [ ] KMP ユニットテスト（`./gradlew test`）
- [ ] Android 動作確認
- [ ] iOS 動作確認（該当する場合）
- [ ] Mock Mode での動作確認（該当する場合）
```

## ブランチ・PR の規則

- base ブランチは `develop`
- ブランチ名が `feature/xxx` の場合、type は `feat`
- ブランチ名が `fix/xxx` の場合、type は `fix`
- 複数の独立した変更が混在している場合は分割を提案する

## 注意事項

PR 作成は外部への公開操作のため、内容を提示してユーザーの最終確認を取ってから `gh pr create` を実行する。

## コード例は `.claude/skills/pr/template.md` を参照
