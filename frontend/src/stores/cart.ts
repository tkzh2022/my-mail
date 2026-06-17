import { defineStore } from 'pinia'
import { del, get, post, put } from '@/services/http'
import type { ApiResponse, CartItem } from '@/types'

interface CartItemResponse {
  id: number
  product_id: number
  product_name: string
  product_image: string
  price: number
  quantity: number
  selected: boolean
  available: boolean
}

function mapCartItem(item: CartItemResponse): CartItem {
  return {
    id: item.id,
    productId: item.product_id,
    productName: item.product_name,
    productImage: item.product_image,
    price: item.price,
    quantity: item.quantity,
    selected: item.selected,
    available: item.available,
  }
}

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [] as CartItem[],
    loading: false,
  }),

  getters: {
    totalItems: (state) =>
      state.items.reduce((sum, item) => sum + item.quantity, 0),

    totalPrice: (state) =>
      state.items
        .filter((item) => item.selected && item.available)
        .reduce((sum, item) => sum + item.price * item.quantity, 0),

    selectedItems: (state) =>
      state.items.filter((item) => item.selected && item.available),
  },

  actions: {
    async fetchCart(): Promise<void> {
      this.loading = true
      try {
        const res = await get<ApiResponse<CartItemResponse[]>>('/cart/items')
        this.items = res.data.map(mapCartItem)
      } finally {
        this.loading = false
      }
    },

    async addItem(productId: number, quantity: number): Promise<void> {
      const existing = this.items.find((item) => item.productId === productId)

      if (existing) {
        const previousQuantity = existing.quantity
        existing.quantity += quantity
        try {
          await put(`/cart/items/${existing.id}`, {
            quantity: existing.quantity,
          })
        } catch (error) {
          existing.quantity = previousQuantity
          throw error
        }
        return
      }

      try {
        const res = await post<ApiResponse<CartItemResponse>>('/cart/items', {
          product_id: productId,
          quantity,
        })
        this.items.push(mapCartItem(res.data))
      } catch (error) {
        throw error
      }
    },

    async removeItem(id: number): Promise<void> {
      const index = this.items.findIndex((item) => item.id === id)
      if (index === -1) return

      const removed = this.items[index]
      this.items.splice(index, 1)

      try {
        await del(`/cart/items/${id}`)
      } catch (error) {
        this.items.splice(index, 0, removed)
        throw error
      }
    },

    async updateQuantity(id: number, qty: number): Promise<void> {
      const item = this.items.find((i) => i.id === id)
      if (!item) return

      const previousQuantity = item.quantity
      item.quantity = qty

      try {
        await put(`/cart/items/${id}`, { quantity: qty })
      } catch (error) {
        item.quantity = previousQuantity
        throw error
      }
    },

    async toggleSelect(id: number): Promise<void> {
      const item = this.items.find((i) => i.id === id)
      if (!item) return

      const previousSelected = item.selected
      item.selected = !item.selected

      try {
        await put(`/cart/items/${id}`, { selected: item.selected })
      } catch (error) {
        item.selected = previousSelected
        throw error
      }
    },

    async selectAll(selected: boolean): Promise<void> {
      const previous = this.items.map((item) => ({
        id: item.id,
        selected: item.selected,
      }))

      this.items.forEach((item) => {
        if (item.available) {
          item.selected = selected
        }
      })

      try {
        await put('/cart/items/select-all', { selected })
      } catch (error) {
        previous.forEach(({ id, selected: wasSelected }) => {
          const item = this.items.find((i) => i.id === id)
          if (item) {
            item.selected = wasSelected
          }
        })
        throw error
      }
    },
  },
})
