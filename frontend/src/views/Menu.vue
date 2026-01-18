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
          <el-icon v-if="scope.row.icon">
            <component :is="getIconComponent(scope.row.icon)" />
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
              <span :style="{ marginLeft: menu.level * 20 + 'px' }">{{ menu.menuName }}</span>
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
  UserFilled, User, Menu as MenuFilled, DocumentCopy, List, Search, Plus, 
  Check, ArrowDown, ArrowUp, ArrowLeft, ArrowRight, SwitchButton, View, 
  House, Avatar, Document, Calendar, Clock, CircleCheck, Refresh, 
  DataAnalysis, Bell, Message, ChatDotRound, Phone, Location, Link, 
  Star, StarFilled, Lock, Unlock, Warning, WarningFilled, InfoFilled, SuccessFilled, QuestionFilled, 
  Close, ZoomIn, ZoomOut, FullScreen, Download, Upload, Sort, 
  Filter, Share, Printer, Files, Folder, FolderOpened, Notebook
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

const iconList = [
  { name: 'HomeFilled', label: '首页', component: HomeFilled },
  { name: 'House', label: '房屋', component: House },
  { name: 'Setting', label: '设置', component: Setting },
  { name: 'Menu', label: '菜单', component: MenuFilled },
  { name: 'List', label: '列表', component: List },
  { name: 'View', label: '视图', component: View },
  { name: 'Operation', label: '操作', component: Operation },
  { name: 'UserFilled', label: '人员', component: UserFilled },
  { name: 'User', label: '用户', component: User },
  { name: 'Avatar', label: '头像', component: Avatar },
  { name: 'OfficeBuilding', label: '部门', component: OfficeBuilding },
  { name: 'DocumentCopy', label: '文档副本', component: DocumentCopy },
  { name: 'Document', label: '文档', component: Document },
  { name: 'Files', label: '文件', component: Files },
  { name: 'Folder', label: '文件夹', component: Folder },
  { name: 'FolderOpened', label: '打开文件夹', component: FolderOpened },
  { name: 'Notebook', label: '笔记本', component: Notebook },
  { name: 'Calendar', label: '日历', component: Calendar },
  { name: 'Clock', label: '时钟', component: Clock },
  { name: 'Edit', label: '编辑', component: Edit },
  { name: 'Delete', label: '删除', component: Delete },
  { name: 'Plus', label: '添加', component: Plus },
  { name: 'Check', label: '确认', component: Check },
  { name: 'CircleCheck', label: '圆形确认', component: CircleCheck },
  { name: 'Search', label: '搜索', component: Search },
  { name: 'Refresh', label: '刷新', component: Refresh },
  { name: 'ZoomIn', label: '放大', component: ZoomIn },
  { name: 'ZoomOut', label: '缩小', component: ZoomOut },
  { name: 'FullScreen', label: '全屏', component: FullScreen },
  { name: 'Sort', label: '排序', component: Sort },
  { name: 'Filter', label: '筛选', component: Filter },
  { name: 'Share', label: '分享', component: Share },
  { name: 'Printer', label: '打印', component: Printer },
  { name: 'Download', label: '下载', component: Download },
  { name: 'Upload', label: '上传', component: Upload },
  { name: 'ArrowUp', label: '向上箭头', component: ArrowUp },
  { name: 'ArrowDown', label: '向下箭头', component: ArrowDown },
  { name: 'ArrowLeft', label: '向左箭头', component: ArrowLeft },
  { name: 'ArrowRight', label: '向右箭头', component: ArrowRight },
  { name: 'SuccessFilled', label: '成功', component: SuccessFilled },
  { name: 'Warning', label: '警告', component: Warning },
  { name: 'WarningFilled', label: '警告填充', component: WarningFilled },
  { name: 'InfoFilled', label: '信息', component: InfoFilled },
  { name: 'QuestionFilled', label: '问题', component: QuestionFilled },
  { name: 'Close', label: '关闭', component: Close },
  { name: 'Lock', label: '锁定', component: Lock },
  { name: 'Unlock', label: '解锁', component: Unlock },
  { name: 'Star', label: '星星', component: Star },
  { name: 'StarFilled', label: '星星填充', component: StarFilled },
  { name: 'SwitchButton', label: '开关', component: SwitchButton },
  { name: 'Bell', label: '铃铛', component: Bell },
  { name: 'Message', label: '消息', component: Message },
  { name: 'ChatDotRound', label: '聊天圆点', component: ChatDotRound },
  { name: 'Phone', label: '电话', component: Phone },
  { name: 'Location', label: '位置', component: Location },
  { name: 'Link', label: '链接', component: Link },
  { name: 'DataAnalysis', label: '数据分析', component: DataAnalysis }
]

const getIconComponent = (iconName) => {
  const icon = iconList.find(item => item.name === iconName)
  return icon ? icon.component : null
}

const menuRules = reactive({
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }]
})

const loadMenuData = async () => {
  try {
    const response = await getMenuTree()
    if (response.code === 200) {
      const processMenuTree = (menus) => {
        return menus.map(menu => {
          const processedMenu = { ...menu }
          processedMenu.hasChildren = processedMenu.children && processedMenu.children.length > 0
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

const handleAddMenu = () => {
  dialogTitle.value = '添加菜单'
  resetForm()
  dialogVisible.value = true
}

const handleEditMenu = async (menu) => {
  dialogTitle.value = '编辑菜单'
  try {
    const response = await getMenuById(menu.id)
    if (response.code === 200) {
      const menuData = response.data
      if (menuData.icon && !iconList.find(item => item.name === menuData.icon)) {
        menuData.icon = ''
      }
      Object.assign(menuForm, menuData)
      dialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取菜单详情失败')
  }
}

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

const handleStatusChange = async (menu) => {
  try {
    await updateMenu(menu)
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    menu.status = menu.status === 1 ? 0 : 1
  }
}

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
