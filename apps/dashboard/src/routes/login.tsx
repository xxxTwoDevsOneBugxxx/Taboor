import { createFileRoute, redirect } from '@tanstack/react-router'

import BrandPanel from '#/feature/auth/Components/BrandPanel'
import LoginForm from '#/feature/auth/Components/LoginForm'
import { useAuthStore } from '#/feature/auth/auth-store'
import { ensureAuthInitialized } from '#/lib/client'
import { isPlatformAdmin, isEndUser } from '#/lib/isPlatformAdmin'

export const Route = createFileRoute('/login')({
  beforeLoad: async () => {
    await ensureAuthInitialized()

    const { isAuthenticated, user } = useAuthStore.getState()

    if (isAuthenticated) {
      if (isEndUser(user)) {
        useAuthStore.getState().clearAuth()
        return
      }
      throw redirect({
        to: isPlatformAdmin(user) ? '/plans' : '/dashboard',
      })
    }
  },

  component: LoginPage,
})

function LoginPage() {
  return (
    <div className="flex h-dvh overflow-hidden">
      <BrandPanel />
      <LoginForm />
    </div>
  )
}
