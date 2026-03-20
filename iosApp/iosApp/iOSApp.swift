import SwiftUI
import Shared

@main
struct iOSApp: App {
    @StateObject private var loginViewModel = IosLoginViewModel()

    init() {
        #if MOCK_MODE
        MockConfig.shared.isEnabled = true
        #endif
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(loginViewModel)
        }
    }
}
