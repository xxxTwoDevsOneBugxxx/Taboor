using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Tenants.Requests;

/// <summary>
/// HTTP request body payload for renewing an existing tenant subscription.
/// </summary>
public record RenewTenantSubscriptionRequest(
    BillingCycle? BillingCycle = null,
    int? CustomDays = null,
    string? ExternalPaymentId = null
);
