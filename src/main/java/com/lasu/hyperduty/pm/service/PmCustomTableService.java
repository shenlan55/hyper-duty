package com.lasu.hyperduty.pm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.common.dto.TaskBindingDTO;
import com.lasu.hyperduty.pm.entity.PmCustomTable;
import com.lasu.hyperduty.pm.entity.PmCustomTableColumn;
import com.lasu.hyperduty.pm.entity.PmCustomTableRow;
import com.lasu.hyperduty.pm.service.PmCustomTableService;
import java.util.List;








public interface PmCustomTableService extends IService<PmCustomTable> {

    Page<PmCustomTable> pageList(Integer pageNum, Integer pageSize, Long projectId);

    PmCustomTable createTable(PmCustomTable table, List<PmCustomTableColumn> columns);

    PmCustomTable updateTable(PmCustomTable table, List<PmCustomTableColumn> columns);

    void deleteTable(Long id);

    List<PmCustomTableColumn> getColumns(Long tableId);

    List<PmCustomTableRow> getRows(Long tableId);

    PmCustomTableRow createRow(Long tableId, String rowData);

    PmCustomTableRow updateRow(Long id, String rowData);

    void deleteRow(Long id);

    void reorderRows(Long tableId, List<Long> rowIds);

    List<TaskBindingDTO> getTaskBindings(Long taskId, Long employeeId);

    void bindRow(Long taskId, Long tableId, Long rowId, String orderNo, String title, Long employeeId);

    void unbindRow(Long bindingId, Long taskId, Long employeeId);
}
