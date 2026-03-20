import Foundation
import Shared
import HSMacro

@KmpObservableViewModel
@MainActor
class IosLoginViewModel: ObservableObject {
    // LoginViewModel はシングルトンのため deinit で dispose() を呼ばない（opt-out）
    deinit {}
}

@KmpForwardAll
extension IosLoginViewModel: @MainActor LoginViewModelInterface {
    func login(username: String, password: String)
    func logout()
}
