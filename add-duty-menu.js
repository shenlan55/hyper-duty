// 用于添加值班管理菜单的脚本
// 可以通过前端控制台或Node.js运行此脚本

// 菜单数据
const menuData = [
  // 值班管理目录菜单
  {
    menuName: '值班管理',
    parentId: 0, // 顶级菜单
    path: '/duty',
    component: 'views/duty/DutyLayout.vue',
    perm: '',
    type: 1, // 1=目录
    icon: 'Calendar',
    sort: 3, // 排在系统管理后面
    status: 1
  },
  // 值班表管理菜单
  {
    menuName: '值班表管理',
    parentId: null, // 会在添加父菜单后更新
    path: '/duty/schedule',
    component: 'views/duty/DutySchedule.vue',
    perm: 'duty:schedule:list',
    type: 2, // 2=菜单
    icon: 'DocumentCopy',
    sort: 1,
    status: 1
  },
  // 值班安排菜单
  {
    menuName: '值班安排',
    parentId: null, // 会在添加父菜单后更新
    path: '/duty/assignment',
    component: 'views/duty/DutyAssignment.vue',
    perm: 'duty:assignment:list',
    type: 2, // 2=菜单
    icon: 'CalendarCheck',
    sort: 2,
    status: 1
  },
  // 值班记录菜单
  {
    menuName: '值班记录',
    parentId: null, // 会在添加父菜单后更新
    path: '/duty/record',
    component: 'views/duty/DutyRecord.vue',
    perm: 'duty:record:list',
    type: 2, // 2=菜单
    icon: 'Document',
    sort: 3,
    status: 1
  }
];

// 模拟API请求函数
const request = async (url, method, data) => {
  const response = await fetch(`http://localhost:8080/api${url}`, {
    method: method,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    },
    body: data ? JSON.stringify(data) : null
  });
  return response.json();
};

// 添加菜单的函数
const addMenu = async (menu) => {
  return request('/menu', 'POST', menu);
};

// 执行添加菜单的逻辑
const addDutyMenu = async () => {
  try {
    // 1. 添加值班管理目录菜单
    console.log('添加值班管理目录菜单...');
    const dutyMenuResponse = await addMenu(menuData[0]);
    const dutyMenuId = dutyMenuResponse.data.id;
    console.log('值班管理目录菜单添加成功，ID:', dutyMenuId);
    
    // 2. 更新子菜单的parentId
    menuData[1].parentId = dutyMenuId;
    menuData[2].parentId = dutyMenuId;
    menuData[3].parentId = dutyMenuId;
    
    // 3. 添加子菜单
    for (let i = 1; i < menuData.length; i++) {
      const subMenu = menuData[i];
      console.log(`添加${subMenu.menuName}菜单...`);
      const subMenuResponse = await addMenu(subMenu);
      console.log(`${subMenu.menuName}菜单添加成功，ID:`, subMenuResponse.data.id);
    }
    
    console.log('所有值班管理菜单添加成功！');
  } catch (error) {
    console.error('添加菜单失败:', error);
  }
};

// 如果在浏览器环境中运行
if (typeof window !== 'undefined') {
  // 将函数暴露给全局，以便在控制台调用
  window.addDutyMenu = addDutyMenu;
  console.log('添加值班管理菜单的函数已就绪，请调用 addDutyMenu() 执行添加操作。');
} else {
  // 如果在Node.js环境中运行，直接执行
  addDutyMenu();
}
