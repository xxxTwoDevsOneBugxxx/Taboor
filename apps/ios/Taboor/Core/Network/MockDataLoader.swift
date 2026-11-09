import Foundation

// MARK: - Mock Data Loader & Seeding Engine
// Emulates the ASP.NET Core API server with seed JSON files and realistic latency

public final class MockDataLoader {
    public static let shared = MockDataLoader()

    private let jsonDecoder: JSONDecoder

    private init() {
        self.jsonDecoder = JSONDecoder()
    }

    /// Loads and decodes a seed JSON file wrapped in `ApiResponse<T>` or directly as `T`
    public func load<T: Decodable>(filename: String, simulateDelay: TimeInterval = 0.20) async throws -> T {
        // 1. Simulate server network latency
        if simulateDelay > 0 {
            try? await Task.sleep(nanoseconds: UInt64(simulateDelay * 1_000_000_000))
        }

        // 2. Locate file in main bundle or fallback to Seeds path
        guard let data = loadData(forResource: filename) else {
            print("[MockDataLoader] ⚠️ Could not find seed file: \(filename)")
            throw NetworkError.custom("Seed file \(filename) not found")
        }

        // 3. First attempt: Decode as unified ApiResponse<T>
        if let apiResponse = try? jsonDecoder.decode(ApiResponse<T>.self, from: data),
           let payload = apiResponse.data {
            return payload
        }

        // 4. Second attempt: Decode directly as T
        do {
            let result = try jsonDecoder.decode(T.self, from: data)
            return result
        } catch {
            print("[MockDataLoader] ❌ Failed to decode \(filename) as \(T.self): \(error)")
            throw NetworkError.decodingError(error.localizedDescription)
        }
    }

    private func loadData(forResource filename: String) -> Data? {
        let baseName = (filename as NSString).deletingPathExtension
        let ext = (filename as NSString).pathExtension.isEmpty ? "json" : (filename as NSString).pathExtension

        // A. Try directly in Bundle.main
        if let url = Bundle.main.url(forResource: baseName, withExtension: ext) {
            return try? Data(contentsOf: url)
        }

        // B. Try in subdirectory "Seeds" inside Bundle.main
        if let url = Bundle.main.url(forResource: baseName, withExtension: ext, subdirectory: "Seeds") {
            return try? Data(contentsOf: url)
        }

        // C. Fallback for testing/simulator: look inside app bundle directory tree
        if let bundlePath = Bundle.main.resourcePath {
            let possiblePaths = [
                "\(bundlePath)/\(filename)",
                "\(bundlePath)/Seeds/\(filename)",
                "\(bundlePath)/Resources/Seeds/\(filename)"
            ]
            for path in possiblePaths {
                if let data = try? Data(contentsOf: URL(fileURLWithPath: path)) {
                    return data
                }
            }
        }

        return nil
    }
}
