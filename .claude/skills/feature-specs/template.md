# feature-specs テンプレート

## MockAppComponent への新機能追加パターン

```kotlin
// shared/ui-model/src/commonMain/.../di/MockAppComponent.kt
class MockAppComponent : AppComponent() {
    // 既存のリポジトリ・ViewModel ...

    // 追加パターン
    private val xxxRepo = object : XxxRepository {
        override suspend fun fetchItems(): Result<List<XxxItem>> =
            Result.success(mockXxxItems)
    }
    override val xxxViewModel: XxxViewModel = XxxViewModel(xxxRepo)
}

// ファイル末尾にモックデータを定義
private val mockXxxItems = listOf(
    XxxItem(id = "1", title = "Mock Item 1"),
    XxxItem(id = "2", title = "Mock Item 2"),
)
```

## AppDestination の現在の全 case

```swift
enum AppDestination: Hashable, Codable {
    case tab(TabItem)
    case playlistDetail(id: String, name: String)
    case song(String)              // TODO: SongDetailScreen
    case album(String)             // TODO: AlbumDetailScreen
    case artist(String)            // TODO: ArtistDetailScreen
    case catalogPlaylist(String)   // TODO: CatalogPlaylistDetailScreen
}
```

| case | preferredTab | 遷移先 |
|------|-------------|--------|
| `.tab(tab)` | `tab` | タブ切り替えのみ |
| `.playlistDetail` | `.playlist` | PlaylistDetailScreen |
| `.song` | `.home` | SongDetailScreen（TODO） |
| `.album` | `.search` | AlbumDetailScreen（TODO） |
| `.artist` | `.search` | ArtistDetailScreen（TODO） |
| `.catalogPlaylist` | `.search` | CatalogPlaylistDetailScreen（TODO） |

## Apple Music API 認証フロー

```
Developer Token (JWT, local.properties → AppleMusicConfig.DEVELOPER_TOKEN)
  └── Authorization: Bearer <token>  // 全リクエスト

Music User Token (iOS MusicKit → TokenStorage → multiplatform-settings)
  └── Music-User-Token: <token>      // /v1/me/ 系エンドポイント
```

## Music Player アーキテクチャ

### iOS
```
MusicPlayerManager (Swift)
  ├── ApplicationMusicPlayer を操作（MusicKit）
  ├── 状態変化を監視
  └── playerViewModel.onXxx() を呼ぶ → KMP PlayerViewModel に状態を書き込む

IosPlayerViewModel (@KmpObservableViewModel)
  └── uiState: PlayerUiState を @Published で公開
      → MiniPlayerView / FullPlayerScreen が observe
```

### Android
```
MusicPlayerController (KMP actual / Android)
  └── ExoPlayer で 30秒プレビュー URL を再生
      ※ フル再生は MusicKit 専用（Android 非対応）
```

## iOS ルーティングのディープリンク処理フロー

```
onOpenURL
  └── ContentViewModel.handleDeepLink(_:)
        ├── selectTab(tab)           // タブ切り替え
        └── pendingDeepLink = dest   // 非タブ遷移のみ

deepLinkNavigation modifier（各スクリーン）
  └── onChange(pendingDeepLink)
        ├── selectedTab == self.tab かチェック
        ├── navigationPath.append(destination)
        └── consumeDeepLink()
```

## 既存 TabItem

| case | URL Scheme host |
|------|----------------|
| `.home` | `home` |
| `.search` | `search` |
| `.library` | `library` |
| `.playlist` | `playlist` |
| `.recipe` | `recipe` |

## Mock Mode の切り替え方法

| プラットフォーム | 方法 |
|--------------|------|
| Android | Android Studio の Build Variants で `mockDebug` を選択 |
| iOS | Xcode のスキーム選択で `iosApp-Mock` を選ぶ |

## テスト実行コマンド早見表

```bash
./gradlew test                          # 全モジュール
./gradlew :shared:data:test             # data のみ
./gradlew :shared:ui-model:test         # ui-model のみ
./gradlew :shared:ui-model:test --tests "*.XxxViewModelTest"  # 特定クラス
```
