<template>
  <div class="category-nav">
    <el-scrollbar>
      <div class="category-list">
        <el-tag
          v-for="cat in categories"
          :key="cat.id"
          :type="selectedId === cat.id ? '' : 'info'"
          size="large"
          class="category-tag"
          @click="selectCategory(cat.id)">
          {{ cat.name }}
        </el-tag>
      </div>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/services/http'

const router = useRouter()
const categories = ref<any[]>([])
const selectedId = ref<number | null>(null)

onMounted(async () => {
  const res = await get<any[]>('/products/categories')
  categories.value = res || []
})

function selectCategory(id: number) {
  selectedId.value = id
  router.push({ path: '/products', query: { categoryId: String(id) } })
}
</script>

<style scoped lang="scss">
.category-list {
  display: flex;
  gap: 8px;
  padding: 8px 0;
}

.category-tag {
  cursor: pointer;
  flex-shrink: 0;
}
</style>
