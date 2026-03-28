# pr テンプレート

## gh pr create コマンド例

```bash
gh pr create \
  --base develop \
  --title "feat: アーティスト詳細画面を追加" \
  --body "$(cat <<'EOF'
## 概要

Apple Music API の artist エンドポイントを使ったアーティスト詳細画面を実装した。
アルバム一覧をグリッド表示する ArtistScreen と KMP ViewModel を追加。

## 変更内容

- `shared/domain/.../Artist.kt` — ドメインエンティティ追加
- `shared/data/.../ArtistRepositoryImpl.kt` — リポジトリ実装追加
- `shared/ui-model/.../ArtistViewModel.kt` — ViewModel 追加
- `iosApp/.../viewModel/IosArtistViewModel.swift` — iOS ラッパー追加
- `iosApp/.../View/Screen/ArtistScreen.swift` — 画面追加

## テスト

- [x] KMP ユニットテスト（`./gradlew :shared:ui-model:test`）
- [ ] Android 動作確認
- [ ] iOS 動作確認
- [ ] Mock Mode での動作確認
EOF
)"
```

## PR タイトル例

| type | タイトル例 |
|------|-----------|
| `feat` | `feat: アーティスト詳細画面を追加` |
| `fix` | `fix: HomeScreen で楽曲一覧が空のまま表示される問題を修正` |
| `refactor` | `refactor: SearchViewModel のエラー処理を onFailure パターンに統一` |
| `test` | `test: ArtistViewModelTest を追加` |
| `chore` | `chore: Ktor バージョンを 3.1.0 に更新` |
