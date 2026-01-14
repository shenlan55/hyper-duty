<template>
  <div class="menu-container">
    <h2>菜单管理</h2>
    <el-button type="primary" @click="handleAddMenu" style="margin-bottom: 20px">+ 添加菜单</el-button>
    
    <el-table :data="menuList" style="width: 100%" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" row-key="id">
      <el-table-column prop="menuName" label="菜单名称" min-width="200">
        <template #default="scope">
          <el-icon v-if="scope.row.type === 1"><OfficeBuilding /></el-icon>
          <el-icon v-else-if="scope.row.type === 2"><Menu /></el-icon>
          <el-icon v-else><Operation /></el-icon>
          {{ scope.row.menuName }}
        </template>
      </el-table-column>
      <el-table-column prop="type" label="菜单类型" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.type === 1" type="success">目录</el-tag>
          <el-tag v-else-if="scope.row.type === 2" type="primary">菜单</el-tag>
          <el-tag v-else type="info">按钮</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="路由路径" min-width="150" />
      <el-table-column prop="component" label="组件路径" min-width="200" />
      <el-table-column prop="perm" label="权限标识" min-width="150" />
      <el-table-column prop="icon" label="菜单图标" width="120">
        <template #default="scope">
          <el-icon>
            <!-- 根据图标名称渲染对应的组件 -->
            <component :is="iconList.find(item => item.name === scope.row.icon)?.component" v-if="scope.row.icon" />
          </el-icon>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEditMenu(scope.row)" style="margin-right: 8px">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="handleDeleteMenu(scope.row.id)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑菜单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form :model="menuForm" :rules="menuRules" ref="menuFormRef" label-width="100px">
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="menuForm.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="父菜单" prop="parentId">
          <el-select v-model="menuForm.parentId" placeholder="请选择父菜单">
            <el-option label="顶级菜单" :value="0" />
            <el-option
              v-for="menu in allMenus"
              :key="menu.id"
              :label="menu.menuName"
              :value="menu.id"
              :disabled="menu.type === 3"
            >
              <span style="margin-left: {{ menu.level * 20 }}px">{{ menu.menuName }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="菜单类型" prop="type">
          <el-select v-model="menuForm.type" placeholder="请选择菜单类型">
            <el-option label="目录" :value="1" />
            <el-option label="菜单" :value="2" />
            <el-option label="按钮" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="路由路径" prop="path" v-if="menuForm.type !== 3">
          <el-input v-model="menuForm.path" placeholder="请输入路由路径" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component" v-if="menuForm.type === 2">
          <el-input v-model="menuForm.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item label="权限标识" prop="perm">
          <el-input v-model="menuForm.perm" placeholder="请输入权限标识" />
        </el-form-item>
        <el-form-item label="菜单图标" prop="icon">
          <el-select v-model="menuForm.icon" placeholder="请选择菜单图标">
            <el-option label="无" value="" />
            <el-option
              v-for="icon in iconList"
              :key="icon.name"
              :label="icon.label"
              :value="icon.name"
            >
              <div style="display: flex; align-items: center; padding: 4px 0;">
                <el-icon style="margin-right: 8px;">
                  <component :is="icon.component" />
                </el-icon>
                <span>{{ icon.label }} ({{ icon.name }})</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="menuForm.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="menuForm.status"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Menu, OfficeBuilding, Operation, Edit, Delete, HomeFilled, Setting, 
  OfficeBuilding as OfficeBuildingFilled, UserFilled, User, Menu as MenuFilled,
  DocumentCopy, 
  List, Search, Plus, Check, ArrowDown, ArrowUp, ArrowLeft, ArrowRight,
  SwitchButton, View, House
} from '@element-plus/icons-vue'
import { getMenuList, getMenuById, addMenu, updateMenu, deleteMenu, getMenuTree } from '../api/menu'

const menuList = ref([])
const allMenus = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('添加菜单')
const menuFormRef = ref()
const menuForm = reactive({
  id: null,
  menuName: '',
  parentId: 0,
  path: '',
  component: '',
  perm: '',
  type: 1,
  icon: '',
  sort: 0,
  status: 1
})

// 图标列表，用于选择
const iconList = [
  // 基础图标
  { name: 'HomeFilled', label: '首页', component: HomeFilled },
  { name: 'House', label: '房屋', component: House },
  { name: 'Setting', label: '设置', component: Setting },
  { name: 'Menu', label: '菜单', component: MenuFilled },
  { name: 'List', label: '列表', component: List },
  { name: 'View', label: '视图', component: View },
  { name: 'Operation', label: '操作', component: Operation },
  
  // 人员/用户相关
  { name: 'UserFilled', label: '人员', component: UserFilled },
  { name: 'User', label: '用户', component: User },
  
  // 部门/组织相关
  { name: 'OfficeBuilding', label: '部门', component: OfficeBuildingFilled },
  
  // 文档相关
  { name: 'DocumentCopy', label: '文档副本', component: DocumentCopy },
  
  // 操作相关
  { name: 'Edit', label: '编辑', component: Edit },
  { name: 'Delete', label: '删除', component: Delete },
  { name: 'Plus', label: '添加', component: Plus },
  { name: 'Check', label: '确认', component: Check },
  { name: 'Search', label: '搜索', component: Search },
  
  // 箭头相关
  { name: 'ArrowUp', label: '向上箭头', component: ArrowUp },
  { name: 'ArrowDown', label: '向下箭头', component: ArrowDown },
  { name: 'ArrowLeft', label: '向左箭头', component: ArrowLeft },
  { name: 'ArrowRight', label: '向右箭头', component: ArrowRight },
  
  // 其他
  { name: 'SwitchButton', label: '开关', component: SwitchButton }
]

const menuRules = reactive({
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }]
})

// 加载菜单数据
const loadMenuData = async () => {
  try {
    const response = await getMenuTree()
    if (response.code === 200) {
      // 处理菜单数据，添加hasChildren属性，用于树形表格显示
      const processMenuTree = (menus) => {
        return menus.map(menu => {
          // 深拷贝菜单对象
          const processedMenu = { ...menu }
          // 设置hasChildren属性
          processedMenu.hasChildren = processedMenu.children && processedMenu.children.length > 0
          // 递归处理子菜单
          if (processedMenu.children && processedMenu.children.length > 0) {
            processedMenu.children = processMenuTree(processedMenu.children)
          }
          return processedMenu
        })
      }
      
      const menuData = processMenuTree(response.data)
      allMenus.value = flattenMenuTree(menuData)
      menuList.value = menuData
    }
  } catch (error) {
    ElMessage.error('获取菜单列表失败')
  }
}

// 扁平化菜单树
const flattenMenuTree = (menus, level = 0) => {
  let result = []
  menus.forEach(menu => {
    const menuWithLevel = { ...menu, level }
    result.push(menuWithLevel)
    if (menu.children && menu.children.length > 0) {
      result = result.concat(flattenMenuTree(menu.children, level + 1))
    }
  })
  return result
}

// 处理添加菜单
const handleAddMenu = () => {
  dialogTitle.value = '添加菜单'
  resetForm()
  dialogVisible.value = true
}

// 处理编辑菜单
const handleEditMenu = async (menu) => {
  dialogTitle.value = '编辑菜单'
  try {
    const response = await getMenuById(menu.id)
    if (response.code === 200) {
      Object.assign(menuForm, response.data)
      dialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取菜单详情失败')
  }
}

// 处理删除菜单
const handleDeleteMenu = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该菜单吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await deleteMenu(id)
    if (response.code === 200) {
      ElMessage.success('删除菜单成功')
      loadMenuData()
    }
  } catch (error) {
    if (error.message !== 'cancel') {
      ElMessage.error('删除菜单失败')
    }
  }
}

// 处理状态变更
const handleStatusChange = async (menu) => {
  try {
    await updateMenu(menu)
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    // 恢复原来的状态
    menu.status = menu.status === 1 ? 0 : 1
  }
}

// 重置表单
const resetForm = () => {
  menuForm.id = null
  menuForm.menuName = ''
  menuForm.parentId = 0
  menuForm.path = ''
  menuForm.component = ''
  menuForm.perm = ''
  menuForm.type = 1
  menuForm.icon = ''
  menuForm.sort = 0
  menuForm.status = 1
  if (menuFormRef.value) {
    menuFormRef.value.resetFields()
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!menuFormRef.value) return
  await menuFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let response
        if (menuForm.id) {
          response = await updateMenu(menuForm)
        } else {
          response = await addMenu(menuForm)
        }
        if (response.code === 200) {
          ElMessage.success(menuForm.id ? '更新菜单成功' : '添加菜单成功')
          dialogVisible.value = false
          loadMenuData()
        }
      } catch (error) {
        ElMessage.error(menuForm.id ? '更新菜单失败' : '添加菜单失败')
      }
    }
  })
}

// 组件挂载时加载数据
onMounted(() => {
  loadMenuData()
})
</script>

<style scoped>
.menu-container {
  padding: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
