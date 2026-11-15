import { useTranslation } from 'react-i18next'
import {
  Building2Icon,
  CalendarIcon,
  CreditCardIcon,
  ExternalLinkIcon,
  EyeIcon,
  MailIcon,
  MoreVerticalIcon,
  PhoneIcon,
  UserIcon,
  BanIcon,
  CheckCircle2Icon,
} from 'lucide-react'
import type { TenantListItem } from '../types/tenant-types'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  formatDate,
  renderSubscriptionBadge,
  renderTenantStatusBadge,
} from './detail/tenant-detail-helpers'

interface TenantMobileCardProps {
  tenant: TenantListItem
  onViewDetails?: (tenant: TenantListItem) => void
  onToggleStatus?: (tenant: TenantListItem) => void
  onManageSubscription?: (tenant: TenantListItem) => void
}

export function TenantMobileCard({
  tenant,
  onViewDetails,
  onToggleStatus,
  onManageSubscription,
}: TenantMobileCardProps) {
  const { t, i18n } = useTranslation('common')
  const isArabic = i18n.language === 'ar'
  const dateLocale = isArabic ? 'ar-EG' : 'en-US'

  const isActive =
    tenant.status === 'Active' || (tenant.status as unknown) === 1
  const isSubIssue =
    tenant.subscriptionStatus === 'Canceled' ||
    tenant.subscriptionStatus === 'Expired' ||
    tenant.subscriptionStatus === 'PastDue' ||
    (tenant.subscriptionStatus as unknown) === 3 ||
    (tenant.subscriptionStatus as unknown) === 4 ||
    (tenant.subscriptionStatus as unknown) === 5

  return (
    <div className="flex flex-col gap-2.5 p-3.5 text-xs transition-colors hover:bg-muted/15">
      {/* Card Header: Logo, Name, Slug & Actions */}
      <div className="flex items-start justify-between gap-2.5">
        <div className="flex items-center gap-2.5 min-w-0 flex-1">
          {tenant.logoUrl ? (
            <img
              src={tenant.logoUrl}
              alt={tenant.name}
              className="size-9 rounded-lg object-cover border shrink-0 bg-background"
            />
          ) : (
            <div className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary border font-semibold text-xs">
              <Building2Icon className="size-4" />
            </div>
          )}

          <div className="flex flex-col min-w-0">
            <span className="font-semibold text-foreground text-sm truncate">
              {tenant.name}
            </span>
            <span className="text-[11px] text-muted-foreground font-mono truncate">
              /{tenant.slug}
            </span>
          </div>
        </div>

        {/* 3-dots Menu Button */}
        <div onClick={(e) => e.stopPropagation()}>
          <DropdownMenu>
            <DropdownMenuTrigger
              render={
                <Button
                  variant="ghost"
                  size="icon-xs"
                  onClick={(e) => e.stopPropagation()}
                  className="size-7 text-muted-foreground hover:text-foreground hover:bg-muted shrink-0"
                >
                  <MoreVerticalIcon className="size-4" />
                </Button>
              }
            />
            <DropdownMenuContent align="end" className="w-48 text-xs">
              <DropdownMenuItem
                onClick={() => onViewDetails?.(tenant)}
                className="gap-2 cursor-pointer"
              >
                <EyeIcon className="size-3.5 text-muted-foreground" />
                {t('tenantsTable.actions.viewDetails')}
              </DropdownMenuItem>

              <DropdownMenuItem
                onClick={() => window.open(`/t/${tenant.slug}`, '_blank')}
                className="gap-2 cursor-pointer"
              >
                <ExternalLinkIcon className="size-3.5 text-muted-foreground" />
                {t('tenantsTable.actions.visitPage')}
              </DropdownMenuItem>

              {isSubIssue && (
                <DropdownMenuItem
                  onClick={() => onManageSubscription?.(tenant)}
                  className="gap-2 cursor-pointer text-amber-600 dark:text-amber-400 hover:bg-amber-50 dark:hover:bg-amber-950/40"
                >
                  <CreditCardIcon className="size-3.5 text-amber-500 shrink-0" />
                  {t('tenantsTable.actions.manageSubscription')}
                </DropdownMenuItem>
              )}

              <DropdownMenuSeparator />

              <DropdownMenuItem
                onClick={() => onToggleStatus?.(tenant)}
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
      </div>

      {/* Middle Grid: Contact & Plan */}
      <div className="rounded-lg bg-muted/30 border border-border/50 p-2.5 space-y-1.5 text-xs">
        {/* Owner & Contact */}
        <div className="flex items-center justify-between gap-2 text-muted-foreground">
          <div className="flex items-center gap-1.5 min-w-0">
            <UserIcon className="size-3 shrink-0 text-muted-foreground/70" />
            <span className="font-medium text-foreground truncate">
              {tenant.ownerName ?? tenant.name}
            </span>
          </div>

          <div className="flex items-center gap-1 font-mono text-[11px] shrink-0 text-muted-foreground">
            <PhoneIcon className="size-3 shrink-0 opacity-70" />
            <span>{tenant.contactPhone}</span>
          </div>
        </div>

        {/* Email */}
        <div className="flex items-center gap-1 font-mono text-[11px] text-muted-foreground truncate">
          <MailIcon className="size-3 shrink-0 opacity-70" />
          <span className="truncate">{tenant.contactEmail}</span>
        </div>

        {/* Current Plan & Price */}
        <div className="flex items-center justify-between pt-1 border-t border-border/50">
          <div className="flex items-center gap-1.5 font-medium text-foreground">
            <CreditCardIcon className="size-3 text-primary shrink-0" />
            <span>{tenant.planName ?? '—'}</span>
          </div>
          {tenant.planPrice !== null && tenant.planPrice !== undefined && (
            <span className="font-mono text-muted-foreground text-[11px]">
              {tenant.planPrice} {isArabic ? 'ج.م / شهر' : 'EGP / mo'}
            </span>
          )}
        </div>
      </div>

      {/* Card Footer: Badges & Joined Date */}
      <div className="flex items-center justify-between gap-2 pt-0.5 flex-wrap">
        <div className="flex items-center gap-1.5 flex-wrap">
          {renderTenantStatusBadge(tenant.status, t)}
          {renderSubscriptionBadge(tenant.subscriptionStatus, t)}
        </div>

        <div className="flex items-center gap-1 text-[11px] text-muted-foreground font-mono ms-auto">
          <CalendarIcon className="size-3 shrink-0 opacity-60" />
          <span>{formatDate(tenant.createdAt, dateLocale)}</span>
        </div>
      </div>
    </div>
  )
}
