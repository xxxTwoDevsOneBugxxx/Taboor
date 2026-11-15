import { useTranslation } from 'react-i18next'
import {
  BanIcon,
  Building2Icon,
  CheckCircle2Icon,
  ExternalLinkIcon,
} from 'lucide-react'
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'
import { useTenant, useToggleTenantStatus } from '../../hooks/use-tenants'
import { useDirection } from '#/hooks/use-direction'
import { TenantDetailSkeleton } from './TenantDetailSkeleton'
import { TenantDetailHeader } from './TenantDetailHeader'
import { TenantContactCard } from './TenantContactCard'
import { TenantSubscriptionCard } from './TenantSubscriptionCard'
import { TenantMembersCard } from './TenantMembersCard'
import { TenantHistoryCard } from './TenantHistoryCard'

export interface TenantDetailSheetProps {
  tenantId: string | null
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function TenantDetailSheet({
  tenantId,
  open,
  onOpenChange,
}: TenantDetailSheetProps) {
  const { t } = useTranslation('common')
  const direction = useDirection()
  const { toggleStatus, isToggling } = useToggleTenantStatus()

  // Fetch tenant details when sheet is open and tenantId is present
  const { data: tenant, isLoading } = useTenant(
    open && tenantId ? tenantId : '',
  )

  const isActive =
    tenant?.status === 'Active' || (tenant?.status as unknown) === 1

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent
        side={direction === 'rtl' ? 'left' : 'right'}
        className="w-full sm:max-w-lg md:max-w-xl p-0 flex flex-col h-full bg-card text-card-foreground shadow-2xl"
      >
        {/* Top Header */}
        <SheetHeader className="p-5 border-b shrink-0 bg-background/50">
          <div className="flex items-center gap-2">
            <Building2Icon className="size-5 text-primary shrink-0" />
            <SheetTitle className="text-base font-semibold">
              {t('tenantDetails.title')}
            </SheetTitle>
          </div>
          <SheetDescription className="text-xs text-muted-foreground">
            {t('tenantDetails.subtitle')}
          </SheetDescription>
        </SheetHeader>

        {/* Content Body */}
        <div className="flex-1 overflow-y-auto p-5 space-y-5">
          {isLoading ? (
            <TenantDetailSkeleton />
          ) : !tenant ? (
            <div className="py-12 text-center text-muted-foreground text-sm">
              {t('tenantDetails.fields.noSubscription')}
            </div>
          ) : (
            <>
              {/* Profile Header */}
              <TenantDetailHeader tenant={tenant} />

              {/* Contact & Owner */}
              <TenantContactCard tenant={tenant} />

              {/* Current Subscription */}
              <TenantSubscriptionCard
                subscription={tenant.currentSubscription}
              />

              {/* Team Members */}
              <TenantMembersCard members={tenant.members} />

              {/* Subscription History */}
              <TenantHistoryCard subscriptions={tenant.subscriptions} />
            </>
          )}
        </div>

        {/* Footer Actions */}
        {tenant && (
          <SheetFooter className="p-4 border-t shrink-0 bg-background/50 flex flex-row items-center justify-between gap-2">
            <Button
              variant={isActive ? 'destructive' : 'outline'}
              size="sm"
              onClick={() => toggleStatus(tenant)}
              disabled={isToggling}
              className={cn(
                'text-xs gap-1.5 cursor-pointer',
                !isActive &&
                  'text-emerald-600 border-emerald-600/40 hover:bg-emerald-50 dark:hover:bg-emerald-950/30 hover:text-emerald-700',
              )}
            >
              {isActive ? (
                <>
                  <BanIcon className="size-3.5" />
                  <span>{t('tenantsTable.actions.suspend')}</span>
                </>
              ) : (
                <>
                  <CheckCircle2Icon className="size-3.5" />
                  <span>{t('tenantsTable.actions.activate')}</span>
                </>
              )}
            </Button>

            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => onOpenChange(false)}
                className="text-xs cursor-pointer"
              >
                {t('tenantDetails.actions.close')}
              </Button>

              <Button
                variant="default"
                size="sm"
                onClick={() => window.open(`/t/${tenant.slug}`, '_blank')}
                className="text-xs gap-1.5 cursor-pointer"
              >
                <ExternalLinkIcon className="size-3.5" />
                <span>{t('tenantDetails.actions.visitPage')}</span>
              </Button>
            </div>
          </SheetFooter>
        )}
      </SheetContent>
    </Sheet>
  )
}
