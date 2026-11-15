import { useTranslation } from 'react-i18next'
import { HistoryIcon } from 'lucide-react'
import type { TenantSubscription } from '../../types/tenant-types'
import {
  formatDate,
  renderSubscriptionBadge,
} from './tenant-detail-helpers'

interface TenantHistoryCardProps {
  subscriptions?: TenantSubscription[]
}

export function TenantHistoryCard({ subscriptions }: TenantHistoryCardProps) {
  const { t, i18n } = useTranslation('common')
  const dateLocale = i18n.language === 'ar' ? 'ar-EG' : 'en-US'

  if (!subscriptions || subscriptions.length <= 1) {
    return null
  }

  return (
    <div className="rounded-xl border bg-background/60 p-4 space-y-3 shadow-2xs">
      <div className="flex items-center gap-2 font-medium text-xs text-foreground border-b pb-2">
        <HistoryIcon className="size-4 text-primary" />
        <span>{t('tenantDetails.sections.subscriptionHistory')}</span>
      </div>

      <div className="divide-y divide-border/60">
        {subscriptions.map((sub) => (
          <div
            key={sub.id}
            className="flex items-center justify-between py-2 text-xs"
          >
            <div className="flex flex-col">
              <span className="font-medium text-foreground">
                {sub.planName}
              </span>
              <span className="text-[10px] text-muted-foreground font-mono">
                {formatDate(sub.startDate, dateLocale)} –{' '}
                {formatDate(sub.endDate, dateLocale)}
              </span>
            </div>
            <div>{renderSubscriptionBadge(sub.status, t)}</div>
          </div>
        ))}
      </div>
    </div>
  )
}
