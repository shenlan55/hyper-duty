package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.dto.TaskBindingDTO;
import com.lasu.hyperduty.entity.PmCustomTable;
import com.lasu.hyperduty.entity.PmCustomTableColumn;
import com.lasu.hyperduty.entity.PmCustomTableRow;
import com.lasu.hyperduty.entity.PmTaskCustomRow;
import com.lasu.hyperduty.mapper.PmCustomTableColumnMapper;
import com.lasu.hyperduty.mapper.PmCustomTableMapper;
import com.lasu.hyperduty.mapper.PmCustomTableRowMapper;
import com.lasu.hyperduty.mapper.PmTaskCustomRowMapper;
import com.lasu.hyperduty.service.PmCustomTableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PmCustomTableServiceImpl extends ServiceImpl<PmCustomTableMapper, PmCustomTable> implements PmCustomTableService {

    private final PmCustomTableColumnMapper columnMapper;
    private final PmCustomTableRowMapper rowMapper;
    private final PmTaskCustomRowMapper taskCustomRowMapper;

    @Override
    public Page<PmCustomTable> pageList(Integer pageNum, Integer pageSize, Long projectId) {
        Page<PmCustomTable> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PmCustomTable> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(PmCustomTable::getProjectId, projectId);
        }
        wrapper.orderByDesc(PmCustomTable::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional
    public PmCustomTable createTable(PmCustomTable table, List<PmCustomTableColumn> columns) {
        table.setCreateTime(LocalDateTime.now());
        table.setUpdateTime(LocalDateTime.now());
        this.save(table);

        if (columns != null && !columns.isEmpty()) {
            for (PmCustomTableColumn column : columns) {
                column.setTableId(table.getId());
                column.setCreateTime(LocalDateTime.now());
                columnMapper.insert(column);
            }
        }
        return table;
    }

    @Override
    @Transactional
    public PmCustomTable updateTable(PmCustomTable table, List<PmCustomTableColumn> columns) {
        table.setUpdateTime(LocalDateTime.now());
        this.updateById(table);

        if (columns != null) {
            LambdaQueryWrapper<PmCustomTableColumn> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(PmCustomTableColumn::getTableId, table.getId());
            columnMapper.delete(deleteWrapper);

            for (PmCustomTableColumn column : columns) {
                column.setTableId(table.getId());
                column.setCreateTime(LocalDateTime.now());
                columnMapper.insert(column);
            }
        }
        return table;
    }

    @Override
    @Transactional
    public void deleteTable(Long id) {
        LambdaQueryWrapper<PmCustomTableColumn> columnWrapper = new LambdaQueryWrapper<>();
        columnWrapper.eq(PmCustomTableColumn::getTableId, id);
        columnMapper.delete(columnWrapper);

        LambdaQueryWrapper<PmCustomTableRow> rowWrapper = new LambdaQueryWrapper<>();
        rowWrapper.eq(PmCustomTableRow::getTableId, id);
        rowMapper.delete(rowWrapper);

        LambdaQueryWrapper<PmTaskCustomRow> bindingWrapper = new LambdaQueryWrapper<>();
        bindingWrapper.eq(PmTaskCustomRow::getTableId, id);
        taskCustomRowMapper.delete(bindingWrapper);

        this.removeById(id);
    }

    @Override
    public List<PmCustomTableColumn> getColumns(Long tableId) {
        return columnMapper.selectByTableId(tableId);
    }

    @Override
    public List<PmCustomTableRow> getRows(Long tableId) {
        return rowMapper.selectByTableId(tableId);
    }

    @Override
    public PmCustomTableRow createRow(Long tableId, String rowData) {
        PmCustomTableRow row = new PmCustomTableRow();
        row.setTableId(tableId);
        row.setRowData(rowData);
        row.setCreateTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());
        rowMapper.insert(row);
        return row;
    }

    @Override
    public PmCustomTableRow updateRow(Long id, String rowData) {
        PmCustomTableRow row = rowMapper.selectById(id);
        if (row != null) {
            row.setRowData(rowData);
            row.setUpdateTime(LocalDateTime.now());
            rowMapper.updateById(row);
        }
        return row;
    }

    @Override
    @Transactional
    public void deleteRow(Long id) {
        LambdaQueryWrapper<PmTaskCustomRow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmTaskCustomRow::getRowId, id);
        taskCustomRowMapper.delete(wrapper);
        rowMapper.deleteById(id);
    }

    @Override
    public List<TaskBindingDTO> getTaskBindings(Long taskId) {
        List<PmTaskCustomRow> bindings = taskCustomRowMapper.selectByTaskId(taskId);
        if (bindings == null || bindings.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> tableIds = bindings.stream()
                .map(PmTaskCustomRow::getTableId)
                .distinct()
                .collect(Collectors.toList());
        List<PmCustomTable> tables = this.listByIds(tableIds);
        Map<Long, String> tableNameMap = tables.stream()
                .collect(Collectors.toMap(PmCustomTable::getId, PmCustomTable::getTableName));

        List<Long> rowIds = bindings.stream()
                .map(PmTaskCustomRow::getRowId)
                .distinct()
                .collect(Collectors.toList());
        List<PmCustomTableRow> rows = rowMapper.selectBatchIds(rowIds);
        Map<Long, String> rowDataMap = rows.stream()
                .collect(Collectors.toMap(PmCustomTableRow::getId, PmCustomTableRow::getRowData));

        List<TaskBindingDTO> result = new ArrayList<>();
        for (PmTaskCustomRow binding : bindings) {
            TaskBindingDTO dto = new TaskBindingDTO();
            BeanUtils.copyProperties(binding, dto);
            dto.setTableName(tableNameMap.get(binding.getTableId()));
            dto.setRowData(rowDataMap.get(binding.getRowId()));
            result.add(dto);
        }

        return result;
    }

    @Override
    public void bindRow(Long taskId, Long tableId, Long rowId) {
        PmTaskCustomRow binding = new PmTaskCustomRow();
        binding.setTaskId(taskId);
        binding.setTableId(tableId);
        binding.setRowId(rowId);
        binding.setCreateTime(LocalDateTime.now());
        taskCustomRowMapper.insert(binding);
    }

    @Override
    public void unbindRow(Long bindingId) {
        taskCustomRowMapper.deleteById(bindingId);
    }
}
