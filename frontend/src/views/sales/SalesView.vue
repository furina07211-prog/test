<template>
  <div class="page-padding">
    <div class="card page-toolbar">
      <el-input v-model="query.salesNo" placeholder="销售单号" clearable style="width: 180px" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px">
        <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="query.customerId" placeholder="客户" clearable filterable style="width: 180px">
        <el-option v-for="item in customerOptions" :key="item.id" :label="item.customerName" :value="item.id" />
      </el-select>
      <el-select v-model="query.warehouseId" placeholder="仓库" clearable style="width: 160px">
        <el-option v-for="item in warehouseOptions" :key="item.id" :label="item.warehouseName || item.name" :value="item.id" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
      <el-button type="success" @click="openCreate">新建销售单</el-button>
    </div>

    <div class="list-card card">
      <el-table :data="tableData" class="app-table" stripe v-loading="loading">
        <el-table-column prop="salesNo" label="销售单号" width="170" />
        <el-table-column prop="customerName" label="客户" min-width="140" />
        <el-table-column prop="warehouseName" label="仓库" min-width="130" />
        <el-table-column prop="orderStatus" label="状态" width="110">
          <template #default="scope">{{ formatOrderStatus(scope.row.orderStatus) }}</template>
        </el-table-column>
        <el-table-column label="出库进度" width="160">
          <template #default="scope">{{ formatQty(scope.row.shippedQty) }} / {{ formatQty(scope.row.totalQty) }}</template>
        </el-table-column>
        <el-table-column prop="orderTime" label="下单时间" width="170" />
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
                @click="openShip(scope.row)"
                :disabled="scope.row.orderStatus !== 'APPROVED' || Number(scope.row.pendingQty || 0) <= 0"
              >
                出库
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

    <el-dialog v-model="createVisible" title="新建销售单" width="960px" destroy-on-close>
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
            <el-form-item label="客户" prop="customerId">
              <el-select v-model="createForm.customerId" filterable placeholder="请选择客户" style="width: 100%">
                <el-option v-for="item in customerOptions" :key="item.id" :label="item.customerName" :value="item.id" />
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
            <el-form-item label="下单时间" prop="orderTime">
              <el-date-picker
                v-model="createForm.orderTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择下单时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="createForm.remark" placeholder="请输入备注（可选）" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="card" style="padding: 12px">
          <div class="line-header">
            <strong>销售明细</strong>
            <el-button size="small" @click="addItem">添加明细</el-button>
          </div>
          <el-table :data="createForm.items" class="app-table" size="small" empty-text="请先添加销售明细">
            <el-table-column label="水果" min-width="180">
              <template #default="scope">
                <el-select
                  v-model="scope.row.fruitId"
                  filterable
                  placeholder="请选择水果"
                  style="width: 100%"
                  :disabled="!createForm.warehouseId"
                  @change="onFruitChange(scope.row)"
                >
                  <el-option v-for="item in salesFruitOptions" :key="item.id" :label="item.fruitName" :value="item.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="单位" width="80">
              <template #default="scope">
                <span>{{ scope.row.unit || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="可用库存" width="120">
              <template #default="scope">
                <span>{{ formatQty(stockOfFruit(scope.row.fruitId)) }}</span>
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
          <p class="help" v-if="!createForm.warehouseId">请先选择仓库，再选择可用库存水果</p>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="itemsVisible" title="销售明细" width="760px" destroy-on-close>
      <el-table :data="currentItems" class="app-table" size="small" empty-text="暂无明细数据">
        <el-table-column prop="fruitName" label="水果" min-width="120" />
        <el-table-column prop="quantity" label="销售数量" width="110" />
        <el-table-column prop="shippedQty" label="已出数量" width="110" />
        <el-table-column prop="pendingQty" label="待出数量" width="110" />
        <el-table-column prop="unitPrice" label="单价" width="110" />
        <el-table-column prop="subtotal" label="小计" width="110" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="shipVisible" title="分批出库" width="820px" destroy-on-close>
      <el-table :data="shipItems" class="app-table" size="small" empty-text="暂无可出库明细">
        <el-table-column prop="fruitName" label="水果" min-width="120" />
        <el-table-column prop="quantity" label="销售数量" width="100" />
        <el-table-column prop="shippedQty" label="已出" width="90" />
        <el-table-column prop="pendingQty" label="待出" width="90" />
        <el-table-column label="本次出库" width="150">
          <template #default="scope">
            <el-input-number v-model="scope.row.shipQty" :min="0" :max="Number(scope.row.pendingQty || 0)" :precision="2" style="width: 100%" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" @click="submitShip">确认出库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { basicApi, salesApi } from '@/api/modules'
import { useWarehouseStock } from '@/composables/useWarehouseStock'

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已提交', value: 'SUBMITTED' },
  { label: '已审核', value: 'APPROVED' },
  { label: '已出库', value: 'SHIPPED' }
]

const orderStatusLabelMap = {
  DRAFT: '草稿',
  SUBMITTED: '已提交',
  APPROVED: '已审核',
  SHIPPED: '已出库'
}

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({
  pageNo: 1,
  pageSize: 10,
  salesNo: '',
  status: '',
  customerId: null,
  warehouseId: null
})

const customerOptions = ref([])
const warehouseOptions = ref([])
const fruitOptions = ref([])

const { loadWarehouseStock, stockMap, stockOfFruit } = useWarehouseStock()

const fruitMap = computed(() => {
  const map = {}
  fruitOptions.value.forEach((item) => {
    map[item.id] = item
  })
  return map
})

const salesFruitOptions = computed(() => {
  if (!createForm.warehouseId) return []
  return fruitOptions.value.filter((item) => Number(stockMap.value[item.id] || 0) > 0)
})

const createVisible = ref(false)
const createRef = ref()
const createForm = reactive({
  customerId: null,
  warehouseId: null,
  orderTime: '',
  remark: '',
  items: []
})

const createRules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  orderTime: [{ required: true, message: '请选择下单时间', trigger: 'change' }],
  items: [{ type: 'array', min: 1, message: '至少添加一条销售明细', trigger: 'change' }]
}

const itemsVisible = ref(false)
const currentItems = ref([])

const shipVisible = ref(false)
const currentOrderId = ref(null)
const shipItems = ref([])

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
    createForm.items.forEach((item) => {
      if (item.fruitId && Number(stockMap.value[item.fruitId] || 0) <= 0) {
        item.fruitId = null
        item.unit = ''
      }
    })
  }
)

const loadData = async () => {
  loading.value = true
  try {
    const res = await salesApi.orders({ ...query })
    tableData.value = res?.records || []
    total.value = res?.total || 0
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.pageNo = 1
  query.salesNo = ''
  query.status = ''
  query.customerId = null
  query.warehouseId = null
  loadData()
}

const onPageChange = (pageNo) => {
  query.pageNo = pageNo
  loadData()
}

const openCreate = () => {
  createForm.customerId = null
  createForm.warehouseId = null
  createForm.orderTime = ''
  createForm.remark = ''
  createForm.items = []
  createVisible.value = true
}

const addItem = () => {
  createForm.items.push({ fruitId: null, unit: '', quantity: 0, unitPrice: 0, remark: '' })
}

const removeItem = (index) => {
  createForm.items.splice(index, 1)
}

const validateCreateItems = () => {
  if (!createForm.items.length) {
    ElMessage.warning('请至少添加一条销售明细')
    return false
  }
  for (const [index, item] of createForm.items.entries()) {
    const lineNo = index + 1
    if (!item.fruitId) {
      ElMessage.warning(`第${lineNo}行未选择水果`)
      return false
    }
    if (Number(stockOfFruit(item.fruitId)) <= 0) {
      ElMessage.warning(`第${lineNo}行水果无可用库存`)
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
      quantity: item.quantity,
      unitPrice: item.unitPrice,
      remark: item.remark
    }))
  }

  await salesApi.createOrder(payload)
  ElMessage.success('销售单创建成功')
  createVisible.value = false
  query.pageNo = 1
  loadData()
}

const submitOrder = async (row) => {
  if (row.orderStatus !== 'DRAFT') {
    ElMessage.warning('只有草稿状态可以提交')
    return
  }
  await salesApi.submit(row.id)
  ElMessage.success('提交成功')
  loadData()
}

const approveOrder = async (row) => {
  if (row.orderStatus !== 'SUBMITTED') {
    ElMessage.warning('只有已提交状态可以审核')
    return
  }
  await salesApi.approve(row.id)
  ElMessage.success('审核成功')
  loadData()
}

const openItems = async (row) => {
  currentItems.value = await salesApi.items(row.id)
  itemsVisible.value = true
}

const openShip = async (row) => {
  if (row.orderStatus !== 'APPROVED') {
    ElMessage.warning('只有已审核状态可以出库')
    return
  }
  const items = await salesApi.items(row.id)
  shipItems.value = (items || []).map((item) => ({ ...item, shipQty: 0 }))
  currentOrderId.value = row.id
  shipVisible.value = true
}

const submitShip = async () => {
  const shipPayloadItems = shipItems.value
    .filter((item) => Number(item.shipQty || 0) > 0)
    .map((item) => ({ itemId: item.id, shipQty: Number(item.shipQty) }))

  if (!shipPayloadItems.length) {
    ElMessage.warning('请至少填写一条本次出库数量')
    return
  }

  await salesApi.ship(currentOrderId.value, {
    operatorId: getOperatorId(),
    items: shipPayloadItems
  })
  ElMessage.success('出库成功')
  shipVisible.value = false
  await loadData()
  if (itemsVisible.value && currentOrderId.value) {
    currentItems.value = await salesApi.items(currentOrderId.value)
  }
}

const loadOptions = async () => {
  const [customers, warehouses, fruits] = await Promise.all([
    basicApi.customers({ pageNo: 1, pageSize: 200 }),
    basicApi.warehouses(),
    basicApi.fruits({ pageNo: 1, pageSize: 200 })
  ])
  customerOptions.value = customers?.records || customers || []
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

.help {
  margin: 8px 0 0;
  color: var(--text-secondary);
  font-size: 12px;
}
</style>
