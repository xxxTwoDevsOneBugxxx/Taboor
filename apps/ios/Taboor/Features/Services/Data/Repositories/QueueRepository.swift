import Foundation

// MARK: - Queue & Appointment Repository Protocol
public protocol QueueRepositoryProtocol {
    func getActiveTickets() async throws -> [UserTicket]
    func joinQueue(placeId: String, branchId: String, counterId: String?) async throws -> UserTicket
    func bookAppointment(placeId: String, branchId: String, date: String, timeSlot: String, doctorName: String?) async throws -> UserTicket
    func delayTicket(id: String, minutes: Int) async throws
    func cancelTicket(id: String) async throws
    func getHistory() async throws -> [TicketHistoryItem]
}

// MARK: - Queue Repository Implementation (Mock-Ready & Real API Connected)
public final class QueueRepository: QueueRepositoryProtocol {
    public static let shared = QueueRepository()

    private let apiClient: ApiClient
    private let mockLoader: MockDataLoader

    public init(apiClient: ApiClient = .shared, mockLoader: MockDataLoader = .shared) {
        self.apiClient = apiClient
        self.mockLoader = mockLoader
    }

    public func getActiveTickets() async throws -> [UserTicket] {
        if AppConstants.API.useMockData {
            let dtos: [TicketDTO] = try await mockLoader.load(filename: "active_tickets_seed.json", simulateDelay: 0.15)
            return dtos.map { $0.toDomain() }
        } else {
            let dtos: [TicketDTO] = try await apiClient.request(
                endpoint: AppConstants.API.Endpoints.activeTickets,
                method: .get,
                requiresAuth: true
            )
            return dtos.map { $0.toDomain() }
        }
    }

    public func joinQueue(placeId: String, branchId: String, counterId: String? = nil) async throws -> UserTicket {
        let requestDto = JoinQueueRequestDTO(placeId: placeId, branchId: branchId, counterId: counterId)

        if AppConstants.API.useMockData {
            try? await Task.sleep(nanoseconds: 200_000_000)
            let randomNum = Int.random(in: 15...45)
            return UserTicket(
                id: "ticket_\(UUID().uuidString.prefix(6))",
                ticketNumber: "Q-\(randomNum)",
                serviceName: "Branch Queue",
                serviceNameAr: "طابور الخدمة",
                branchLabel: "Main Branch",
                branchLabelAr: "الفرع الرئيسي",
                counterLabel: "Counter 1",
                counterLabelAr: "شباك 1",
                waitingAhead: 3,
                estMinutes: 12,
                currentServing: "Q-\(randomNum - 3)",
                progress: 0.20,
                isActive: true,
                bookingType: .liveQueue,
                placeId: placeId,
                latitude: 30.0585,
                longitude: 31.3465
            )
        } else {
            let dto: TicketDTO = try await apiClient.request(
                endpoint: AppConstants.API.Endpoints.joinQueue,
                method: .post,
                body: requestDto,
                requiresAuth: true
            )
            return dto.toDomain()
        }
    }

    public func bookAppointment(placeId: String, branchId: String, date: String, timeSlot: String, doctorName: String?) async throws -> UserTicket {
        let requestDto = BookAppointmentRequestDTO(placeId: placeId, branchId: branchId, date: date, timeSlot: timeSlot, doctorName: doctorName)

        if AppConstants.API.useMockData {
            try? await Task.sleep(nanoseconds: 200_000_000)
            let randomNum = Int.random(in: 10...30)
            return UserTicket(
                id: "ticket_\(UUID().uuidString.prefix(6))",
                ticketNumber: "S-\(randomNum)",
                serviceName: "Scheduled Appointment",
                serviceNameAr: "موعد مسبق الحجز",
                branchLabel: "Clinic Branch",
                branchLabelAr: "فرع العيادة",
                counterLabel: "Room 1",
                counterLabelAr: "غرفة الكشف 1",
                waitingAhead: 0,
                estMinutes: 0,
                currentServing: "S-00",
                progress: 0.0,
                isActive: true,
                bookingType: .scheduled,
                scheduledDate: date,
                scheduledTime: timeSlot,
                placeId: placeId,
                latitude: 30.0585,
                longitude: 31.3465
            )
        } else {
            let dto: TicketDTO = try await apiClient.request(
                endpoint: AppConstants.API.Endpoints.bookAppointment,
                method: .post,
                body: requestDto,
                requiresAuth: true
            )
            return dto.toDomain()
        }
    }

    public func delayTicket(id: String, minutes: Int) async throws {
        let requestDto = DelayTicketRequestDTO(ticketId: id, delayMinutes: minutes)

        if AppConstants.API.useMockData {
            try? await Task.sleep(nanoseconds: 100_000_000)
        } else {
            let _: EmptyPayload = try await apiClient.request(
                endpoint: AppConstants.API.Endpoints.delayTicket(id),
                method: .post,
                body: requestDto,
                requiresAuth: true
            )
        }
    }

    public func cancelTicket(id: String) async throws {
        if AppConstants.API.useMockData {
            try? await Task.sleep(nanoseconds: 100_000_000)
        } else {
            let _: EmptyPayload = try await apiClient.request(
                endpoint: AppConstants.API.Endpoints.cancelTicket(id),
                method: .post,
                requiresAuth: true
            )
        }
    }

    public func getHistory() async throws -> [TicketHistoryItem] {
        if AppConstants.API.useMockData {
            let dtos: [TicketHistoryDTO] = try await mockLoader.load(filename: "history_seed.json", simulateDelay: 0.15)
            return dtos.map { $0.toDomain() }
        } else {
            let dtos: [TicketHistoryDTO] = try await apiClient.request(
                endpoint: AppConstants.API.Endpoints.ticketHistory,
                method: .get,
                requiresAuth: true
            )
            return dtos.map { $0.toDomain() }
        }
    }
}
