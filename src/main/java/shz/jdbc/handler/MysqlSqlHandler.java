package shz.jdbc.handler;

import shz.core.*;
import shz.core.io.IOHelp;
import shz.core.model.PageInfo;
import shz.jdbc.model.Column;
import shz.jdbc.model.PrimaryKey;
import shz.jdbc.model.Table;
import shz.orm.Tnp;
import shz.orm.sql.segment.Segment;

import java.io.InputStream;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MysqlSqlHandler implements DefaultSqlHandler {
    @Override
    public void escape(StringBuilder sb, Object val) {
        //date_format(xxx, '%Y-%m-%d %H:%i:%s')
        if (val == null) sb.append("null");
        else if (val instanceof Boolean || val instanceof Number) sb.append(val);
        else if (val instanceof ZonedDateTime)
            sb.append('\'').append(Timestamp.from(((ZonedDateTime) val).toInstant())).append('\'');
        else if (val instanceof LocalDateTime)
            sb.append('\'').append(Timestamp.valueOf((LocalDateTime) val)).append('\'');
        else if (val instanceof Instant) sb.append('\'').append(Timestamp.from((Instant) val)).append('\'');
        else if (val instanceof LocalDate)
            sb.append('\'').append(Timestamp.valueOf(LocalDateTime.of((LocalDate) val, LocalTime.MIN))).append('\'');
        else if (val instanceof Date)
            sb.append('\'').append(Timestamp.from(Instant.ofEpochMilli(((Date) val).getTime()))).append('\'');
        else if (val instanceof LocalTime) sb.append('\'').append(Time.valueOf((LocalTime) val)).append('\'');
        else {
            sb.append('\'');
            String s = val.toString();
            if (!StringHelp.escape(sb::append, sb::append, s, '\'')) sb.append(s);
            sb.append('\'');
        }
    }

    @Override
    public final void wrap(StringBuilder sb, Object val) {
        sb.append('`').append(val).append('`');
    }

    @Override
    public final boolean supportIgnore() {
        return true;
    }

    @Override
    public final void page(StringBuilder sb, PageInfo<?> pageInfo) {
        sb.append(" LIMIT ").append(pageInfo.getMin()).append(',').append(pageInfo.getSize());
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
        String schemAndName = (NullHelp.isBlank(table.getTableSchem()) ? "" : "`" + table.getTableSchem() + "`.") + "`" + table.getTableName() + "`";
        sb.append("DROP TABLE IF EXISTS ").append(schemAndName).append(multiSqlSep());
        sb.append("CREATE TABLE IF NOT EXISTS ").append(schemAndName).append("(");
        for (Column column : table.getColumns()) {
            sb.append("`").append(column.getColumnName()).append("`");
            appendColumnType(sb, column);
            appendCharacter(sb, column);
            appendDefaultValue(sb, column);
            if (column.getRemarks() != null) sb.append(" COMMENT '").append(column.getRemarks()).append("'");
            sb.append(",");
        }
        if (NullHelp.nonEmpty(table.getPrimaryKeys()))
            sb.append("PRIMARY KEY ")
                    .append(table.getPrimaryKeys().stream().map(PrimaryKey::getColumnName).collect(Collectors.joining("`,`", "(`", "`)")))
                    .append(",");
        sb.replace(sb.length() - 1, sb.length(), ")");
        sb.append("ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '")
                .append(table.getRemarks() == null ? "" : table.getRemarks()).append("' ROW_FORMAT = Dynamic").append(multiSqlSep());
        return sb.toString();
    }

    private void appendColumnType(StringBuilder sb, Column column) {
        sb.append(" ");
        String[] split;
        switch (column.getDataType()) {
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL:
                split = column.getTypeName().split("\\s+");
                sb.append(split[0]).append("(").append(column.getColumnSize()).append(",").append(column.getDecimalDigits()).append(")");
                if (split.length == 2) sb.append(" ").append(split[1]);
                break;
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
                split = column.getTypeName().split("\\s+");
                if (split.length == 1) sb.append(split[0]).append("(").append(column.getColumnSize()).append(")");
                else sb.append(column.getTypeName());
                break;
            case Types.BIT:
            case Types.BOOLEAN:
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
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
                sb.append(" CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
        }
    }

    private void appendDefaultValue(StringBuilder sb, Column column) {
        sb.append(column.getNullable() == 0 ? " NOT NULL" : " NULL");
        if (column.getColumnDef() == null) {
            if (column.getNullable() == 0) return;
            sb.append(" DEFAULT NULL");
        } else {
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
        sb.append(" LIKE ");
        wrapTable(sb, src);
        return sb.toString();
    }

    @Override
    public final String createDatabase(String database) {
        return "CREATE DATABASE IF NOT EXISTS `" + database + "` DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci";
    }

    @Override
    public final String dropDatabase(String database) {
        return "DROP DATABASE IF EXISTS `" + database + "`";
    }

    @Override
    public final String backupDatabase(String disk, String database) {
        return "BACKUP DATABASE IF EXISTS `" + database + "` TO DISK = `" + disk + "` WITH FORMAT";
    }

    @Override
    public final String restoreDatabase(String disk, String database) {
        return "RESTORE DATABASE IF EXISTS `" + database + "` FROM DISK = `" + disk + "`";
    }

    @Override
    public List<String> fromIs(InputStream is) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        AtomicBoolean note = new AtomicBoolean();
        IOHelp.read(IOHelp.newBufferedReader(is), (Consumer<String>) s -> {
            if (NullHelp.isBlank(s)) return;
            String line = s.trim();
            if (line.startsWith("/*")) {
                note.set(true);
                return;
            }
            if (line.startsWith("*/")) {
                note.set(false);
                return;
            }
            if (note.get() || line.startsWith("-- ")) return;

            sb.append(line);

            if (line.endsWith(";")) {
                result.add(sb.substring(0, sb.length() - 1));
                sb.delete(0, sb.length());
            }
        });
        NullHelp.requireNon(note.get() || sb.length() > 0, "无效sql脚本");
        return result;
    }
}
