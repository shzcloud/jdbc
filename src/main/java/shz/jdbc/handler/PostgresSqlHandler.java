package shz.jdbc.handler;

import shz.core.*;
import shz.core.model.PageInfo;
import shz.core.time.TimeHelp;
import shz.jdbc.model.Column;
import shz.jdbc.model.PrimaryKey;
import shz.jdbc.model.Table;
import shz.orm.Tnp;
import shz.orm.sql.segment.Segment;

import java.io.InputStream;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.*;
import java.util.*;

public class PostgresSqlHandler implements DefaultSqlHandler {
    @Override
    public void escape(StringBuilder sb, Object val) {
        if (val == null) sb.append("null");
        else if (val instanceof Boolean || val instanceof Number) sb.append(val);
        else if (val instanceof Timestamp)
            sb.append("TO_TIMESTAMP('").append(TimeHelp.format((Timestamp) val)).append("','YYYY-MM-DD HH24:MI:SS')");
        else if (val instanceof ZonedDateTime)
            sb.append("TO_TIMESTAMP('").append(TimeHelp.format(((ZonedDateTime) val).toLocalDateTime())).append("','YYYY-MM-DD HH24:MI:SS')");
        else if (val instanceof LocalDateTime)
            sb.append("TO_TIMESTAMP('").append(TimeHelp.format((LocalDateTime) val)).append("','YYYY-MM-DD HH24:MI:SS')");
        else if (val instanceof Instant)
            sb.append("TO_TIMESTAMP('").append(TimeHelp.format((Instant) val)).append("','YYYY-MM-DD HH24:MI:SS')");
        else if (val instanceof LocalDate)
            sb.append("TO_DATE('").append(TimeHelp.format((LocalDate) val, TimeHelp.DATE_PATTERN)).append("','YYYY-MM-DD')");
        else if (val instanceof Date)
            sb.append("TO_TIMESTAMP('").append(TimeHelp.format((Date) val)).append("','YYYY-MM-DD HH24:MI:SS')");
        else if (val instanceof LocalTime)
            sb.append("TO_TIMESTAMP('").append(TimeHelp.format((LocalTime) val, TimeHelp.TIME_PATTERN)).append("','HH24:MI:SS')");
        else {
            sb.append('\'');
            String s = val.toString();
            if (!StringHelp.escape(sb::append, sb::append, s, '\'')) sb.append(s);
            sb.append('\'');
        }
    }

    @Override
    public final void wrap(StringBuilder sb, Object val) {
        sb.append('\"').append(val).append('\"');
    }

    @Override
    public final void page(StringBuilder sb, PageInfo<?> pageInfo) {
        sb.append(" LIMIT ").append(pageInfo.getSize()).append(" OFFSET ").append(pageInfo.getMin());
    }

    @Override
    public final String humpToUnderline(String name) {
        return DefaultSqlHandler.super.humpToUnderline(name);
    }

    @Override
    public final String aliasToField(String name) {
        return DefaultSqlHandler.super.aliasToField(name);
    }

    @Override
    public final List<Segment> segments(String sql) {
        return DefaultSqlHandler.super.segments(sql);
    }

    @Override
    public final String createTable(Table table) {
        StringBuilder sb = new StringBuilder();
        String schemAndName = (NullHelp.isBlank(table.getTableSchem()) ? "" : "\"" + table.getTableSchem() + "\".") + "\"" + table.getTableName() + "\"";
        sb.append("DROP TABLE IF EXISTS ").append(schemAndName).append(multiSqlSep());
        sb.append("CREATE TABLE ").append(schemAndName).append(" (");
        for (Column column : table.getColumns()) {
            sb.append("\"").append(column.getColumnName()).append("\"");
            appendColumnType(sb, column);
            appendCharacter(sb, column);
            appendDefaultValue(sb, column);
            sb.append(",");
        }
        sb.replace(sb.length() - 1, sb.length(), ")");
        sb.append(multiSqlSep());

        for (Column column : table.getColumns())
            if (column.getRemarks() != null)
                sb.append("COMMENT ON COLUMN ").append(schemAndName).append(".\"").append(column.getColumnName()).append("\" IS '").append(column.getRemarks()).append("'").append(multiSqlSep());

        sb.append("COMMENT ON TABLE ").append(schemAndName).append(" IS '").append(table.getRemarks() == null ? "" : table.getRemarks()).append("'").append(multiSqlSep());

        if (NullHelp.nonEmpty(table.getPrimaryKeys())) for (PrimaryKey primaryKey : table.getPrimaryKeys())
            sb.append("ALTER TABLE ").append(schemAndName).append(" ADD CONSTRAINT \"").append(primaryKey.getPkName()).append("\" PRIMARY KEY (\"").append(primaryKey.getColumnName()).append("\")").append(multiSqlSep());

        return sb.toString();
    }

    private void appendColumnType(StringBuilder sb, Column column) {
        sb.append(" ");
        switch (column.getDataType()) {
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL:
                sb.append(column.getTypeName()).append("(").append(column.getColumnSize()).append(",").append(column.getDecimalDigits()).append(")");
                break;
            case Types.BIT:
            case Types.BOOLEAN:
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.TIME_WITH_TIMEZONE:
            case Types.TIMESTAMP_WITH_TIMEZONE:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                sb.append(column.getTypeName()).append("(").append(column.getColumnSize()).append(")");
                break;
            default:
                sb.append(column.getTypeName());
        }
    }

    private void appendCharacter(StringBuilder sb, Column column) {
        switch (column.getDataType()) {
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
                sb.append(" COLLATE \"pg_catalog\".\"default\"");
        }
    }

    private void appendDefaultValue(StringBuilder sb, Column column) {
        sb.append(column.getNullable() == 0 ? " NOT NULL" : "");
        if (column.getColumnDef() != null) {
            sb.append(" DEFAULT ");
            switch (column.getDataType()) {
                case Types.BIT:
                case Types.BOOLEAN:
                case Types.TINYINT:
                case Types.SMALLINT:
                case Types.INTEGER:
                case Types.BIGINT:
                case Types.FLOAT:
                case Types.REAL:
                case Types.DOUBLE:
                case Types.NUMERIC:
                case Types.DECIMAL:
                case Types.DATE:
                case Types.TIME:
                case Types.TIMESTAMP:
                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                case Types.NULL:
                case Types.OTHER:
                case Types.JAVA_OBJECT:
                case Types.DISTINCT:
                case Types.STRUCT:
                case Types.ARRAY:
                case Types.REF:
                case Types.DATALINK:
                case Types.ROWID:
                case Types.REF_CURSOR:
                case Types.TIME_WITH_TIMEZONE:
                case Types.TIMESTAMP_WITH_TIMEZONE:
                    sb.append(column.getColumnDef());
                    break;
                default:
                    sb.append("'").append(column.getColumnDef()).append("'");
            }
        }
    }

    @Override
    public final String dropTable(Tnp tnp) {
        StringBuilder sb = new StringBuilder();
        sb.append("DROP TABLE IF EXISTS ");
        wrapTable(sb, tnp);
        return sb.toString();
    }

    @Override
    public final String copyTableStructure(Tnp src, Tnp des) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ");
        wrapTable(sb, des);
        sb.append(" (LIKE ");
        wrapTable(sb, src);
        sb.append(" INCLUDING ALL)");
        return sb.toString();
    }

    @Override
    public final String createDatabase(String database) {
        return null;
    }

    @Override
    public final String dropDatabase(String database) {
        return null;
    }

    @Override
    public final String backupDatabase(String disk, String database) {
        return null;
    }

    @Override
    public final String restoreDatabase(String disk, String database) {
        return null;
    }

    @Override
    public List<String> fromIs(InputStream is) {
        return Collections.emptyList();
    }
}
