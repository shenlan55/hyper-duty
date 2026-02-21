import { ref, reactive, computed } from 'vue'

/**
 * 分页相关的hooks
 * @param {Object} options 配置选项
 * @param {number} options.currentPage 默认当前页码
 * @param {number} options.pageSize 默认每页大小
 * @param {number[]} options.pageSizes 可选的每页大小列表
 * @param {number} options.total 默认总记录数
 * @returns {Object} 分页相关的状态和方法
 */
export function usePagination(options = {}) {
  const {
    currentPage: defaultCurrentPage = 1,
    pageSize: defaultPageSize = 10,
    pageSizes = [10, 20, 50, 100],
    total: defaultTotal = 0
  } = options

  // 当前页码
  const currentPage = ref(defaultCurrentPage)
  // 每页大小
  const pageSize = ref(defaultPageSize)
  // 总记录数
  const total = ref(defaultTotal)
  // 可选的每页大小列表
  const pageSizesList = ref(pageSizes)

  // 分页配置
  const pagination = reactive({
    currentPage: defaultCurrentPage,
    pageSize: defaultPageSize,
    pageSizes: pageSizes,
    total: defaultTotal
  })

  // 监听currentPage和pageSize的变化，更新pagination对象
  currentPage.value = defaultCurrentPage
  pageSize.value = defaultPageSize

  // 处理页码变化
  const handleCurrentChange = (val) => {
    currentPage.value = val
    pagination.currentPage = val
  }

  // 处理每页大小变化
  const handleSizeChange = (val) => {
    pageSize.value = val
    pagination.pageSize = val
    currentPage.value = 1
    pagination.currentPage = 1
  }

  // 更新总记录数
  const updateTotal = (val) => {
    total.value = val
    pagination.total = val
  }

  // 重置分页状态
  const resetPagination = () => {
    currentPage.value = defaultCurrentPage
    pageSize.value = defaultPageSize
    total.value = defaultTotal
    pagination.currentPage = defaultCurrentPage
    pagination.pageSize = defaultPageSize
    pagination.total = defaultTotal
  }

  // 获取分页参数
  const getPageParams = computed(() => {
    return {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
  })

  return {
    // 状态
    currentPage,
    pageSize,
    total,
    pageSizesList,
    pagination,
    
    // 方法
    handleCurrentChange,
    handleSizeChange,
    updateTotal,
    resetPagination,
    getPageParams
  }
}

/**
 * 带搜索功能的分页hooks
 * @param {Object} options 配置选项
 * @returns {Object} 分页和搜索相关的状态和方法
 */
export function useSearchPagination(options = {}) {
  const pagination = usePagination(options)
  
  // 搜索关键字
  const searchQuery = ref('')
  
  // 处理搜索
  const handleSearch = (val) => {
    searchQuery.value = val
    // 搜索时重置到第一页
    pagination.currentPage.value = 1
    pagination.pagination.currentPage = 1
  }
  
  // 重置搜索
  const resetSearch = () => {
    searchQuery.value = ''
  }
  
  // 重置所有状态
  const resetAll = () => {
    pagination.resetPagination()
    resetSearch()
  }
  
  return {
    ...pagination,
    searchQuery,
    handleSearch,
    resetSearch,
    resetAll
  }
}
