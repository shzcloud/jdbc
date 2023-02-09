package shz.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import shz.core.*;
import shz.core.cl.ClassLoaderHelp;
import shz.core.st.tst.LTST;
import shz.core.structure.limiter.Limiter;
import shz.core.type.TypeHelp;
import shz.jdbc.entity.SysDs;
import shz.orm.ClassInfo;
import shz.orm.OrmService;
import shz.orm.enums.Condition;
import shz.orm.enums.DataType;
import shz.orm.exception.OrmClassNoFieldException;
import shz.orm.param.OrmMapConsumer;
import shz.orm.param.OrmMapFilter;
import shz.orm.param.OrmMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;


public class JdbcService extends JdbcServiceHelper {
    private static final Logger log = LoggerFactory.getLogger(JdbcService.class);

    private boolean logEnabled = true;

    public final void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    @Override
    protected void log(Level level, Supplier<String> sql) {
        if (!logEnabled) return;
        switch (level) {
            case TRACE:
                if (log.isTraceEnabled()) log.trace(sql.get());
                break;
            case DEBUG:
                if (log.isDebugEnabled()) log.debug(sql.get());
                break;
            case INFO:
                if (log.isInfoEnabled()) log.info(sql.get());
                break;
            case WARN:
                if (log.isWarnEnabled()) log.warn(sql.get());
                break;
            case ERROR:
                if (log.isErrorEnabled()) log.error(sql.get());
                break;
            default:
                break;
        }
    }

    private static final JdbcService NULL = new JdbcService();
    private static final Map<String, JdbcService> DS_SERVICE_CACHE = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    protected final <S extends OrmService> S service(String dsName) {
        return (S) getService(dsName);
    }

    public final JdbcService getService(String dsName) {
        if (NullHelp.isEmpty(dsName)) return null;
        JdbcService service = DS_SERVICE_CACHE.computeIfAbsent(dsName, k -> {
            SysDs ds = selectOne(SysDs.class, true, null, whereSql(nonNullClassInfo(SysDs.class), "name", dsName, Condition.EQ, Boolean.FALSE));
            return ds == null ? NULL : createService(ds);
        });
        return service == JdbcService.NULL ? null : service;
    }

    protected JdbcService createService(SysDs ds) {
        JdbcService service = (JdbcService) AccessibleHelp.newInstance(ClassLoaderHelp.load(ds.getServiceClassName()));
        NullHelp.requireNonNull(service, "实例化JdbcService类:%s失败,数据源名称:%s", ds.getServiceClassName(), ds.getName());
        service.setDataSource(ds.getDriverClassName(), ds.getUrl(), ds.getUsername(), ds.getPassword());
        return service;
    }

    public final void removeService(String dsName) {
        DS_SERVICE_CACHE.remove(dsName);
    }

    @Override
    public final void query(OrmMapping mapping, Type type, Limiter limiter, OrmMapFilter mapFilter, OrmMapConsumer mapConsumer, int fetchSize, String sql, Object... params) {
        NullHelp.requireNon(mapFilter == null && mapConsumer == null);
        Connection conn = conn();
        if (conn == null) return;
        PreparedStatement pst = null;
        ResultSet rst = null;
        try {
            preSet(conn);
            pst = conn.prepareStatement(sql);
            if (fetchSize != Integer.MAX_VALUE) pst.setFetchSize(fetchSize <= 0 ? 3000 : fetchSize);
            setPst(pst, params);
            log(Level.DEBUG, () -> logSql(sql, params));
            rst = pst.executeQuery();

            ResultSetMetaData metaData = rst.getMetaData();
            int count = metaData.getColumnCount();
            int capacity = (int) Math.ceil(count / 0.75f);

            Map<String, String> columnMapping = columnMapping(mapping, type, metaData, sql);
            while (rst.next()) {
                if (limiter != null && limiter.isLimit()) break;
                Map<String, Object> map = new LinkedHashMap<>(capacity, 1.0f);
                for (Map.Entry<String, String> entry : columnMapping.entrySet())
                    map.put(entry.getValue(), sqlHandler.valueMap(rst.getObject(entry.getKey())));
                //mapFilter不为null时的过滤,即映射为对象之前就过滤
                if (mapFilter != null && !mapFilter.test(map)) continue;
                if (mapConsumer == null) {
                    if (limiter != null && limiter.isLimitAfter()) break;
                    continue;
                }
                //进行数据的消费
                mapConsumer.accept(map);
            }
        } catch (Exception e) {
            log(Level.ERROR, () -> logSql(sql, params));
            throw PRException.of(e);
        } finally {
            close(rst, pst, conn);
        }
    }

    /**
     * 根据sql缓存对象映射关系(当使用select * 时,禁止手动修改或删除数据库字段,否则应当禁用该缓存)
     */
    protected static final LTST<Map<String, String>> SQL_COLUMN_MAPPING_CACHE = LTST.of();

    protected Map<String, String> columnMapping(OrmMapping mapping, Type type, ResultSetMetaData metaData, String sql) throws SQLException {
        char[] chars = sql.toCharArray();
        Map<String, String> columnMapping = SQL_COLUMN_MAPPING_CACHE.get(chars);
        if (columnMapping != null) return columnMapping;
        columnMapping = ToMap.get(metaData.getColumnCount()).build();
        DataType dataType = dataType(type);
        if (dataType == DataType.MAP || dataType == DataType.LIST || dataType == DataType.ARRAY || dataType == DataType.COMMON)
            simpleColumnMapping(mapping, metaData, columnMapping);
        else if (dataType == DataType.DEFAULT) defaultColumnMapping(mapping, type, metaData, columnMapping);
        else nestedColumMapping(mapping, type, metaData, columnMapping);
        SQL_COLUMN_MAPPING_CACHE.put(chars, columnMapping);
        return columnMapping;
    }

    private void simpleColumnMapping(OrmMapping mapping, ResultSetMetaData metaData, Map<String, String> columnMapping) throws SQLException {
        int count = metaData.getColumnCount();
        if (mapping == null) {
            for (int i = 0; i < count; ++i) {
                String columnLabel = metaData.getColumnLabel(i + 1);
                columnMapping.put(columnLabel, columnLabel);
            }
        } else {
            for (int i = 0; i < count; ++i) {
                String columnLabel = metaData.getColumnLabel(i + 1);
                columnMapping.put(columnLabel, NullHelp.nullOrElse(mapping.apply(columnLabel), columnLabel));
            }
        }
    }

    private void defaultColumnMapping(OrmMapping mapping, Type type, ResultSetMetaData metaData, Map<String, String> columnMapping) throws SQLException {
        Class<?> cls = TypeHelp.toClass(type);
        List<Field> fields = AccessibleHelp.fields(cls);
        if (fields.isEmpty()) throw new OrmClassNoFieldException(cls, "");
        ClassInfo classInfo = classInfo(cls);
        int count = metaData.getColumnCount();
        if (mapping == null) {
            for (int i = 0; i < count; ++i) {
                String columnLabel = metaData.getColumnLabel(i + 1);
                columnMapping.put(columnLabel, defaultColumnMapping(classInfo, fields, columnLabel));
            }
        } else {
            for (int i = 0; i < count; ++i) {
                String columnLabel = metaData.getColumnLabel(i + 1);
                columnMapping.put(columnLabel, defaultColumnMapping(classInfo, fields, NullHelp.nullOrElse(mapping.apply(columnLabel), columnLabel)));
            }
        }
    }

    private String defaultColumnMapping(ClassInfo classInfo, List<Field> fields, String column) {
        for (Field field : fields)
            if ((classInfo != null && column.equals(classInfo.toColumnName(field.getName()))) || sqlHandler.aliasToField(column).equals(field.getName()))
                return field.getName();
        return null;
    }

    private void nestedColumMapping(OrmMapping mapping, Type type, ResultSetMetaData metaData, Map<String, String> columnMapping) throws SQLException {
        Class<?> cls = TypeHelp.toClass(type);
        List<Field> fields = AccessibleHelp.fields(cls);
        if (fields.isEmpty()) throw new OrmClassNoFieldException(cls, "");
        ClassInfo classInfo = classInfo(cls);
        Map<Field, Type> fieldTypeMap = TypeHelp.fieldType(type);
        int count = metaData.getColumnCount();
        if (mapping == null) {
            for (int i = 0; i < count; ++i) {
                String columnLabel = metaData.getColumnLabel(i + 1);
                columnMapping.put(columnLabel, nestedColumMapping(classInfo, fieldTypeMap, fields, columnLabel, ""));
            }
        } else {
            for (int i = 0; i < count; ++i) {
                String columnLabel = metaData.getColumnLabel(i + 1);
                columnMapping.put(columnLabel, nestedColumMapping(classInfo, fieldTypeMap, fields, NullHelp.nullOrElse(mapping.apply(columnLabel), columnLabel), ""));
            }
        }
    }

    private String nestedColumMapping(ClassInfo classInfo, Map<Field, Type> fieldTypeMap, List<Field> fields, String column, String mapField) {
        if (fields.isEmpty()) return null;
        Type[] types;
        Class<?> cls;
        String key;
        for (Field field : fields) {
            if (classInfo != null && column.equals(classInfo.toColumnName(field.getName()))) return field.getName();
            String fieldName = sqlHandler.aliasToField(column);
            if (fieldName.equals(field.getName()) || fieldName.equals(mapField + field.getName())) return fieldName;
            Class<?> fCls = TypeHelp.fieldClass(field, fieldTypeMap);
            if (Map.class.isAssignableFrom(fCls) || TypeHelp.likeCommon(fCls)) continue;
            if (Collection.class.isAssignableFrom(fCls) || fCls.isArray()) {
                if ((types = TypeHelp.getActualTypeArguments(TypeHelp.fieldType(field, fieldTypeMap))) == null || types.length != 1)
                    continue;
                if (!TypeHelp.likeModel(cls = TypeHelp.toClass(types[0]))) continue;
                key = nestedColumMapping(classInfo(cls), fieldTypeMap, AccessibleHelp.fields(cls), column, mapField + field.getName() + ".");
                if (key == null) continue;
                return key;
            }
            key = nestedColumMapping(classInfo(fCls), fieldTypeMap, AccessibleHelp.fields(fCls), column, mapField + field.getName() + ".");
            if (key != null) return key;
        }
        return null;
    }
}
