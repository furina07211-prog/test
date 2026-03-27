<template>
  <div class="page-padding">
    <div class="card page-toolbar">
      <el-input v-model="query.purchaseNo" placeholder="采购单号" clearable style="width: 180px" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px">
        <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="query.supplierId" placeholder="供应商" clearable filterable style="width: 180px">
        <el-option v-for="item in supplierOptions" :key="item.id" :label="item.supplierName" :value="item.id" />
      </el-select>
      <el-select v-model="query.warehouseId" placeholder="仓库" clearable style="width: 160px">
        <el-option v-for="item in warehouseOptions" :key="item.id" :label="item.warehouseName || item.name" :value="item.id" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
      <el-button type="success" @click="openCreate">新建采购单</el-button>
    </div>

    <div class="list-card card">
      <el-table :data="tableData" class="app-table" stripe v-loading="loading">
        <el-table-column prop="purchaseNo" label="采购单号" width="170" />
        <el-table-column prop="supplierName" label="供应商" min-width="140" />
        <el-table-column prop="warehouseName" label="仓库" min-width="130" />
        <el-table-column prop="orderStatus" label="状态" width="110">
          <template #default="scope">{{ formatOrderStatus(scope.row.orderStatus) }}</template>
        </el-table-column>
        <el-table-column label="收货进度" width="160">
          <template #default="scope">{{ formatQty(scope.row.receivedQty) }} / {{ formatQty(scope.row.totalQty) }}</template>
        </el-table-column>
        <el-table-column prop="orderDate" label="下单日期" width="120" />
        <el-table-column prop="expectedArrivalDate" label="预计到货" width="120" />
        <el-table-column prop="totalAmount" label="总金额" width="110" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <div class="table-actions">
              <el-button size="small" type="primary" @click="openItems(scope.row)">明细</el-button>
              <el-button size="small" @click="submitOrder(scope.row)" :disabled="scope.row.orderStatus !== 'DRAFT'">提交</el-button>
              <el-button size="small" type="warning" @click="approveOrder(scope.row)" :disabled="scope.row.orderStatus !== 'SUBMITTED'">审核</el-button>
              <el-button
                size="small"
                type="success"
                @click="openReceive(scope.row)"
                :disabled="scope.row.orderStatus !== 'APPROVED' || Number(scope.row.pendingQty || 0) <= 0"
              >
                收货
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="app-pagination">
        <el-pagination
          background
          layout="prev, pager, next, jumper, ->, total"
          :current-page="query.pageNo"
          :page-size="query.pageSize"
          :total="total"
          @current-change="onPageChange"
        />
      </div>
    </div>

    <el-dialog v-model="createVisible" title="新建采购单" width="980px" destroy-on-close>
      <el-form
        ref="createRef"
        :model="createForm"
        :rules="createRules"
        label-width="100px"
        :hide-required-asterisk="false"
        require-asterisk-position="left"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="createForm.supplierId" filterable placeholder="请选择供应商" style="width: 100%">
                <el-option v-for="item in supplierOptions" :key="item.id" :label="item.supplierName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="仓库" prop="warehouseId">
              <el-select v-model="createForm.warehouseId" placeholder="请选择仓库" style="width: 100%">
                <el-option v-for="item in warehouseOptions" :key="item.id" :label="item.warehouseName || item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下单日期" prop="orderDate">
              <el-date-picker v-model="createForm.orderDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择下单日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计到货" prop="expectedArrivalDate">
              <el-date-picker
                v-model="createForm.expectedArrivalDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择预计到货日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" placeholder="请输入备注（可选）" />
        </el-form-item>

        <div class="card" style="padding: 12px">
          <div class="line-header">
            <strong>采购明细</strong>
            <el-button size="small" @click="addItem">添加明细</el-button>
          </div>
          <el-table :data="createForm.items" class="app-table" size="small" empty-text="请先添加采购明细">
            <el-table-column label="水果" min-width="160">
              <template #default="scope">
                <el-select v-model="scope.row.fruitId" filterable placeholder="请选择水果" style="width: 100%" @change="onFruitChange(scope.row)">
                  <el-option v-for="item in fruitOptions" :key="item.id" :label="item.fruitName" :value="item.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="单位" width="80">
              <template #default="scope">
                <span>{{ scope.row.unit || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="仓库库存(参考)" width="130">
              <template #default="scope">
                <span>{{ formatQty(stockOfFruit(scope.row.fruitId)) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="批次号" min-width="120">
              <template #default="scope">
                <el-input v-model="scope.row.batchNo" placeholder="请输入批次号" />
              </template>
            </el-table-column>
            <el-table-column label="生产日期" width="140">
              <template #default="scope">
                <el-date-picker v-model="scope.row.productionDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="到期日期" width="140">
              <template #default="scope">
                <el-date-picker v-model="scope.row.expirationDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="数量" width="120">
              <template #default="scope">
                <el-input-number v-model="scope.row.quantity" :min="0" :precision="2" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="单价" width="120">
              <template #default="scope">
                <el-input-number v-model="scope.row.unitPrice" :min="0" :precision="2" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="120">
              <template #default="scope">
                <el-input v-model="scope.row.remark" placeholder="可选" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="scope">
                <el-button type="danger" link @click="removeItem(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="itemsVisible" title="采购明细" width="860px" destroy-on-close>
      <el-table :data="currentItems" class="app-table" size="small" empty-text="暂无明细数据">
        <el-table-column prop="fruitName" label="水果" min-width="120" />
        <el-table-column prop="batchNo" label="批次号" min-width="110" />
        <el-table-column prop="quantity" label="采购数量" width="110" />
        <el-table-column prop="receivedQty" label="已收数量" width="110" />
        <el-table-column prop="pendingQty" label="待收数量" width="110" />
        <el-table-column prop="unitPrice" label="单价" width="110" />
        <el-table-column prop="expirationDate" label="到期日期" width="120" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="receiveVisible" title="分批收货" width="860px" destroy-on-close>
      <el-table :data="receiveItems" class="app-table" size="small" empty-text="暂无可收货明细">
        <el-table-column prop="fruitName" label="水果" min-width="120" />
        <el-table-column prop="batchNo" label="批次号" min-width="110" />
        <el-table-column prop="quantity" label="采购数量" width="100" />
        <el-table-column prop="receivedQty" label="已收" width="90" />
        <el-table-column prop="pendingQty" label="待收" width="90" />
        <el-table-column label="本次收货" width="150">
          <template #default="scope">
            <el-input-number v-model="scope.row.receiveQty" :min="0" :max="Number(scope.row.pendingQty || 0)" :precision="2" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="单位成本" width="140">
          <template #default="scope">
            <el-input-number v-model="scope.row.unitCost" :min="0" :precision="2" style="width: 100%" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="receiveVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReceive">确认收货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { basicApi, purchaseApi } from '@/api/modules'
import { useWarehouseStock } from '@/composables/useWarehouseStock'

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已提交', value: 'SUBMITTED' },
  { label: '已审核', value: 'APPROVED' },
  { label: '已入库', value: 'RECEIVED' }
]

const orderStatusLabelMap = {
  DRAFT: '草稿',
  SUBMITTED: '已提交',
  APPROVED: '已审核',
  RECEIVED: '已入库'
}

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({
  pageNo: 1,
  pageSize: 10,
  purchaseNo: '',
  status: '',
  supplierId: null,
  warehouseId: null
})

const supplierOptions = ref([])
const warehouseOptions = ref([])
const fruitOptions = ref([])

const { loadWarehouseStock, stockOfFruit } = useWarehouseStock()

const fruitMap = computed(() => {
  const map = {}
  fruitOptions.value.forEach((item) => {
    map[item.id] = item
  })
  return map
})

const createVisible = ref(false)
const createRef = ref()
const createForm = reactive({
  supplierId: null,
  warehouseId: null,
  orderDate: '',
  expectedArrivalDate: '',
  remark: '',
  items: []
})

const createRules = {
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  orderDate: [{ required: true, message: '请选择下单日期', trigger: 'change' }],
  items: [{ type: 'array', min: 1, message: '至少添加一条采购明细', trigger: 'change' }]
}

const itemsVisible = ref(false)
const currentItems = ref([])

const receiveVisible = ref(false)
const currentOrderId = ref(null)
const receiveItems = ref([])

const formatOrderStatus = (status) => orderStatusLabelMap[status] || status || '-'
const formatQty = (value) => Number(value || 0).toFixed(2)

const getOperatorId = () => {
  const raw = localStorage.getItem('fruit-user')
  if (!raw) return 1
  try {
    const parsed = JSON.parse(raw)
    return parsed?.id || 1
  } catch (e) {
    return 1
  }
}

const onFruitChange = (row) => {
  const fruit = fruitMap.value[row.fruitId]
  row.unit = fruit?.unit || ''
}

watch(
  () => createForm.warehouseId,
  async (warehouseId) => {
    await loadWarehouseStock(warehouseId)
  }
)

const loadData = async () => {
  loading.value = true
  try {
    const res = await purchaseApi.orders({ ...query })
    tableData.value = res?.records || []
    total.value = res?.total || 0
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.pageNo = 1
  query.purchaseNo = ''
  query.status = ''
  query.supplierId = null
  query.warehouseId = null
  loadData()
}

const onPageChange = (pageNo) => {
  query.pageNo = pageNo
  loadData()
}

const openCreate = () => {
  createForm.supplierId = null
  createForm.warehouseId = null
  createForm.orderDate = ''
  createForm.expectedArrivalDate = ''
  createForm.remark = ''
  createForm.items = []
  createVisible.value = true
}

const addItem = () => {
  createForm.items.push({
    fruitId: null,
    unit: '',
    batchNo: '',
    productionDate: '',
    expirationDate: '',
    quantity: 0,
    unitPrice: 0,
    remark: ''
  })
}

const removeItem = (index) => {
  createForm.items.splice(index, 1)
}

const validateCreateItems = () => {
  if (!createForm.items.length) {
    ElMessage.warning('请至少添加一条采购明细')
    return false
  }
  for (const [index, item] of createForm.items.entries()) {
    const lineNo = index + 1
    if (!item.fruitId) {
      ElMessage.warning(`第${lineNo}行未选择水果`)
      return false
    }
    if (!item.batchNo) {
      ElMessage.warning(`第${lineNo}行未填写批次号`)
      return false
    }
    if (!item.expirationDate) {
      ElMessage.warning(`第${lineNo}行未填写到期日期`)
      return false
    }
    if (Number(item.quantity || 0) <= 0) {
      ElMessage.warning(`第${lineNo}行数量必须大于0`)
      return false
    }
    if (Number(item.unitPrice || 0) < 0) {
      ElMessage.warning(`第${lineNo}行单价不能小于0`)
      return false
    }
  }
  return true
}

const submitCreate = async () => {
  const valid = await createRef.value.validate().catch(() => false)
  if (!valid || !validateCreateItems()) return

  const payload = {
    ...createForm,
    items: createForm.items.map((item) => ({
      fruitId: item.fruitId,
      batchNo: item.batchNo,
      productionDate: item.productionDate,
      expirationDate: item.expirationDate,
      quantity: item.quantity,
      unitPrice: item.unitPrice,
      remark: item.remark
    }))
  }

  await purchaseApi.createOrder(payload)
  ElMessage.success('采购单创建成功')
  createVisible.value = false
  query.pageNo = 1
  loadData()
}

const submitOrder = async (row) => {
  if (row.orderStatus !== 'DRAFT') {
    ElMessage.warning('只有草稿状态可以提交')
    return
  }
  await purchaseApi.submit(row.id)
  ElMessage.success('提交成功')
  loadData()
}

const approveOrder = async (row) => {
  if (row.orderStatus !== 'SUBMITTED') {
    ElMessage.warning('只有已提交状态可以审核')
    return
  }
  await purchaseApi.approve(row.id)
  ElMessage.success('审核成功')
  loadData()
}

const openItems = async (row) => {
  currentItems.value = await purchaseApi.items(row.id)
  itemsVisible.value = true
}

const openReceive = async (row) => {
  if (row.orderStatus !== 'APPROVED') {
    ElMessage.warning('只有已审核状态可以收货')
    return
  }
  const items = await purchaseApi.items(row.id)
  receiveItems.value = (items || []).map((item) => ({ ...item, receiveQty: 0, unitCost: item.unitPrice }))
  currentOrderId.value = row.id
  receiveVisible.value = true
}

const submitReceive = async () => {
  const receivePayloadItems = receiveItems.value
    .filter((item) => Number(item.receiveQty || 0) > 0)
    .map((item) => ({ itemId: item.id, receiveQty: Number(item.receiveQty), unitCost: Number(item.unitCost || item.unitPrice || 0) }))

  if (!receivePayloadItems.length) {
    ElMessage.warning('请至少填写一条本次收货数量')
    return
  }

  await purchaseApi.receive(currentOrderId.value, { operatorId: getOperatorId(), items: receivePayloadItems })
  ElMessage.success('收货入库成功')
  receiveVisible.value = false
  await loadData()
  if (itemsVisible.value && currentOrderId.value) {
    currentItems.value = await purchaseApi.items(currentOrderId.value)
  }
}

const loadOptions = async () => {
  const [suppliers, warehouses, fruits] = await Promise.all([
    basicApi.suppliers({ pageNo: 1, pageSize: 200 }),
    basicApi.warehouses(),
    basicApi.fruits({ pageNo: 1, pageSize: 200 })
  ])
  supplierOptions.value = suppliers?.records || suppliers || []
  warehouseOptions.value = warehouses || []
  fruitOptions.value = fruits?.records || fruits || []
}

onMounted(async () => {
  await loadOptions()
  loadData()
})
</script>

<style scoped>
.line-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
</style>
