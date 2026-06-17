<template>
  <div class="address-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <h3>My Addresses</h3>
          <el-button type="primary" @click="showDialog = true">Add Address</el-button>
        </div>
      </template>
      <el-table :data="addresses" v-loading="loading">
        <el-table-column prop="receiverName" label="Name" width="100" />
        <el-table-column prop="receiverPhone" label="Phone" width="140" />
        <el-table-column label="Address">
          <template #default="{ row }">
            {{ row.province }} {{ row.city }} {{ row.district }} {{ row.detailAddress }}
          </template>
        </el-table-column>
        <el-table-column label="Default" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault" type="success" size="small">Default</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="editAddress(row)">Edit</el-button>
            <el-button size="small" type="danger" @click="deleteAddress(row.id)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="editingId ? 'Edit Address' : 'Add Address'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="Name">
          <el-input v-model="form.receiverName" />
        </el-form-item>
        <el-form-item label="Phone">
          <el-input v-model="form.receiverPhone" />
        </el-form-item>
        <el-form-item label="Province">
          <el-input v-model="form.province" />
        </el-form-item>
        <el-form-item label="City">
          <el-input v-model="form.city" />
        </el-form-item>
        <el-form-item label="District">
          <el-input v-model="form.district" />
        </el-form-item>
        <el-form-item label="Detail">
          <el-input v-model="form.detailAddress" type="textarea" />
        </el-form-item>
        <el-form-item label="Default">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">Cancel</el-button>
        <el-button type="primary" :loading="saving" @click="saveAddress">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post, put, del } from '@/services/http'

const addresses = ref<any[]>([])
const loading = ref(false)
const saving = ref(false)
const showDialog = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: 0,
})

onMounted(() => fetchAddresses())

async function fetchAddresses() {
  loading.value = true
  try {
    const res = await get<any[]>('/users/addresses')
    addresses.value = res || []
  } finally {
    loading.value = false
  }
}

function editAddress(row: any) {
  editingId.value = row.id
  Object.assign(form, row)
  showDialog.value = true
}

async function saveAddress() {
  saving.value = true
  try {
    if (editingId.value) {
      await put(`/users/addresses/${editingId.value}`, form)
    } else {
      await post('/users/addresses', form)
    }
    ElMessage.success('Address saved')
    showDialog.value = false
    editingId.value = null
    resetForm()
    await fetchAddresses()
  } catch (e: any) {
    ElMessage.error(e.message || 'Save failed')
  } finally {
    saving.value = false
  }
}

async function deleteAddress(id: number) {
  await ElMessageBox.confirm('Delete this address?', 'Confirm')
  await del(`/users/addresses/${id}`)
  ElMessage.success('Deleted')
  await fetchAddresses()
}

function resetForm() {
  form.receiverName = ''
  form.receiverPhone = ''
  form.province = ''
  form.city = ''
  form.district = ''
  form.detailAddress = ''
  form.isDefault = 0
}
</script>

<style scoped lang="scss">
.address-page {
  max-width: 900px;
  margin: 24px auto;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

h3 {
  margin: 0;
}
</style>
