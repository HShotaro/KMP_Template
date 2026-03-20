import Foundation
import Shared
import HSMacro

@KmpObservableViewModel
@MainActor
class IosHomeViewModel: ObservableObject {}

@KmpForwardAll
extension IosHomeViewModel: @MainActor HomeViewModelInterface {
    func loadPosts()
}
