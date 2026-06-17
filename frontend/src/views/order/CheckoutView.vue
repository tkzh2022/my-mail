<template>
  <div class="checkout-page">
    <h2>Confirm Order</h2>

    <el-card class="section">
      <template #header><h3>Shipping Address</h3></template>
      <p v-if="selectedAddress">
        {{ selectedAddress.receiverName }} {{ selectedAddress.receiverPhone }}<br>
        {{ selectedAddress.province }} {{ selectedAddress.city }} {{ selectedAddress.district }} {{ selectedAddress.detailAddress }}
      </p>
      <el-button v-else @click="$router.push('/user/addresses')">Add Address</el-button>
    </el-card>

    <el-card class="section">
      <template #header><h3>Order Items</h3></template>
      <div v-for="item in cartStore.selectedItems" :key="item.id" class="order-item">
        <el-image :src="item.productImage" fit="cover" class="item-img" />
        <div class="item-info">
          <p>{{ item.productName }}</p>
          <p class="item-price">¥{{ item.price }} × {{ item.quantity }}</p>
        </div>
      </div>
    </el-card>

    <el-card class="section">
      <template #header><h3>Remark</h3></template>
      <el-input v-model="remark" placeholder="Optional note for the seller" />
    </el-card>

    <div class="checkout-footer">
      <div class="total">Total: <span class="price">¥{{ cartStore.totalPrice.toFixed(2) }}</span></div>
      <el-button type="danger" size="large" :loading="submitting" @click="submitOrder">
        Place Order
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { get, post } from '@/services/http'
import { useCartStore } from '@/stores/cart'
import { v4 as uuidv4 } from 'uuid'

const router = useRouter()
const cartStore = useCartStore()
const selectedAddress = ref<any>(null)
const remark = ref('')
const submitting = ref(false)

onMounted(async () => {
  const addresses = await get<any[]>('/users/addresses')
  selectedAddress.value = addresses?.find((a: any) => a.isDefault) || addresses?.[0]
})

async function submitOrder() {
  if (!selectedAddress.value) {
    ElMessage.warning('Please add a shipping address')
    return
  }
  submitting.value = true
  try {
    const items = cartStore.selectedItems.map(item => ({
      productId: item.productId,
      productName: item.productName,
      productImage: item.productImage,
      price: item.price,
      quantity: item.quantity,
      merchantId: 1,
    }))
    const order = await post<any>('/orders', {
      idempotencyKey: uuidv4(),
      receiverName: selectedAddress.value.receiverName,
      receiverPhone: selectedAddress.value.receiverPhone,
      receiverAddress: `${selectedAddress.value.province} ${selectedAddress.value.city} ${selectedAddress.value.district} ${selectedAddress.value.detailAddress}`,
      remark: remark.value,
      items,
    })
    ElMessage.success('Order placed!')
    router.push(`/orders/${order.orderNo}`)
  } catch (e: any) {
    ElMessage.error(e.message || 'Order failed')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.checkout-page {
  max-width: 800px;
  margin: 24px auto;
  padding: 0 16px;
}

.section {
  margin-bottom: 16px;
}

.order-item {
  display: flex;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.item-img {
  width: 60px;
  height: 60px;
}

.item-price {
  color: #f56c6c;
}

.checkout-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 0;
}

.total .price {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
}
</style>
