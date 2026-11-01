# Taboor Backend Architecture & Agent Instructions

This directory houses the backend ASP.NET Core API for Taboor.
All development in `apps/api` must strictly follow the root guidelines defined in [../../AGENTS.md](../../AGENTS.md).

### Quick Summary:
* **System Level:** Traditional Monolith
* **Boundary Level:** Feature Boundaries (`Taboor_Application/Features/<FeatureName>/`)
* **Code Level:** Vertical Slice Architecture (VSA) + CQRS (MediatR)
* **Direct EF Core:** No Generic Repositories, no custom UnitOfWork, no Specification Pattern.
* **Exceptions:** Global Exception Handler (`IExceptionHandler`) + Result Pattern for business errors.
