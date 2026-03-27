<template>
  <div class="page-padding">
    <div class="card" style="margin-bottom:16px; display:flex; gap:12px; align-items:flex-end; flex-wrap:wrap;">
      <el-input v-model="query.fruitName" placeholder="按水果名称" style="width:200px" clearable />
      <el-input v-model="query.batchNo" placeholder="按批次号" style="width:200px" clearable />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <el-button type="success" @click="openCreate">新增批次</el-button>
    </div>

    <div class="card">
      <el-table :data="tableData" stripe style="width:100%" :row-class-name="rowClassName" v-loading="loading">
        <el-table-column prop="batchNo" label="批次号" width="140" />
        <el-table-column prop="fruitName" label="水果" min-width="140" />
        <el-table-column prop="warehouseName" label="仓库" min-width="140" />
        <el-table-column prop="availableQty" label="可用库存" width="120" />
        <el-table-column prop="safeStock" label="安全库存" width="120" />
        <el-table-column prop="expirationDate" label="到期日" width="140" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:12px; text-align:right;">
        <el-pagination
          background
          layout="prev, pager, next, jumper, ->, total"
          :current-page="query.pageNo"
          :page-size="query.pageSize"
          :total="total"
          @current-change="(p)=>{query.pageNo=p;loadData();}"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑批次' : '新增批次'" width="540px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="水果" prop="fruitId">
          <el-select v-model="form.fruitId" filterable placeholder="选择水果" style="width:100%">
            <el-option v-for="f in fruitOptions" :key="f.id" :label="f.fruitName" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="form.warehouseId" placeholder="选择仓库" style="width:100%">
            <el-option v-for="w in warehouseOptions" :key="w.id" :label="w.warehouseName || w.name" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="批次号" prop="batchNo">
          <el-input v-model="form.batchNo" />
        </el-form-item>
        <el-form-item label="生产日期" prop="productionDate">
          <el-date-picker v-model="form.productionDate" type="date" style="width:100%" />
        </el-form-item>
        <el-form-item label="到期日" prop="expirationDate">
          <el-date-picker v-model="form.expirationDate" type="date" style="width:100%" />
        </el-form-item>
        <el-form-item label="总数量" prop="totalQty">
          <el-input-number v-model="form.totalQty" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="可用数量" prop="availableQty">
          <el-input-number v-model="form.availableQty" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="单位成本" prop="unitCost">
          <el-input-number v-model="form.unitCost" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="在库" value="IN_STOCK" />
            <el-option label="冻结" value="LOCKED" />
            <el-option label="失效" value="EXPIRED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { inventoryApi, basicApi } from '@/api/modules'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ pageNo: 1, pageSize: 10, fruitName: '', batchNo: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  fruitId: null,
  warehouseId: null,
  batchNo: '',
  productionDate: '',
  expirationDate: '',
  totalQty: 0,
  availableQty: 0,
  unitCost: 0,
  status: 'IN_STOCK'
})

const rules = {
  fruitId: [{ required: true, message: '请选择水果', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  batchNo: [{ required: true, message: '请输入批次号', trigger: 'blur' }],
  expirationDate: [{ required: true, message: '请选择到期日', trigger: 'change' }],
  availableQty: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

const fruitOptions = ref([])
const warehouseOptions = ref([])

const safeValue = (row) => row.safeStockQty ?? row.safe_stock_qty ?? row.safeStock ?? 0

const rowClassName = ({ row }) => {
  const safe = safeValue(row)
  if (safe && Number(row.availableQty) <= Number(safe)) return 'row-low'
  return ''
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await inventoryApi.batches({ ...query })
    tableData.value = res.records || res.items || res || []
    total.value = res.total || (res.records ? res.records.length : 0)
  } finally {
    loading.value = false
  }
}

const reset = () => {
  query.fruitName = ''
  query.batchNo = ''
  query.pageNo = 1
  loadData()
}

const openCreate = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    fruitId: null,
    warehouseId: null,
    batchNo: '',
    productionDate: '',
    expirationDate: '',
    totalQty: 0,
    availableQty: 0,
    unitCost: 0,
    status: 'IN_STOCK'
  })
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    if (isEdit.value) {
      await inventoryApi.updateBatch(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await inventoryApi.createBatch(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除批次 ${row.batchNo} 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await inventoryApi.deleteBatch(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

const loadOptions = async () => {
  const [fruits, warehouses] = await Promise.all([basicApi.fruits({}), basicApi.warehouses()])
  fruitOptions.value = fruits?.records || fruits || []
  warehouseOptions.value = warehouses || []
}

onMounted(() => {
  loadOptions()
  loadData()
})
</script>

<style scoped>
.row-low {
  --el-table-tr-bg-color: #fff1f1;
}
</style>
