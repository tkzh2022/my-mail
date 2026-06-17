<template>
  <div class="product-detail-page" v-loading="loading">
    <template v-if="product">
      <el-row :gutter="24">
        <el-col :span="10">
          <el-carousel height="400px" v-if="product.images?.length">
            <el-carousel-item v-for="(img, index) in product.images" :key="index">
              <el-image :src="img" fit="contain" class="main-image" />
            </el-carousel-item>
          </el-carousel>
        </el-col>
        <el-col :span="14">
          <h1 class="product-title">{{ product.name }}</h1>
          <p class="product-subtitle">{{ product.subtitle }}</p>
          <div class="price-section">
            <span class="current-price">¥{{ product.price }}</span>
            <span class="original-price" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
          </div>
          <div class="meta-info">
            <el-tag>{{ product.salesCount }} sold</el-tag>
            <el-tag type="success" v-if="product.stock > 0">In Stock ({{ product.stock }})</el-tag>
            <el-tag type="danger" v-else>Out of Stock</el-tag>
          </div>
          <div class="quantity-section">
            <span>Quantity:</span>
            <el-input-number v-model="quantity" :min="1" :max="product.stock" size="large" />
          </div>
          <div class="actions">
            <el-button type="primary" size="large" :disabled="product.stock === 0" @click="addToCart">
              Add to Cart
            </el-button>
            <el-button type="danger" size="large" :disabled="product.stock === 0" @click="buyNow">
              Buy Now
            </el-button>
          </div>
        </el-col>
      </el-row>

      <el-divider />

      <div class="description-section">
        <h2>Product Description</h2>
        <div v-html="product.description"></div>
      </div>

      <el-divider />

      <div class="related-section" v-if="relatedProducts.length">
        <h2>Related Products</h2>
        <el-row :gutter="16">
          <el-col :span="4" v-for="rp in relatedProducts" :key="rp.id">
            <el-card shadow="hover" class="related-card" @click="$router.push(`/products/${rp.id}`)">
              <el-image :src="rp.images?.[0]" fit="cover" class="related-image" />
              <p class="related-name">{{ rp.name }}</p>
              <p class="related-price">¥{{ rp.price }}</p>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { get, post } from '@/services/http'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const product = ref<any>(null)
const relatedProducts = ref<any[]>([])
const loading = ref(false)
const quantity = ref(1)

onMounted(() => fetchProduct())
watch(() => route.params.id, () => fetchProduct())

async function fetchProduct() {
  const id = route.params.id
  if (!id) return
  loading.value = true
  try {
    product.value = await get<any>(`/products/${id}`)
    const related = await get<any[]>(`/products/${id}/related`)
    relatedProducts.value = related || []
  } finally {
    loading.value = false
  }
}

async function addToCart() {
  try {
    await cartStore.addItem(product.value.id, quantity.value)
    ElMessage.success('Added to cart')
  } catch (e: any) {
    ElMessage.error(e.message || 'Failed to add')
  }
}

function buyNow() {
  addToCart().then(() => router.push('/cart'))
}
</script>

<style scoped lang="scss">
.product-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.main-image {
  width: 100%;
  height: 400px;
}

.product-title {
  font-size: 24px;
  margin: 0 0 8px;
}

.product-subtitle {
  color: #666;
  margin: 0 0 16px;
}

.price-section {
  margin: 16px 0;
  background: #fdf6ec;
  padding: 12px;
  border-radius: 4px;
}

.current-price {
  font-size: 28px;
  color: #f56c6c;
  font-weight: bold;
}

.original-price {
  font-size: 16px;
  color: #999;
  text-decoration: line-through;
  margin-left: 12px;
}

.meta-info {
  display: flex;
  gap: 8px;
  margin: 16px 0;
}

.quantity-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 24px 0;
}

.actions {
  display: flex;
  gap: 16px;
  margin-top: 24px;
}

.related-card {
  cursor: pointer;
  margin-bottom: 16px;
}

.related-image {
  width: 100%;
  height: 120px;
}

.related-name {
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin: 4px 0;
}

.related-price {
  color: #f56c6c;
  font-weight: bold;
  margin: 0;
}
</style>
