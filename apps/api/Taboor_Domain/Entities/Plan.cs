namespace Taboor_Domain.Entities
{
    /// <summary>
    /// Represents a platform-level subscription plan defined and managed by Platform Admins.
    /// Business tenants subscribe to a Plan via subscriptions.
    /// Fully encapsulated Rich Domain Entity: state changes are strictly governed by domain methods.
    /// </summary>
    public class Plan
    {
        public int Id { get; private set; }

        public string Name { get; private set; } = null!;

        public string? Description { get; private set; }

        public decimal Price { get; private set; }

        public BillingCycle BillingCycle { get; private set; }

        /// <summary>
        /// Maximum number of branches the business tenant may open.
        /// </summary>
        public int MaxBranches { get; private set; }

        /// <summary>
        /// Maximum number of services allowed in the business-wide service catalog.
        /// </summary>
        public int MaxServices { get; private set; }

        /// <summary>
        /// Maximum number of catalog services that may be active in a single branch at once.
        /// </summary>
        public int MaxServicesPerBranch { get; private set; }

        /// <summary>
        /// Indicates whether the plan is active and available.
        /// Plans are never hard-deleted; they are soft-toggled using this flag.
        /// </summary>
        public bool IsActive { get; private set; } = true;

        public DateTime CreatedAt { get; private set; }

        public DateTime? UpdatedAt { get; private set; }

        /// <summary>
        /// Private parameterless constructor for EF Core materialization via reflection.
        /// Prevents instantiating an uninitialized entity outside the domain.
        /// </summary>
        private Plan() { }

        /// <summary>
        /// Public domain constructor for creating a new valid subscription plan.
        /// </summary>
        public Plan(
            string name,
            string? description,
            decimal price,
            BillingCycle billingCycle,
            int maxBranches,
            int maxServices,
            int maxServicesPerBranch)
        {
            Name = name.Trim();
            Description = description?.Trim();
            Price = price;
            BillingCycle = billingCycle;
            MaxBranches = maxBranches;
            MaxServices = maxServices;
            MaxServicesPerBranch = maxServicesPerBranch;
            IsActive = true;
            CreatedAt = DateTime.UtcNow;
        }

        #region Domain Behaviors

        /// <summary>
        /// Updates the core business details of the plan.
        /// </summary>
        public void Update(
            string name,
            string? description,
            decimal price,
            BillingCycle billingCycle,
            int maxBranches,
            int maxServices,
            int maxServicesPerBranch)
        {
            Name = name.Trim();
            Description = description?.Trim();
            Price = price;
            BillingCycle = billingCycle;
            MaxBranches = maxBranches;
            MaxServices = maxServices;
            MaxServicesPerBranch = maxServicesPerBranch;
            UpdatedAt = DateTime.UtcNow;
        }

        /// <summary>
        /// Activates the plan so tenants can subscribe to it.
        /// </summary>
        public void Activate()
        {
            IsActive = true;
            UpdatedAt = DateTime.UtcNow;
        }

        /// <summary>
        /// Deactivates the plan without deleting it (soft toggle).
        /// </summary>
        public void Deactivate()
        {
            IsActive = false;
            UpdatedAt = DateTime.UtcNow;
        }

        /// <summary>
        /// Toggles the plan active status.
        /// </summary>
        public void ToggleStatus()
        {
            IsActive = !IsActive;
            UpdatedAt = DateTime.UtcNow;
        }

        #endregion
    }
}