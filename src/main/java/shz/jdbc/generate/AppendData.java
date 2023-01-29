package shz.jdbc.generate;

import shz.jdbc.model.Column;
import shz.jdbc.model.Table;

import java.util.*;
import java.util.function.BiConsumer;

public abstract class AppendData {
    Generator generator;

    protected abstract String comment(Table table);

    protected List<String> annotations(Tgp tgp, Set<String> imports) {
        return new LinkedList<>();
    }

    protected abstract String cls(Tgp tgp, Set<String> imports);

    protected List<String> content(Tgp tgp, Set<String> imports) {
        return new LinkedList<>();
    }

    protected final boolean writeEntity(Tgp tgp) {
        return (generator.flags(tgp.table) & Generator.FILE_ENTITY) != 0 && tgp.entityGenInfo != null;
    }

    protected final boolean writeAddVo(Tgp tgp) {
        return (generator.flags(tgp.table) & Generator.FILE_VO) != 0 && tgp.addVoGenInfo != null;
    }

    protected final boolean writeUpdateVo(Tgp tgp) {
        return (generator.flags(tgp.table) & Generator.FILE_VO) != 0 && tgp.updateVoGenInfo != null;
    }

    protected final boolean writeQueryVo(Tgp tgp) {
        return (generator.flags(tgp.table) & Generator.FILE_VO) != 0 && tgp.queryVoGenInfo != null;
    }

    protected final boolean writeDetailVo(Tgp tgp) {
        return (generator.flags(tgp.table) & Generator.FILE_VO) != 0 && tgp.detailVoGenInfo != null;
    }

    protected final boolean writeApi(Tgp tgp) {
        return (generator.flags(tgp.table) & Generator.FILE_API) != 0 && tgp.apiGenInfo != null;
    }

    protected final boolean writeService(Tgp tgp) {
        return (generator.flags(tgp.table) & Generator.FILE_SERVICE) != 0 && tgp.serviceGenInfo != null;
    }

    protected final boolean writeServiceImpl(Tgp tgp) {
        return (generator.flags(tgp.table) & Generator.FILE_SERVICE_IMPL) != 0 && tgp.serviceImplGenInfo != null;
    }

    protected final boolean writeRepository(Tgp tgp) {
        return (generator.flags(tgp.table) & Generator.FILE_REPOSITORY) != 0 && tgp.repositoryGenInfo != null;
    }

    protected final boolean writeController(Tgp tgp) {
        return (generator.flags(tgp.table) & Generator.FILE_CONTROLLER) != 0 && tgp.controllerGenInfo != null;
    }

    protected final boolean writeEnum(Tgp tgp) {
        return (generator.flags(tgp.table) & Generator.FILE_ENUM) != 0 && tgp.enumGenInfo != null;
    }

    protected final boolean writeApiClient(Tgp tgp) {
        return (generator.flags(tgp.table) & Generator.FILE_API_CLIENT) != 0 && tgp.apiClientGenInfo != null;
    }

    protected final boolean writeApiClientFallback(Tgp tgp) {
        return (generator.flags(tgp.table) & Generator.FILE_API_CLIENT_FALLBACK) != 0 && tgp.apiClientFallbackGenInfo != null;
    }

    protected final void write(Tgp tgp, GenInfo genInfo, List<String> data, BiConsumer<Tgp, List<String>> append, String filename) {
        generator.write(tgp, genInfo, data, append, filename);
    }

    protected final Class<?> superEntity(Table table) {
        return generator.superEntity(table);
    }

    protected final String className(Table table) {
        return generator.className(table);
    }

    protected final String fieldName(Column column) {
        return generator.fieldName(column);
    }

    protected final String primaryKeyType(Table table) {
        return generator.primaryKeyType(table);
    }

    protected final String getType(Column column) {
        return generator.getType(column);
    }

    protected final String getImport(String type) {
        return generator.getImport(type);
    }

    protected final String module(Table table) {
        return generator.module(table);
    }

    protected final String urlSuffix(Table table) {
        return generator.urlSuffix(table);
    }

    protected final String version() {
        return generator.version();
    }

    protected final String requestMapping(Table table) {
        return generator.requestMapping(table);
    }

    protected final String date() {
        return generator.date();
    }

    protected final String desc(Table table) {
        return generator.desc(table);
    }

    protected final String apiTags(Table table) {
        return generator.apiTags(table);
    }

    protected final String user(Table table) {
        return generator.user(table);
    }

    protected final String apiClientIdPrefix(Table table) {
        return generator.apiClientIdPrefix(table);
    }

    private static final Map<String, String> ENUM_TYPE_CACHE = new HashMap<>();

    protected final void putEnumType(Table table, Column column, String type) {
        ENUM_TYPE_CACHE.put(table.getTableName() + ":" + column.getColumnName(), type);
    }

    protected final String getEnumType(Table table, Column column) {
        return ENUM_TYPE_CACHE.get(table.getTableName() + ":" + column.getColumnName());
    }

    private static final Map<String, String> ENUM_DESC_CACHE = new HashMap<>();

    protected final void putEnumDesc(Table table, Column column, String desc) {
        ENUM_DESC_CACHE.put(table.getTableName() + ":" + column.getColumnName(), desc);
    }

    protected final String getEnumDesc(Table table, Column column) {
        return ENUM_DESC_CACHE.get(table.getTableName() + ":" + column.getColumnName());
    }

    protected final String fieldComment(Tgp tgp, Column column, Set<String> imports) {
        String enumType = getEnumType(tgp.table, column);
        if (enumType == null) return column.getRemarks();
        imports.add("import " + tgp.enumGenInfo.packageName + "." + enumType + ";");
        return getEnumDesc(tgp.table, column) + "{@link " + enumType + "}";
    }
}
