import * as React from 'react'
import { useTranslation } from 'react-i18next'
import type { SortingState } from '@tanstack/react-table'
import {
  CheckCircle2Icon,
  ClockIcon,
  AlertTriangleIcon,
  XCircleIcon,
  BanIcon,
} from 'lucide-react'
import {
  DataTable,
  DataTableFacetedFilter,
  type FilterOption,
} from '@/components/common/data-table'
import { getTenantsColumns } from './tenants-columns'
import { TenantDetailSheet } from './TenantDetailSheet'
import { TenantMobileCard } from './TenantMobileCard'
import { usePrefetchTenant, useTenants } from '../hooks/use-tenants'
import type {
  SubscriptionStatus,
  TenantListItem,
  TenantStatus,
} from '../types/tenant-types'
import { useDebounce } from '#/hooks/use-debounce'

export function TenantsTable() {
  const { t, i18n } = useTranslation('common')

  // Query, Filter & Sort States
  const [pageNumber, setPageNumber] = React.useState(1)
  const [pageSize, setPageSize] = React.useState(10)
  const [searchTerm, setSearchTerm] = React.useState('')
  const [statusFilter, setStatusFilter] = React.useState<TenantStatus | ''>('')
  const [subStatusFilter, setSubStatusFilter] = React.useState<
    SubscriptionStatus | ''
  >('')
  const [sorting, setSorting] = React.useState<SortingState>([])

  // Detail Sheet State
  const [selectedTenantId, setSelectedTenantId] = React.useState<string | null>(
    null,
  )
  const [isDetailOpen, setIsDetailOpen] = React.useState(false)

  // Debounce search input to prevent server query spam (300ms sweet spot)
  const debouncedSearch = useDebounce(searchTerm, 300)

  // Reset page to 1 when filters, search, or sorting changes
  React.useEffect(() => {
    setPageNumber(1)
  }, [debouncedSearch, statusFilter, subStatusFilter, sorting])

  // Extract sorting parameters for API
  const currentSort = sorting[0]
  const sortBy = currentSort ? currentSort.id : undefined
  const sortDescending = currentSort ? currentSort.desc : undefined

  // Fetch data via TanStack Query (with keepPreviousData enabled)
  const { data, isLoading } = useTenants({
    pageNumber,
    pageSize,
    search: debouncedSearch ? debouncedSearch.trim() : undefined,
    status: statusFilter ? statusFilter : undefined,
    subStatus: subStatusFilter ? subStatusFilter : undefined,
    sortBy,
    sortDescending,
  })

  const hasActiveFilters = Boolean(
    searchTerm || statusFilter || subStatusFilter || sorting.length > 0,
  )

  const handleResetFilters = () => {
    setSearchTerm('')
    setStatusFilter('')
    setSubStatusFilter('')
    setSorting([])
    setPageNumber(1)
  }

  const prefetchTenant = usePrefetchTenant()

  const handleOpenDetails = React.useCallback((tenant: TenantListItem) => {
    setSelectedTenantId(tenant.id)
    setIsDetailOpen(true)
  }, [])

  const columns = React.useMemo(
    () =>
      getTenantsColumns(t, i18n, {
        onViewDetails: handleOpenDetails,
        onHoverDetails: (tenant) => prefetchTenant(tenant.id),
        onToggleStatus: (tenant) => {
          console.log('Toggle status for:', tenant.id)
        },
        onManageSubscription: (tenant) => {
          console.log('Manage subscription for:', tenant.id)
        },
      }),
    [t, i18n, handleOpenDetails, prefetchTenant],
  )

  const subscriptionOptions: FilterOption[] = React.useMemo(
    () => [
      {
        value: 'Active',
        label: t('tenantsTable.subStatus.active'),
        icon: CheckCircle2Icon,
        dotClassName: 'bg-emerald-500 animate-pulse',
        iconClassName: 'text-emerald-500 dark:text-emerald-400',
        itemClassName: 'text-emerald-700 dark:text-emerald-400 font-medium',
        badgeClassName:
          'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400 border-emerald-200/50 dark:border-emerald-800/40',
      },
      {
        value: 'Trialing',
        label: t('tenantsTable.subStatus.trialing'),
        icon: ClockIcon,
        iconClassName: 'text-blue-500 dark:text-blue-400',
        itemClassName: 'text-blue-700 dark:text-blue-400 font-medium',
        badgeClassName:
          'bg-blue-50 text-blue-700 dark:bg-blue-950/40 dark:text-blue-400 border-blue-200/50 dark:border-blue-800/40',
      },
      {
        value: 'PastDue',
        label: t('tenantsTable.subStatus.pastDue'),
        icon: AlertTriangleIcon,
        iconClassName: 'text-amber-500 dark:text-amber-400',
        itemClassName: 'text-amber-700 dark:text-amber-400 font-medium',
        badgeClassName:
          'bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-400 border-amber-200/50 dark:border-amber-800/40',
      },
      {
        value: 'Canceled',
        label: t('tenantsTable.subStatus.canceled'),
        icon: XCircleIcon,
        iconClassName: 'text-rose-500 dark:text-rose-400',
        itemClassName: 'text-rose-700 dark:text-rose-400 font-medium',
        badgeClassName:
          'bg-rose-50 text-rose-700 dark:bg-rose-950/40 dark:text-rose-400 border-rose-200/50 dark:border-rose-800/40',
      },
      {
        value: 'Expired',
        label: t('tenantsTable.subStatus.expired'),
        icon: BanIcon,
        iconClassName: 'text-muted-foreground',
        itemClassName: 'text-muted-foreground font-medium',
        badgeClassName:
          'bg-muted text-muted-foreground border-border/60',
      },
    ],
    [t],
  )

  const tenantStatusOptions: FilterOption[] = React.useMemo(
    () => [
      {
        value: 'Active',
        label: t('tenantsTable.tenantStatus.active'),
        icon: CheckCircle2Icon,
        iconClassName: 'text-emerald-600 dark:text-emerald-400',
        itemClassName: 'text-emerald-700 dark:text-emerald-400 font-medium',
        badgeClassName:
          'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400 border-emerald-200/50 dark:border-emerald-800/40',
      },
      {
        value: 'Suspended',
        label: t('tenantsTable.tenantStatus.suspended'),
        icon: BanIcon,
        iconClassName: 'text-rose-600 dark:text-rose-400',
        itemClassName: 'text-rose-700 dark:text-rose-400 font-medium',
        badgeClassName:
          'bg-rose-50 text-rose-700 dark:bg-rose-950/40 dark:text-rose-400 border-rose-200/50 dark:border-rose-800/40',
      },
    ],
    [t],
  )

  return (
    <>
      <DataTable<TenantListItem, unknown>
        columns={columns}
        data={data?.items ?? []}
        isLoading={isLoading}
        onRowClick={handleOpenDetails}
        onRowHover={(tenant) => prefetchTenant(tenant.id)}
        hoverIntentDelay={120}
        renderMobileItem={(tenant) => (
          <TenantMobileCard
            tenant={tenant}
            onViewDetails={handleOpenDetails}
            onToggleStatus={(t) => {
              console.log('Toggle status for:', t.id)
            }}
            onManageSubscription={(t) => {
              console.log('Manage subscription for:', t.id)
            }}
          />
        )}
        serverPagination={{
          pageNumber,
          pageSize,
          totalCount: data?.pagination?.totalCount ?? 0,
          totalPages: data?.pagination?.totalPages ?? 1,
          onPageChange: (newPage) => setPageNumber(newPage),
          onPageSizeChange: (newSize) => {
            setPageSize(newSize)
            setPageNumber(1)
          },
        }}
        serverSorting={{
          sorting,
          onSortingChange: setSorting,
        }}
        searchConfig={{
          value: searchTerm,
          onChange: setSearchTerm,
          placeholder: t('tenantsTable.filters.searchPlaceholder'),
        }}
        filterSlot={
          <div className="flex items-center gap-2 flex-wrap">
            <DataTableFacetedFilter
              title={t('tenantsTable.filters.subscriptionStatus')}
              value={subStatusFilter}
              onChange={(val) =>
                setSubStatusFilter(val as SubscriptionStatus | '')
              }
              options={subscriptionOptions}
              clearLabel={t('tenantsTable.filters.clearFilter')}
            />

            <DataTableFacetedFilter
              title={t('tenantsTable.filters.tenantStatus')}
              value={statusFilter}
              onChange={(val) => setStatusFilter(val as TenantStatus | '')}
              options={tenantStatusOptions}
              clearLabel={t('tenantsTable.filters.clearFilter')}
            />
          </div>
        }
        onResetFilters={handleResetFilters}
        hasActiveFilters={hasActiveFilters}
      />

      {/* Tenant Details Slide-over Sheet */}
      <TenantDetailSheet
        tenantId={selectedTenantId}
        open={isDetailOpen}
        onOpenChange={setIsDetailOpen}
      />
    </>
  )
}
