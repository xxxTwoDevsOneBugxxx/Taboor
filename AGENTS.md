# Taboor (طابور) — Architecture & Development Guidelines (`AGENTS.md`)

This document serves as the single source of truth for architectural standards, design patterns, coding conventions, and best practices for the **Taboor** repository. All developers and AI agents working on this codebase **MUST** adhere to these rules.

---

## 1. Project & Domain Overview

**Taboor (طابور)** is a multi-tenant SaaS platform for **Smart Queue & Appointment Management** serving service-oriented businesses (clinics, beauty salons, auto repair workshops, offices, government/customer centers).

### Monorepo Structure
* **`apps/api`**: Backend Web API built with **ASP.NET Core (.NET 10)**.
* **`apps/dashboard`**: Web Management Portal built with **React / TanStack Start / TanStack Router** and Tailwind CSS.
* **`apps/mobile`**: Mobile application for end customers built with **Flutter** (iOS & Android).

---

## 2. Architectural Blueprint

The backend architecture is structured around three precise levels:

```
┌────────────────────────────────────────────────────────┐
│ 1. SYSTEM LEVEL: Traditional Monolithic               │
│    Single deployable unit, single database, in-process  │
└────────────────────────────────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│ 2. BOUNDARY LEVEL: Feature Boundaries (Package-by-Feature)
│    Features partitioned by business domains, isolated  │
└────────────────────────────────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│ 3. CODE LEVEL: Vertical Slice Architecture + CQRS      │
│    Self-contained Use Cases via MediatR & EF Core      │
└────────────────────────────────────────────────────────┘
```

| Level | Strategy | Implementation |
| :--- | :--- | :--- |
| **System Level** | **Traditional Monolithic** | Single process runtime, single unified database, single CI/CD deployment unit. Avoids premature microservice or multi-process operational overhead. |
| **Boundary Level** | **Feature Boundaries** | Grouped by business domain (`Auth`, `Plans`, `Queues`, `Tenants`, `Appointments`). Features do not leak or depend on each other's private internals. |
| **Code Level** | **Vertical Slice + CQRS** | Each Use Case is an isolated slice (Command or Query) executed via MediatR with direct EF Core operations. |

---

## 3. Strict Prohibitions & Deprecations (Anti-Patterns to Avoid)

The following patterns were previously evaluated or partially used and are **STRICTLY DEPRECATED / FORBIDDEN** in new code:

* ❌ **NO Generic Repositories** (`IRepository<T>`, `Repository<T>`): EF Core's `DbSet<T>` already serves as a repository. Wrapping it introduces leaky abstractions and cripples LINQ capabilities.
* ❌ **NO Custom Unit of Work Wrapper** (`IUnitOfWork`, `UnitOfWork`): EF Core's `DbContext` is already a Unit of Work. Use `_context.SaveChangesAsync()` directly.
* ❌ **NO Specification Pattern** (`BaseSpecification`, `ISpecification`, `SpecificationEvaluator`): Replaced by native C# LINQ `.Where()`, `.Include()`, and `.Select()` projections.
* ❌ **NO "God Services" / Multi-Method Services** (e.g. `PlanService.cs` with 20 methods): Do NOT put all operations into a single service class. Each operation must be a standalone Use Case handler.
* ❌ **NO Business Logic in Controllers**: Controllers are thin driving adapters. They inject only `ISender` and dispatch commands/queries.
* ❌ **NO Manual `try-catch` in Handlers for Flow Control**: Handlers do not catch exceptions to return fail status. Use `Result` for expected business failures, and let unexpected exceptions bubble to the `GlobalExceptionHandler`.

---

## 4. Vertical Slice & CQRS Standards

### Folder Structure
All application business logic lives in `Taboor_Application/Features/`:

```text
Taboor_Application/
└── Features/
    └── <FeatureName>/               # e.g., Plans, Queues, Auth
        ├── Commands/
        │   └── <ActionName>/        # e.g., CreatePlan, JoinQueue
        │       ├── <ActionName>Command.cs
        │       ├── <ActionName>CommandHandler.cs
        │       ├── <ActionName>Validator.cs
        │       └── <ActionName>Response.cs (optional)
        │
        └── Queries/
            └── <QueryName>/         # e.g., GetActivePlans, GetLiveQueue
                ├── <QueryName>Query.cs
                ├── <QueryName>QueryHandler.cs
                └── <QueryName>Dto.cs
```

### Conventions for Commands (Write Operations)
* Commands represent **an intent to change state**.
* Defined as immutable **`record`**:
  ```csharp
  public record CreatePlanCommand(string Name, decimal Price, BillingCycle BillingCycle) 
      : IRequest<Result<int>>;
  ```
* Input validation **MUST** use **FluentValidation**:
  ```csharp
  public class CreatePlanValidator : AbstractValidator<CreatePlanCommand>
  {
      public CreatePlanValidator()
      {
          RuleFor(x => x.Name).NotEmpty().MaximumLength(100);
          RuleFor(x => x.Price).GreaterThan(0);
      }
  }
  ```
* Handler accesses `ApplicationDbContext` directly:
  ```csharp
  public class CreatePlanCommandHandler(ApplicationDbContext context) 
      : IRequestHandler<CreatePlanCommand, Result<int>>
  {
      public async Task<Result<int>> Handle(CreatePlanCommand request, CancellationToken ct)
      {
          var plan = new Plan(request.Name, request.Price, request.BillingCycle);
          context.Plans.Add(plan);
          await context.SaveChangesAsync(ct);
          return Result<int>.Success(plan.Id);
      }
  }
  ```

### Conventions for Queries (Read Operations)
* Queries represent **data retrieval** and **MUST NOT** modify database state.
* Defined as immutable **`record`**:
  ```csharp
  public record GetActivePlansQuery : IRequest<Result<List<PlanDto>>>;
  ```
* Handlers **MUST** use `.AsNoTracking()` and `.Select()` projections for optimal performance:
  ```csharp
  public class GetActivePlansQueryHandler(ApplicationDbContext context) 
      : IRequestHandler<GetActivePlansQuery, Result<List<PlanDto>>>
  {
      public async Task<Result<List<PlanDto>>> Handle(GetActivePlansQuery request, CancellationToken ct)
      {
          var plans = await context.Plans
              .AsNoTracking()
              .Where(p => p.IsActive)
              .OrderBy(p => p.Price)
              .Select(p => new PlanDto(p.Id, p.Name, p.Price))
              .ToListAsync(ct);

          return Result<List<PlanDto>>.Success(plans);
      }
  }
  ```

---

## 5. Error Handling & Exception Strategy (The Hybrid Approach)

We apply a strict distinction between **Expected Business Failures** and **Unexpected Exceptional Errors**:

```
                       ┌───────────────────────┐
                       │     Incoming Error    │
                       └───────────────────────┘
                                   │
                  Is it an expected business outcome?
                  (e.g., duplicate email, invalid credentials)
                                   │
                    ┌──────────────┴──────────────┐
                   YES                            NO
                    ▼                             ▼
       ┌────────────────────────┐    ┌─────────────────────────┐
       │   Result<T>.Failure    │    │   GlobalExceptionHandler│
       │   No Exception thrown  │    │   Central 400/404/500   │
       │   Lightweight & Fast   │    │   Logs & ProblemDetails │
       └────────────────────────┘    └─────────────────────────┘
```

### 1. Expected Business Failures (Result Pattern)
* Use `Result` or `Result<T>` with structured `Error` types:
  ```csharp
  if (await context.Users.AnyAsync(u => u.Email == request.Email, ct))
  {
      return Result<int>.Failure(Error.Conflict("User.DuplicateEmail", "Email already in use."));
  }
  ```

### 2. True Exceptional Errors (Global Exception Handler)
* Centralized in `Taboor_API` using ASP.NET Core's **`IExceptionHandler`**:
  * Unified **`AppException`** with decoupled **`ErrorType`** enum:
    * **`ErrorType.Validation`** ➔ **HTTP 400 Bad Request** with dictionary of validation errors.
    * **`ErrorType.NotFound`** ➔ **HTTP 404 Not Found**.
    * **`ErrorType.Conflict`** ➔ **HTTP 409 Conflict**.
    * **`ErrorType.Forbidden`** ➔ **HTTP 403 Forbidden**.
    * **`ErrorType.Unauthorized`** ➔ **HTTP 401 Unauthorized**.
  * **Unhandled / Technical Exceptions** (DB timeout, NullRef) ➔ Logged with trace, returns **HTTP 500 Internal Server Error** via unified `ApiResponse`.

---

## 6. Controller Standards

Controllers in `Taboor_API` must remain thin and declarative:
* Inject only **`ISender`** (or `IMediator`).
* Do not inject `DbContext`, repositories, or business services.
* Map `Result<T>` cleanly to HTTP response helpers:
  ```csharp
  [ApiController]
  [Route("api/[controller]")]
  public class PlansController(ISender sender) : ControllerBase
  {
      [HttpPost]
      public async Task<IActionResult> Create([FromBody] CreatePlanCommand command)
      {
          var result = await sender.Send(command);
          return result.IsSuccess 
              ? CreatedAtAction(nameof(GetById), new { id = result.Value }, result) 
              : BadRequest(result.Error);
      }

      [HttpGet]
      public async Task<IActionResult> GetAll()
      {
          var result = await sender.Send(new GetActivePlansQuery());
          return Ok(result);
      }
  }
  ```

---

## 7. C# Language & Type Guidelines

* **Commands, Queries, DTOs**: Use C# **`record`** with positional parameters for immutability and conciseness.
* **Domain Entities**: Use standard C# **`class`** with private setters and meaningful domain methods (encapsulation).
* **Dependency Injection**: Prefer **C# 12+ Primary Constructors** (e.g. `public class MyHandler(ApplicationDbContext context) : ...`).
* **Nullability**: Respect C# `<Nullable>enable</Nullable>`. Avoid null forgiving operator `!` unless verified.
* **CancellationToken**: Always propagate `CancellationToken` from controller down to EF Core async calls.

---

## 8. Summary Checklist for Any New Feature

When adding a new feature or endpoint, follow this exact workflow:
1. Define the Domain Entity in `Taboor_Domain/Entities/` (if new).
2. Configure Entity Mapping in `Taboor_Infrastructure/Data/Configurations/`.
3. Create the Vertical Slice folder in `Taboor_Application/Features/<FeatureName>/`.
4. Create the `Command` or `Query` as a `record`.
5. If Command: create FluentValidation validator in `<Action>Validator.cs`.
6. Implement the Handler injecting `ApplicationDbContext` directly.
7. Expose via a thin Controller action in `Taboor_API/Controllers/` dispatching via `_sender.Send()`.
