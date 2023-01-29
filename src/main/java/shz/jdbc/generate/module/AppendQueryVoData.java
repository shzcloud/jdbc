package shz.jdbc.generate.module;

import shz.core.ToSet;
import shz.jdbc.generate.AppendData;
import shz.jdbc.generate.Tgp;
import shz.jdbc.model.Column;
import shz.jdbc.model.Table;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AppendQueryVoData extends AppendData {
    @Override
    protected String comment(Table table) {
        return "/**\n" +
                " * 查询" + desc(table) + "\n" +
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
        return "public class Query" + className(tgp.table) + "Vo";
    }

    @Override
    protected List<String> content(Tgp tgp, Set<String> imports) {
        List<String> content = new LinkedList<>();

        List<Column> columns = tgp.table.getColumns();
        Set<String> excludedColumns = excludedColumns();

        content.add("    @Getter");
        content.add("    @Setter");
        content.add("    @ToString");
        content.add("    public static class Vo {");

        for (Column column : columns) {
            if (excludedColumns.contains(column.getColumnName().toLowerCase())) continue;
            content.add("        /**");
            content.add("         * " + fieldComment(tgp, column, imports));
            content.add("         */");
            content.add(fieldContent(column, imports));
        }
        content.add("    }");

        return content;
    }

    protected Set<String> excludedColumns() {
        return ToSet.asSet("del_flag", "root_id", "level", "tag", "parent_id");
    }

    private String fieldContent(Column column, Set<String> imports) {
        String type = getType(column);
        String entityFieldImport = getImport(type);
        if (entityFieldImport != null) imports.add(entityFieldImport);
        return "        private " + type + " " + fieldName(column) + ";\n";
    }
}
