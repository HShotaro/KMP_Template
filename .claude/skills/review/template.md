# review テンプレート

## 問題がある場合の出力例

```
[重要度: 高] shared/ui-model/src/commonMain/.../ArtistViewModel.kt:28
`loadData()` が複数回呼ばれると重複した coroutine が起動される
`viewModelScope.launch` の前に既存ジョブをキャンセルするか、`loadData()` の冒頭で isLoading チェックを追加する

[重要度: 中] shared/ui-model/src/commonTest/.../ArtistViewModelTest.kt
エラー系のテストケースがない
FakeArtistRepository でエラーを返すケースを追加する

[重要度: 低] shared/ui-model/src/commonMain/.../ArtistViewModel.kt:45
TODO コメントが残っている
実装済みであれば削除する
```

## 問題なしの場合の出力例

```
レビュー完了: 問題なし
```

## 重要度の目安

| 重要度 | 基準 | 対応 |
|--------|------|------|
| 高 | クラッシュ・データ不整合・セキュリティ問題 | コミット前に必ず修正 |
| 中 | テスト不足・設計上の問題・保守性の低下 | 対応を推奨（任意） |
| 低 | スタイル・命名・コメントなど | 必要に応じて対応 |
