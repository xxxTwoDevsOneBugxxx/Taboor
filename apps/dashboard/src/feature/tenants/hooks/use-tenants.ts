import * as React from 'react'
import {
  keepPreviousData,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import { tenantsApi } from '../api/tenants-api'
import type { TenantsQueryParams } from '../types/tenant-types'

export const TENANTS_QUERY_KEY = ['tenants'] as const

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
