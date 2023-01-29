package shz.jdbc.generate.module;

import shz.core.*;
import shz.core.msg.ServerFailureMsg;
import shz.core.stack.a.IArrayStack;
import shz.core.tag.LLTag;
import shz.jdbc.generate.AppendData;
import shz.jdbc.generate.Tgp;
import shz.jdbc.model.Column;
import shz.jdbc.model.PrimaryKey;
import shz.jdbc.model.Table;
import shz.orm.entity.BaseEntity;
import shz.orm.entity.RecordEntity;
import shz.orm.entity.TreeEntity;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppendEntityData extends AppendData {
    @Override
    protected String comment(Table table) {
        return "/**\n" +
                " * " + desc(table) + "\n" +
                " */";
    }

    @Override
    protected List<String> annotations(Tgp tgp, Set<String> imports) {
        List<String> annotations = new LinkedList<>();

        annotations.add("@Getter");
        annotations.add("@Setter");

        if (superEntity(tgp.table) == null) annotations.add("@ToString");
        else annotations.add("@ToString(callSuper = true)");

        if (NullHelp.isBlank(tgp.table.getTableSchem()))
            annotations.add("@Table(\"" + tgp.table.getTableName() + "\")");
        else
            annotations.add("@Table(value = \"" + tgp.table.getTableName() + "\", schema = \"" + tgp.table.getTableSchem() + "\")");

        imports.add("import lombok.Getter;");
        imports.add("import lombok.Setter;");
        imports.add("import lombok.ToString;");
        imports.add("import shz.orm.annotation.Table;");

        return annotations;
    }

    @Override
    protected String cls(Tgp tgp, Set<String> imports) {
        String className = className(tgp.table);
        Class<?> superEntity = superEntity(tgp.table);
        if (superEntity == null) return "public class " + className;

        imports.add("import " + superEntity.getName() + ";");
        if (TreeEntity.class.isAssignableFrom(superEntity))
            return "public class " + className + " extends " + superEntity.getSimpleName() + "<" + className + ">";
        if (BaseEntity.class.isAssignableFrom(superEntity)
                || RecordEntity.class.isAssignableFrom(superEntity))
            return "public class " + className + " extends " + superEntity.getSimpleName();

        return "public class " + className;
    }

    @Override
    protected List<String> content(Tgp tgp, Set<String> imports) {
        List<String> content = new LinkedList<>();

        Set<String> primaryKeys = ToSet.collect(tgp.table.getPrimaryKeys().stream().map(PrimaryKey::getColumnName));
        String className = className(tgp.table);
        List<String> data = new LinkedList<>();

        tgp.table.getColumns().forEach(column -> {
            String fieldName = fieldName(column);

            if (!AccessibleHelp.fields(superEntity(tgp.table), field -> field.getName().equals(fieldName), 1).isEmpty())
                return;

            String type = getType(column);

            boolean hasEnum = writeEnum(tgp);
            if (hasEnum) {
                switch (type) {
                    case "Boolean":
                    case "Long":
                    case "Float":
                    case "Double":
                    case "LocalDate":
                    case "LocalTime":
                    case "LocalDateTime":
                        hasEnum = false;
                        break;
                    default:
                        break;
                }
            }

            String enumType = "", enumDesc = "";
            if (hasEnum) {
                LLTag<String, List<LLTag<String, String>>> llTag = getEnum(column.getRemarks());
                hasEnum = llTag != null;
                if (hasEnum) {
                    //生成枚举
                    enumType = className + StringHelp.capitalize(fieldName) + "Enum";
                    enumDesc = llTag.tag == null ? "" : llTag.tag;

                    data.add("package " + tgp.enumGenInfo.packageName + ";\n");

                    boolean nameCode;
                    if (!"String".equals(type)) nameCode = false;
                    else {
                        nameCode = true;
                        Pattern pattern = Pattern.compile("[A-Za-z_$\\d]+");
                        Pattern nameCodePattern = Pattern.compile("^[A-Za-z_$][A-Za-z_$\\d]*$");
                        for (LLTag<String, String> codeValue : llTag.data) {
                            if (!pattern.matcher(codeValue.tag).matches())
                                throw PRException.of(ServerFailureMsg.fail("枚举字符异常,类:%s,属性:%s", className, fieldName));

                            if (!nameCodePattern.matcher(codeValue.tag).matches()) {
                                nameCode = false;
                                break;
                            }
                        }
                    }

                    if (nameCode) {
                        data.add("import shz.core.enums.NameCodeEnum;");
                        data.add("import lombok.Getter;");
                        data.add("\n/**\n" +
                                " * " + desc(tgp.table) + enumDesc + "\n" +
                                " */");
                        data.add("@Getter");
                        data.add("public enum " + enumType + " implements NameCodeEnum<String> {");

                        for (LLTag<String, String> codeValue : llTag.data)
                            data.add("    " + codeValue.tag + "(\"" + codeValue.data + "\"),");

                        data.add("\n    ;\n");
                        data.add("    private final String value;\n");
                        data.add("    " + enumType + "(String value) {");
                    } else {
                        data.add("import shz.core.enums.IEnum;");
                        data.add("import lombok.Getter;");
                        data.add("\n/**\n" +
                                " * " + desc(tgp.table) + enumDesc + "\n" +
                                " */");
                        data.add("@Getter");
                        data.add("public enum " + enumType + " implements IEnum<" + type + ", String> {");

                        for (LLTag<String, String> codeValue : llTag.data) {
                            String code = "V_" + codeValue.tag;
                            switch (type) {
                                case "Byte":
                                    data.add("    " + code + "((byte)" + codeValue.tag + ", \"" + codeValue.data + "\"),");
                                    break;
                                case "Short":
                                    data.add("    " + code + "((short)" + codeValue.tag + ", \"" + codeValue.data + "\"),");
                                    break;
                                case "Integer":
                                    data.add("    " + code + "(" + codeValue.tag + ", \"" + codeValue.data + "\"),");
                                    break;
                                default:
                                    data.add("    " + code + "(\"" + codeValue.tag + "\", \"" + codeValue.data + "\"),");
                            }
                        }

                        data.add("\n    ;\n");
                        data.add("    private final " + type + " code;");
                        data.add("    private final String value;\n");
                        data.add("    " + enumType + "(" + type + " code, String value) {");
                        data.add("        this.code = code;");
                    }
                    data.add("        this.value = value;\n    }\n}");

                    write(tgp, tgp.enumGenInfo, data, null, enumType + ".java");

                    putEnumType(tgp.table, column, enumType);
                    putEnumDesc(tgp.table, column, enumDesc);
                }
            }

            StringBuilder sb = new StringBuilder();
            if (NullHelp.nonBlank(column.getRemarks())) {
                sb.append("    /**\n     * ");
                if (hasEnum) {
                    sb.append(enumDesc).append("{@link ").append(enumType).append("}");
                    imports.add("import " + tgp.enumGenInfo.packageName + "." + enumType + ";");
                } else sb.append(column.getRemarks());
                sb.append("\n     */\n");
            }

            boolean isPrimaryKey = primaryKeys.contains(column.getColumnName());
            String s;
            if (isPrimaryKey) s = idAnnotation(tgp.table, imports);
            else s = columnAnnotation(tgp.table, column, imports);
            if (NullHelp.nonBlank(s)) sb.append("    ").append(s).append("\n");

            sb.append("    private ").append(type).append(" ").append(fieldName).append(";");

            String entityFieldImport = getImport(type);
            if (entityFieldImport != null) imports.add(entityFieldImport);

            content.add(sb.toString());
        });
        return content;
    }

    protected String idAnnotation(Table table, Set<String> imports) {
        imports.add("import shz.orm.annotation.Id;");
        return "@Id";
    }

    protected String columnAnnotation(Table table, Column column, Set<String> imports) {
        imports.add("import shz.orm.annotation.Where;");
        return "@Where";
    }

    /**
     * 提取remarks为枚举值
     */
    private LLTag<String, List<LLTag<String, String>>> getEnum(String remarks) {
        if (NullHelp.isBlank(remarks)) return null;
        String codeRegex = "\\w+";
        String valueRegex = ".+";
        String s1Regex = "\\s*[,，;；、]+\\s*";
        String s2Regex = "\\s*[:：.\\s-]+\\s*";
        String regex = "((" + codeRegex + "(" + s2Regex + ")" + valueRegex + ")((" + s1Regex + ")(" + codeRegex + "(" + s2Regex + ")" + valueRegex + "))*)";
        int flags = Pattern.MULTILINE | Pattern.DOTALL;

        Matcher matcher = Pattern.compile(regex, flags).matcher(remarks);
        if (!matcher.find()) return null;
        Pattern s2Pattern = Pattern.compile(s2Regex, flags);
        Pattern codePattern = Pattern.compile(codeRegex, flags);
        Pattern valuePattern = Pattern.compile(valueRegex, flags);
        String s = matcher.group(1);

        String[] cvs = Arrays.stream(s.split(s1Regex)).map(String::trim).toArray(String[]::new);
        List<LLTag<String, String>> result = new ArrayList<>(cvs.length);
        for (String cv : cvs) {
            String[] c_v = cv.split(s2Regex);

            Matcher codeMatcher = null;
            int i = 0;
            for (; i < c_v.length - 1; ++i) {
                codeMatcher = codePattern.matcher(c_v[i]);
                if (codeMatcher.find()) break;
            }
            if (codeMatcher == null || i == c_v.length - 1) continue;
            String code = codeMatcher.group(0);

            String containsValue = cv.substring(cv.indexOf(code) + code.length());

            Matcher s2Matcher = s2Pattern.matcher(containsValue);
            if (!s2Matcher.find()) continue;
            String s2 = s2Matcher.group(0);

            Matcher valueMatcher = valuePattern.matcher(containsValue.substring(containsValue.indexOf(s2) + s2.length()));
            if (!valueMatcher.find()) continue;
            String value = valueMatcher.group(0);

            result.add(new LLTag<>(code, clear(value)));
        }

        if (result.isEmpty()) return null;
        int delta = remarks.length() - s.length();
        String sp = null;
        if (delta > 0) sp = RegexHelp.find(remarks.substring(0, delta), "[\u4e00-\u9fa5]+");
        return new LLTag<>(sp, result);
    }

    /**
     * 清除缺失不完整的括号
     */
    private String clear(String s) {
        int len = s.length();
        if (len <= 1) return s;
        Set<Integer> clears = new HashSet<>();
        IArrayStack stack = IArrayStack.of();
        for (int i = 0; i < len; ++i) {
            char c = s.charAt(i);
            if (c == '(' || c == '（') {
                clears.add(i);
                stack.push(i);
            } else if (c == ')' || c == '）') {
                if (!stack.isEmpty()) clears.remove(stack.pop());
                else clears.add(i);
            }
        }
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; ++i) if (!clears.contains(i)) sb.append(s.charAt(i));
        return sb.toString();
    }
}
