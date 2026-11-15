import { Logo } from '#/components/brand/logo'
import { Button } from '#/components/ui/button'
import {
  Field,
  FieldError,
  FieldGroup,
  FieldLabel,
} from '#/components/ui/field'
import { Input } from '#/components/ui/input'
import { useNavigate } from '@tanstack/react-router'
import { Eye, EyeOff } from 'lucide-react'
import { useMemo, useState } from 'react'
import { useLogin } from '../hooks/use-login'
import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { createLoginSchema } from '../auth-schema'
import type { LoginFormValues } from '../auth-schema'
import { Spinner } from '#/components/ui/spinner'
import { useTranslation } from 'react-i18next'
import { isPlatformAdmin, isEndUser } from '#/lib/isPlatformAdmin'

export default function LoginForm() {
  const navigate = useNavigate()
  const login = useLogin()
  const [showPassword, setShowPassword] = useState(false)
  const [notAuthorized, setNotAuthorized] = useState(false)
  const { t } = useTranslation('auth')

  const resolver = useMemo(() => zodResolver(createLoginSchema(t)), [t])

  const form = useForm({
    resolver,
    defaultValues: {
      email: '',
      password: '',
    },
  })

  const onSubmit = (values: LoginFormValues) => {
    setNotAuthorized(false)
    login.mutate(values, {
      onSuccess: async (data) => {
        // Mobile end-user accounts ('Customer' / 'User') are not permitted on the web dashboard
        if (isEndUser(data.user)) {
          setNotAuthorized(true)
          // revoke the just-issued refresh cookie so session doesn't persist
          const { useAuthStore } = await import('../auth-store')
          const { apiClient } = await import('#/lib/client')
          useAuthStore.getState().clearAuth()
          try {
            await apiClient.post('/Auth/logout')
          } catch {
            // ignore logout failure — we already cleared local auth
          }
          return
        }

        if (isPlatformAdmin(data.user)) {
          navigate({ to: '/plans' })
        } else {
          // Business tenant
          navigate({ to: '/dashboard' })
        }
      },
    })
  }

  return (
    <main className="flex flex-1 items-center justify-center bg-paper px-6 py-8 sm:px-12">
      <div className="w-full max-w-lg">
        <div className="mb-6 lg:hidden">
          <Logo size="sm" />
        </div>

        <span className="text-xs font-semibold tracking-widest text-teal uppercase">
          {t('brand.eyebrow')}
        </span>
        <h1 className="mt-3 text-3xl font-black tracking-tight text-ink sm:text-4xl">
          {t('brand.title')}
        </h1>
        <p className="mt-2 text-sm text-muted-foreground">
          {t('brand.subtitle')}
        </p>

        <form className="mt-7 space-y-4" onSubmit={form.handleSubmit(onSubmit)}>
          <FieldGroup>
            <Controller
              name="email"
              control={form.control}
              render={({ field, fieldState }) => (
                <Field data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor={field.name}>
                    {t('form.email')}
                  </FieldLabel>
                  <Input
                    {...field}
                    id={field.name}
                    autoComplete="email"
                    placeholder={t('form.emailPlaceholder')}
                    className="h-11 text-base"
                    aria-invalid={fieldState.invalid}
                  />
                  {fieldState.invalid && (
                    <FieldError errors={[fieldState.error]} />
                  )}
                </Field>
              )}
            />

            <Controller
              name="password"
              control={form.control}
              render={({ field, fieldState }) => (
                <Field data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor={field.name}>
                    {t('form.password')}
                  </FieldLabel>
                  <div className="relative">
                    <Input
                      {...field}
                      id={field.name}
                      type={showPassword ? 'text' : 'password'}
                      autoComplete="current-password"
                      placeholder={t('form.passwordPlaceholder')}
                      className="h-11 pe-11 text-base"
                      aria-invalid={fieldState.invalid}
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword((v) => !v)}
                      className="absolute top-1/2 end-3 -translate-y-1/2 rounded-md p-1 text-muted-foreground transition-colors hover:text-ink focus-visible:ring-3 focus-visible:ring-ring/50 focus-visible:outline-none"
                      aria-label={
                        showPassword
                          ? t('form.hidePassword')
                          : t('form.showPassword')
                      }
                    >
                      {showPassword ? (
                        <EyeOff className="size-4" />
                      ) : (
                        <Eye className="size-4" />
                      )}
                    </button>
                  </div>
                  {fieldState.invalid && (
                    <FieldError errors={[fieldState.error]} />
                  )}
                </Field>
              )}
            />
          </FieldGroup>

          <div className="flex items-center justify-between">
            <label className="flex cursor-pointer items-center gap-2 text-sm text-muted-foreground select-none">
              <input
                type="checkbox"
                name="remember"
                className="size-4 rounded border-input accent-teal"
              />
              {t('form.rememberMe')}
            </label>
            <a
              href="#"
              className="text-sm font-medium text-teal transition-colors hover:text-ink"
            >
              {t('form.forgotPassword')}
            </a>
          </div>

          <Button
            type="submit"
            className="h-12 w-full text-base"
            disabled={login.isPending}
          >
            {login.isPending ? (
              <>
                {t('form.submitting')}
                <Spinner />
              </>
            ) : (
              t('form.submit')
            )}
          </Button>

          {notAuthorized && (
            <p className="text-sm text-destructive" role="alert">
              {t('form.notAuthorized')}
            </p>
          )}

          {login.isError && !notAuthorized && (
            <p className="text-sm text-destructive" role="alert">
              {t('form.invalidCredentials')}
            </p>
          )}
        </form>
      </div>
    </main>
  )
}
