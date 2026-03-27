<template>
  <div class="page-padding">
    <div class="card" style="margin-bottom:16px; display:flex; gap:12px; align-items:center; flex-wrap:wrap;">
      <el-input v-model="query.purchaseNo" placeholder="采购单号" style="width:180px" clearable />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:160px">
        <el-option label="草稿" value="DRAFT" />
        <el-option label="已提交" value="SUBMITTED" />
        <el-option label="已审核" value="APPROVED" />
        <el-option label="已入库" value="RECEIVED" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="openCreate">新建采购单</el-button>
    </div>

    <div class="card">
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="purchaseNo" label="采购单号" width="160" />
        <el-table-column prop="supplierName" label="供应商" min-width="140" />
        <el-table-column prop="warehouseName" label="仓库" min-width="140" />
        <el-table-column prop="orderStatus" label="状态" width="110" />
        <el-table-column prop="orderDate" label="下单日期" width="120" />
        <el-table-column prop="expectedArrivalDate" label="预计到货" width="120" />
        <el-table-column prop="totalAmount" label="金额" width="110" />
        <el-table-column label="操作" width="260">
          <template #default="scope">
            <el-button size="small" type="primary" @click="openItems(scope.row)">明细</el-button>
            <el-button size="small" @click="submitOrder(scope.row)">提交</el-button>
            <el-button size="small" type="warning" @click="approveOrder(scope.row)">审核</el-button>
            <el-button size="small" type="success" @click="openReceive(scope.row)">入库</el-button>
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

    <!-- 创建采购单 -->
    <el-dialog v-model="createVisible" title="新建采购单" width="720px">
      <el-form :model="createForm" :rules="createRules" ref="createRef" label-width="100px">
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="createForm.supplierId" filterable style="width:100%">
            <el-option v-for="s in supplierOptions" :key="s.id" :label="s.supplierName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="createForm.warehouseId" style="width:100%">
            <el-option v-for="w in warehouseOptions" :key="w.id" :label="w.warehouseName || w.name" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单日期" prop="orderDate">
          <el-date-picker v-model="createForm.orderDate" type="date" style="width:100%" />
        </el-form-item>
        <el-form-item label="预计到货" prop="expectedArrivalDate">
          <el-date-picker v-model="createForm.expectedArrivalDate" type="date" style="width:100%" />
        </el-form-item>
        <div class="card" style="padding:12px;">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
            <strong>采购明细</strong>
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
            <el-table-column label="批次号" width="140">
              <template #default="scope">
                <el-input v-model="scope.row.batchNo" />
              </template>
            </el-table-column>
            <el-table-column label="到期日" width="150">
              <template #default="scope">
                <el-date-picker v-model="scope.row.expirationDate" type="date" style="width:100%" />
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
    <el-dialog v-model="itemsVisible" title="采购明细" width="680px">
      <el-table :data="currentItems" size="small">
        <el-table-column prop="fruitId" label="水果" />
        <el-table-column prop="batchNo" label="批次号" />
        <el-table-column prop="quantity" label="订购量" />
        <el-table-column prop="receivedQty" label="已收" />
        <el-table-column prop="unitPrice" label="单价" />
        <el-table-column prop="expirationDate" label="到期" />
      </el-table>
    </el-dialog>

    <!-- 入库 -->
    <el-dialog v-model="receiveVisible" title="入库确认" width="720px">
      <el-table :data="receiveItems" size="small">
        <el-table-column prop="batchNo" label="批次号" width="140" />
        <el-table-column prop="quantity" label="订购量" width="120" />
        <el-table-column prop="receivedQty" label="已收" width="120" />
        <el-table-column label="本次入库" width="160">
          <template #default="scope">
            <el-input-number v-model="scope.row.receivedQtyInput" :min="0" :max="scope.row.quantity" :precision="2" />
          </template>
        </el-table-column>
        <el-table-column label="单位成本" width="140">
          <template #default="scope">
            <el-input-number v-model="scope.row.unitCost" :min="0" :precision="2" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="receiveVisible=false">取消</el-button>
        <el-button type="primary" @click="submitReceive">确认入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { purchaseApi, basicApi } from '@/api/modules'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ pageNo: 1, pageSize: 10, purchaseNo: '', status: '' })

const createVisible = ref(false)
const createRef = ref()
const createForm = reactive({
  supplierId: null,
  warehouseId: null,
  orderDate: '',
  expectedArrivalDate: '',
  items: []
})
const createRules = {
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  orderDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  items: [{ type: 'array', min: 1, message: '至少添加一条明细', trigger: 'change' }]
}

const supplierOptions = ref([])
const warehouseOptions = ref([])
const fruitOptions = ref([])

const itemsVisible = ref(false)
const currentItems = ref([])

const receiveVisible = ref(false)
const currentOrderId = ref(null)
const receiveItems = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await purchaseApi.orders({ ...query })
    tableData.value = res.records || res.items || res || []
    total.value = res.total || (res.records ? res.records.length : 0)
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  createForm.supplierId = null
  createForm.warehouseId = null
  createForm.orderDate = ''
  createForm.expectedArrivalDate = ''
  createForm.items = []
  createVisible.value = true
}

const addItem = () => {
  createForm.items.push({ fruitId: null, batchNo: '', expirationDate: '', quantity: 0, unitPrice: 0 })
}
const removeItem = (idx) => createForm.items.splice(idx, 1)

const submitCreate = () => {
  createRef.value.validate(async (valid) => {
    if (!valid) return
    await purchaseApi.createOrder(createForm)
    ElMessage.success('创建成功')
    createVisible.value = false
    loadData()
  })
}

const submitOrder = (row) => {
  if (row.orderStatus !== 'DRAFT') return ElMessage.warning('仅草稿可提交')
  purchaseApi.submit(row.id).then(() => {
    ElMessage.success('提交成功')
    loadData()
  })
}

const approveOrder = (row) => {
  if (row.orderStatus !== 'SUBMITTED') return ElMessage.warning('仅已提交可审核')
  purchaseApi.approve(row.id).then(() => {
    ElMessage.success('审核通过')
    loadData()
  })
}

const openItems = async (row) => {
  currentItems.value = await purchaseApi.items(row.id)
  itemsVisible.value = true
}

const openReceive = async (row) => {
  if (row.orderStatus !== 'APPROVED') return ElMessage.warning('仅已审核可入库')
  const items = await purchaseApi.items(row.id)
  receiveItems.value = items.map((it) => ({ ...it, receivedQtyInput: 0, unitCost: it.unitPrice }))
  currentOrderId.value = row.id
  receiveVisible.value = true
}

const submitReceive = async () => {
  const payload = {
    operatorId: 1,
    items: receiveItems.value.map((it) => ({ itemId: it.id, receivedQty: it.receivedQtyInput, unitCost: it.unitCost }))
  }
  await purchaseApi.receive(currentOrderId.value, payload)
  ElMessage.success('入库成功')
  receiveVisible.value = false
  loadData()
}

const loadOptions = async () => {
  const [suppliers, warehouses, fruits] = await Promise.all([
    basicApi.suppliers({}),
    basicApi.warehouses(),
    basicApi.fruits({})
  ])
  supplierOptions.value = suppliers?.records || suppliers || []
  warehouseOptions.value = warehouses || []
  fruitOptions.value = fruits?.records || fruits || []
}

onMounted(() => {
  loadOptions()
  loadData()
})
</script>
