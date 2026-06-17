<template>
  <div class="product-list-page">
    <SearchBar />

    <div class="filters">
      <el-select v-model="sortBy" placeholder="Sort" @change="fetchProducts">
        <el-option label="Newest" value="newest" />
        <el-option label="Price: Low to High" value="price_asc" />
        <el-option label="Price: High to Low" value="price_desc" />
        <el-option label="Most Popular" value="sales" />
      </el-select>
    </div>

    <el-row :gutter="16" v-loading="loading">
      <el-col :xs="12" :sm="8" :md="6" v-for="product in products" :key="product.id">
        <el-card class="product-card" shadow="hover" @click="$router.push(`/products/${product.id}`)">
          <el-image :src="product.image || product.images?.[0]" fit="cover" class="product-image" />
          <div class="product-info">
            <p class="product-name" v-html="product.highlightName || product.name"></p>
            <p class="product-price">¥{{ product.price }}</p>
            <p class="product-sales">{{ product.salesCount }} sold</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && products.length === 0" description="No products found">
      <el-button @click="$router.push('/')">Browse All Products</el-button>
    </el-empty>

    <el-pagination
      v-if="total > 0"
      v-model:current-page="page"
      :page-size="20"
      :total="total"
      layout="prev, pager, next"
      class="pagination"
      @current-change="fetchProducts" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { get } from '@/services/http'
import SearchBar from '@/components/SearchBar.vue'

const route = useRoute()
const products = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const sortBy = ref('newest')

onMounted(() => fetchProducts())

watch(() => route.query, () => {
  page.value = 1
  fetchProducts()
})

async function fetchProducts() {
  loading.value = true
  try {
    const keyword = route.query.keyword as string
    const categoryId = route.query.categoryId as string

    let res: any
    if (keyword) {
      res = await get<any>(`/search/products?keyword=${encodeURIComponent(keyword)}&page=${page.value}&size=20&sort=${sortBy.value}`)
    } else {
      const params = new URLSearchParams({ page: String(page.value), size: '20', sort: sortBy.value })
      if (categoryId) params.set('categoryId', categoryId)
      res = await get<any>(`/products?${params}`)
    }
    products.value = res.items || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.product-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px;
}

.filters {
  display: flex;
  justify-content: flex-end;
  margin: 16px 0;
}

.product-card {
  margin-bottom: 16px;
  cursor: pointer;
}

.product-image {
  width: 100%;
  height: 200px;
}

.product-info {
  padding: 8px 0;
}

.product-name {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin: 4px 0;
}

.product-price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
  margin: 4px 0;
}

.product-sales {
  color: #999;
  font-size: 12px;
  margin: 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
