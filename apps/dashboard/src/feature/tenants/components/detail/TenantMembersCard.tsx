import { useTranslation } from 'react-i18next'
import { UsersIcon } from 'lucide-react'
import type { TenantMember } from '../../types/tenant-types'
import {
  getInitials,
  getRoleBadgeClass,
  getRoleLabel,
} from './tenant-detail-helpers'
import { cn } from '@/lib/utils'

interface TenantMembersCardProps {
  members?: TenantMember[]
}

export function TenantMembersCard({ members }: TenantMembersCardProps) {
  const { t } = useTranslation('common')

  return (
    <div className="rounded-xl border bg-background/60 p-4 space-y-3 shadow-2xs">
      <div className="flex items-center justify-between border-b pb-2">
        <div className="flex items-center gap-2 font-medium text-xs text-foreground">
          <UsersIcon className="size-4 text-primary" />
          <span>{t('tenantDetails.sections.teamMembers')}</span>
        </div>
        <span className="text-[11px] rounded-full bg-secondary text-secondary-foreground font-semibold px-2 py-0.5">
          {members?.length ?? 0}
        </span>
      </div>

      {members && members.length > 0 ? (
        <div className="divide-y divide-border/60">
          {members.map((member) => (
            <div
              key={member.id}
              className="flex items-center justify-between py-2.5 first:pt-0 last:pb-0 gap-2"
            >
              <div className="flex items-center gap-2.5 min-w-0">
                <div className="flex size-7 shrink-0 items-center justify-center rounded-full bg-secondary/80 text-secondary-foreground font-semibold text-[10px]">
                  {getInitials(member.fullName)}
                </div>
                <div className="flex flex-col min-w-0">
                  <span className="font-medium text-foreground text-xs truncate">
                    {member.fullName}
                  </span>
                  <span className="text-[11px] text-muted-foreground font-mono truncate">
                    {member.email}
                  </span>
                </div>
              </div>

              <div className="flex items-center gap-1.5 shrink-0">
                <span
                  className={cn(
                    'rounded-md px-2 py-0.5 text-[10px] font-medium border',
                    getRoleBadgeClass(member.role),
                  )}
                >
                  {getRoleLabel(member.role, t)}
                </span>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <p className="text-xs text-muted-foreground py-2">
          {t('tenantDetails.fields.noMembers')}
        </p>
      )}
    </div>
  )
}
