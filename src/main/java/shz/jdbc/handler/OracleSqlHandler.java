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
import java.util.regex.Pattern;

public class OracleSqlHandler implements DefaultSqlHandler {
    @Override
    public void escape(StringBuilder sb, Object val) {
        if (val == null) sb.append("null");
        else if (val instanceof Boolean || val instanceof Number) sb.append(val);
        else if (val instanceof Timestamp)
            sb.append("TO_DATE('").append(TimeHelp.format((Timestamp) val)).append("','YYYY-MM-DD HH24:MI:SS')");
        else if (val instanceof ZonedDateTime)
            sb.append("TO_DATE('").append(TimeHelp.format(((ZonedDateTime) val).toLocalDateTime())).append("','YYYY-MM-DD HH24:MI:SS')");
        else if (val instanceof LocalDateTime)
            sb.append("TO_DATE('").append(TimeHelp.format((LocalDateTime) val)).append("','YYYY-MM-DD HH24:MI:SS')");
        else if (val instanceof Instant)
            sb.append("TO_DATE('").append(TimeHelp.format((Instant) val)).append("','YYYY-MM-DD HH24:MI:SS')");
        else if (val instanceof LocalDate)
            sb.append("TO_DATE('").append(TimeHelp.format((LocalDate) val, TimeHelp.DATE_PATTERN)).append("','YYYY-MM-DD')");
        else if (val instanceof Date)
            sb.append("TO_DATE('").append(TimeHelp.format((Date) val)).append("','YYYY-MM-DD HH24:MI:SS')");
        else if (val instanceof LocalTime)
            sb.append("TO_DATE('").append(TimeHelp.format((LocalTime) val, TimeHelp.TIME_PATTERN)).append("','HH24:MI:SS')");
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
        sb.insert(0, "SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (");
        sb.append(") row_ where rownum <=").append(pageInfo.getMax()).append(") WHERE rownum_ >=").append(pageInfo.getMin());
    }

    @Override
    public final String humpToUnderline(String name) {
        return StringHelp.humpToUnderlineUpperCase(name);
    }

    @Override
    public final String aliasToField(String name) {
        return StringHelp.underlineToHump(name, false);
    }

    @Override
    public List<Segment> segments(String sql) {
        sql = RegexHelp.replace(sql, Pattern.compile("(?i:SELECT)(.+?)(?i:FROM)"), matcher -> {
            String s = matcher.group(0);
            StringBuilder sb = new StringBuilder(s.length() + 50);
            sb.append(s, 0, 6);
            String selects = matcher.group(1);
            String[] arr = selects.split(",");
            String[] a;
            for (int i = 0; i < arr.length; ++i) {
                a = arr[i].trim().split("\\s+");
                if (i>0) sb.append(',');
                else sb.append(' ');
                if (a.length == 2) sb.append(a[0]).append(' ').append(humpToUnderline(a[1]));
                else sb.append(arr[i]);
            }
            sb.append(s, selects.length() + 6, s.length());
            return sb.toString();
        });
        return Segment.of(sql);
    }

    @Override
    public final String createTable(Table table) {
        StringBuilder sb = new StringBuilder();
        String schemAndName = (NullHelp.isBlank(table.getTableSchem()) ? "" : "\"" + table.getTableSchem() + "\".") + "\"" + table.getTableName() + "\"";
        sb.append("CREATE TABLE ").append(schemAndName).append(" (");
        for (Column column : table.getColumns()) {
            sb.append("\"").append(column.getColumnName()).append("\"");
            appendColumnType(sb, column);
            if (column.getNullable() == 0) sb.append(" NOT NULL");
            sb.append(",");
        }
        sb.replace(sb.length() - 1, sb.length(), ")");
        sb.append(multiSqlSep());

        for (Column column : table.getColumns())
            if (column.getRemarks() != null)
                sb.append("COMMENT ON COLUMN ").append(schemAndName).append(".\"").append(column.getColumnName())
                        .append("\" IS '").append(column.getRemarks()).append("'").append(multiSqlSep());

        sb.append("COMMENT ON TABLE ").append(schemAndName).append(" IS '").append(table.getRemarks() == null ? "" : table.getRemarks()).append("'").append(multiSqlSep());

        if (NullHelp.nonEmpty(table.getPrimaryKeys()))
            for (PrimaryKey primaryKey : table.getPrimaryKeys())
                sb.append("ALTER TABLE ").append(schemAndName).append(" ADD CHECK (\"").append(primaryKey.getColumnName()).append("\" IS NOT NULL)").append(multiSqlSep())
                        .append("ALTER TABLE ").append(schemAndName).append("ADD PRIMARY KEY (\"").append(primaryKey.getColumnName()).append("\")").append(multiSqlSep());

        return sb.toString();
    }

    private void appendColumnType(StringBuilder sb, Column column) {
        sb.append(" ");
        String[] split;
        switch (column.getDataType()) {
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.NUMERIC:
                split = column.getTypeName().split("\\s+");
                sb.append(split[0]).append("(").append(column.getColumnSize()).append(",").append(column.getDecimalDigits()).append(")");
                if (split.length == 2) sb.append(" ").append(split[1]);
                break;
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.DECIMAL:
                split = column.getTypeName().split("\\s+");
                if (split.length == 1)
                    sb.append(split[0]).append("(")
                            .append(column.getColumnSize() <= 0 || column.getColumnSize() > 38 ? 38 : column.getColumnSize())
                            .append(")");
                else sb.append(column.getTypeName());
                break;
            case Types.BIT:
            case Types.BOOLEAN:
                sb.append(column.getTypeName()).append("(").append(column.getColumnSize()).append(")");
                break;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                sb.append(column.getTypeName()).append("(").append(column.getColumnSize()).append(" ").append(" BYTE").append(")");
                break;
            default:
                sb.append(column.getTypeName());
        }
    }

    @Override
    public final String dropTable(Tnp tnp) {
        StringBuilder sb = new StringBuilder();
        sb.append("DROP TABLE ");
        wrapTable(sb, tnp);
        return sb.toString();
    }

    @Override
    public final String copyTableStructure(Tnp src, Tnp des) {
        return null;
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
