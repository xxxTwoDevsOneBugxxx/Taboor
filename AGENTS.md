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
| **Code Level** | **Vertical Slice + CQRS** | Each Use Case is an isolated slice (Command or Query) executed via MediatR with direct EF Core operations through `IApplicationDbContext`. |

---

## 3. Strict Prohibitions & Deprecations (Anti-Patterns to Avoid)

The following patterns are **STRICTLY DEPRECATED / FORBIDDEN** in this codebase:

* ❌ **NO Generic Repositories** (`IRepository<T>`, `Repository<T>`): EF Core's `DbSet<T>` already serves as a repository. Wrapping it introduces leaky abstractions and cripples LINQ capabilities.
* ❌ **NO Custom Unit of Work Wrapper** (`IUnitOfWork`, `UnitOfWork`): EF Core's `DbContext` is already a Unit of Work. Use `context.SaveChangesAsync()` directly.
* ❌ **NO Specification Pattern** (`BaseSpecification`, `ISpecification`, `SpecificationEvaluator`): Replaced by native C# LINQ `.Where()`, `.Include()`, and `.Select()` projections.
* ❌ **NO "God Services" / Multi-Method Services** (e.g. `PlanService.cs` with 20 methods): Do NOT put all operations into a single service class. Each operation must be a standalone Use Case handler.
* ❌ **NO Business Logic in Controllers**: Controllers are thin driving adapters. They inject only `ISender` and dispatch commands/queries.
* ❌ **NO Manual `try-catch` or `ModelState.IsValid` in Controllers/Handlers**: Flow control and error responses are handled automatically by `ValidationBehavior` and `GlobalExceptionHandler`.
* ❌ **NO Public Entity Setters or Parameterized Constructors**: Entities must be rich domain models with `private set;`, `private ParameterlessConstructor() { }`, and Named Static Factory Methods (`Entity.Create(...)`).

---

## 4. Vertical Slice & CQRS Standards

### Folder Structure
All application business logic lives in `Taboor_Application/Features/`:

```text
Taboor_Application/
└── Features/
    └── <FeatureName>/               # e.g., Plans, Queues, Auth
        ├── Commands/
        │   └── <ActionName>/        # e.g., CreatePlan, UpdatePlan, ActivatePlan
        │       ├── <ActionName>Command.cs
        │       ├── <ActionName>CommandHandler.cs
        │       └── <ActionName>Validator.cs (for write operations)
        │
        ├── Queries/
        │   └── <QueryName>/         # e.g., GetAllPlans, GetPlanById
        │       ├── <QueryName>Query.cs
        │       └── <QueryName>QueryHandler.cs
        │
        └── DTOs/                    # Feature-shared DTOs
            └── <FeatureName>Dto.cs  # e.g., PlanDto.cs (immutable record)
```

---

### Database Abstraction Port (`IApplicationDbContext`)
To maintain Clean Architecture and prevent circular dependencies between `Taboor_Application` and `Taboor_Infrastructure`:
* `IApplicationDbContext` is defined in `Taboor_Application/Common/Interfaces/IApplicationDbContext.cs`.
* Implemented by `ApplicationDbContext` in `Taboor_Infrastructure`.
* Registered in `InfrastructureContainer.cs` via Service Forwarding:
  ```csharp
  services.AddDbContext<ApplicationDbContext>(...);
  services.AddScoped<IApplicationDbContext>(sp => sp.GetRequiredService<ApplicationDbContext>());
  ```
* All Command and Query handlers inject **`IApplicationDbContext`**, never concrete `ApplicationDbContext`.

---

### Conventions for Commands (Write Operations)
* Commands represent **an intent to change state**.
* Defined as immutable **`record`**:
  ```csharp
  public record CreatePlanCommand(
      string Name,
      string? Description,
      decimal Price,
      BillingCycle BillingCycle,
      int MaxBranches,
      int MaxServices,
      int MaxServicesPerBranch
  ) : IRequest<PlanDto>;
  ```
* Input validation **MUST** use **FluentValidation** in `<ActionName>Validator.cs`:
  ```csharp
  public class CreatePlanValidator : AbstractValidator<CreatePlanCommand>
  {
      public CreatePlanValidator()
      {
          RuleFor(x => x.Name).NotEmpty().MaximumLength(100);
          RuleFor(x => x.Price).GreaterThanOrEqualTo(0);
          RuleFor(x => x.BillingCycle).IsInEnum();
      }
  }
  ```
* Handler accesses `IApplicationDbContext` directly, enforces business rules via `AppException`, and uses the Domain Entity's Static Factory Method:
  ```csharp
  public class CreatePlanCommandHandler(IApplicationDbContext context) 
      : IRequestHandler<CreatePlanCommand, PlanDto>
  {
      public async Task<PlanDto> Handle(CreatePlanCommand request, CancellationToken ct)
      {
          var normalizedName = request.Name.Trim().ToLower();
          if (await context.Plans.AnyAsync(p => p.Name.ToLower() == normalizedName, ct))
          {
              throw AppException.Conflict("A plan with this name already exists.");
          }

          var plan = Plan.Create(
              request.Name,
              request.Description,
              request.Price,
              request.BillingCycle,
              request.MaxBranches,
              request.MaxServices,
              request.MaxServicesPerBranch);

          context.Plans.Add(plan);
          await context.SaveChangesAsync(ct);

          return new PlanDto(
              plan.Id,
              plan.Name,
              plan.Description,
              plan.Price,
              plan.BillingCycle,
              plan.MaxBranches,
              plan.MaxServices,
              plan.MaxServicesPerBranch,
              plan.IsActive,
              plan.CreatedAt,
              plan.UpdatedAt);
      }
  }
  ```

---

### Conventions for Queries (Read Operations)
* Queries represent **data retrieval** and **MUST NOT** modify database state.
* Defined as immutable **`record`**:
  ```csharp
  public record GetAllPlansQuery(bool OnlyActivated = false) : IRequest<IReadOnlyList<PlanDto>>;
  ```
* Handlers **MUST** use `.AsNoTracking()` and `.Select()` projections directly to DTOs for maximum performance (no AutoMapper on read path):
  ```csharp
  public class GetAllPlansQueryHandler(IApplicationDbContext context) 
      : IRequestHandler<GetAllPlansQuery, IReadOnlyList<PlanDto>>
  {
      public async Task<IReadOnlyList<PlanDto>> Handle(GetAllPlansQuery request, CancellationToken ct)
      {
          var query = context.Plans.AsNoTracking();

          if (request.OnlyActivated)
          {
              query = query.Where(p => p.IsActive);
          }

          return await query
              .OrderBy(p => p.Price)
              .Select(p => new PlanDto(
                  p.Id,
                  p.Name,
                  p.Description,
                  p.Price,
                  p.BillingCycle,
                  p.MaxBranches,
                  p.MaxServices,
                  p.MaxServicesPerBranch,
                  p.IsActive,
                  p.CreatedAt,
                  p.UpdatedAt))
              .ToListAsync(ct);
      }
  }
  ```

---

## 5. Error Handling & Validation Pipeline

We employ a unified, zero-boilerplate exception architecture:

```
Incoming HTTP Request
       │
       ▼
[MediatR Pipeline: ValidationBehavior]
       │   ├── Input Invalid? ➔ throw AppException.Validation(errors)
       ▼   └── Input Valid?   ➔ Proceed to Handler
[Use Case Handler]
       │   ├── Entity Not Found?  ➔ throw AppException.NotFound(msg)
       │   ├── Business Conflict? ➔ throw AppException.Conflict(msg)
       │   ├── Unauthorized?      ➔ throw AppException.Unauthorized(msg)
       │   └── Operation Succeeded ➔ Return DTO
       ▼
[GlobalExceptionHandler (IExceptionHandler)]
       └── Catches AppException ➔ Formats RFC 7807 ProblemDetails + ApiResponse HTTP 400/404/409
       └── Catches Unhandled Ex ➔ Logs trace + returns HTTP 500
```

1. **Input Validation**: Executed automatically by `ValidationBehavior<TRequest, TResponse>` using FluentValidation validators.
2. **Business Failures**: Handlers throw typed `AppException` factory methods:
   * `AppException.NotFound("Resource not found.")` ➔ HTTP 404
   * `AppException.Conflict("Resource already exists.")` ➔ HTTP 409
   * `AppException.Validation(failures)` ➔ HTTP 400
   * `AppException.Forbidden("Forbidden.")` ➔ HTTP 403
   * `AppException.Unauthorized("Unauthorized.")` ➔ HTTP 401
3. **Response Envelope**: All successful responses are wrapped in `ApiResponse<T>.SuccessResponse(data, message, statusCode)` for consistency with frontend and mobile clients.

---

## 6. Controller Standards

Controllers in `Taboor_API` are thin HTTP facades:
* Inject **only `ISender`** via C# 12 Primary Constructor.
* No `DbContext`, repositories, or business services.
* No manual `try-catch` blocks or `ModelState` checks.
* Action methods delegate directly to MediatR via `await sender.Send(...)`:

```csharp
[Route("api/[controller]")]
[ApiController]
[Authorize(Roles = $"{SD.PlatformAdmin},Admin,PlatformAdmin")]
public class PlansController(ISender sender) : ControllerBase
{
    [HttpGet]
    public async Task<IActionResult> GetAll([FromQuery] bool onlyActivated = false, CancellationToken ct = default)
    {
        var plans = await sender.Send(new GetAllPlansQuery(onlyActivated), ct);
        return Ok(ApiResponse<IReadOnlyList<PlanDto>>.SuccessResponse(plans));
    }

    [HttpGet("{id:int}")]
    public async Task<IActionResult> GetById([FromRoute] int id, CancellationToken ct = default)
    {
        var plan = await sender.Send(new GetPlanByIdQuery(id), ct);
        return Ok(ApiResponse<PlanDto>.SuccessResponse(plan));
    }

    [HttpPost]
    public async Task<IActionResult> Create([FromBody] CreatePlanCommand command, CancellationToken ct = default)
    {
        var plan = await sender.Send(command, ct);
        return CreatedAtAction(nameof(GetById), new { id = plan.Id }, 
            ApiResponse<PlanDto>.SuccessResponse(plan, "Plan created successfully.", HttpStatusCode.Created));
    }

    [HttpPut("{id:int}")]
    public async Task<IActionResult> Update([FromRoute] int id, [FromBody] UpdatePlanRequest request, CancellationToken ct = default)
    {
        var command = new UpdatePlanCommand(
            id, request.Name, request.Description, request.Price, 
            request.BillingCycle, request.MaxBranches, request.MaxServices, request.MaxServicesPerBranch);

        var plan = await sender.Send(command, ct);
        return Ok(ApiResponse<PlanDto>.SuccessResponse(plan, "Plan updated successfully."));
    }

    [HttpPatch("{id:int}/activate")]
    public async Task<IActionResult> Activate([FromRoute] int id, CancellationToken ct = default)
    {
        await sender.Send(new ActivatePlanCommand(id), ct);
        return Ok(ApiResponse<object>.SuccessResponse(null, "Plan activated successfully."));
    }

    [HttpDelete("{id:int}")]
    public async Task<IActionResult> Delete([FromRoute] int id, CancellationToken ct = default)
    {
        await sender.Send(new DeletePlanCommand(id), ct);
        return Ok(ApiResponse<object>.SuccessResponse(null, "Plan deleted successfully."));
    }
}
```

---

## 7. Domain Entity Encapsulation Guidelines

Domain entities in `Taboor_Domain/Entities/` must strictly enforce encapsulation:
* **Properties**: All properties must have `private set;` (or `init;` where appropriate).
* **Constructors**: Parameterless constructor must be `private Entity() { }` (reserved for EF Core reflection).
* **Factory Method**: New entities must be created via Named Static Factory Methods (`Entity.Create(...)`) initializing properties internally.
* **Domain Behaviors**: State transitions must be explicit domain methods (e.g. `entity.Activate()`, `entity.Deactivate()`, `entity.Update(...)`). Avoid generic or blind state mutators.

```csharp
public class Plan
{
    public int Id { get; private set; }
    public string Name { get; private set; } = null!;
    public bool IsActive { get; private set; } = true;
    public DateTime CreatedAt { get; private set; }
    public DateTime? UpdatedAt { get; private set; }

    private Plan() { }

    public static Plan Create(string name, decimal price, ...)
    {
        return new Plan
        {
            Name = name.Trim(),
            Price = price,
            IsActive = true,
            CreatedAt = DateTime.UtcNow
        };
    }

    public void Update(string name, decimal price, ...)
    {
        Name = name.Trim();
        Price = price;
        UpdatedAt = DateTime.UtcNow;
    }

    public void Activate()
    {
        IsActive = true;
        UpdatedAt = DateTime.UtcNow;
    }

    public void Deactivate()
    {
        IsActive = false;
        UpdatedAt = DateTime.UtcNow;
    }
}
```

---

## 8. Summary Checklist for Any New Feature (Agent Blueprint)

When creating or refactoring any feature, follow this exact workflow:
1. **Domain Entity**: Check/encapsulate the entity in `Taboor_Domain/Entities/` (`private set;`, `Entity.Create(...)`, domain methods).
2. **Database Context Port**: Ensure entity `DbSet<T>` is exposed on `IApplicationDbContext`.
3. **Vertical Slice Folder**: Create `Taboor_Application/Features/<FeatureName>/` with `Commands/`, `Queries/`, and `DTOs/`.
4. **DTOs**: Define immutable records in `DTOs/<FeatureName>Dto.cs`.
5. **Commands & Validators**:
   - Create `<Action>Command.cs` as immutable record implementing `IRequest<TResponse>`.
   - Create `<Action>Validator.cs` inheriting from `AbstractValidator<<Action>Command>`.
   - Create `<Action>CommandHandler.cs` injecting `IApplicationDbContext`.
6. **Queries**:
   - Create `<Query>Query.cs` as record implementing `IRequest<TResponse>`.
   - Create `<Query>QueryHandler.cs` using `.AsNoTracking()` and `.Select()` direct projection.
7. **Controller**:
   - Expose via thin controller in `Taboor_API/Controllers/` injecting only `ISender`.
   - Dispatch requests via `await sender.Send(...)` and wrap results in `ApiResponse<T>.SuccessResponse(...)`.
8. **Verification**: Run `dotnet build apps/api/Taboor.slnx` to guarantee 0 warnings/errors.
