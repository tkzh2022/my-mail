import { defineStore } from 'pinia'
import { post } from '@/services/http'
import type { ApiResponse, UserRole } from '@/types'

const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refresh_token'
const USER_KEY = 'user'

export interface AuthUser {
  id: number
  username: string
  role: UserRole
  avatar?: string
}

export interface LoginCredentials {
  account: string
  password: string
  loginType?: 'password' | 'sms_code'
}

export interface RegisterData {
  username: string
  email: string
  phone: string
  password: string
  verificationCode: string
}

interface LoginResponseData {
  user_id: number
  username: string
  role: UserRole
  token: string
  refresh_token: string
  expires_in?: number
}

interface RegisterResponseData {
  user_id: number
  username: string
  token: string
  refresh_token: string
}

interface RefreshResponseData {
  token: string
  refresh_token: string
}

function parseStoredUser(raw: string | null): AuthUser | null {
  if (!raw) return null
  try {
    return JSON.parse(raw) as AuthUser
  } catch {
    return null
  }
}

function persistAuth(
  token: string | null,
  refreshToken: string | null,
  user: AuthUser | null
): void {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }

  if (refreshToken) {
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  } else {
    localStorage.removeItem(REFRESH_TOKEN_KEY)
  }

  if (user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  } else {
    localStorage.removeItem(USER_KEY)
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY),
    refreshToken: localStorage.getItem(REFRESH_TOKEN_KEY),
    user: parseStoredUser(localStorage.getItem(USER_KEY)),
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.user?.role === 'admin',
    isMerchant: (state) => state.user?.role === 'merchant',
    userRole: (state) => state.user?.role ?? null,
  },

  actions: {
    loadFromStorage() {
      this.token = localStorage.getItem(TOKEN_KEY)
      this.refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)
      this.user = parseStoredUser(localStorage.getItem(USER_KEY))
    },

    setSession(
      token: string,
      refreshToken: string,
      user: AuthUser
    ): void {
      this.token = token
      this.refreshToken = refreshToken
      this.user = user
      persistAuth(token, refreshToken, user)
    },

    clearSession(): void {
      this.token = null
      this.refreshToken = null
      this.user = null
      persistAuth(null, null, null)
    },

    async login(credentials: LoginCredentials): Promise<void> {
      const res = await post<ApiResponse<LoginResponseData>>('/auth/login', {
        account: credentials.account,
        password: credentials.password,
        login_type: credentials.loginType ?? 'password',
      })

      this.setSession(res.data.token, res.data.refresh_token, {
        id: res.data.user_id,
        username: res.data.username,
        role: res.data.role,
      })
    },

    async register(data: RegisterData): Promise<void> {
      const res = await post<ApiResponse<RegisterResponseData>>(
        '/auth/register',
        {
          username: data.username,
          email: data.email,
          phone: data.phone,
          password: data.password,
          verification_code: data.verificationCode,
        }
      )

      this.setSession(res.data.token, res.data.refresh_token, {
        id: res.data.user_id,
        username: res.data.username,
        role: 'user',
      })
    },

    async logout(): Promise<void> {
      try {
        if (this.token) {
          await post<ApiResponse<null>>('/auth/logout')
        }
      } finally {
        this.clearSession()
      }
    },

    async refreshAuth(): Promise<void> {
      if (!this.refreshToken) return

      const res = await post<ApiResponse<RefreshResponseData>>(
        '/auth/refresh',
        {
          refresh_token: this.refreshToken,
        }
      )

      this.token = res.data.token
      this.refreshToken = res.data.refresh_token
      persistAuth(this.token, this.refreshToken, this.user)
    },
  },
})
