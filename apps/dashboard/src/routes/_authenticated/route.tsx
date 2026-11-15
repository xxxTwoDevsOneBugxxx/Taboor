import { createFileRoute, redirect, useLocation } from '@tanstack/react-router'
import { useAuthStore } from '#/feature/auth/auth-store'
import { ensureAuthInitialized } from '#/lib/client'
import { DashboardShell } from '#/components/layout/DashboardShell'
import {
  tenantNavConfig,
  platformNavConfig,
  PLATFORM_PATHS,
} from '#/components/layout/sidebarConfig'
import { isPlatformAdmin, isEndUser } from '#/lib/isPlatformAdmin'

export const Route = createFileRoute('/_authenticated')({
  beforeLoad: async ({ location }) => {
    await ensureAuthInitialized()

    const { isAuthenticated, user } = useAuthStore.getState()

    if (!isAuthenticated) {
      throw redirect({
        to: '/login',
      })
    }

    // End-user mobile accounts ('Customer' / 'User') are not permitted on the web dashboard
    if (isEndUser(user)) {
      useAuthStore.getState().clearAuth()
      throw redirect({ to: '/login' })
    }

    const isPlatformRoute = PLATFORM_PATHS.some(
      (p) => location.pathname === p || location.pathname.startsWith(`${p}/`),
    )
    const isTenantRoute = location.pathname.startsWith('/dashboard')
    const isAdmin = isPlatformAdmin(user)

    // Strict isolation: Admin ONLY on platform layout, Customer ONLY on tenant layout
    if (isAdmin && isTenantRoute) {
      throw redirect({ to: '/plans' })
    }
    if (!isAdmin && isPlatformRoute) {
      // At this point isAdmin==false means Customer (User already handled above)
      throw redirect({ to: '/dashboard' })
    }
  },

  component: AuthenticatedLayout,
})

function AuthenticatedLayout() {
  const pathname = useLocation({
    select: (location) => location.pathname,
  })

  const isPlatformRoute = PLATFORM_PATHS.some(
    (p) => pathname === p || pathname.startsWith(`${p}/`),
  )

  const navConfig = isPlatformRoute ? platformNavConfig : tenantNavConfig

  return <DashboardShell navConfig={navConfig} />
}
