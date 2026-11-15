import { useTranslation } from 'react-i18next'
import { CreditCardIcon, RefreshCwIcon } from 'lucide-react'
import type { TenantSubscription } from '../../types/tenant-types'
import {
  formatBillingCycle,
  formatDate,
  renderSubscriptionBadge,
} from './tenant-detail-helpers'

interface TenantSubscriptionCardProps {
  subscription?: TenantSubscription | null
}

export function TenantSubscriptionCard({
  subscription,
}: TenantSubscriptionCardProps) {
  const { t, i18n } = useTranslation('common')
  const isArabic = i18n.language === 'ar'
  const dateLocale = isArabic ? 'ar-EG' : 'en-US'

  return (
    <div className="rounded-xl border bg-background/60 p-4 space-y-3 shadow-2xs">
      <div className="flex items-center justify-between border-b pb-2">
        <div className="flex items-center gap-2 font-medium text-xs text-foreground">
          <CreditCardIcon className="size-4 text-primary" />
          <span>{t('tenantDetails.sections.currentSubscription')}</span>
        </div>
        {subscription && renderSubscriptionBadge(subscription.status, t)}
      </div>

      {subscription ? (
        <div className="space-y-3 text-xs">
          <div className="flex items-baseline justify-between">
            <div className="space-y-0.5">
              <span className="text-sm font-semibold text-foreground">
                {subscription.planName}
              </span>
              <div className="text-[11px] text-muted-foreground">
                {formatBillingCycle(subscription.billingCycle, isArabic)}
              </div>
            </div>
            <div className="text-end">
              <span className="text-base font-bold text-foreground">
                {subscription.price}
              </span>
              <span className="text-[11px] text-muted-foreground ms-1">
                {isArabic ? 'ج.م' : 'EGP'}
              </span>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-2 pt-2 border-t text-xs">
            <div>
              <span className="text-[11px] text-muted-foreground block">
                {t('tenantDetails.fields.period')}
              </span>
              <span className="font-mono text-foreground">
                {formatDate(subscription.startDate, dateLocale)} –{' '}
                {formatDate(subscription.endDate, dateLocale)}
              </span>
            </div>

            <div>
              <span className="text-[11px] text-muted-foreground block">
                {t('tenantDetails.fields.autoRenew')}
              </span>
              <span className="inline-flex items-center gap-1 font-medium">
                <RefreshCwIcon className="size-3 text-muted-foreground" />
                {subscription.autoRenew
                  ? t('tenantDetails.fields.enabled')
                  : t('tenantDetails.fields.disabled')}
              </span>
            </div>

            {subscription.trialEndsAt && (
              <div className="col-span-2">
                <span className="text-[11px] text-muted-foreground block">
                  {t('tenantDetails.fields.trialEnds')}
                </span>
                <span className="font-mono text-blue-600 dark:text-blue-400">
                  {formatDate(subscription.trialEndsAt, dateLocale)}
                </span>
              </div>
            )}

            {subscription.canceledAt && (
              <div className="col-span-2 rounded-md bg-rose-50 dark:bg-rose-950/30 p-2 border border-rose-200/50 text-rose-700 dark:text-rose-400">
                <span className="font-semibold block">
                  {t('tenantDetails.fields.canceledAt')}:{' '}
                  {formatDate(subscription.canceledAt, dateLocale)}
                </span>
                {subscription.cancellationReason && (
                  <span className="text-[11px]">
                    {t('tenantDetails.fields.cancellationReason')}:{' '}
                    {subscription.cancellationReason}
                  </span>
                )}
              </div>
            )}
          </div>
        </div>
      ) : (
        <p className="text-xs text-muted-foreground py-2">
          {t('tenantDetails.fields.noSubscription')}
        </p>
      )}
    </div>
  )
}
