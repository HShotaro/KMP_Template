---
name: feature-specs
description: 機能仕様インデックス。タスクに関連する機能ドキュメントへのガイド。planner・implementer-kmp・implementer-ios・explorer が参照する。
---

# feature-specs スキル

計画・実装前に関連する機能ドメインの仕様を確認する。

## 実行手順

タスクに関連する機能のドキュメントを Read する。関係ない機能は読み飛ばしてよい。

| 機能 | 読むドキュメント | 読む場面 |
|------|----------------|---------|
| Home (iOS) | `docs/feature/home/ios.md` | HomeScreen・HomeViewModel・ジャンルタブ関連（iOS） |
| Home (Android) | `docs/feature/home/android.md` | HomeScreen・HomeViewModel・ジャンルタブ関連（Android） |
| Search (iOS) | `docs/feature/search/ios.md` | SearchScreen・SearchViewModel・候補表示・検索結果関連（iOS） |
| Search (Android) | `docs/feature/search/android.md` | SearchScreen・SearchViewModel・候補表示・検索結果関連（Android） |
| Playlist (iOS) | `docs/feature/playlist/ios.md` | PlaylistScreen・PlaylistDetailScreen・プレイリスト作成・トラック一覧関連（iOS） |
| Playlist (Android) | `docs/feature/playlist/android.md` | PlaylistScreen・PlaylistDetailScreen・プレイリスト作成・トラック一覧関連（Android） |
| Library (iOS) | `docs/feature/library/ios.md` | LibraryScreen・LibraryViewModel・最近再生・アルバム・曲セクション関連（iOS） |
| Library (Android) | `docs/feature/library/android.md` | LibraryScreen・LibraryViewModel・最近再生・アルバム・曲セクション関連（Android） |
| Music Player (iOS) | `docs/feature/music-player/ios.md`, `docs/feature/music-player/ios-impl.md` | MusicKit・MiniPlayer・FullPlayer 関連 |
| Music Player (Android) | `docs/feature/music-player/android.md` | ExoPlayer・プレビュー再生関連 |
| Apple Music API | `docs/api/apple_music_api.md`, `docs/api/apple_music_api_android.md` | API エンドポイント・認証・レスポンス構造 |
| Mock Mode | `docs/mock_mode.md` | MockAppComponent・新機能追加時の Mock 対応 |
| テスト仕様（方針・Fake クラス・インフラ） | `docs/testing/test.md`, `docs/testing/uitest.md` | テスト追加・FakeRepository 確認・UITest セットアップ |
| テストケース（画面固有） | `docs/feature/<feature-name>/testcase.md` | 各画面の KMP ユニット・iOS ユニット・iOS UITest・Android UITest ケース一覧 |
| プロダクト要件（画面固有） | `docs/feature/<feature-name>/products-requirement.md` | 画面の目的・ユーザーストーリー・表示コンテンツ・UX フロー |
| 歌詞 | `docs/feature/music-player/lyrics.md` | 歌詞表示機能関連 |
| Music Video Screen (iOS) | `docs/feature/music-video/ios.md` | MusicVideoScreen・MusicVideoViewModel・ジャンルタブ・ビデオグリッド関連（iOS） |
| Music Video Screen (Android) | `docs/feature/music-video/android.md` | MusicVideoScreen・MusicVideoViewModel・ジャンルタブ・ビデオグリッド関連（Android） |
| Music Video Player (iOS) | `docs/feature/music-video-player/ios.md` | MusicVideo プレイヤー関連（AVQueuePlayer・全画面再生）（iOS） |
| Music Video Player (Android) | `docs/feature/music-video-player/android.md` | MusicVideo プレイヤー関連（ExoPlayer・全画面再生）（Android） |

## 重要な設計原則（template.md で詳細を確認）

### Apple Music API
- 全リクエストに `Authorization: Bearer <DEVELOPER_TOKEN>` が必要
- `/v1/me/` 系エンドポイントには `Music-User-Token` も必要
- `AppleMusicApi` が `TokenStorage` を参照してヘッダーを動的付与するため、Repository 層は認証を意識不要

### Music Player
- iOS: Swift `MusicPlayerManager` が再生を完結管理し、KMP `PlayerViewModel` に状態を書き込む
- Android: Media3（ExoPlayer）で 30 秒プレビュー再生（フル再生は iOS 専用）
- KMP `PlayerViewModel` は iOS では「状態コンテナ」のみ。再生操作メソッドは持たない

### Mock Mode
- 新機能追加時は必ず `MockAppComponent` にリポジトリ実装と ViewModel 上書きを追加する
- モックデータは `MockAppComponent.kt` 末尾のファイルプライベート変数として定義する

## コード例は `.claude/skills/feature-specs/template.md` を参照
