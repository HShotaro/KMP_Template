import SwiftUI
import Shared

struct HomeScreen: View {
    @EnvironmentObject var loginViewModel: IosLoginViewModel
    @StateObject private var homeViewModel = IosHomeViewModel()

    var body: some View {
        NavigationStack {
            Group {
                if homeViewModel.uiState.isLoading {
                    ProgressView()
                } else if let error = homeViewModel.uiState.errorMessage {
                    Text(error)
                        .foregroundStyle(.red)
                        .padding()
                } else {
                    List(homeViewModel.uiState.posts, id: \.id) { post in
                        VStack(alignment: .leading, spacing: 4) {
                            Text(post.title)
                                .font(.headline)
                            Text(post.body)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                                .lineLimit(2)
                        }
                        .padding(.vertical, 4)
                    }
                }
            }
            .navigationTitle("Posts")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Logout") {
                        loginViewModel.logout()
                    }
                }
            }
            .onAppear { homeViewModel.loadPosts() }
        }
    }
}
