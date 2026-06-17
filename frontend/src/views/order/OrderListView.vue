<template>
  <div class="order-list-page">
    <h2>My Orders</h2>
    <el-tabs v-model="activeTab" @tab-change="fetchOrders">
      <el-tab-pane label="All" name="all" />
      <el-tab-pane label="Unpaid" name="0" />
      <el-tab-pane label="Paid" name="1" />
      <el-tab-pane label="Shipped" name="2" />
      <el-tab-pane label="Delivered" name="3" />
    </el-tabs>

    <div v-loading="loading">
      <el-card v-for="order in orders" :key="order.id" class="order-card"
        @click="$router.push(`/orders/${order.orderNo}`)">
        <div class="order-header">
          <span>Order: {{ order.orderNo }}</span>
          <el-tag :type="statusType(order.status)">{{ statusText(order.status) }}</el-tag>
        </div>
        <div class="order-body">
          <span class="amount">¥{{ order.payAmount }}</span>
          <span class="date">{{ order.createdAt }}</span>
        </div>
      </el-card>

      <el-empty v-if="!loading && orders.length === 0" description="No orders" />
    </div>

    <el-pagination v-if="total > 0" v-model:current-page="page" :page-size="10" :total="total"
      layout="prev, pager, next" class="pagination" @current-change="fetchOrders" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { get } from '@/services/http'

const orders = ref<any[]>([])
const loading = ref(false)
const activeTab = ref('all')
const page = ref(1)
const total = ref(0)

onMounted(() => fetchOrders())

async function fetchOrders() {
  loading.value = true
  try {
    const status = activeTab.value === 'all' ? undefined : activeTab.value
    const params = new URLSearchParams({ page: String(page.value), size: '10' })
    if (status) params.set('status', status)
    const res = await get<any>(`/orders?${params}`)
    orders.value = res.items || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function statusText(s: number) {
  return ['Unpaid', 'Paid', 'Shipped', 'Delivered', 'Completed', 'Cancelled'][s] || 'Unknown'
}

function statusType(s: number): '' | 'success' | 'warning' | 'danger' | 'info' {
  return (['warning', 'success', 'info', 'info', 'success', 'danger'] as const)[s] || ''
}
</script>

<style scoped lang="scss">
.order-list-page {
  max-width: 800px;
  margin: 24px auto;
  padding: 0 16px;
}

.order-card {
  margin-bottom: 12px;
  cursor: pointer;
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.order-body {
  display: flex;
  justify-content: space-between;
}

.amount {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.date {
  color: #999;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
