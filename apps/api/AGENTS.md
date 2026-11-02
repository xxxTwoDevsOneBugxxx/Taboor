# Taboor Backend Architecture & Agent Instructions

This directory houses the backend ASP.NET Core API for Taboor.
All development in `apps/api` must strictly follow the root guidelines defined in [../../AGENTS.md](../../AGENTS.md).

### Quick Summary:
* **System Level:** Traditional Monolith
* **Boundary Level:** Feature Boundaries (`Taboor_Application/Features/<FeatureName>/`)
* **Code Level:** Vertical Slice Architecture (VSA) + CQRS (MediatR)
* **Direct EF Core:** Direct `IApplicationDbContext` operations in handlers; no Generic Repositories, no UnitOfWork wrappers, no Specification Pattern.
* **Exceptions & Validation:** `GlobalExceptionHandler` (`IExceptionHandler`) + typed `AppException` + `FluentValidation` via `ValidationBehavior`.
* **Domain Encapsulation:** Rich Domain Entities with `private set;`, `Entity.Create(...)` static factories, and explicit domain methods.
