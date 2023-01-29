package shz.jdbc.generate.module;

import shz.core.ToSet;
import shz.jdbc.generate.AppendData;
import shz.jdbc.generate.Tgp;
import shz.jdbc.model.Column;
import shz.jdbc.model.Table;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AppendAddVoData extends AppendData {
    @Override
    protected String comment(Table table) {
        return "/**\n" +
                " * 新增" + desc(table) + "\n" +
                " */";
    }

    @Override
    protected List<String> annotations(Tgp tgp, Set<String> imports) {
        List<String> annotations = new LinkedList<>();

        annotations.add("@Getter");
        annotations.add("@Setter");
        annotations.add("@ToString");

        imports.add("import lombok.Getter;");
        imports.add("import lombok.Setter;");
        imports.add("import lombok.ToString;");

        return annotations;
    }

    @Override
    protected String cls(Tgp tgp, Set<String> imports) {
        return "public class Add" + className(tgp.table) + "Vo";
    }

    @Override
    protected List<String> content(Tgp tgp, Set<String> imports) {
        List<String> content = new LinkedList<>();

        List<Column> columns = tgp.table.getColumns();
        Set<String> excludedColumns = excludedColumn();
        String s;
        for (Column column : columns) {
            if (excludedColumns.contains(column.getColumnName().toLowerCase())) continue;
            content.add("    /**");
            content.add("     * " + fieldComment(tgp, column, imports));
            content.add("     */");
            s = validAnnotation(column, imports);
            if (s != null) content.add(s);
            content.add(fieldContent(column, imports));
        }

        return content;
    }

    protected Set<String> excludedColumn() {
        return ToSet.asSet("del_flag", "root_id", "level", "tag", "create_time", "update_time", "create_by", "update_by", "id", "version");
    }

    private String validAnnotation(Column column, Set<String> imports) {
        if (column.getNullable() == 1 || column.getColumnDef() != null) return null;
        if ("String".equals(getType(column))) {
            imports.add("import javax.validation.constraints.NotBlank;");
            return "    @NotBlank(message = \"" + column.getRemarks() + "不能为空\")";
        }
        imports.add("import javax.validation.constraints.NotNull;");
        return "    @NotNull(message = \"" + column.getRemarks() + "不能为空\")";
    }

    private String fieldContent(Column column, Set<String> imports) {
        String type = getType(column);
        String entityFieldImport = getImport(type);
        if (entityFieldImport != null) imports.add(entityFieldImport);
        return "    private " + type + " " + fieldName(column) + ";";
    }
}
