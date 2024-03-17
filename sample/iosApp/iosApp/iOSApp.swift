import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    private let mainViewController = MainViewController()

	var body: some Scene {
		WindowGroup {
			ContentView(rootViewController: mainViewController)
		}
	}
}