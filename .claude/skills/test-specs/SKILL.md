---
name: test-specs
description: KMP・iOS ユニットテスト仕様ガイド。新規テスト追加時に implementer-kmp が参照する。テスト実行はユーザーが行う。
---

# test-specs スキル

新規ユニットテスト追加・修正時の仕様ガイド。

> **テスト実行はユーザーが行う**。Claude はテストコードを実装するのみ。
> UI テスト（XCUITest / Android Compose UI Tests）はワークフロー対象外。Xcode / Android Studio から手動実行する。

## テスト種別と配置先

| 種別 | 配置先 | ユーザー実行コマンド |
|---|---|---|
| KMP ユニットテスト（ui-model） | `shared/ui-model/src/commonTest/` | `./gradlew :shared:ui-model:test` |
| KMP ユニットテスト（data） | `shared/data/src/commonTest/` | `./gradlew :shared:data:test` |
| iOS ユニットテスト | `iosApp/iosAppTests/` | Xcode: `iosApp-Mock` スキームで Cmd+U |

## FakeRepository の選択

新規 ViewModel のテストには対応する FakeRepository が必要。

| 状況 | 対応 |
|---|---|
| `:shared:testing` に既存の Fake がある | そのまま使用 |
| 新規 Repository の場合 | `FakeXxxRepository` を `:shared:testing/src/commonMain/` に追加 |

既存 Fake 一覧は `docs/testing/test.md` の `:shared:testing` セクションを参照。

## テスト追加の方針

- ViewModel の変更には必ず対応するユニットテストを追加する
- 成功ケース・失敗ケースの両方をテストする
- 新規 Repository には `FakeXxxRepository` を `:shared:testing` に作成する
- KMP テストは `StandardTestDispatcher` + `advanceUntilIdle()` パターンを使う
- iOS テストは `@MainActor` をクラスでなくメソッドに付け、KMP 型はローカル変数で生成する（Swift 6 concurrency）

## コード例は `.claude/skills/test-specs/template.md` を参照
