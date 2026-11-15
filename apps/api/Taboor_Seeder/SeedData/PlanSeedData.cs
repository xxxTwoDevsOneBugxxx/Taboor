using Taboor_Domain.Entities;

namespace Taboor_Seeder.SeedData;

public static class PlanSeedData
{
    public record PlanDefinition(
        string Name,
        string Description,
        decimal Price,
        BillingCycle BillingCycle,
        int MaxBranches,
        int MaxServices,
        int MaxServicesPerBranch);

    public static IReadOnlyList<PlanDefinition> Plans => new List<PlanDefinition>
    {
        new(
            Name: "Starter",
            Description: "باقة البداية للعيادات الفردية وصالونات الحلاقة والورش المستقلة.",
            Price: 199.00m,
            BillingCycle: BillingCycle.Monthly,
            MaxBranches: 1,
            MaxServices: 5,
            MaxServicesPerBranch: 5
        ),
        new(
            Name: "Pro",
            Description: "باقة المحترفين للمراكز الطبية التخصصية، ومراكز التجميل وصيانة السيارات.",
            Price: 499.00m,
            BillingCycle: BillingCycle.Monthly,
            MaxBranches: 3,
            MaxServices: 15,
            MaxServicesPerBranch: 15
        ),
        new(
            Name: "Enterprise",
            Description: "باقة المؤسسات والشركات الكبرى، سلاسل المستشفيات ومعامل التحاليل ومراكز الخدمة.",
            Price: 1499.00m,
            BillingCycle: BillingCycle.Monthly,
            MaxBranches: 10,
            MaxServices: 50,
            MaxServicesPerBranch: 50
        )
    };
}
