import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import type { UserRole } from '@/types'

export function useAuth() {
  const authStore = useAuthStore()
  const router = useRouter()

  const { token, refreshToken, user, isLoggedIn, isAdmin, isMerchant, userRole } =
    storeToRefs(authStore)

  function requireAuth(): boolean {
    if (!authStore.isLoggedIn) {
      router.push('/login')
      return false
    }
    return true
  }

  function hasRole(role: UserRole): boolean {
    return authStore.userRole === role
  }

  return {
    token,
    refreshToken,
    user,
    isLoggedIn,
    isAdmin,
    isMerchant,
    userRole,
    login: authStore.login,
    register: authStore.register,
    logout: authStore.logout,
    refreshAuth: authStore.refreshAuth,
    loadFromStorage: authStore.loadFromStorage,
    requireAuth,
    hasRole,
  }
}
