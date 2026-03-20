export CONFIGURATION ?= Debug
export SDK_NAME ?= iphonesimulator
export ARCHS ?= arm64

.PHONY: help ksp build-ui build-data build-ios build-android test test-data test-ui test-android test-ios clean sync xcodegen open

help:
	@echo "Available commands:"
	@echo "  make ksp            - Run KSP code generation for :shared:ui-model"
	@echo "  make build-ui       - Build shared:ui-model module"
	@echo "  make build-data     - Build shared:data module"
	@echo "  make build-ios      - Build the iOS framework"
	@echo "  make ios-embed      - Run embedAndSignAppleFrameworkForXcode (for Xcode Build Phase)"
	@echo "  make build-android  - Build the Android application"
	@echo "  make xcodegen       - Regenerate iosApp.xcodeproj from iosApp/project.yml"
	@echo "  make open           - Regenerate iosApp.xcodeproj and open in Xcode"
	@echo "  make test           - Run all tests"
	@echo "  make test-data      - Run tests for :shared:data"
	@echo "  make test-ui        - Run tests for :shared:ui-model"
	@echo "  make test-android   - Run Android instrumented UI tests"
	@echo "  make test-ios       - Run iOS XCUITests on simulator"
	@echo "  make clean          - Clean all build directories"
	@echo "  make sync           - Run Gradle sync"

ksp:
	./gradlew :shared:ui-model:kspDebugKotlinAndroid \
	          :shared:ui-model:kspKotlinIosArm64 \
	          :shared:ui-model:kspKotlinIosSimulatorArm64

build-ui:
	./gradlew :shared:ui-model:assembleDebug

build-data:
	./gradlew :shared:data:assembleDebug

build-ios:
	./gradlew :shared:ui-model:assembleDebug

ios-embed:
	@if [ "YES" = "$(OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED)" ]; then \
		echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""; \
	else \
		./gradlew :shared:ui-model:embedAndSignAppleFrameworkForXcode; \
	fi

test:
	./gradlew test

test-data:
	./gradlew :shared:data:test

test-ui:
	./gradlew :shared:ui-model:test

test-android:
	./gradlew :composeApp:connectedDebugAndroidTest

IOS_SIMULATOR ?= iPhone 16
IOS_OS ?= 18.5
test-ios:
	xcodebuild test \
	  -project iosApp/iosApp.xcodeproj \
	  -scheme iosApp \
	  -destination "platform=iOS Simulator,name=$(IOS_SIMULATOR),OS=$(IOS_OS)" \
	  -only-testing:iosAppUITests

build-android:
	./gradlew :composeApp:assembleDebug

clean:
	./gradlew clean

sync:
	./gradlew help

xcodegen:
	cd iosApp && xcodegen generate

open: xcodegen
	open iosApp/iosApp.xcodeproj
