export type UserRole =
  | 'platformAdmin'
  | 'PlatformAdmin'
  | 'Admin'
  | 'Business'
  | 'Customer'
  | 'User'
  | 'Taboor Admin'
  | 'Taboor Customer'
  | 'Taboor User'
  | (string & {})

export interface AppUser {
  id: string
  email: string
  fullName: string
  role: UserRole
}

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  token: string
  refreshToken?: string | null
  user: AppUser
}

export interface RefreshResponse {
  accessToken: string
  user: AppUser
}

export type { ApiResponse } from '#/types/api'
