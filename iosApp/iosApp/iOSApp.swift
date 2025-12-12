import SwiftUI
import shared

@main
struct iOSApp: App {

    init() {
        // Initialize Koin
        KoinInitializer().initialize()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
