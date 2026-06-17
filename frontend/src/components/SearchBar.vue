<template>
  <div class="search-bar">
    <el-autocomplete
      v-model="keyword"
      :fetch-suggestions="fetchSuggestions"
      placeholder="Search products..."
      size="large"
      clearable
      class="search-input"
      @select="handleSelect"
      @keyup.enter="handleSearch">
      <template #append>
        <el-button :icon="Search" @click="handleSearch" />
      </template>
    </el-autocomplete>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { get } from '@/services/http'

const router = useRouter()
const keyword = ref('')

async function fetchSuggestions(query: string, cb: Function) {
  if (query.length < 2) {
    cb([])
    return
  }
  try {
    const suggestions = await get<string[]>(`/search/suggest?prefix=${encodeURIComponent(query)}`)
    cb((suggestions || []).map(s => ({ value: s })))
  } catch {
    cb([])
  }
}

function handleSearch() {
  if (keyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: keyword.value.trim() } })
  }
}

function handleSelect(item: { value: string }) {
  keyword.value = item.value
  handleSearch()
}
</script>

<style scoped lang="scss">
.search-bar {
  max-width: 600px;
  margin: 0 auto;
}

.search-input {
  width: 100%;
}
</style>
