import { useTranslation } from 'react-i18next'
import { MailIcon, PhoneIcon, UserCheckIcon } from 'lucide-react'
import type { TenantDetail } from '../../types/tenant-types'

interface TenantContactCardProps {
  tenant: TenantDetail
}

export function TenantContactCard({ tenant }: TenantContactCardProps) {
  const { t } = useTranslation('common')

  const owner = tenant.members?.find(
    (m) =>
      String(m.role).toLowerCase() === 'owner' || (m.role as unknown) === 1,
  )

  return (
    <div className="rounded-xl border bg-background/60 p-4 space-y-3 shadow-2xs">
      <div className="flex items-center gap-2 font-medium text-xs text-foreground border-b pb-2">
        <UserCheckIcon className="size-4 text-primary" />
        <span>{t('tenantDetails.sections.contactAndOwner')}</span>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
        {/* Owner */}
        {owner && (
          <div className="flex flex-col gap-0.5">
            <span className="text-[11px] text-muted-foreground">
              {t('tenantDetails.fields.ownerName')}
            </span>
            <span className="font-medium text-foreground">{owner.fullName}</span>
          </div>
        )}

        {/* Contact Email */}
        <div className="flex flex-col gap-0.5">
          <span className="text-[11px] text-muted-foreground">
            {t('tenantDetails.fields.contactEmail')}
          </span>
          <a
            href={`mailto:${tenant.contactEmail}`}
            className="inline-flex items-center gap-1 font-mono text-primary hover:underline"
          >
            <MailIcon className="size-3 shrink-0" />
            <span className="truncate">{tenant.contactEmail}</span>
          </a>
        </div>

        {/* Contact Phone */}
        <div className="flex flex-col gap-0.5">
          <span className="text-[11px] text-muted-foreground">
            {t('tenantDetails.fields.contactPhone')}
          </span>
          <a
            href={`tel:${tenant.contactPhone}`}
            className="inline-flex items-center gap-1 font-mono text-foreground hover:underline"
          >
            <PhoneIcon className="size-3 shrink-0 text-muted-foreground" />
            <span>{tenant.contactPhone}</span>
          </a>
        </div>

        {/* Public Slug */}
        <div className="flex flex-col gap-0.5">
          <span className="text-[11px] text-muted-foreground">
            {t('tenantDetails.fields.slug')}
          </span>
          <span className="font-mono text-muted-foreground">{tenant.slug}</span>
        </div>
      </div>
    </div>
  )
}
