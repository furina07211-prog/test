<template>
  <div class="page-shell">
    <header class="page-header">
      <div class="page-title">
        <h2>基础资料</h2>
        <p>水果分类、档案、供应商、客户和仓库信息统一维护，为采购与销售业务提供主数据支撑。</p>
      </div>
    </header>

    <el-tabs v-model="activeTab" class="surface section-block">
      <el-tab-pane label="分类" name="category">
        <div class="toolbar"><el-button type="warning" @click="saveCategory">新增分类</el-button></div>
        <el-form :model="categoryForm" inline class="mini-form">
          <el-form-item label="分类名"><el-input v-model="categoryForm.categoryName" /></el-form-item>
          <el-form-item label="排序"><el-input-number v-model="categoryForm.sortNo" :min="0" /></el-form-item>
        </el-form>
        <el-table :data="categories" border><el-table-column prop="categoryName" label="分类名" /><el-table-column prop="sortNo" label="排序" /></el-table>
      </el-tab-pane>

      <el-tab-pane label="水果档案" name="fruit">
        <div class="toolbar"><el-button type="warning" @click="saveFruit">新增水果</el-button></div>
        <el-form :model="fruitForm" inline class="mini-form">
          <el-form-item label="编码"><el-input v-model="fruitForm.fruitCode" /></el-form-item>
          <el-form-item label="名称"><el-input v-model="fruitForm.fruitName" /></el-form-item>
          <el-form-item label="分类ID"><el-input-number v-model="fruitForm.categoryId" :min="1" /></el-form-item>
          <el-form-item label="单位"><el-input v-model="fruitForm.unit" /></el-form-item>
          <el-form-item label="安全库存"><el-input-number v-model="fruitForm.safeStockQty" :min="0" /></el-form-item>
        </el-form>
        <el-table :data="fruits.records || []" border>
          <el-table-column prop="fruitCode" label="编码" />
          <el-table-column prop="fruitName" label="名称" />
          <el-table-column prop="safeStockQty" label="安全库存" />
          <el-table-column prop="warningDays" label="预警天数" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="供应商/客户/仓库" name="partner">
        <div class="split-grid">
          <div>
            <div class="toolbar"><el-button type="warning" @click="saveSupplier">新增供应商</el-button></div>
            <el-form :model="supplierForm" inline class="mini-form"><el-form-item label="编码"><el-input v-model="supplierForm.supplierCode" /></el-form-item><el-form-item label="名称"><el-input v-model="supplierForm.supplierName" /></el-form-item></el-form>
            <el-table :data="suppliers.records || []" border><el-table-column prop="supplierCode" label="编码" /><el-table-column prop="supplierName" label="名称" /></el-table>
          </div>
          <div>
            <div class="toolbar"><el-button type="warning" @click="saveCustomer">新增客户</el-button></div>
            <el-form :model="customerForm" inline class="mini-form"><el-form-item label="编码"><el-input v-model="customerForm.customerCode" /></el-form-item><el-form-item label="名称"><el-input v-model="customerForm.customerName" /></el-form-item></el-form>
            <el-table :data="customers.records || []" border><el-table-column prop="customerCode" label="编码" /><el-table-column prop="customerName" label="名称" /></el-table>
          </div>
        </div>
        <div class="toolbar" style="margin-top: 18px"><el-button type="warning" @click="saveWarehouse">新增仓库</el-button></div>
        <el-form :model="warehouseForm" inline class="mini-form"><el-form-item label="编码"><el-input v-model="warehouseForm.warehouseCode" /></el-form-item><el-form-item label="名称"><el-input v-model="warehouseForm.warehouseName" /></el-form-item></el-form>
        <el-table :data="warehouses" border><el-table-column prop="warehouseCode" label="编码" /><el-table-column prop="warehouseName" label="名称" /></el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { basicApi } from '@/api/modules'

const activeTab = ref('category')
const categories = ref([])
const fruits = ref({ records: [] })
const suppliers = ref({ records: [] })
const customers = ref({ records: [] })
const warehouses = ref([])

const categoryForm = reactive({ categoryName: '', sortNo: 1, status: 1 })
const fruitForm = reactive({ fruitCode: '', fruitName: '', categoryId: 1, unit: 'kg', safeStockQty: 50, warningDays: 2, shelfLifeDays: 7, status: 1 })
const supplierForm = reactive({ supplierCode: '', supplierName: '', status: 1 })
const customerForm = reactive({ customerCode: '', customerName: '', status: 1 })
const warehouseForm = reactive({ warehouseCode: '', warehouseName: '', status: 1 })

async function loadAll() {
  const [categoryData, fruitData, supplierData, customerData, warehouseData] = await Promise.all([
    basicApi.categories(),
    basicApi.fruits({ current: 1, size: 20 }),
    basicApi.suppliers({ current: 1, size: 20 }),
    basicApi.customers({ current: 1, size: 20 }),
    basicApi.warehouses()
  ])
  categories.value = categoryData
  fruits.value = fruitData
  suppliers.value = supplierData
  customers.value = customerData
  warehouses.value = warehouseData
}

async function saveCategory() { await basicApi.createCategory(categoryForm); ElMessage.success('分类已保存'); await loadAll() }
async function saveFruit() { await basicApi.createFruit(fruitForm); ElMessage.success('水果已保存'); await loadAll() }
async function saveSupplier() { await basicApi.createSupplier(supplierForm); ElMessage.success('供应商已保存'); await loadAll() }
async function saveCustomer() { await basicApi.createCustomer(customerForm); ElMessage.success('客户已保存'); await loadAll() }
async function saveWarehouse() { await basicApi.createWarehouse(warehouseForm); ElMessage.success('仓库已保存'); await loadAll() }

onMounted(loadAll)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.mini-form {
  margin-bottom: 12px;
}

.split-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

@media (max-width: 960px) {
  .split-grid {
    grid-template-columns: 1fr;
  }
}
</style>
