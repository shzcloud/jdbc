package shz.jdbc.generate.module;

import shz.jdbc.generate.AppendData;
import shz.jdbc.generate.Tgp;
import shz.jdbc.model.Table;
import shz.orm.entity.RecordEntity;
import shz.orm.entity.TreeEntity;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AppendServiceImplData extends AppendData {
    @Override
    protected String comment(Table table) {
        return null;
    }

    @Override
    protected List<String> annotations(Tgp tgp, Set<String> imports) {
        List<String> annotations = new LinkedList<>();

        annotations.add("@Service");

        imports.add("import org.springframework.stereotype.Service;");

        return annotations;
    }

    @Override
    protected String cls(Tgp tgp, Set<String> imports) {
        String className = className(tgp.table);
        Class<?> superEntity = superEntity(tgp.table);

        imports.add("import " + tgp.serviceGenInfo.packageName + "." + className + "Service;");
        imports.add("import " + tgp.entityGenInfo.packageName + "." + className + ";");

        if (superEntity != null && RecordEntity.class.isAssignableFrom(superEntity)) {
            imports.add("import shz.orm.record.AbstractRecordService;");
            return "class " + className + "ServiceImpl extends AbstractRecordService<" + className + "> implements " + className + "Service";
        }

        imports.add("import shz.jdbc.BaseService;");
        imports.add("import " + tgp.repositoryGenInfo.packageName + "." + className + "Repository;");

        return "class " + className + "ServiceImpl extends BaseService<" + className + ", " + className + "Repository> implements " + className + "Service";
    }

    @Override
    protected List<String> content(Tgp tgp, Set<String> imports) {
        List<String> content = new LinkedList<>();
        String className = className(tgp.table);
        String desc = desc(tgp.table);
        Class<?> superEntity = superEntity(tgp.table);

        if (superEntity != null && RecordEntity.class.isAssignableFrom(superEntity)) {
            content.add("    @Autowired");
            content.add("    JdbcService jdbcService;\n");

            content.add("    @Override");
            content.add("    public PageInfo<Query" + className + "Vo.Vo> page(PageVo<Query" + className + "Vo, Query" + className + "Vo.Vo> pageVo) {");
            content.add("        Query" + className + "Vo reqVo = pageVo.getData();");
            content.add("        ActionRunner<" + className + "> runner = runner(null, null, null, jdbcService.whereSql(" + className + ".class, reqVo, null, false));");
            content.add("        PageInfo<" + className + "> page = page(pageVo.map(), runner, null, Comparator.comparing(" + className + "::getCreateTime));");
            content.add("        return page.map(entity -> FieldSetter.copy(entity, new Query" + className + "Vo.Vo()));");
            content.add("    }\n");

            content.add("    @Override");
            content.add("    public void record(" + className + " record) {");
            content.add("        insert(record);");
            content.add("    }\n");

            content.add("    @Override");
            content.add("    public void batchRecord(List<" + className + "> records) {");
            content.add("        batchInsert(records);");
            content.add("    }");

            imports.add("import " + tgp.queryVoGenInfo.packageName + ".Query" + className + "Vo;");
            imports.add("import shz.jdbc.JdbcService;");
            imports.add("import shz.core.model.PageInfo;");
            imports.add("import shz.spring.model.PageVo;");
            imports.add("import shz.core.function.ActionRunner;");
            imports.add("import shz.core.FieldSetter;");
            imports.add("import java.util.Comparator;");
            imports.add("import java.util.List;");
        } else if (superEntity != null && TreeEntity.class.isAssignableFrom(superEntity)) {
            content.add("    @Lock(\"" + desc + "\")");
            content.add("    public int add(@LockKey(\"parentId\") " + className + " entity) {");
            content.add("        check(entity);");
            content.add("        return jdbcService.insertTree(entity, tree -> ClientFailureMsg.requireNon(checkUniqueForInsert(tree, \"parentId\", \"name\"), \"" + desc + "已经存在\"), \"" + desc + "\");");
            content.add("    }\n");

            content.add("    @Override");
            content.add("    protected void check(" + className + " entity) {");
            content.add("    }\n");

            content.add("    @Lock(\"" + desc + "\")");
            content.add("    public int update(@LockKey(\"parentId\") " + className + " entity) {");
            content.add("        checkId(entity.getId());");
            content.add("        check(entity);");
            content.add("        return jdbcService.updateTree(entity, tree -> ClientFailureMsg.requireNon(checkUniqueForUpdate(tree, \"parentId\", \"name\"), \"" + desc + "已经存在\"),  \"" + desc + "\");");
            content.add("    }\n");

            content.add("    @Override");
            content.add("    public int delete(Collection<?> ids) {");
            content.add("        checkId(ids);");
            content.add("        int row = super.delete(ids);");
            content.add("        ServerFailureMsg.requireNon(row != ids.size(), \"删除" + desc + "失败\");");
            content.add("        return row;");
            content.add("    }\n");

            content.add("    @Override");
            content.add("    public List<" + className + "> list() {");
            content.add("        return selectList(null);");
            content.add("    }\n");

            content.add("    @Override");
            content.add("    public " + className + "DetailVo detail(Long id) {");
            content.add("        " + className + " entity = selectById(id);");
            content.add("        ClientFailureMsg.requireNonNull(entity, \"" + desc + "不存在\");");
            content.add("        return FieldSetter.copy(entity, new " + className + "DetailVo());");
            content.add("    }");

            imports.add("import " + tgp.detailVoGenInfo.packageName + "." + className + "DetailVo;");
            imports.add("import shz.core.msg.ClientFailureMsg;");
            imports.add("import shz.core.msg.ServerFailureMsg;");
            imports.add("import shz.core.FieldSetter;");
            imports.add("import java.util.List;");
            imports.add("import java.util.Collection;");
            imports.add("import shz.core.lock.Lock;");
            imports.add("import shz.core.lock.LockKey;");
        } else {
            content.add("    @Lock(\"" + desc + "\")");
            content.add("    public int add(@LockKey(\"code\") " + className + " entity) {");
            content.add("        check(entity);");
            content.add("        ClientFailureMsg.requireNon(checkUniqueForInsert(entity, \"code\"), \"" + desc + "已经存在\");");
            content.add("        return insert(entity);");
            content.add("    }\n");

            content.add("    @Override");
            content.add("    protected void check(" + className + " entity) {");
            content.add("    }\n");

            content.add("    @Lock(\"" + desc + "\")");
            content.add("    public int update(@LockKey(\"code\") " + className + " entity) {");
            content.add("        checkId(entity.getId());");
            content.add("        check(entity);");
            content.add("        " + className + " oldEntity = selectById(entity.getId());");
            content.add("        ClientFailureMsg.requireNonNull(oldEntity, \"" + desc + "不存在\");");
            content.add("        ClientFailureMsg.requireNon(checkUniqueForUpdate(entity, \"code\"), \"" + desc + "已经存在\");");
            content.add("        return updateById(entity);");
            content.add("    }\n");

            content.add("    @Override");
            content.add("    public int delete(Collection<?> ids) {");
            content.add("        checkId(ids);");
            content.add("        int row = super.delete(ids);");
            content.add("        ServerFailureMsg.requireNon(row != ids.size(), \"删除" + desc + "失败\");");
            content.add("        return row;");
            content.add("    }\n");

            content.add("    @Override");
            content.add("    public PageInfo<Query" + className + "Vo.Vo> page(PageVo<Query" + className + "Vo, Query" + className + "Vo.Vo> pageVo) {");
            content.add("        Query" + className + "Vo reqVo = pageVo.getData();");
            content.add("        PageInfo<" + className + "> page = selectPage(pageVo.simple(), reqVo);");
            content.add("        return page.map(entity -> FieldSetter.copy(entity, new Query" + className + "Vo.Vo()));");
            content.add("    }\n");

            content.add("    @Override");
            content.add("    public " + className + "DetailVo detail(Long id) {");
            content.add("        " + className + " entity = selectById(id);");
            content.add("        ClientFailureMsg.requireNonNull(entity, \"" + desc + "不存在\");");
            content.add("        return FieldSetter.copy(entity, new " + className + "DetailVo());");
            content.add("    }");

            imports.add("import " + tgp.queryVoGenInfo.packageName + ".Query" + className + "Vo;");
            imports.add("import " + tgp.detailVoGenInfo.packageName + "." + className + "DetailVo;");
            imports.add("import shz.core.model.PageInfo;");
            imports.add("import shz.spring.model.PageVo;");
            imports.add("import shz.core.msg.ClientFailureMsg;");
            imports.add("import shz.core.msg.ServerFailureMsg;");
            imports.add("import shz.core.FieldSetter;");
            imports.add("import java.util.Collection;");
            imports.add("import shz.core.lock.Lock;");
            imports.add("import shz.core.lock.LockKey;");
        }

        return content;
    }
}
