package com.lasu.hyperduty.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.dto.TaskBindingDTO;
import com.lasu.hyperduty.entity.PmCustomTable;
import com.lasu.hyperduty.entity.PmCustomTableColumn;
import com.lasu.hyperduty.entity.PmCustomTableRow;
import com.lasu.hyperduty.entity.SysEmployee;
import com.lasu.hyperduty.service.PmCustomTableService;
import com.lasu.hyperduty.service.SysEmployeeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/pm/custom-table")
@RequiredArgsConstructor
public class PmCustomTableController {

    private final PmCustomTableService pmCustomTableService;
    private final SysEmployeeService sysEmployeeService;

    @GetMapping("/page")
    public ResponseResult<Page<PmCustomTable>> pageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long projectId) {
        Page<PmCustomTable> page = pmCustomTableService.pageList(pageNum, pageSize, projectId);
        return ResponseResult.success(page);
    }

    @GetMapping("/{id}")
    public ResponseResult<PmCustomTable> getTableDetail(@PathVariable Long id) {
        PmCustomTable table = pmCustomTableService.getById(id);
        return ResponseResult.success(table);
    }

    @GetMapping("/{id}/columns")
    public ResponseResult<List<PmCustomTableColumn>> getColumns(@PathVariable Long id) {
        List<PmCustomTableColumn> columns = pmCustomTableService.getColumns(id);
        return ResponseResult.success(columns);
    }

    @GetMapping("/{id}/rows")
    public ResponseResult<List<PmCustomTableRow>> getRows(@PathVariable Long id) {
        List<PmCustomTableRow> rows = pmCustomTableService.getRows(id);
        return ResponseResult.success(rows);
    }

    @PostMapping
    public ResponseResult<PmCustomTable> createTable(@RequestBody CreateTableRequest request) {
        PmCustomTable created = pmCustomTableService.createTable(request.getTable(), request.getColumns());
        return ResponseResult.success(created);
    }

    @PutMapping
    public ResponseResult<PmCustomTable> updateTable(@RequestBody CreateTableRequest request) {
        PmCustomTable updated = pmCustomTableService.updateTable(request.getTable(), request.getColumns());
        return ResponseResult.success(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteTable(@PathVariable Long id) {
        pmCustomTableService.deleteTable(id);
        return ResponseResult.success();
    }

    @PostMapping("/{tableId}/row")
    public ResponseResult<PmCustomTableRow> createRow(
            @PathVariable Long tableId,
            @RequestBody RowDataRequest request) {
        PmCustomTableRow created = pmCustomTableService.createRow(tableId, request.getRowData());
        return ResponseResult.success(created);
    }

    @PutMapping("/row/{id}")
    public ResponseResult<PmCustomTableRow> updateRow(
            @PathVariable Long id,
            @RequestBody RowDataRequest request) {
        PmCustomTableRow updated = pmCustomTableService.updateRow(id, request.getRowData());
        return ResponseResult.success(updated);
    }

    @DeleteMapping("/row/{id}")
    public ResponseResult<Void> deleteRow(@PathVariable Long id) {
        pmCustomTableService.deleteRow(id);
        return ResponseResult.success();
    }

    @PostMapping("/{tableId}/reorder")
    public ResponseResult<Void> reorderRows(
            @PathVariable Long tableId,
            @RequestBody ReorderRequest request) {
        pmCustomTableService.reorderRows(tableId, request.getRowIds());
        return ResponseResult.success();
    }

    /**
     * 获取当前登录用户的 employeeId
     */
    private Long getCurrentEmployeeId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        String username = authentication.getName();
        LambdaQueryWrapper<SysEmployee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysEmployee::getUsername, username);
        SysEmployee employee = sysEmployeeService.getOne(wrapper);
        return employee != null ? employee.getId() : null;
    }

    @GetMapping("/task/{taskId}/bindings")
    public ResponseResult<List<TaskBindingDTO>> getTaskBindings(@PathVariable Long taskId) {
        Long employeeId = getCurrentEmployeeId();
        List<TaskBindingDTO> bindings = pmCustomTableService.getTaskBindings(taskId, employeeId);
        return ResponseResult.success(bindings);
    }

    @PostMapping("/task/{taskId}/bind")
    public ResponseResult<Void> bindRow(
            @PathVariable Long taskId,
            @RequestBody BindRequest request) {
        Long employeeId = getCurrentEmployeeId();
        pmCustomTableService.bindRow(taskId, request.getTableId(), request.getRowId(), request.getOrderNo(), employeeId);
        return ResponseResult.success();
    }

    @DeleteMapping("/task/{taskId}/bind/{bindingId}")
    public ResponseResult<Void> unbindRow(
            @PathVariable Long taskId,
            @PathVariable Long bindingId) {
        Long employeeId = getCurrentEmployeeId();
        pmCustomTableService.unbindRow(bindingId, taskId, employeeId);
        return ResponseResult.success();
    }

    @Data
    public static class CreateTableRequest {
        private PmCustomTable table;
        private List<PmCustomTableColumn> columns;
    }

    @Data
    public static class RowDataRequest {
        private String rowData;
    }

    @Data
    public static class BindRequest {
        private Long tableId;
        private Long rowId;
        private String orderNo;
    }

    @Data
    public static class ReorderRequest {
        private List<Long> rowIds;
    }
}
