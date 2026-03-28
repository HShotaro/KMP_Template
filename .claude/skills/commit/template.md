# commit テンプレート

## コミットメッセージ例

### 新機能
```
feat: アーティスト詳細画面を追加

Apple Music API の artist エンドポイントを使い、
アルバム一覧を表示するArtistScreenを実装した。
```

### バグ修正
```
fix: HomeScreen で楽曲一覧が空のまま表示される問題を修正

observeUiState の呼び出しが onAppear より前に行われていなかったため
初回ロード時に state が反映されなかった。
```

### リファクタリング
```
refactor: SearchViewModel のエラー処理を onFailure パターンに統一
```

### テスト追加
```
test: PlaylistViewModelTest を追加
```

### ビルド設定
```
chore: shared:network の Ktor バージョンを 3.1.0 に更新
```

## 分割を提案するケース

以下のように独立した変更が混在している場合は分割を提案する:

```
# NG: 2つの独立した変更が混在
feat: アーティスト画面を追加 + SearchViewModel のリファクタリング

# OK: 分割する
feat: アーティスト詳細画面を追加
refactor: SearchViewModel のエラー処理を onFailure パターンに統一
```
