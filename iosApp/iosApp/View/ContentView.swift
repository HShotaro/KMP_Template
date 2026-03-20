import SwiftUI
import Shared

struct ContentView: View {
    @EnvironmentObject var loginViewModel: IosLoginViewModel

    var body: some View {
        if loginViewModel.uiState.isAuthenticated {
            HomeScreen()
        } else {
            LoginScreen()
        }
    }
}
