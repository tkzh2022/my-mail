<template>
  <div class="merchant-products">
    <div class="header-row">
      <h2>My Products</h2>
      <el-button type="primary" @click="$router.push('/merchant/products/new')">Add Product</el-button>
    </div>

    <el-tabs v-model="statusFilter" @tab-change="fetchProducts">
      <el-tab-pane label="All" name="all" />
      <el-tab-pane label="Pending Audit" name="0" />
      <el-tab-pane label="Online" name="1" />
      <el-tab-pane label="Offline" name="2" />
      <el-tab-pane label="Rejected" name="3" />
    </el-tabs>

    <el-table :data="products" v-loading="loading">
      <el-table-column prop="name" label="Product Name" min-width="200" />
      <el-table-column prop="price" label="Price" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="Stock" width="80" />
      <el-table-column prop="salesCount" label="Sales" width="80" />
      <el-table-column label="Status" width="120">
        <template #default="{ row }">
          <el-tag :type="['warning','success','info','danger'][row.status]" size="small">
            {{ ['Pending','Online','Offline','Rejected'][row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/merchant/products/${row.id}/edit`)">Edit</el-button>
          <el-button size="small" type="danger" v-if="row.status === 1" @click="takeOffline(row.id)">Offline</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-if="total > 0" v-model:current-page="page" :page-size="20" :total="total"
      layout="prev, pager, next" class="pagination" @current-change="fetchProducts" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, put } from '@/services/http'

const products = ref<any[]>([])
const loading = ref(false)
const statusFilter = ref('all')
const page = ref(1)
const total = ref(0)

onMounted(() => fetchProducts())

async function fetchProducts() {
  loading.value = true
  try {
    const params = new URLSearchParams({ page: String(page.value), size: '20' })
    if (statusFilter.value !== 'all') params.set('status', statusFilter.value)
    const res = await get<any>(`/merchant/products?${params}`)
    products.value = res.items || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function takeOffline(id: number) {
  await ElMessageBox.confirm('Take this product offline?', 'Confirm')
  await put(`/merchant/products/${id}/offline`, {})
  ElMessage.success('Product taken offline')
  await fetchProducts()
}
</script>

<style scoped lang="scss">
.merchant-products {
  padding: 24px;
}
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
