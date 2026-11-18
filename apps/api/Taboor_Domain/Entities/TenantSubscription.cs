namespace Taboor_Domain.Entities;

/// <summary>
/// Represents a commercial subscription contract binding a business Tenant to a specific Plan.
/// Fully encapsulated Rich Domain Entity: state changes and transitions are strictly governed by domain methods.
/// </summary>
public class TenantSubscription
{
    public Guid Id { get; private set; }

    public Guid TenantId { get; private set; }

    public Tenant Tenant { get; private set; } = null!;

    public int PlanId { get; private set; }

    public Plan Plan { get; private set; } = null!;

    public SubscriptionStatus Status { get; private set; }

    /// <summary>
    /// Start date of the current active subscription period.
    /// </summary>
    public DateTime StartDate { get; private set; }

    /// <summary>
    /// End date of the current subscription period (next scheduled billing date).
    /// </summary>
    public DateTime EndDate { get; private set; }

    /// <summary>
    /// End date of the free trial period (if applicable).
    /// </summary>
    public DateTime? TrialEndsAt { get; private set; }

    /// <summary>
    /// Whether this subscription automatically renews upon reaching EndDate.
    /// </summary>
    public bool AutoRenew { get; private set; } = true;

    /// <summary>
    /// Reference ID assigned by the external payment gateway (e.g., Paymob subscription ID or Stripe sub_xxx).
    /// Null for local/mock environments or until gateway checkout finishes.
    /// </summary>
    public string? ExternalSubscriptionId { get; private set; }

    /// <summary>
    /// The timestamp when the subscription was canceled (null if active).
    /// </summary>
    public DateTime? CanceledAt { get; private set; }

    /// <summary>
    /// Reason provided for canceling the subscription.
    /// </summary>
    public string? CancellationReason { get; private set; }

    public DateTime CreatedAt { get; private set; }

    public DateTime? UpdatedAt { get; private set; }

    /// <summary>
    /// Private constructor for EF Core reflection materialization.
    /// </summary>
    private TenantSubscription() { }

    /// <summary>
    /// Static Factory Method to create a new free-trial subscription for a newly onboarded tenant.
    /// </summary>
    public static TenantSubscription CreateTrial(Guid tenantId, int planId, int trialDays = 14, string? externalSubscriptionId = null)
    {
        var now = DateTime.UtcNow;
        var trialEnd = now.AddDays(trialDays);

        return new TenantSubscription
        {
            Id = Guid.NewGuid(),
            TenantId = tenantId,
            PlanId = planId,
            Status = SubscriptionStatus.Trialing,
            StartDate = now,
            EndDate = trialEnd,
            TrialEndsAt = trialEnd,
            AutoRenew = false,
            ExternalSubscriptionId = externalSubscriptionId?.Trim(),
            CreatedAt = now
        };
    }

    /// <summary>
    /// Static Factory Method to create an active paid subscription.
    /// </summary>
    public static TenantSubscription CreateActive(
        Guid tenantId,
        int planId,
        DateTime startDate,
        DateTime endDate,
        string? externalSubscriptionId = null,
        bool autoRenew = true)
    {
        var now = DateTime.UtcNow;

        return new TenantSubscription
        {
            Id = Guid.NewGuid(),
            TenantId = tenantId,
            PlanId = planId,
            Status = SubscriptionStatus.Active,
            StartDate = startDate,
            EndDate = endDate,
            TrialEndsAt = null,
            AutoRenew = autoRenew,
            ExternalSubscriptionId = externalSubscriptionId?.Trim(),
            CreatedAt = now
        };
    }

    #region Domain Behaviors & State Transitions

    /// <summary>
    /// Updates the external payment gateway reference identifier.
    /// </summary>
    public void UpdateExternalSubscriptionId(string externalSubscriptionId)
    {
        ExternalSubscriptionId = externalSubscriptionId.Trim();
        UpdatedAt = DateTime.UtcNow;
    }

    /// <summary>
    /// Renews the current subscription period to a new end date upon successful payment.
    /// </summary>
    public void Renew(DateTime newEndDate, string? externalSubscriptionId = null)
    {
        Status = SubscriptionStatus.Active;
        StartDate = EndDate > DateTime.UtcNow ? EndDate : DateTime.UtcNow;
        EndDate = newEndDate;
        TrialEndsAt = null;
        CanceledAt = null;
        CancellationReason = null;
        if (!string.IsNullOrWhiteSpace(externalSubscriptionId))
        {
            ExternalSubscriptionId = externalSubscriptionId.Trim();
        }
        UpdatedAt = DateTime.UtcNow;
    }

    /// <summary>
    /// Changes the plan (Upgrade or Downgrade) and updates the billing period end date.
    /// </summary>
    public void ChangePlan(int newPlanId, DateTime newEndDate)
    {
        PlanId = newPlanId;
        Status = SubscriptionStatus.Active;
        EndDate = newEndDate;
        TrialEndsAt = null;
        CanceledAt = null;
        CancellationReason = null;
        UpdatedAt = DateTime.UtcNow;
    }

    /// <summary>
    /// Cancels the subscription with an optional reason.
    /// </summary>
    public void Cancel(string? reason = null)
    {
        Status = SubscriptionStatus.Canceled;
        AutoRenew = false;
        CanceledAt = DateTime.UtcNow;
        CancellationReason = reason?.Trim();
        UpdatedAt = DateTime.UtcNow;
    }

    /// <summary>
    /// Marks the subscription as past due (payment failed / entering grace period).
    /// </summary>
    public void MarkPastDue()
    {
        Status = SubscriptionStatus.PastDue;
        UpdatedAt = DateTime.UtcNow;
    }

    /// <summary>
    /// Expires the subscription completely after end date / grace period passes.
    /// </summary>
    public void Expire()
    {
        Status = SubscriptionStatus.Expired;
        AutoRenew = false;
        UpdatedAt = DateTime.UtcNow;
    }

    /// <summary>
    /// Evaluates whether the tenant currently has active operational access (Active or within Trial).
    /// </summary>
    public bool IsActiveOrInTrial()
    {
        var now = DateTime.UtcNow;
        if (Status == SubscriptionStatus.Active && EndDate >= now)
            return true;

        if (Status == SubscriptionStatus.Trialing && TrialEndsAt.HasValue && TrialEndsAt.Value >= now)
            return true;

        return false;
    }

    #endregion
}
