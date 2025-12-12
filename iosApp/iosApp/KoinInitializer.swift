import Foundation
import shared

class KoinInitializer {
    func initialize() {
        // Start Koin for iOS
        let koinApp = KoinIOSKt.startKoinIOS()
        _ = koinApp
    }
}
