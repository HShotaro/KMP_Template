import SwiftUI

struct LoginScreen: View {
    @EnvironmentObject var loginViewModel: IosLoginViewModel
    @State private var username: String = ""
    @State private var password: String = ""

    var body: some View {
        VStack(spacing: 16) {
            Spacer()
            Text("KMP Template")
                .font(.largeTitle)
                .bold()

            TextField("Username", text: $username)
                .textFieldStyle(.roundedBorder)
                .autocorrectionDisabled()
                .textInputAutocapitalization(.never)

            SecureField("Password", text: $password)
                .textFieldStyle(.roundedBorder)

            if let error = loginViewModel.uiState.errorMessage {
                Text(error)
                    .foregroundStyle(.red)
                    .font(.caption)
            }

            Button("Login") {
                loginViewModel.login(username: username, password: password)
            }
            .buttonStyle(.borderedProminent)
            .frame(maxWidth: .infinity)

            Spacer()
        }
        .padding(24)
    }
}
