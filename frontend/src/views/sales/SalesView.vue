<template>
  <div class="page-padding">
    <div class="card" style="margin-bottom:16px; display:flex; gap:12px; align-items:center; flex-wrap:wrap;">
      <el-input v-model="query.salesNo" placeholder="销售单号" style="width:180px" clearable />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:160px">
        <el-option label="草稿" value="DRAFT" />
        <el-option label="已提交" value="SUBMITTED" />
        <el-option label="已审核" value="APPROVED" />
        <el-option label="已出库" value="SHIPPED" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="openCreate">新建销售单</el-button>
    </div>

    <div class="card">
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="salesNo" label="销售单号" width="160" />
        <el-table-column prop="customerName" label="客户" min-width="140" />
        <el-table-column prop="warehouseName" label="仓库" min-width="140" />
        <el-table-column prop="orderStatus" label="状态" width="110" />
        <el-table-column prop="orderTime" label="下单时间" width="160" />
        <el-table-column prop="totalAmount" label="金额" width="110" />
        <el-table-column label="操作" width="260">
          <template #default="scope">
            <el-button size="small" type="primary" @click="openItems(scope.row)">明细</el-button>
            <el-button size="small" @click="submitOrder(scope.row)">提交</el-button>
            <el-button size="small" type="warning" @click="approveOrder(scope.row)">审核</el-button>
            <el-button size="small" type="success" @click="openShip(scope.row)">出库</el-button>
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

    <!-- 创建销售单 -->
    <el-dialog v-model="createVisible" title="新建销售单" width="720px">
      <el-form :model="createForm" :rules="createRules" ref="createRef" label-width="100px">
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="createForm.customerId" filterable style="width:100%">
            <el-option v-for="c in customerOptions" :key="c.id" :label="c.customerName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="createForm.warehouseId" style="width:100%">
            <el-option v-for="w in warehouseOptions" :key="w.id" :label="w.warehouseName || w.name" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间" prop="orderTime">
          <el-date-picker v-model="createForm.orderTime" type="datetime" style="width:100%" />
        </el-form-item>
        <div class="card" style="padding:12px;">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
            <strong>销售明细</strong>
            <el-button size="small" @click="addItem">添加明细</el-button>
          </div>
          <el-table :data="createForm.items" size="small">
            <el-table-column label="水果" min-width="160">
              <template #default="scope">
                <el-select v-model="scope.row.fruitId" filterable placeholder="水果" style="width:100%">
                  <el-option v-for="f in fruitOptions" :key="f.id" :label="f.fruitName" :value="f.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="120">
              <template #default="scope">
                <el-input-number v-model="scope.row.quantity" :min="0" :precision="2" />
              </template>
            </el-table-column>
            <el-table-column label="单价" width="120">
              <template #default="scope">
                <el-input-number v-model="scope.row.unitPrice" :min="0" :precision="2" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90">
              <template #default="scope">
                <el-button type="text" @click="removeItem(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="createVisible=false">取消</el-button>
        <el-button type="primary" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>

    <!-- 明细查看 -->
    <el-dialog v-model="itemsVisible" title="销售明细" width="680px">
      <el-table :data="currentItems" size="small">
        <el-table-column prop="fruitId" label="水果" />
        <el-table-column prop="batchId" label="批次" />
        <el-table-column prop="quantity" label="数量" />
        <el-table-column prop="unitPrice" label="单价" />
        <el-table-column prop="subtotal" label="小计" />
      </el-table>
    </el-dialog>

    <!-- 出库 -->
    <el-dialog v-model="shipVisible" title="出库" width="420px">
      <el-form :model="shipForm" label-width="100px">
        <el-form-item label="操作人ID">
          <el-input-number v-model="shipForm.operatorId" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible=false">取消</el-button>
        <el-button type="primary" @click="submitShip">确认出库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { salesApi, basicApi } from '@/api/modules'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ pageNo: 1, pageSize: 10, salesNo: '', status: '' })

const createVisible = ref(false)
const createRef = ref()
const createForm = reactive({
  customerId: null,
  warehouseId: null,
  orderTime: '',
  items: []
})
const createRules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  orderTime: [{ required: true, message: '请选择时间', trigger: 'change' }],
  items: [{ type: 'array', min: 1, message: '至少添加一条明细', trigger: 'change' }]
}

const customerOptions = ref([])
const warehouseOptions = ref([])
const fruitOptions = ref([])

const itemsVisible = ref(false)
const currentItems = ref([])

const shipVisible = ref(false)
const shipForm = reactive({ orderId: null, operatorId: 1 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await salesApi.orders({ ...query })
    tableData.value = res.records || res.items || res || []
    total.value = res.total || (res.records ? res.records.length : 0)
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  createForm.customerId = null
  createForm.warehouseId = null
  createForm.orderTime = ''
  createForm.items = []
  createVisible.value = true
}

const addItem = () => {
  createForm.items.push({ fruitId: null, quantity: 0, unitPrice: 0 })
}
const removeItem = (idx) => createForm.items.splice(idx, 1)

const submitCreate = () => {
  createRef.value.validate(async (valid) => {
    if (!valid) return
    await salesApi.createOrder(createForm)
    ElMessage.success('创建成功')
    createVisible.value = false
    loadData()
  })
}

const submitOrder = (row) => {
  if (row.orderStatus !== 'DRAFT') return ElMessage.warning('仅草稿可提交')
  salesApi.submit(row.id).then(() => {
    ElMessage.success('提交成功')
    loadData()
  })
}

const approveOrder = (row) => {
  if (row.orderStatus !== 'SUBMITTED') return ElMessage.warning('仅已提交可审核')
  salesApi.approve(row.id).then(() => {
    ElMessage.success('审核通过')
    loadData()
  })
}

const openItems = async (row) => {
  currentItems.value = await salesApi.items(row.id)
  itemsVisible.value = true
}

const openShip = (row) => {
  if (row.orderStatus !== 'APPROVED') return ElMessage.warning('仅已审核可出库')
  shipForm.orderId = row.id
  shipVisible.value = true
}

const submitShip = async () => {
  try {
    await salesApi.ship(shipForm.orderId, { operatorId: shipForm.operatorId })
    ElMessage.success('出库成功')
    shipVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '库存不足或出库失败')
  }
}

const loadOptions = async () => {
  const [customers, warehouses, fruits] = await Promise.all([
    basicApi.customers({}),
    basicApi.warehouses(),
    basicApi.fruits({})
  ])
  customerOptions.value = customers?.records || customers || []
  warehouseOptions.value = warehouses || []
  fruitOptions.value = fruits?.records || fruits || []
}

onMounted(() => {
  loadOptions()
  loadData()
})
</script>
