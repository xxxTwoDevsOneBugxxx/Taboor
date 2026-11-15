import { keepPreviousData, useQuery } from '@tanstack/react-query'
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
  })
}
