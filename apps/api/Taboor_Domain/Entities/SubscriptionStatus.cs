namespace Taboor_Domain.Entities;

/// <summary>
/// Represents the lifecycle states of a tenant's subscription to a plan.
/// </summary>
public enum SubscriptionStatus
{
    /// <summary>
    /// Subscription is currently in a free evaluation trial period.
    /// </summary>
    Trialing = 1,

    /// <summary>
    /// Subscription is paid, valid, and active.
    /// </summary>
    Active = 2,

    /// <summary>
    /// Billing renewal failed or grace period is active before suspension.
    /// </summary>
    PastDue = 3,

    /// <summary>
    /// Subscription was explicitly canceled by the tenant or platform admin.
    /// </summary>
    Canceled = 4,

    /// <summary>
    /// Subscription duration has fully elapsed without renewal.
    /// </summary>
    Expired = 5
}
