import { apiClient } from '#/lib/client'
import type { ApiResponse } from '#/types/api'
import type {
  PagedTenantsResult,
  TenantDetail,
  TenantListItem,
  TenantsQueryParams,
} from '../types/tenant-types'

export const tenantsApi = {
  getTenants: async (
    params?: TenantsQueryParams,
  ): Promise<PagedTenantsResult> => {
    const response = await apiClient.get<ApiResponse<TenantListItem[]>>(
      '/Tenants',
      {
        params,
      },
    )

    return {
      items: response.data.data ?? [],
      pagination: response.data.pagination ?? {
        pageNumber: params?.pageNumber ?? 1,
        pageSize: params?.pageSize ?? 10,
        totalCount: response.data.data?.length ?? 0,
        totalPages: 1,
        hasPreviousPage: false,
        hasNextPage: false,
      },
    }
  },

  getTenantById: async (id: string): Promise<TenantDetail> => {
    const response = await apiClient.get<ApiResponse<TenantDetail>>(
      `/Tenants/${id}`,
    )
    return response.data.data
  },
}
