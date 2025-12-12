import Foundation
import UserNotifications
import UIKit

class PushNotificationManager: NSObject {

    static let shared = PushNotificationManager()

    func registerForPushNotifications() {
        UNUserNotificationCenter.current()
            .requestAuthorization(options: [.alert, .sound, .badge]) { granted, _ in
                guard granted else { return }
                DispatchQueue.main.async {
                    UIApplication.shared.registerForRemoteNotifications()
                }
            }
    }

    func handleRegistration(deviceToken: Data) {
        let tokenParts = deviceToken.map { data in String(format: "%02.2hhx", data) }
        let token = tokenParts.joined()
        print("Device Token: \(token)")

        // Send token to your server
        // sendTokenToServer(token)
    }

    func handleNotification(_ notification: UNNotification) {
        let userInfo = notification.request.content.userInfo

        // Handle notification data
        if let deepLink = userInfo["deepLink"] as? String {
            // Handle deep link from notification
            handleDeepLink(deepLink)
        }
    }

    private func handleDeepLink(_ urlString: String) {
        // Deep link handling logic
        print("Handling deep link: \(urlString)")
    }
}

// Implement UNUserNotificationCenterDelegate
extension PushNotificationManager: UNUserNotificationCenterDelegate {

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        handleNotification(notification)
        completionHandler([.banner, .sound])
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        handleNotification(response.notification)
        completionHandler()
    }
}
