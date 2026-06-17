<template>
  <div class="cart-page">
    <h2>Shopping Cart</h2>
    <el-table :data="cartStore.items" v-loading="cartStore.loading" empty-text="Your cart is empty">
      <el-table-column width="50">
        <template #default="{ row }">
          <el-checkbox :model-value="row.selected" @change="cartStore.toggleSelect(row.id)" />
        </template>
      </el-table-column>
      <el-table-column label="Product" min-width="300">
        <template #default="{ row }">
          <div class="product-cell">
            <el-image :src="row.productImage" fit="cover" class="cart-img" />
            <span>{{ row.productName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="Price" width="120">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column label="Quantity" width="160">
        <template #default="{ row }">
          <el-input-number v-model="row.quantity" :min="1" size="small"
            @change="(val: number) => cartStore.updateQuantity(row.id, val)" />
        </template>
      </el-table-column>
      <el-table-column label="Subtotal" width="120">
        <template #default="{ row }">¥{{ (row.price * row.quantity).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="Action" width="80">
        <template #default="{ row }">
          <el-button type="danger" size="small" text @click="cartStore.removeItem(row.id)">Remove</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="cart-footer" v-if="cartStore.items.length > 0">
      <div class="total">
        Total: <span class="price">¥{{ cartStore.totalPrice.toFixed(2) }}</span>
        ({{ cartStore.selectedItems.length }} items)
      </div>
      <el-button type="primary" size="large" :disabled="cartStore.selectedItems.length === 0"
        @click="$router.push('/checkout')">
        Checkout
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useCartStore } from '@/stores/cart'

const cartStore = useCartStore()
onMounted(() => cartStore.fetchCart())
</script>

<style scoped lang="scss">
.cart-page {
  max-width: 1000px;
  margin: 24px auto;
  padding: 0 16px;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cart-img {
  width: 60px;
  height: 60px;
  flex-shrink: 0;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 0;
  border-top: 1px solid #eee;
  margin-top: 16px;
}

.total .price {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
}
</style>
