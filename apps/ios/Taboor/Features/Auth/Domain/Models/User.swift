import Foundation

// Domain Entity (Clean Architecture)
struct User: Identifiable, Equatable {
    let id: String
    let email: String
    let fullName: String
    let token: String
}
