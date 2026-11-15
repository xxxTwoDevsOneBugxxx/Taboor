import type { PaginationMetadata } from '#/types/api'

export type TenantStatus = 'Active' | 'Suspended'

export type SubscriptionStatus =
  | 'Trialing'
  | 'Active'
  | 'PastDue'
  | 'Canceled'
  | 'Expired'

export type BillingCycle = 'Monthly' | 'Annually' | 'Quarterly' | 1 | 2 | 3

export interface TenantListItem {
  id: string
  name: string
  slug: string
  logoUrl?: string | null
  contactEmail: string
  contactPhone: string
  status: TenantStatus
  createdAt: string
  ownerName?: string | null
  ownerEmail?: string | null
  ownerPhone?: string | null
  planId?: number | null
  planName?: string | null
  planPrice?: number | null
  billingCycle?: BillingCycle | null
  subscriptionStatus?: SubscriptionStatus | null
  subscriptionStartDate?: string | null
  subscriptionEndDate?: string | null
  autoRenew?: boolean | null
}

export interface TenantSubscription {
  id: string
  planId: number
  planName: string
  price: number
  billingCycle: BillingCycle
  status: SubscriptionStatus
  startDate: string
  endDate: string
  trialEndsAt?: string | null
  autoRenew: boolean
  externalSubscriptionId?: string | null
  canceledAt?: string | null
  cancellationReason?: string | null
}

export interface TenantMember {
  id: string
  userId: string
  fullName: string
  email: string
  phoneNumber: string
  role: 'Owner' | 'Admin' | 'Staff' | number
  jobTitle?: string | null
  isActive: boolean
  joinedAt: string
}

export interface TenantDetail {
  id: string
  name: string
  slug: string
  logoUrl?: string | null
  contactEmail: string
  contactPhone: string
  status: TenantStatus
  createdAt: string
  updatedAt?: string | null
  currentSubscription?: TenantSubscription | null
  subscriptions: TenantSubscription[]
  members: TenantMember[]
}

export interface TenantsQueryParams {
  pageNumber?: number
  pageSize?: number
  search?: string
  status?: TenantStatus
  subStatus?: SubscriptionStatus
  planId?: number
  sortBy?: string
  sortDescending?: boolean
}

export interface PagedTenantsResult {
  items: TenantListItem[]
  pagination: PaginationMetadata
}
