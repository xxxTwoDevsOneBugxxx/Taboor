import type { ColumnDef } from '@tanstack/react-table'
import type { TFunction, i18n as I18nType } from 'i18next'
import {
  Building2Icon,
  CheckCircle2Icon,
  ClockIcon,
  AlertTriangleIcon,
  XCircleIcon,
  PhoneIcon,
  MailIcon,
  CreditCardIcon,
  MoreVerticalIcon,
  EyeIcon,
  BanIcon,
  ExternalLinkIcon,
} from 'lucide-react'
import type {
  SubscriptionStatus,
  TenantListItem,
  TenantStatus,
} from '../types/tenant-types'
import { DataTableColumnHeader } from '@/components/common/data-table/DataTableColumnHeader'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

function getSubscriptionBadge(
  status: SubscriptionStatus | number | undefined | null,
  t: TFunction,
) {
  // Support string and numeric enum values defensively
  // Trialing = 1, Active = 2, PastDue = 3, Canceled = 4, Expired = 5
  if (status === 'Active' || status === 2) {
    return (
      <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-500/10 px-2.5 py-0.5 text-xs font-medium text-emerald-600 dark:text-emerald-400">
        <span className="size-1.5 rounded-full bg-emerald-500 animate-pulse" />
        {t('tenantsTable.subStatus.active')}
      </span>
    )
  }

  if (status === 'Trialing' || status === 1) {
    return (
      <span className="inline-flex items-center gap-1.5 rounded-full bg-blue-500/10 px-2.5 py-0.5 text-xs font-medium text-blue-600 dark:text-blue-400">
        <ClockIcon className="size-3" />
        {t('tenantsTable.subStatus.trialing')}
      </span>
    )
  }

  if (status === 'PastDue' || status === 3) {
    return (
      <span className="inline-flex items-center gap-1.5 rounded-full bg-amber-500/10 px-2.5 py-0.5 text-xs font-medium text-amber-600 dark:text-amber-400">
        <AlertTriangleIcon className="size-3" />
        {t('tenantsTable.subStatus.pastDue')}
      </span>
    )
  }

  if (status === 'Canceled' || status === 4) {
    return (
      <span className="inline-flex items-center gap-1.5 rounded-full bg-rose-500/10 px-2.5 py-0.5 text-xs font-medium text-rose-600 dark:text-rose-400">
        <XCircleIcon className="size-3" />
        {t('tenantsTable.subStatus.canceled')}
      </span>
    )
  }

  return (
    <span className="inline-flex items-center gap-1.5 rounded-full bg-muted px-2.5 py-0.5 text-xs font-medium text-muted-foreground">
      {t('tenantsTable.subStatus.expired')}
    </span>
  )
}

function getTenantStatusBadge(
  status: TenantStatus | number,
  t: TFunction,
) {
  // Active = 1, Suspended = 2
  const isActive = status === 'Active' || status === 1

  if (isActive) {
    return (
      <span className="inline-flex items-center gap-1 rounded-md bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400 px-2 py-0.5 text-[11px] font-medium border border-emerald-200/50 dark:border-emerald-800/40">
        <CheckCircle2Icon className="size-3" />
        {t('tenantsTable.tenantStatus.active')}
      </span>
    )
  }

  return (
    <span className="inline-flex items-center gap-1 rounded-md bg-rose-50 text-rose-700 dark:bg-rose-950/40 dark:text-rose-400 px-2 py-0.5 text-[11px] font-medium border border-rose-200/50 dark:border-rose-800/40">
      <BanIcon className="size-3" />
      {t('tenantsTable.tenantStatus.suspended')}
    </span>
  )
}

interface TenantsColumnsOptions {
  onViewDetails?: (tenant: TenantListItem) => void
  onToggleStatus?: (tenant: TenantListItem) => void
  onManageSubscription?: (tenant: TenantListItem) => void
  onHoverDetails?: (tenant: TenantListItem) => void
}

export function getTenantsColumns(
  t: TFunction,
  i18n: I18nType,
  options?: TenantsColumnsOptions,
): ColumnDef<TenantListItem>[] {
  const isArabic = i18n.language === 'ar'
  const dateLocale = isArabic ? 'ar-EG' : 'en-US'

  return [
    {
      accessorKey: 'name',
      header: ({ column }) => (
        <DataTableColumnHeader
          column={column}
          title={t('tenantsTable.columns.tenant')}
        />
      ),
      cell: ({ row }) => {
        const item = row.original
        return (
          <div className="flex items-center gap-2.5 min-w-0">
            {item.logoUrl ? (
              <img
                src={item.logoUrl}
                alt={item.name}
                className="size-8 rounded-lg object-cover border shrink-0"
              />
            ) : (
              <div className="flex size-8 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary border font-semibold text-xs">
                <Building2Icon className="size-3.5" />
              </div>
            )}
            <div className="flex flex-col min-w-0">
              <span className="font-semibold text-foreground text-xs sm:text-sm">
                {item.name}
              </span>
              <span className="text-[11px] text-muted-foreground font-mono">
                /{item.slug}
              </span>
            </div>
          </div>
        )
      },
    },
    {
      accessorKey: 'contact',
      header: t('tenantsTable.columns.contact'),
      cell: ({ row }) => {
        const item = row.original
        return (
          <div className="flex flex-col gap-0.5 text-xs min-w-0">
            <span className="font-medium text-foreground">
              {item.ownerName ?? item.name}
            </span>
            <span className="inline-flex items-center gap-1 text-[11px] text-muted-foreground font-mono">
              <MailIcon className="size-3 shrink-0 text-muted-foreground/70" />
              <span>{item.contactEmail}</span>
            </span>
            <span className="inline-flex items-center gap-1 text-[11px] text-muted-foreground/80 font-mono">
              <PhoneIcon className="size-3 shrink-0 text-muted-foreground/70" />
              <span>{item.contactPhone}</span>
            </span>
          </div>
        )
      },
    },
    {
      accessorKey: 'planName',
      header: ({ column }) => (
        <DataTableColumnHeader
          column={column}
          title={t('tenantsTable.columns.plan')}
        />
      ),
      cell: ({ row }) => {
        const item = row.original
        if (!item.planName) {
          return <span className="text-xs text-muted-foreground">—</span>
        }
        return (
          <div className="flex flex-col gap-0.5 text-xs">
            <div className="flex items-center gap-1.5">
              <CreditCardIcon className="size-3 text-primary" />
              <span className="font-semibold text-foreground">
                {item.planName}
              </span>
            </div>
            {item.planPrice !== null && item.planPrice !== undefined && (
              <span className="text-[11px] text-muted-foreground font-mono">
                {item.planPrice} {isArabic ? 'ج.م / شهر' : 'EGP / mo'}
              </span>
            )}
          </div>
        )
      },
    },
    {
      accessorKey: 'subscriptionStatus',
      header: t('tenantsTable.columns.subscriptionStatus'),
      cell: ({ row }) =>
        getSubscriptionBadge(row.original.subscriptionStatus, t),
    },
    {
      accessorKey: 'status',
      header: t('tenantsTable.columns.accountStatus'),
      cell: ({ row }) => getTenantStatusBadge(row.original.status, t),
    },
    {
      accessorKey: 'createdAt',
      header: ({ column }) => (
        <DataTableColumnHeader
          column={column}
          title={t('tenantsTable.columns.createdAt')}
        />
      ),
      cell: ({ row }) => {
        const date = new Date(row.original.createdAt)
        return (
          <span className="text-xs text-muted-foreground font-mono">
            {date.toLocaleDateString(dateLocale, {
              year: 'numeric',
              month: 'short',
              day: 'numeric',
            })}
          </span>
        )
      },
    },
    {
      id: 'actions',
      header: () => (
        <div className="text-end pe-2 font-medium text-xs text-muted-foreground">
          {t('tenantsTable.columns.actions')}
        </div>
      ),
      cell: ({ row }) => {
        const item = row.original
        const isActive =
          item.status === 'Active' || (item.status as unknown) === 1
        const isSubIssue =
          item.subscriptionStatus === 'Canceled' ||
          item.subscriptionStatus === 'Expired' ||
          item.subscriptionStatus === 'PastDue' ||
          (item.subscriptionStatus as unknown) === 3 ||
          (item.subscriptionStatus as unknown) === 4 ||
          (item.subscriptionStatus as unknown) === 5

        return (
          <div
            className="flex justify-end pe-1"
            onClick={(e) => e.stopPropagation()}
          >
            <DropdownMenu>
              <DropdownMenuTrigger
                render={
                  <Button
                    variant="ghost"
                    size="icon-xs"
                    onClick={(e) => e.stopPropagation()}
                    onMouseEnter={() => options?.onHoverDetails?.(item)}
                    className="size-7 text-muted-foreground hover:text-foreground hover:bg-muted"
                  >
                    <MoreVerticalIcon className="size-4" />
                  </Button>
                }
              />
              <DropdownMenuContent align="end" className="w-48 text-xs">
                <DropdownMenuItem
                  onClick={() => options?.onViewDetails?.(item)}
                  className="gap-2 cursor-pointer"
                >
                  <EyeIcon className="size-3.5 text-muted-foreground" />
                  {t('tenantsTable.actions.viewDetails')}
                </DropdownMenuItem>

                <DropdownMenuItem
                  onClick={() => window.open(`/t/${item.slug}`, '_blank')}
                  className="gap-2 cursor-pointer"
                >
                  <ExternalLinkIcon className="size-3.5 text-muted-foreground" />
                  {t('tenantsTable.actions.visitPage')}
                </DropdownMenuItem>

                {isSubIssue && (
                  <DropdownMenuItem
                    onClick={() => options?.onManageSubscription?.(item)}
                    className="gap-2 cursor-pointer text-amber-600 dark:text-amber-400 hover:bg-amber-50 dark:hover:bg-amber-950/40 hover:text-amber-700 dark:hover:text-amber-300 focus:bg-amber-50 dark:focus:bg-amber-950/40 focus:text-amber-700"
                  >
                    <CreditCardIcon className="size-3.5 text-amber-500 shrink-0" />
                    {t('tenantsTable.actions.manageSubscription')}
                  </DropdownMenuItem>
                )}

                <DropdownMenuSeparator />

                <DropdownMenuItem
                  onClick={() => options?.onToggleStatus?.(item)}
                  className={
                    isActive
                      ? 'gap-2 cursor-pointer text-rose-600 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/40 hover:text-rose-700 dark:hover:text-rose-300 focus:bg-rose-50 dark:focus:bg-rose-950/40 focus:text-rose-700'
                      : 'gap-2 cursor-pointer text-emerald-600 dark:text-emerald-400 hover:bg-emerald-50 dark:hover:bg-emerald-950/40 hover:text-emerald-700 dark:hover:text-emerald-300 focus:bg-emerald-50 dark:focus:bg-emerald-950/40 focus:text-emerald-700'
                  }
                >
                  {isActive ? (
                    <>
                      <BanIcon className="size-3.5 text-rose-500 shrink-0" />
                      <span>{t('tenantsTable.actions.suspend')}</span>
                    </>
                  ) : (
                    <>
                      <CheckCircle2Icon className="size-3.5 text-emerald-600 dark:text-emerald-400 shrink-0" />
                      <span>{t('tenantsTable.actions.activate')}</span>
                    </>
                  )}
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        )
      },
    },
  ]
}
