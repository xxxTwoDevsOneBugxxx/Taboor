using MediatR;
using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Tenants.Commands.RenewTenantSubscription;

/// <summary>
/// Command to renew an existing tenant subscription contract.
/// Extends the active period according to plan billing cycle or custom days.
/// </summary>
public record RenewTenantSubscriptionCommand(
    Guid TenantId,
    BillingCycle? BillingCycle = null,
    int? CustomDays = null,
    string? ExternalPaymentId = null
) : IRequest;
