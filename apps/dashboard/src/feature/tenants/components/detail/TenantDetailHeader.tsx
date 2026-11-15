import * as React from 'react'
import { useTranslation } from 'react-i18next'
import {
  Building2Icon,
  CalendarIcon,
  CheckIcon,
  CopyIcon,
} from 'lucide-react'
import type { TenantDetail } from '../../types/tenant-types'
import {
  formatDate,
  renderSubscriptionBadge,
  renderTenantStatusBadge,
} from './tenant-detail-helpers'

interface TenantDetailHeaderProps {
  tenant: TenantDetail
}

export function TenantDetailHeader({ tenant }: TenantDetailHeaderProps) {
  const { t, i18n } = useTranslation('common')
  const dateLocale = i18n.language === 'ar' ? 'ar-EG' : 'en-US'
  const [copiedSlug, setCopiedSlug] = React.useState(false)

  const handleCopySlug = () => {
    if (!tenant?.slug) return
    navigator.clipboard.writeText(tenant.slug)
    setCopiedSlug(true)
    setTimeout(() => setCopiedSlug(false), 2000)
  }

  const currentSub = tenant.currentSubscription

  return (
    <div className="flex items-start gap-3.5 rounded-xl border bg-background/60 p-4 shadow-2xs">
      {tenant.logoUrl ? (
        <img
          src={tenant.logoUrl}
          alt={tenant.name}
          className="size-14 rounded-xl object-cover border shrink-0 bg-background"
        />
      ) : (
        <div className="flex size-14 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary border font-semibold text-lg">
          <Building2Icon className="size-7" />
        </div>
      )}

      <div className="flex flex-col min-w-0 flex-1 gap-1">
        <h3 className="font-semibold text-foreground text-base truncate">
          {tenant.name}
        </h3>
        <div className="flex items-center gap-1.5 text-xs text-muted-foreground font-mono">
          <span>/{tenant.slug}</span>
          <button
            type="button"
            onClick={handleCopySlug}
            className="p-1 rounded hover:bg-muted text-muted-foreground hover:text-foreground transition-colors cursor-pointer"
            title="Copy Slug"
          >
            {copiedSlug ? (
              <CheckIcon className="size-3 text-emerald-600" />
            ) : (
              <CopyIcon className="size-3" />
            )}
          </button>
        </div>

        <div className="flex items-center gap-2 mt-1 flex-wrap">
          {renderTenantStatusBadge(tenant.status, t)}
          {renderSubscriptionBadge(currentSub?.status, t)}
          <span className="inline-flex items-center gap-1 text-[11px] text-muted-foreground">
            <CalendarIcon className="size-3" />
            {formatDate(tenant.createdAt, dateLocale)}
          </span>
        </div>
      </div>
    </div>
  )
}
