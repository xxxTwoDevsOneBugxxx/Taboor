import * as React from 'react'
import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import type { AxiosError } from 'axios'
import { useTranslation } from 'react-i18next'
import { toast } from '@/components/ui/toast'
import { tenantsApi } from '../api/tenants-api'
import type {
  PagedTenantsResult,
  TenantDetail,
  TenantStatus,
  TenantsQueryParams,
} from '../types/tenant-types'

export const TENANTS_QUERY_KEY = ['tenants'] as const

interface OptimisticContext {
  previousListQueries: [readonly unknown[], PagedTenantsResult | undefined][]
  previousDetail: TenantDetail | undefined
  tenantId: string
}

export function useTenants(params?: TenantsQueryParams) {
  return useQuery({
    queryKey: [...TENANTS_QUERY_KEY, params],
    queryFn: () => tenantsApi.getTenants(params),
    placeholderData: keepPreviousData,
  })
}

export function useTenant(id: string) {
  return useQuery({
    queryKey: [...TENANTS_QUERY_KEY, id],
    queryFn: () => tenantsApi.getTenantById(id),
    enabled: Boolean(id),
    staleTime: 60 * 1000, // Keep fresh in cache for 1 minute
  })
}

/**
 * Hook providing smart prefetch capability for tenant details with cache preservation.
 */
export function usePrefetchTenant() {
  const queryClient = useQueryClient()

  return React.useCallback(
    (id: string) => {
      if (!id) return
      queryClient.prefetchQuery({
        queryKey: [...TENANTS_QUERY_KEY, id],
        queryFn: () => tenantsApi.getTenantById(id),
        staleTime: 60 * 1000,
      })
    },
    [queryClient],
  )
}

/**
 * Mutation hook to activate a suspended tenant organization with full Optimistic UI & rollback.
 */
export function useActivateTenant() {
  const queryClient = useQueryClient()
  const { t } = useTranslation('common')

  return useMutation<
    void,
    AxiosError<{ message?: string; errors?: string[] }>,
    string,
    OptimisticContext
  >({
    mutationFn: (id: string) => tenantsApi.activateTenant(id),
    onMutate: async (id: string) => {
      // 1. Cancel any outgoing refetches so they don't overwrite our optimistic update
      await queryClient.cancelQueries({ queryKey: TENANTS_QUERY_KEY })

      // 2. Snapshot previous list queries matching TENANTS_QUERY_KEY
      const previousListQueries = queryClient.getQueriesData<PagedTenantsResult>({
        queryKey: TENANTS_QUERY_KEY,
      })

      // 3. Snapshot previous detail query for this tenant
      const previousDetail = queryClient.getQueryData<TenantDetail>([
        ...TENANTS_QUERY_KEY,
        id,
      ])

      // 4. Optimistically update list queries
      queryClient.setQueriesData<PagedTenantsResult>(
        { queryKey: TENANTS_QUERY_KEY },
        (old) => {
          if (!old || !Array.isArray(old.items)) return old
          return {
            ...old,
            items: old.items.map((item) =>
              item.id === id
                ? { ...item, status: 'Active' as TenantStatus }
                : item,
            ),
          }
        },
      )

      // 5. Optimistically update single detail query
      queryClient.setQueryData<TenantDetail>(
        [...TENANTS_QUERY_KEY, id],
        (old) => (old ? { ...old, status: 'Active' as TenantStatus } : old),
      )

      return { previousListQueries, previousDetail, tenantId: id }
    },
    onError: (error, _id, context) => {
      // Rollback to snapshots
      if (context?.previousListQueries) {
        context.previousListQueries.forEach(([key, data]) => {
          queryClient.setQueryData(key, data)
        })
      }
      if (context?.previousDetail) {
        queryClient.setQueryData(
          [...TENANTS_QUERY_KEY, context.tenantId],
          context.previousDetail,
        )
      }

      toast.add({
        title: t('tenantsTable.toasts.statusError'),
        description:
          error.response?.data?.errors?.[0] ??
          error.response?.data?.message ??
          undefined,
        type: 'error',
      })
    },
    onSuccess: () => {
      toast.add({
        title: t('tenantsTable.toasts.activatedSuccess'),
        type: 'success',
      })
    },
    onSettled: () => {
      // Re-sync with server state
      queryClient.invalidateQueries({ queryKey: TENANTS_QUERY_KEY })
    },
  })
}

/**
 * Mutation hook to administratively suspend an active tenant organization with full Optimistic UI & rollback.
 */
export function useSuspendTenant() {
  const queryClient = useQueryClient()
  const { t } = useTranslation('common')

  return useMutation<
    void,
    AxiosError<{ message?: string; errors?: string[] }>,
    { id: string; reason?: string },
    OptimisticContext
  >({
    mutationFn: ({ id, reason }: { id: string; reason?: string }) =>
      tenantsApi.suspendTenant(id, reason),
    onMutate: async ({ id }) => {
      // 1. Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: TENANTS_QUERY_KEY })

      // 2. Snapshot previous list queries
      const previousListQueries = queryClient.getQueriesData<PagedTenantsResult>({
        queryKey: TENANTS_QUERY_KEY,
      })

      // 3. Snapshot previous detail query
      const previousDetail = queryClient.getQueryData<TenantDetail>([
        ...TENANTS_QUERY_KEY,
        id,
      ])

      // 4. Optimistically update list queries
      queryClient.setQueriesData<PagedTenantsResult>(
        { queryKey: TENANTS_QUERY_KEY },
        (old) => {
          if (!old || !Array.isArray(old.items)) return old
          return {
            ...old,
            items: old.items.map((item) =>
              item.id === id
                ? { ...item, status: 'Suspended' as TenantStatus }
                : item,
            ),
          }
        },
      )

      // 5. Optimistically update single detail query
      queryClient.setQueryData<TenantDetail>(
        [...TENANTS_QUERY_KEY, id],
        (old) => (old ? { ...old, status: 'Suspended' as TenantStatus } : old),
      )

      return { previousListQueries, previousDetail, tenantId: id }
    },
    onError: (error, _vars, context) => {
      // Rollback to snapshots
      if (context?.previousListQueries) {
        context.previousListQueries.forEach(([key, data]) => {
          queryClient.setQueryData(key, data)
        })
      }
      if (context?.previousDetail) {
        queryClient.setQueryData(
          [...TENANTS_QUERY_KEY, context.tenantId],
          context.previousDetail,
        )
      }

      toast.add({
        title: t('tenantsTable.toasts.statusError'),
        description:
          error.response?.data?.errors?.[0] ??
          error.response?.data?.message ??
          undefined,
        type: 'error',
      })
    },
    onSuccess: () => {
      toast.add({
        title: t('tenantsTable.toasts.suspendedSuccess'),
        type: 'success',
      })
    },
    onSettled: () => {
      // Re-sync with server state
      queryClient.invalidateQueries({ queryKey: TENANTS_QUERY_KEY })
    },
  })
}

/**
 * Convenience hook that toggles a tenant between Active and Suspended states
 * with instant 0ms optimistic UI update, rollback on error, and toast feedback.
 */
export function useToggleTenantStatus() {
  const activateMutation = useActivateTenant()
  const suspendMutation = useSuspendTenant()

  const isToggling = activateMutation.isPending || suspendMutation.isPending

  const toggleStatus = (
    tenant: {
      id: string
      status: TenantStatus | string | number
      name?: string
    },
    reason?: string,
  ) => {
    const isActive =
      tenant.status === 'Active' || (tenant.status as unknown) === 1

    if (isActive) {
      suspendMutation.mutate({ id: tenant.id, reason })
    } else {
      activateMutation.mutate(tenant.id)
    }
  }

  return { toggleStatus, isToggling }
}
