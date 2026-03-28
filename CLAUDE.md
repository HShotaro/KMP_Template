# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## ワークフロー・スキル・エージェント

詳細は以下のドキュメントを参照:
- **[`docs/workflow-overview.md`](docs/workflow-overview.md)** — ワークフロー一覧・フェーズ構成・作業ログの保存先
- **[`docs/workflow-reference.md`](docs/workflow-reference.md)** — サブエージェント・スキルの詳細仕様

---

## Build Commands

利用可能なコマンドの一覧は **`Makefile`** を参照。

For iOS, open `iosApp/` in Xcode via `make open`. The Xcode project (`iosApp.xcodeproj`) is generated from `iosApp/project.yml` using XcodeGen and is not committed to Git. The KMP framework is built as part of the Xcode Build Phase via `make ios-embed`.

## Environment Setup

Add to `local.properties` (not committed):
```
# プロジェクト固有のシークレットをここに追記
```

## Architecture

### Module Dependency Graph

```
:composeApp (Android UI - Compose Multiplatform)
    └── :shared
            └── :shared:ui-model  ← iOS umbrella framework ("Shared.xcframework")
                    ├── :shared:core     (BuildKonfig secrets)
                    ├── :shared:network  (Ktor HTTP client)
                    ├── :shared:domain   (entities, repository interfaces)
                    └── :shared:data     (repository implementations)

:shared:testing  (common test utilities, depends on :shared:domain + :shared:core)
```

### Dependency Injection

kotlin-inject (`me.tatarka.inject`) を使用。KSP は `:shared:ui-model` で実行され、`AppComponent::class.create()` の実装コードを自動生成する。
- **`AppComponent`** (`shared/ui-model/di/`) — `@Singleton @Component`。`Settings` と `HttpClient` は `@Provides` で提供、リポジトリインターフェースのバインドも `@Provides` で定義。
- **Android**: `KmpApp : Application` が `AppComponent::class.create()` でシングルトンを保持。ViewModels は `(application as KmpApp).component.loginViewModel` で取得。
- **iOS**: `IosDependencies` (`@ThreadLocal` object) が `AppComponent::class.create()` を保持し、`IosViewModelProvider` 経由で Swift に公開。Swift は `IosDependencies.shared.provider.homeViewModel` でアクセス。

### ViewModel Layer

ViewModels live in `:shared:ui-model` and extend `androidx.lifecycle.ViewModel`. State is exposed as `StateFlow<UiState>`. iOS interop は `observeUiState` コールバックと `XxxViewModelInterface` プロトコルで行う。

実装パターンの詳細（UiState 必須制約・no-arg コンストラクタ・DI 配線・Swift 6 concurrency 対応）は **[`.claude/skills/arch-patterns/SKILL.md`](.claude/skills/arch-patterns/SKILL.md)** を参照。

### iOS Interop Patterns

iOS ViewModel wrappers (`iosApp/iosApp/viewModel/`) bridge KMP `StateFlow` to `@Published` via `Task { @MainActor }`. ドメインエンティティは `SharedIdentifiable` / `SharedSendable` を実装して KMP 境界を越える。

iOS ViewModel のボイラープレートは `HSMacro`（SPM）の Swift Macro で自動生成する。詳細は **[`.claude/skills/arch-patterns/SKILL.md`](.claude/skills/arch-patterns/SKILL.md)** を参照。

### Network Layer

`:shared:network` uses Ktor:
- Platform-specific HTTP engines: OkHttp (Android), Darwin (iOS)

### iOS File Structure

```
iosApp/iosApp/
├── View/
│   ├── Screen/       # 画面単位の View（XxxScreen.swift）
│   ├── Component/    # 複数画面で共通利用する UI パーツ
│   └── ContentView.swift
├── Route/            # NavigationStack のパス遷移を表す Enum（AppDestination）
├── viewModel/        # IosXxxViewModel
├── Util/             # ユーティリティ
└── Data/             # iOS 固有のデータ層
```

### Localization

ローカライズは `Localizable.xcstrings`（Xcode 15+ 形式）で `ja` / `en` を一元管理する。

- **キー定義**: `iosApp/iosApp/Util/LocalizedStringKey+App.swift` に `LocalizedStringKey` の extension として全キーを `static var` の computed property で定義する。
- **`static let` は使用しない** — `LocalizedStringKey` は `Sendable` 非準拠のため、Swift 6 の concurrency チェックでエラーになる。

### iOS Framework Export

`:shared:ui-model` is the umbrella framework exported to Xcode as `Shared.xcframework`. It re-exports `:shared:core`, `:shared:network`, and `:shared:domain` so Swift can import a single `Shared` framework.

## Mock Mode

実機認証なしに全画面の UI を確認する仕組み。

- **Android**: `composeApp/build.gradle.kts` の `mock` ビルドタイプ → Android Studio の `Build Variants` で `mockDebug` を選択。
- **iOS**: `iosApp-Mock` Scheme を Xcode で選択。
- **共通**: `MockAppComponent` が全リポジトリをインメモリ実装に差し替え。

## Testing

ユニットテストの仕様・FakeRepository 一覧・追加手順は **[`docs/testing/test.md`](docs/testing/test.md)** を参照。
テスト実装パターンは **[`.claude/skills/test-specs/SKILL.md`](.claude/skills/test-specs/SKILL.md)** を参照。

### テスト実行

```bash
./gradlew test                          # 全モジュール
./gradlew :shared:data:test             # data のみ
./gradlew :shared:ui-model:test         # ui-model のみ
```
