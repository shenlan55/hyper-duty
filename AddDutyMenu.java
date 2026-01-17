import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 用于添加值班管理菜单的Java程序
 */
public class AddDutyMenu {
    
    // 数据库连接信息
    private static final String URL = "jdbc:mysql://localhost:3306/hyper_duty?useUnicode=true&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&useLegacyDatetimeCode=false";
    private static final String USER = "root";
    private static final String PASSWORD = "Feiwu2023!";
    
    public static void main(String[] args) {
        Connection connection = null;
        Statement statement = null;
        
        try {
            // 加载驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 建立连接
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            statement = connection.createStatement();
            
            System.out.println("连接数据库成功！");
            
            // 1. 添加值班管理目录菜单
            String addDutyMenu = "INSERT INTO sys_menu (menu_name, parent_id, path, component, perm, type, icon, sort, status, create_time, update_time) "
                              + "VALUES ('值班管理', 0, '/duty', 'views/duty/DutyLayout.vue', '', 1, 'Calendar', 3, 1, NOW(), NOW())";
            
            statement.executeUpdate(addDutyMenu, Statement.RETURN_GENERATED_KEYS);
            
            // 获取刚插入的值班管理目录ID
            long dutyMenuId = 0;
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                dutyMenuId = generatedKeys.getLong(1);
            }
            
            System.out.println("添加值班管理目录菜单成功，ID: " + dutyMenuId);
            
            // 2. 添加值班表管理菜单
            String addScheduleMenu = "INSERT INTO sys_menu (menu_name, parent_id, path, component, perm, type, icon, sort, status, create_time, update_time) "
                                  + "VALUES ('值班表管理', ?, '/duty/schedule', 'views/duty/DutySchedule.vue', 'duty:schedule:list', 2, 'DocumentCopy', 1, 1, NOW(), NOW())";
            
            PreparedStatement scheduleStmt = connection.prepareStatement(addScheduleMenu);
            scheduleStmt.setLong(1, dutyMenuId);
            scheduleStmt.executeUpdate();
            System.out.println("添加值班表管理菜单成功");
            
            // 3. 添加值班安排菜单
            String addAssignmentMenu = "INSERT INTO sys_menu (menu_name, parent_id, path, component, perm, type, icon, sort, status, create_time, update_time) "
                                    + "VALUES ('值班安排', ?, '/duty/assignment', 'views/duty/DutyAssignment.vue', 'duty:assignment:list', 2, 'Calendar', 2, 1, NOW(), NOW())";
            
            PreparedStatement assignmentStmt = connection.prepareStatement(addAssignmentMenu);
            assignmentStmt.setLong(1, dutyMenuId);
            assignmentStmt.executeUpdate();
            System.out.println("添加值班安排菜单成功");
            
            // 4. 添加值班记录菜单
            String addRecordMenu = "INSERT INTO sys_menu (menu_name, parent_id, path, component, perm, type, icon, sort, status, create_time, update_time) "
                                + "VALUES ('值班记录', ?, '/duty/record', 'views/duty/DutyRecord.vue', 'duty:record:list', 2, 'Document', 3, 1, NOW(), NOW())";
            
            PreparedStatement recordStmt = connection.prepareStatement(addRecordMenu);
            recordStmt.setLong(1, dutyMenuId);
            recordStmt.executeUpdate();
            System.out.println("添加值班记录菜单成功");
            
            // 5. 查询添加的菜单，验证结果
            String queryMenus = "SELECT * FROM sys_menu WHERE parent_id = " + dutyMenuId + " OR id = " + dutyMenuId;
            ResultSet resultSet = statement.executeQuery(queryMenus);
            
            System.out.println("\n添加的菜单列表：");
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String menuName = resultSet.getString("menu_name");
                long parentId = resultSet.getLong("parent_id");
                String path = resultSet.getString("path");
                String component = resultSet.getString("component");
                int type = resultSet.getInt("type");
                String icon = resultSet.getString("icon");
                int status = resultSet.getInt("status");
                
                System.out.println("ID: " + id + ", 菜单名称: " + menuName + ", 父ID: " + parentId + ", 路径: " + path + ", 组件: " + component + ", 类型: " + type + ", 图标: " + icon + ", 状态: " + status);
            }
            
            System.out.println("\n所有值班管理菜单添加成功！");
            
        } catch (ClassNotFoundException e) {
            System.out.println("找不到MySQL驱动类：" + e.getMessage());
        } catch (SQLException e) {
            System.out.println("数据库操作失败：" + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.out.println("关闭资源失败：" + e.getMessage());
            }
        }
    }
}
