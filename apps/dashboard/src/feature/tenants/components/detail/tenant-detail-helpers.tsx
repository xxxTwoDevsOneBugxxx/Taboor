import type { TFunction } from 'i18next'
import {
  AlertTriangleIcon,
  BanIcon,
  CheckCircle2Icon,
  ClockIcon,
  XCircleIcon,
} from 'lucide-react'
import type {
  BillingCycle,
  SubscriptionStatus,
  TenantMember,
  TenantStatus,
} from '../../types/tenant-types'

export function getInitials(name: string): string {
  if (!name) return '?'
  const parts = name.trim().split(/\s+/)
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
}

export function formatDate(dateStr?: string | null, locale = 'en-US'): string {
  if (!dateStr) return '—'
  const d = new Date(dateStr)
  return d.toLocaleDateString(locale, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

export function formatBillingCycle(
  cycle?: BillingCycle | null,
  isArabic = false,
): string {
  if (!cycle) return '—'
  if (cycle === 'Monthly' || cycle === 1) return isArabic ? 'شهري' : 'Monthly'
  if (cycle === 'Annually' || cycle === 2) return isArabic ? 'سنوي' : 'Annually'
  if (cycle === 'Quarterly' || cycle === 3)
    return isArabic ? 'ربع سنوي' : 'Quarterly'
  return String(cycle)
}

export function getRoleLabel(role: TenantMember['role'], t: TFunction): string {
  const r = String(role).toLowerCase()
  if (r === 'owner' || r === '1') return t('tenantDetails.roles.owner')
  if (r === 'admin' || r === '2') return t('tenantDetails.roles.admin')
  return t('tenantDetails.roles.staff')
}

export function getRoleBadgeClass(role: TenantMember['role']): string {
  const r = String(role).toLowerCase()
  if (r === 'owner' || r === '1') {
    return 'bg-indigo-50 text-indigo-700 border-indigo-200/60 dark:bg-indigo-950/40 dark:text-indigo-400 dark:border-indigo-800/40'
  }
  if (r === 'admin' || r === '2') {
    return 'bg-teal-50 text-teal-700 border-teal-200/60 dark:bg-teal-950/40 dark:text-teal-400 dark:border-teal-800/40'
  }
  return 'bg-muted text-muted-foreground border-border/60'
}

export function renderSubscriptionBadge(
  status: SubscriptionStatus | number | undefined | null,
  t: TFunction,
) {
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
      <BanIcon className="size-3" />
      {t('tenantsTable.subStatus.expired')}
    </span>
  )
}

export function renderTenantStatusBadge(
  status: TenantStatus | number | undefined,
  t: TFunction,
) {
  const isActive = status === 'Active' || status === 1
  if (isActive) {
    return (
      <span className="inline-flex items-center gap-1 rounded-md bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400 px-2 py-0.5 text-xs font-medium border border-emerald-200/50">
        <CheckCircle2Icon className="size-3" />
        {t('tenantsTable.tenantStatus.active')}
      </span>
    )
  }
  return (
    <span className="inline-flex items-center gap-1 rounded-md bg-rose-50 text-rose-700 dark:bg-rose-950/40 dark:text-rose-400 px-2 py-0.5 text-xs font-medium border border-rose-200/50">
      <BanIcon className="size-3" />
      {t('tenantsTable.tenantStatus.suspended')}
    </span>
  )
}
