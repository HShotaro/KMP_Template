# KMP Template

Kotlin Multiplatform template with 5-layer architecture, DI, MockMode, and sample screens.

## Architecture

```
:composeApp (Android - Compose Multiplatform)
    └── :shared
            └── :shared:ui-model  ← iOS umbrella framework ("Shared.xcframework")
                    ├── :shared:core     (KmpLogger)
                    ├── :shared:network  (Ktor HTTP client, PostApi)
                    ├── :shared:domain   (Post entity, repository interfaces)
                    └── :shared:data     (repository implementations)

:shared:testing  (Fake implementations for tests)
```

## Features

- 5-layer KMP module structure
- kotlin-inject DI
- MockMode (Android: `mockDebug` build type / iOS: `iosApp-Mock` scheme)
- ViewModel + Repository unit tests
- Android Compose UI + iOS XCUITest stubs
- XcodeGen + Makefile build automation
- **KmpLogger** (expect/actual) for cross-platform logging
- **Ktor** HTTP client (OkHttp on Android, Darwin on iOS)
- **HSMacro** Swift macros for iOS ViewModel boilerplate

## Setup

### Android
1. Open in Android Studio
2. Select `mockDebug` build variant to run with mock data

### iOS
1. `cp local.properties.example local.properties`
2. `make open` — generates Xcode project and opens it
3. Set `TEAM_ID` in `iosApp/Configuration/Debug.xcconfig`
4. Select `iosApp-Mock` scheme to run with mock data

## Commands

```bash
make build-ios      # Build Kotlin framework for iOS
make build-android  # Build Android app
make test           # Run all unit tests
make xcodegen       # Regenerate Xcode project
make open           # Regenerate and open in Xcode
```

## Renaming

1. `settings.gradle.kts` → change `rootProject.name`
2. `composeApp/build.gradle.kts` → change `namespace` and `applicationId`
3. `iosApp/Configuration/Debug.xcconfig` → change `PRODUCT_BUNDLE_IDENTIFIER` and `PRODUCT_NAME`
4. Rename Kotlin package `com.example.kmptemplate` globally
