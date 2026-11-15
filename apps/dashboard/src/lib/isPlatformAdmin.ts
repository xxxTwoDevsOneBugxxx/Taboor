import type { AppUser } from '#/feature/auth/auth-types'

export function isPlatformAdmin(user: AppUser | null): boolean {
  if (!user?.role) return false
  const normalized = user.role.toLowerCase()
  return normalized === 'platformadmin' || normalized === 'admin'
}

export function isBusinessTenant(user: AppUser | null): boolean {
  if (!user?.role) return false
  const normalized = user.role.toLowerCase()
  return normalized === 'business'
}

// End-user mobile customer who should not access the dashboard
export function isEndUser(user: AppUser | null): boolean {
  if (!user?.role) return false
  const normalized = user.role.toLowerCase()
  return normalized === 'customer' || normalized === 'user'
}

export function isAllowedForDashboard(user: AppUser | null): boolean {
  return isPlatformAdmin(user) || isBusinessTenant(user)
}

// Aliases for backward-compatibility with existing usages
export const isCustomer = isBusinessTenant
export const isUser = isEndUser

