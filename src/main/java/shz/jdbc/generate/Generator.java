package shz.jdbc.generate;

import shz.core.*;
import shz.core.io.IOHelp;
import shz.core.msg.ServerFailureMsg;
import shz.core.time.TimeHelp;
import shz.jdbc.model.Column;
import shz.jdbc.model.ImportedKey;
import shz.jdbc.model.PrimaryKey;
import shz.jdbc.model.Table;
import shz.orm.entity.RecordEntity;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;

public abstract class Generator {
    protected abstract Connection conn();

    protected String folder() {
        return System.getProperty("user.dir");
    }

    protected abstract GenInfo entityGenInfo(Table table);

    protected abstract GenInfo addVoGenInfo(Table table);

    protected abstract GenInfo updateVoGenInfo(Table table);

    protected abstract GenInfo queryVoGenInfo(Table table);

    protected abstract GenInfo detailVoGenInfo(Table table);

    protected abstract GenInfo apiGenInfo(Table table);

    protected abstract GenInfo serviceGenInfo(Table table);

    protected abstract GenInfo serviceImplGenInfo(Table table);

    protected abstract GenInfo repositoryGenInfo(Table table);

    protected abstract GenInfo controllerGenInfo(Table table);

    protected abstract GenInfo enumGenInfo(Table table);

    protected abstract GenInfo apiClientGenInfo(Table table);

    protected abstract GenInfo apiClientFallbackGenInfo(Table table);

    protected abstract void appendEntityData(Tgp tgp, List<String> data);

    protected abstract void appendAddVoData(Tgp tgp, List<String> data);

    protected abstract void appendUpdateVoData(Tgp tgp, List<String> data);

    protected abstract void appendQueryVoData(Tgp tgp, List<String> data);

    protected abstract void appendDetailVoData(Tgp tgp, List<String> data);

    protected abstract void appendApiData(Tgp tgp, List<String> data);

    protected abstract void appendServiceData(Tgp tgp, List<String> data);

    protected abstract void appendServiceImplData(Tgp tgp, List<String> data);

    protected abstract void appendRepositoryData(Tgp tgp, List<String> data);

    protected abstract void appendControllerData(Tgp tgp, List<String> data);

    protected abstract void appendApiClientData(Tgp tgp, List<String> data);

    protected abstract void appendApiClientFallbackData(Tgp tgp, List<String> data);

    /**
     * 根据表名生成文件
     */
    public final void generate(String catalog, String schemaPattern, String tableNamePattern, String[] types) {
        List<Table> tables = getTables(catalog, schemaPattern, tableNamePattern, types);
        ServerFailureMsg.requireNon(NullHelp.isEmpty(tables), "获取表信息失败");
        List<String> data = new LinkedList<>();

        tables.forEach(table -> {
            String className = className(table);
            long flags = flags(table);

            Tgp tgp = new Tgp(
                    table,
                    entityGenInfo(table),
                    addVoGenInfo(table),
                    updateVoGenInfo(table),
                    queryVoGenInfo(table),
                    detailVoGenInfo(table),
                    apiGenInfo(table),
                    serviceGenInfo(table),
                    serviceImplGenInfo(table),
                    repositoryGenInfo(table),
                    controllerGenInfo(table),
                    enumGenInfo(table),
                    apiClientGenInfo(table),
                    apiClientFallbackGenInfo(table)
            );

            if ((flags & FILE_ENTITY) != 0)
                write(tgp, tgp.entityGenInfo, data, this::appendEntityData, className + ".java");

            if ((flags & FILE_VO) != 0) {
                write(tgp, tgp.addVoGenInfo, data, this::appendAddVoData, "Add" + className + "Vo.java");
                write(tgp, tgp.updateVoGenInfo, data, this::appendUpdateVoData, "Update" + className + "Vo.java");
                write(tgp, tgp.queryVoGenInfo, data, this::appendQueryVoData, "Query" + className + "Vo.java");
                write(tgp, tgp.detailVoGenInfo, data, this::appendDetailVoData, className + "DetailVo.java");
            }

            if ((flags & FILE_API) != 0)
                write(tgp, tgp.apiGenInfo, data, this::appendApiData, className + "Api.java");

            if ((flags & FILE_SERVICE) != 0)
                write(tgp, tgp.serviceGenInfo, data, this::appendServiceData, className + "Service.java");
            if ((flags & FILE_SERVICE_IMPL) != 0)
                write(tgp, tgp.serviceImplGenInfo, data, this::appendServiceImplData, className + "ServiceImpl.java");

            if ((flags & FILE_REPOSITORY) != 0)
                write(tgp, tgp.repositoryGenInfo, data, this::appendRepositoryData, className + "Repository.java");

            if ((flags & FILE_CONTROLLER) != 0)
                write(tgp, tgp.controllerGenInfo, data, this::appendControllerData, className + "Controller.java");

            if ((flags & FILE_API_CLIENT) != 0)
                write(tgp, tgp.apiClientGenInfo, data, this::appendApiClientData, className + "Client.java");
            if ((flags & FILE_API_CLIENT_FALLBACK) != 0)
                write(tgp, tgp.apiClientFallbackGenInfo, data, this::appendApiClientFallbackData, className + "ClientFallbackFactory.java");
        });
    }

    private List<Table> getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) {
        Connection conn = conn();
        if (conn == null) return Collections.emptyList();

        List<Table> result = new LinkedList<>();
        ResultSet tables = null;
        ResultSet primaryKeys = null;
        ResultSet importedKeys = null;
        ResultSet columns = null;
        try {
            catalog = NullHelp.isBlank(catalog) ? conn.getCatalog() : catalog;
            schemaPattern = NullHelp.isBlank(schemaPattern) ? "%" : schemaPattern;
            types = NullHelp.isBlank(types) ? new String[]{"TABLE"} : types;
            DatabaseMetaData dbMetaData = conn.getMetaData();
            tables = dbMetaData.getTables(catalog, schemaPattern, tableNamePattern, types);
            while (tables.next()) {
                Table table = new Table();
                table.setTableCat(getString(tables, "TABLE_CAT"));
                table.setTableSchem(getString(tables, "TABLE_SCHEM"));
                table.setTableName(getString(tables, "TABLE_NAME"));
                table.setTableType(getString(tables, "TABLE_TYPE"));
                table.setRemarks(getString(tables, "REMARKS"));

                table.setTypeCat(getString(tables, "TYPE_CAT"));
                table.setTypeSchem(getString(tables, "TYPE_SCHEM"));
                table.setTypeName(getString(tables, "TYPE_NAME"));
                table.setSelfReferencingColName(getString(tables, "SELF_REFERENCING_COL_NAME"));
                table.setRefGeneration(getString(tables, "REF_GENERATION"));

                //主键
                primaryKeys = dbMetaData.getPrimaryKeys(catalog, schemaPattern, table.getTableName());
                List<PrimaryKey> primaryKeyList = new LinkedList<>();
                while (primaryKeys.next()) {
                    PrimaryKey primaryKey = new PrimaryKey();
                    primaryKey.setTableCat(getString(primaryKeys, "TABLE_CAT"));
                    primaryKey.setTableSchem(getString(primaryKeys, "TABLE_SCHEM"));
                    primaryKey.setTableName(getString(primaryKeys, "TABLE_NAME"));
                    primaryKey.setColumnName(getString(primaryKeys, "COLUMN_NAME"));
                    primaryKey.setKeySeq(getShort(primaryKeys, "KEY_SEQ"));
                    primaryKey.setPkName(getString(primaryKeys, "PK_NAME"));

                    primaryKeyList.add(primaryKey);
                }
                table.setPrimaryKeys(primaryKeyList.isEmpty() ? Collections.emptyList() : ToList.explicitCollect(primaryKeyList.stream().distinct(), primaryKeyList.size()));
                close(primaryKeys);

                //外键
                importedKeys = dbMetaData.getImportedKeys(catalog, schemaPattern, table.getTableName());
                List<ImportedKey> importedKeyList = new LinkedList<>();
                while (importedKeys.next()) {
                    ImportedKey importedKey = new ImportedKey();
                    importedKey.setPkTableCat(getString(importedKeys, "PKTABLE_CAT"));
                    importedKey.setPkTableSchem(getString(importedKeys, "PKTABLE_SCHEM"));
                    importedKey.setPkTableName(getString(importedKeys, "PKTABLE_NAME"));
                    importedKey.setPkColumnName(getString(importedKeys, "PKCOLUMN_NAME"));
                    importedKey.setPkName(getString(importedKeys, "PK_NAME"));

                    importedKey.setFkTableCat(getString(importedKeys, "FKTABLE_CAT"));
                    importedKey.setFkTableSchem(getString(importedKeys, "FKTABLE_SCHEM"));
                    importedKey.setFkTableName(getString(importedKeys, "FKTABLE_NAME"));
                    importedKey.setFkColumnName(getString(importedKeys, "FKCOLUMN_NAME"));
                    importedKey.setFkName(getString(importedKeys, "FK_NAME"));

                    importedKey.setKeySeq(getShort(importedKeys, "KEY_SEQ"));
                    importedKey.setUpdateRule(getShort(importedKeys, "UPDATE_RULE"));
                    importedKey.setDeleteRule(getShort(importedKeys, "DELETE_RULE"));
                    importedKey.setDeferrability(getShort(importedKeys, "DEFERRABILITY"));

                    importedKeyList.add(importedKey);
                }
                table.setImportedKeys(importedKeyList.isEmpty() ? Collections.emptyList() : ToList.explicitCollect(importedKeyList.stream().distinct(), importedKeyList.size()));
                close(importedKeys);

                //列
                columns = dbMetaData.getColumns(catalog, schemaPattern, table.getTableName(), "%");
                List<Column> columnList = new LinkedList<>();
                while (columns.next()) {
                    Column column = new Column();
                    column.setColumnName(getString(columns, "COLUMN_NAME"));
                    column.setDataType(getInt(columns, "DATA_TYPE"));
                    column.setTypeName(getString(columns, "TYPE_NAME"));
                    column.setColumnSize(getInt(columns, "COLUMN_SIZE"));
                    column.setDecimalDigits(getInt(columns, "DECIMAL_DIGITS"));
                    column.setNumPrecRadix(getInt(columns, "NUM_PREC_RADIX"));
                    column.setNullable(getInt(columns, "NULLABLE"));
                    column.setRemarks(getString(columns, "REMARKS"));
                    column.setColumnDef(getString(columns, "COLUMN_DEF"));
                    column.setCharOctetLength(getInt(columns, "CHAR_OCTET_LENGTH"));
                    column.setOrdinalPosition(getInt(columns, "ORDINAL_POSITION"));
                    column.setIsNullable(getString(columns, "IS_NULLABLE"));
                    column.setSourceDataType(getShort(columns, "SOURCE_DATA_TYPE"));
                    column.setIsAutoIncrement(getString(columns, "IS_AUTOINCREMENT"));
                    column.setScopeCatlog(getString(columns, "SCOPE_CATLOG"));
                    column.setScopeSchem(getString(columns, "SCOPE_SCHEMA"));
                    column.setScopeTable(getString(columns, "SCOPE_TABLE"));

                    columnList.add(column);
                }
                table.setColumns(columnList.isEmpty() ? Collections.emptyList() : ToList.explicitCollect(columnList.stream().distinct(), columnList.size()));
                close(columns);

                result.add(table);
            }
        } catch (Throwable t) {
            throw PRException.of(t);
        } finally {
            close(columns);
            close(importedKeys);
            close(primaryKeys);
            close(tables);
            close(conn);
        }
        return result.isEmpty() ? Collections.emptyList() : result;
    }

    private String getString(ResultSet resultSet, String columnLabel) {
        try {
            return resultSet.getString(columnLabel);
        } catch (SQLException e) {
            return null;
        }
    }

    private int getInt(ResultSet resultSet, String columnLabel) {
        try {
            return resultSet.getInt(columnLabel);
        } catch (SQLException e) {
            return 0;
        }
    }

    private short getShort(ResultSet resultSet, String columnLabel) {
        try {
            return resultSet.getShort(columnLabel);
        } catch (SQLException e) {
            return (short) 0;
        }
    }

    private void close(ResultSet rst) {
        if (rst != null) try {
            rst.close();
        } catch (SQLException ignored) {
        }
    }

    private void close(Connection conn) {
        if (conn != null) try {
            conn.close();
        } catch (SQLException ignored) {
        }
    }

    protected static final long FILE_ENTITY = 1L << 1;
    protected static final long FILE_VO = 1L << 2;
    protected static final long FILE_API = 1L << 3;
    protected static final long FILE_SERVICE = 1L << 4;
    protected static final long FILE_SERVICE_IMPL = 1L << 5;
    protected static final long FILE_REPOSITORY = 1L << 6;
    protected static final long FILE_CONTROLLER = 1L << 7;
    protected static final long FILE_ENUM = 1L << 8;
    protected static final long FILE_API_CLIENT = 1L << 9;
    protected static final long FILE_API_CLIENT_FALLBACK = 1L << 10;

    protected static final long ALL = FILE_ENTITY
            | FILE_VO
            | FILE_API
            | FILE_SERVICE
            | FILE_SERVICE_IMPL
            | FILE_REPOSITORY
            | FILE_CONTROLLER
            | FILE_ENUM
            | FILE_API_CLIENT
            | FILE_API_CLIENT_FALLBACK;

    protected long flags(Table table) {
        String tableName = table.getTableName().toLowerCase();
        if (tableName.endsWith("_detail")) return FILE_ENTITY;

        String remarks = table.getRemarks();
        if (NullHelp.nonBlank(remarks)
                && (remarks.endsWith("关系") || remarks.endsWith("关系表") || remarks.endsWith("中间表")))
            return FILE_ENTITY;

        Class<?> superEntity = superEntity(table);
        if (superEntity == null) return ALL;
        if (RecordEntity.class.isAssignableFrom(superEntity)) return ALL & (~FILE_REPOSITORY);

        return ALL;
    }

    protected final void write(Tgp tgp, GenInfo genInfo, List<String> data, BiConsumer<Tgp, List<String>> append, String filename) {
        if (genInfo == null) return;
        File folder = new File(NullHelp.isBlank(genInfo.folder) ? folder() : genInfo.folder);
        if (NullHelp.nonBlank(genInfo.path)) folder = new File(folder, genInfo.path);
        folder = new File(folder, "src/main/java/" + genInfo.packageName.replaceAll("\\.", "/"));
        folder.mkdirs();

        File file = new File(folder, filename);

        if (genInfo.delete || !file.exists()) {
            if (append != null) append.accept(tgp, data);
            IOHelp.write(IOHelp.newBufferedWriter(file.toPath()), data);
            data.clear();
        }
    }

    public final void generate(String schemaPattern, String tableNamePattern) {
        generate(null, schemaPattern, tableNamePattern, null);
    }

    public final void generate(String tableNamePattern) {
        generate(null, tableNamePattern);
    }

    protected Class<?> superEntity(Table table) {
        return null;
    }

    protected String className(Table table) {
        String s = table.getTableName();
        if (s.startsWith("t_") || s.startsWith("T_")) s = s.substring(2);
        return StringHelp.underlineToHump(s.toLowerCase(), true);
    }

    protected String fieldName(Column column) {
        return StringHelp.underlineToHump(column.getColumnName().toLowerCase(), false);
    }

    protected Map<String, String> primaryKeyTypeMap = new HashMap<>();

    protected String primaryKeyType(Table table) {
        String primaryKeyType = primaryKeyTypeMap.get(table.getTableName());
        if (NullHelp.nonBlank(primaryKeyType)) return primaryKeyType;

        Set<String> primaryKeys = ToSet.collect(table.getPrimaryKeys().stream().map(PrimaryKey::getColumnName));
        for (Column column : table.getColumns()) {
            if (primaryKeys.contains(column.getColumnName())) {
                primaryKeyType = getType(column);
                primaryKeyTypeMap.put(table.getTableName(), primaryKeyType);
                return primaryKeyType;
            }
        }

        primaryKeyTypeMap.put(table.getTableName(), "Long");
        return "Long";
    }

    protected String getType(Column column) {
        switch (column.getDataType()) {
            case Types.BIT:
            case Types.BOOLEAN:
                return "Boolean";
            case Types.TINYINT:
                return "Byte";
            case Types.SMALLINT:
                return "Short";
            case Types.INTEGER:
                return "Integer";
            case Types.BIGINT:
                return "Long";
            case Types.FLOAT:
                return "Float";
            case Types.DOUBLE:
                return "Double";
            case Types.NUMERIC:
            case Types.DECIMAL:
                if (column.getDecimalDigits() > 0 || column.getColumnSize() >= 19) return "BigDecimal";
                if (column.getColumnSize() >= 10) return "Long";
                if (column.getColumnSize() >= 5) return "Integer";
                if (column.getColumnSize() >= 3) return "Short";
                if (column.getColumnSize() >= 2) return "Byte";
                return "Boolean";
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.BLOB:
            case Types.CLOB:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.NCLOB:
            case Types.SQLXML:
                return "String";
            case Types.DATE:
                return "LocalDate";
            case Types.TIME:
                return "LocalTime";
            case Types.TIMESTAMP:
            case Types.TIME_WITH_TIMEZONE:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return "LocalDateTime";
        }
        return "String";
    }

    protected String getImport(String type) {
        switch (type) {
            case "BigDecimal":
                return "import java.math.BigDecimal;";
            case "LocalDate":
                return "import java.time.LocalDate;";
            case "LocalTime":
                return "import java.time.LocalTime;";
            case "LocalDateTime":
                return "import java.time.LocalDateTime;";
        }
        return null;
    }

    protected String module(Table table) {
        String s = table.getTableName().toLowerCase();
        if (s.startsWith("t_")) s = s.substring(2);
        int idx = s.indexOf('_');
        return idx != -1 ? s.substring(0, idx) : s;
    }

    protected String urlSuffix(Table table) {
        String s = table.getTableName().toLowerCase();
        if (s.startsWith("t_")) s = s.substring(2);
        String module = module(table);
        if (NullHelp.nonBlank(module) && s.startsWith(module + "_")) return s.substring(module.length() + 1);
        return s;
    }

    protected String version() {
        return "v1";
    }

    protected String requestMapping(Table table) {
        String module = module(table);
        if (NullHelp.isBlank(module)) return "/" + version() + "/" + urlSuffix(table);
        return "/" + module + "/" + version() + "/" + urlSuffix(table);
    }

    protected String date() {
        return TimeHelp.format(LocalDateTime.now(), DateTimeFormatter.ofPattern("yyyy/M/d"));
    }

    protected String desc(Table table) {
        String remarks = table.getRemarks();
        if (NullHelp.nonBlank(remarks) && remarks.endsWith("表")) remarks = remarks.substring(0, remarks.length() - 1);
        return remarks;
    }

    protected String apiTags(Table table) {
        String remarks = table.getRemarks();
        if (NullHelp.nonBlank(remarks)) {
            if (remarks.endsWith("表")) remarks = remarks.substring(0, remarks.length() - 1);
            if (!remarks.endsWith("管理")) remarks += "管理";
        }
        return remarks;
    }

    protected String user(Table table) {
        return System.getProperty("user.name");
    }

    protected String apiClientIdPrefix(Table table) {
        return "";
    }
}
