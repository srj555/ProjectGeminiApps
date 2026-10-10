import AppKit
import Foundation

struct Icon {
    let source: String
    let destination: String
}

let repositoryRoot = URL(fileURLWithPath: FileManager.default.currentDirectoryPath)
let icons = [
    Icon(
        source: "iosApp/Branding/duoplay-icon.svg",
        destination: "profiles/icons/duoplay.png"
    ),
    Icon(
        source: "iosApp/Branding/vman-icon.svg",
        destination: "profiles/icons/vman.png"
    ),
    Icon(
        source: "iosApp/Branding/portal-icon.svg",
        destination: "profiles/icons/portal.png"
    )
]

for icon in icons {
    let sourceURL = repositoryRoot.appendingPathComponent(icon.source)
    let destinationURL = repositoryRoot.appendingPathComponent(icon.destination)
    guard let image = NSImage(contentsOf: sourceURL) else {
        fatalError("Unable to read \(sourceURL.path)")
    }
    guard let bitmap = NSBitmapImageRep(
        bitmapDataPlanes: nil,
        pixelsWide: 1024,
        pixelsHigh: 1024,
        bitsPerSample: 8,
        samplesPerPixel: 3,
        hasAlpha: false,
        isPlanar: false,
        colorSpaceName: .deviceRGB,
        bytesPerRow: 0,
        bitsPerPixel: 0
    ) else {
        fatalError("Unable to allocate icon bitmap")
    }

    NSGraphicsContext.saveGraphicsState()
    NSGraphicsContext.current = NSGraphicsContext(bitmapImageRep: bitmap)
    image.draw(
        in: NSRect(x: 0, y: 0, width: 1024, height: 1024),
        from: .zero,
        operation: .copy,
        fraction: 1
    )
    NSGraphicsContext.restoreGraphicsState()

    guard let png = bitmap.representation(using: .png, properties: [:]) else {
        fatalError("Unable to encode icon PNG")
    }
    try png.write(to: destinationURL, options: .atomic)
}
