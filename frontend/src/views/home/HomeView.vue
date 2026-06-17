<template>
  <div class="home-page">
    <div class="hero-section">
      <el-carousel height="300px">
        <el-carousel-item v-for="i in 3" :key="i">
          <div class="carousel-placeholder">Banner {{ i }}</div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <div class="category-section">
      <h2>Categories</h2>
      <CategoryNav />
    </div>

    <div class="product-section">
      <h2>Recommended Products</h2>
      <el-row :gutter="16" v-loading="loading">
        <el-col :xs="12" :sm="8" :md="6" v-for="product in products" :key="product.id">
          <el-card class="product-card" shadow="hover" @click="goToProduct(product.id)">
            <el-image :src="product.image || product.images?.[0]" fit="cover" class="product-image" />
            <div class="product-info">
              <p class="product-name">{{ product.name }}</p>
              <p class="product-price">¥{{ product.price }}</p>
              <p class="product-sales">{{ product.salesCount }} sold</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <div class="load-more" v-if="hasMore">
        <el-button @click="loadMore" :loading="loading">Load More</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/services/http'
import CategoryNav from '@/components/CategoryNav.vue'

const router = useRouter()
const products = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)

onMounted(() => loadProducts())

async function loadProducts() {
  loading.value = true
  try {
    const res = await get<any>(`/products?page=${page.value}&size=20`)
    products.value.push(...(res.items || []))
    hasMore.value = products.value.length < res.total
  } finally {
    loading.value = false
  }
}

function loadMore() {
  page.value++
  loadProducts()
}

function goToProduct(id: number) {
  router.push(`/products/${id}`)
}
</script>

<style scoped lang="scss">
.home-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px;
}

.hero-section {
  margin-bottom: 32px;
}

.carousel-placeholder {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 24px;
}

.category-section, .product-section {
  margin-bottom: 32px;
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

.load-more {
  text-align: center;
  padding: 24px;
}
</style>
