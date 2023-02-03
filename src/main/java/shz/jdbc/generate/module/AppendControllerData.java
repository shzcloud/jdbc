package shz.jdbc.generate.module;

import shz.core.StringHelp;
import shz.jdbc.generate.AppendData;
import shz.jdbc.generate.Tgp;
import shz.jdbc.model.Table;
import shz.orm.entity.RecordEntity;
import shz.orm.entity.TreeEntity;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AppendControllerData extends AppendData {
    @Override
    protected String comment(Table table) {
        return "/**\n" +
                " * " + apiTags(table) + "\n" +
                " */";
    }

    @Override
    protected List<String> annotations(Tgp tgp, Set<String> imports) {
        List<String> annotations = new LinkedList<>();

        annotations.add("@RestController");
        annotations.add("@RequestMapping(\"" + requestMapping(tgp.table) + "\")");

        imports.add("import org.springframework.web.bind.annotation.RestController;");
        imports.add("import org.springframework.web.bind.annotation.RequestMapping;");

        return annotations;
    }

    @Override
    protected String cls(Tgp tgp, Set<String> imports) {
        String className = className(tgp.table);

        if (writeApi(tgp)) {
            imports.add("import " + tgp.apiGenInfo.packageName + "." + className + "Api;");

            return "public class " + className + "Controller implements " + className + "Api";
        }

        return "public class " + className + "Controller";
    }

    @Override
    protected List<String> content(Tgp tgp, Set<String> imports) {
        List<String> content = new LinkedList<>();

        String className = className(tgp.table);
        String classFieldName = StringHelp.pojo(className);
        String desc = desc(tgp.table);
        Class<?> superEntity = superEntity(tgp.table);

        content.add("    @Autowired");
        content.add("    " + className + "Service " + classFieldName + "Service;\n");

        if (superEntity != null && RecordEntity.class.isAssignableFrom(superEntity)) {
            content.add("    /**");
            content.add("     * " + desc + "分页列表");
            content.add("     */");
            content.add("    @PostMapping(\"page\")");
            content.add("    public Response<PageInfo<Query" + className + "Vo.Vo>> page(@RequestBody @Valid PageVo<Query" + className + "Vo, Query" + className + "Vo.Vo> pageVo) {");
            content.add("        PageInfo<Query" + className + "Vo.Vo> page = " + classFieldName + "Service.page(pageVo);");
            content.add("        return Response.ok(page);");
            content.add("    }");

            imports.add("import " + tgp.queryVoGenInfo.packageName + ".Query" + className + "Vo;");
            imports.add("import shz.core.model.PageInfo;");
            imports.add("import shz.spring.model.PageVo;");
        } else if (superEntity != null && TreeEntity.class.isAssignableFrom(superEntity)) {
            content.add("    /**");
            content.add("     * 新增" + desc);
            content.add("     */");
            content.add("    @PostMapping");
            content.add("    public Response<Long> add(@RequestBody @Valid Add" + className + "Vo vo) {");
            content.add("        " + className + " entity = FieldSetter.copy(vo, new " + className + "());");
            content.add("        " + classFieldName + "Service.add(entity);");
            content.add("        return Response.ok(entity.getId());");
            content.add("    }\n");

            content.add("    /**");
            content.add("     * 编辑" + desc);
            content.add("     */");
            content.add("    @PutMapping");
            content.add("    public Response<Void> update(@RequestBody @Valid Update" + className + "Vo vo) {");
            content.add("        " + className + " entity = FieldSetter.copy(vo, new " + className + "());");
            content.add("        " + classFieldName + "Service.update(entity);");
            content.add("        return Response.ok();");
            content.add("    }\n");

            String type = primaryKeyType(tgp.table);
            String typeImport = getImport(type);
            if (typeImport != null) imports.add(typeImport);

            content.add("    /**");
            content.add("     * 删除" + desc);
            content.add("     *");
            content.add("     * @param ids " + desc + "id集");
            content.add("     */");
            content.add("    @DeleteMapping(\"{ids}\")");
            content.add("    public Response<Void> delete(@PathVariable(\"ids\") List<" + type + "> ids) {");
            content.add("        " + classFieldName + "Service.delete(ids);");
            content.add("        return Response.ok();");
            content.add("    }\n");

            content.add("    /**");
            content.add("     * " + desc + "列表");
            content.add("     */");
            content.add("    @GetMapping(\"list\")");
            content.add("    public Response<List<" + className + ">> list() {");
            content.add("        List<" + className + "> list = " + classFieldName + "Service.list();");
            content.add("        List<" + className + "> tree = TreeEntity.group(list);");
            content.add("        TreeEntity.sort(tree);");
            content.add("        return Response.ok(tree);");
            content.add("    }\n");

            content.add("    /**");
            content.add("     * " + desc + "详情");
            content.add("     *");
            content.add("     * @param id " + desc + "id");
            content.add("     */");
            content.add("    @GetMapping(\"detail/{id}\")");
            content.add("    public Response<" + className + "DetailVo> detail(@PathVariable(\"id\") " + type + " id) {");
            content.add("        " + className + "DetailVo detail = " + classFieldName + "Service.detail(id);");
            content.add("        return Response.ok(detail);");
            content.add("    }");

            imports.add("import " + tgp.addVoGenInfo.packageName + ".Add" + className + "Vo;");
            imports.add("import " + tgp.updateVoGenInfo.packageName + ".Update" + className + "Vo;");
            imports.add("import " + tgp.detailVoGenInfo.packageName + "." + className + "DetailVo;");
            imports.add("import java.util.List;");
            imports.add("import shz.core.FieldSetter;");
            imports.add("import shz.orm.entity.TreeEntity;");
        } else {
            content.add("    /**");
            content.add("     * 新增" + desc);
            content.add("     */");
            content.add("    @PostMapping");
            content.add("    public Response<Long> add(@RequestBody @Valid Add" + className + "Vo vo) {");
            content.add("        " + className + " entity = FieldSetter.copy(vo, new " + className + "());");
            content.add("        " + classFieldName + "Service.add(entity);");
            content.add("        return Response.ok(entity.getId());");
            content.add("    }\n");

            content.add("    /**");
            content.add("     * 编辑" + desc);
            content.add("     */");
            content.add("    @PutMapping");
            content.add("    public Response<Void> update(@RequestBody @Valid Update" + className + "Vo vo) {");
            content.add("        " + className + " entity = FieldSetter.copy(vo, new " + className + "());");
            content.add("        " + classFieldName + "Service.update(entity);");
            content.add("        return Response.ok();");
            content.add("    }\n");

            String type = primaryKeyType(tgp.table);
            String typeImport = getImport(type);
            if (typeImport != null) imports.add(typeImport);

            content.add("    /**");
            content.add("     * 删除" + desc);
            content.add("     *");
            content.add("     * @param ids " + desc + "id集");
            content.add("     */");
            content.add("    @DeleteMapping(\"{ids}\")");
            content.add("    public Response<Void> delete(@PathVariable(\"ids\") List<" + type + "> ids) {");
            content.add("        " + classFieldName + "Service.delete(ids);");
            content.add("        return Response.ok();");
            content.add("    }\n");

            content.add("    /**");
            content.add("     * " + desc + "分页列表");
            content.add("     */");
            content.add("    @PostMapping(\"page\")");
            content.add("    public Response<PageInfo<Query" + className + "Vo.Vo>> page(@RequestBody @Valid PageVo<Query" + className + "Vo, Query" + className + "Vo.Vo> pageVo) {");
            content.add("        PageInfo<Query" + className + "Vo.Vo> pageInfo = " + classFieldName + "Service.page(pageVo);");
            content.add("        return Response.ok(pageInfo);");
            content.add("    }\n");

            content.add("    /**");
            content.add("     * " + desc + "详情");
            content.add("     *");
            content.add("     * @param id " + desc + "id");
            content.add("     */");
            content.add("    @GetMapping(\"detail/{id}\")");
            content.add("    public Response<" + className + "DetailVo> detail(@PathVariable(\"id\") " + type + " id) {");
            content.add("        " + className + "DetailVo detail = " + classFieldName + "Service.detail(id);");
            content.add("        return Response.ok(detail);");
            content.add("    }");

            imports.add("import " + tgp.addVoGenInfo.packageName + ".Add" + className + "Vo;");
            imports.add("import " + tgp.updateVoGenInfo.packageName + ".Update" + className + "Vo;");
            imports.add("import " + tgp.queryVoGenInfo.packageName + ".Query" + className + "Vo;");
            imports.add("import " + tgp.detailVoGenInfo.packageName + "." + className + "DetailVo;");
            imports.add("import java.util.List;");
            imports.add("import shz.core.FieldSetter;");
            imports.add("import shz.core.model.PageInfo;");
            imports.add("import shz.spring.model.PageVo;");
        }

        imports.add("import " + tgp.serviceGenInfo.packageName + "." + className + "Service;");
        imports.add("import " + tgp.entityGenInfo.packageName + "." + className + ";");
        imports.add("import org.springframework.beans.factory.annotation.Autowired;");

        imports.add("import shz.core.model.Response;");
        imports.add("import javax.validation.Valid;");
        imports.add("import org.springframework.web.bind.annotation.*;");

        return content;
    }
}
