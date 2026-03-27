import { ref } from 'vue'
import { inventoryApi } from '@/api/modules'

export function useWarehouseStock() {
  const loadingStock = ref(false)
  const stockMap = ref({})

  const clearStock = () => {
    stockMap.value = {}
  }

  const loadWarehouseStock = async (warehouseId) => {
    if (!warehouseId) {
      clearStock()
      return
    }
    loadingStock.value = true
    try {
      const response = await inventoryApi.batches({
        pageNo: 1,
        pageSize: 500,
        warehouseId
      })
      const records = response?.records || []
      const nextMap = {}
      records.forEach((row) => {
        const fruitId = row.fruitId
        const available = Number(row.availableQty || 0)
        if (!fruitId) return
        if (!nextMap[fruitId]) {
          nextMap[fruitId] = 0
        }
        nextMap[fruitId] += available
      })
      stockMap.value = nextMap
    } finally {
      loadingStock.value = false
    }
  }

  const stockOfFruit = (fruitId) => Number(stockMap.value[fruitId] || 0)

  return {
    loadingStock,
    stockMap,
    clearStock,
    loadWarehouseStock,
    stockOfFruit
  }
}
