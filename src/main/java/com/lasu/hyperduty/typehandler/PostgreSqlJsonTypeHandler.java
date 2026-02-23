package com.lasu.hyperduty.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PostgreSQL JSON字段类型处理器
 * 用于处理String类型与PostgreSQL json/jsonb类型的相互转换
 * 注意：此TypeHandler需要通过@TableField(typeHandler = PostgreSqlJsonTypeHandler.class)显式指定
 */
public class PostgreSqlJsonTypeHandler extends BaseTypeHandler<String> {

    private static final String JSON_TYPE = "json";

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        PGobject jsonObject = new PGobject();
        jsonObject.setType(JSON_TYPE);
        jsonObject.setValue(parameter);
        ps.setObject(i, jsonObject);
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);
        return convertValue(value);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object value = rs.getObject(columnIndex);
        return convertValue(value);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object value = cs.getObject(columnIndex);
        return convertValue(value);
    }

    private String convertValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof PGobject) {
            return ((PGobject) value).getValue();
        }
        return value.toString();
    }
}
