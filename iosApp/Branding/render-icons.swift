import Foundation

struct Icon {
    let profile: String
    let source: String
    let destination: String
}

let repositoryRoot = URL(fileURLWithPath: FileManager.default.currentDirectoryPath)
let temporaryDirectory = FileManager.default.temporaryDirectory
    .appendingPathComponent("project-gemini-icons-\(UUID().uuidString)")
try FileManager.default.createDirectory(at: temporaryDirectory, withIntermediateDirectories: true)
defer { try? FileManager.default.removeItem(at: temporaryDirectory) }

let icons = [
    Icon(
        profile: "duoplay",
        source: "iosApp/Branding/duoplay-icon.svg",
        destination: "profiles/icons/duoplay.png"
    ),
    Icon(
        profile: "vman",
        source: "iosApp/Branding/vman-icon.svg",
        destination: "profiles/icons/vman.png"
    ),
    Icon(
        profile: "portal",
        source: "iosApp/Branding/portal-icon.svg",
        destination: "profiles/icons/portal.png"
    )
]
let androidLegacySizes = [
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
]

func run(_ executable: String, _ arguments: [String]) throws {
    let process = Process()
    process.executableURL = URL(fileURLWithPath: executable)
    process.arguments = arguments
    process.standardOutput = FileHandle.nullDevice
    process.standardError = FileHandle.nullDevice
    try process.run()
    process.waitUntilExit()
    guard process.terminationStatus == 0 else {
        throw NSError(
            domain: "ProjectGeminiIcons",
            code: Int(process.terminationStatus),
            userInfo: [
                NSLocalizedDescriptionKey:
                    "\(executable) failed with status \(process.terminationStatus): \(arguments.joined(separator: " "))"
            ]
        )
    }
}

for icon in icons {
    let sourceURL = repositoryRoot.appendingPathComponent(icon.source)
    let destinationURL = repositoryRoot.appendingPathComponent(icon.destination)
    try FileManager.default.createDirectory(
        at: destinationURL.deletingLastPathComponent(),
        withIntermediateDirectories: true
    )

    try run("/usr/bin/qlmanage", [
        "-t", "-s", "1024", "-o", temporaryDirectory.path, sourceURL.path,
    ])
    let renderedURL = temporaryDirectory.appendingPathComponent(sourceURL.lastPathComponent + ".png")
    let jpegURL = temporaryDirectory.appendingPathComponent(icon.profile + ".jpg")

    // The JPEG round-trip intentionally removes the alpha channel. App Store icons
    // must be opaque, and the source SVG already paints every pixel.
    try run("/usr/bin/sips", [
        "-s", "format", "jpeg", "-s", "formatOptions", "best",
        renderedURL.path, "--out", jpegURL.path,
    ])
    try run("/usr/bin/sips", [
        "-s", "format", "png", jpegURL.path, "--out", destinationURL.path,
    ])

    for (density, size) in androidLegacySizes {
        let directory = repositoryRoot.appendingPathComponent(
            "profiles/android/\(icon.profile)/res/mipmap-\(density)"
        )
        try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
        let launcherURL = directory.appendingPathComponent("ic_launcher.png")
        let roundURL = directory.appendingPathComponent("ic_launcher_round.png")
        try run("/usr/bin/sips", [
            "-z", String(size), String(size), destinationURL.path, "--out", launcherURL.path,
        ])
        try Data(contentsOf: launcherURL).write(to: roundURL, options: .atomic)
    }
}
