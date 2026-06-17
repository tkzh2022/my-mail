<template>
  <div class="product-form-page">
    <h2>{{ isEdit ? 'Edit Product' : 'New Product' }}</h2>
    <el-form :model="form" :rules="rules" ref="formRef" label-width="140px">
      <el-form-item label="Product Name" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="Subtitle">
        <el-input v-model="form.subtitle" />
      </el-form-item>
      <el-form-item label="Category" prop="categoryId">
        <el-cascader v-model="form.categoryId" :options="categories"
          :props="{ value: 'id', label: 'name', children: 'children', emitPath: false }" />
      </el-form-item>
      <el-form-item label="Price" prop="price">
        <el-input-number v-model="form.price" :min="0.01" :precision="2" />
      </el-form-item>
      <el-form-item label="Original Price">
        <el-input-number v-model="form.originalPrice" :min="0" :precision="2" />
      </el-form-item>
      <el-form-item label="Stock" prop="stock">
        <el-input-number v-model="form.stock" :min="0" />
      </el-form-item>
      <el-form-item label="Images">
        <el-input v-model="imageInput" placeholder="Image URL (press Enter to add)" @keyup.enter="addImage" />
        <div class="image-list">
          <el-tag v-for="(img, i) in form.images" :key="i" closable @close="form.images.splice(i, 1)">
            Image {{ i + 1 }}
          </el-tag>
        </div>
      </el-form-item>
      <el-form-item label="Description">
        <el-input v-model="form.description" type="textarea" :rows="6" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSubmit">
          {{ isEdit ? 'Update' : 'Submit for Review' }}
        </el-button>
        <el-button @click="$router.back()">Cancel</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { get, post, put } from '@/services/http'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const saving = ref(false)
const categories = ref<any[]>([])
const imageInput = ref('')
const isEdit = computed(() => !!route.params.id)

const form = reactive({
  name: '',
  subtitle: '',
  categoryId: null as number | null,
  price: 0,
  originalPrice: 0,
  stock: 0,
  images: [] as string[],
  description: '',
})

const rules: FormRules = {
  name: [{ required: true, message: 'Required', trigger: 'blur' }],
  categoryId: [{ required: true, message: 'Required', trigger: 'change' }],
  price: [{ required: true, message: 'Required', trigger: 'blur' }],
  stock: [{ required: true, message: 'Required', trigger: 'blur' }],
}

onMounted(async () => {
  const cats = await get<any[]>('/products/categories')
  categories.value = cats || []
  if (isEdit.value) {
    const product = await get<any>(`/products/${route.params.id}`)
    if (product) Object.assign(form, product)
  }
})

function addImage() {
  if (imageInput.value && form.images.length < 10) {
    form.images.push(imageInput.value)
    imageInput.value = ''
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value) {
      await put(`/merchant/products/${route.params.id}`, form)
      ElMessage.success('Product updated')
    } else {
      await post('/merchant/products', form)
      ElMessage.success('Product submitted for review')
    }
    router.push('/merchant/products')
  } catch (e: any) {
    ElMessage.error(e.message || 'Save failed')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped lang="scss">
.product-form-page {
  max-width: 700px;
  margin: 24px auto;
  padding: 0 16px;
}
.image-list {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
