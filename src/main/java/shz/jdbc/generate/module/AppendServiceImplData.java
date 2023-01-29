package shz.jdbc.generate.module;

import shz.core.NullHelp;
import shz.jdbc.generate.AppendData;
import shz.jdbc.generate.Tgp;
import shz.jdbc.model.Table;
import shz.orm.entity.RecordEntity;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AppendServiceImplData extends AppendData {
    @Override
    protected String comment(Table table) {
        String date = date();
        return "/**\n" +
                " * @author " + user(table) + "\n" +
                (NullHelp.isBlank(date) ? "" : " * @date " + date + "\n") +
                " * @description " + desc(table) + "服务实现类\n" +
                " */";
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
        Class<?> superEntity = superEntity(tgp.table);

        if (superEntity != null && RecordEntity.class.isAssignableFrom(superEntity)) {
            content.add("    @Autowired");
            content.add("    JdbcService jdbcService;\n");

            content.add("    @Override");
            content.add("    public PageInfo<Query" + className + "Vo.Vo> page(PageVo<Query" + className + "Vo, Query" + className + "Vo.Vo> pageVo) {");
            content.add("        Query" + className + "Vo reqVo = pageVo.getData();");
            content.add("        ClassInfo classInfo = ClassInfo.getNonNull(" + className + ".class);");
            content.add("        ActionRunner<" + className + "> runner = runner(selectMap(new " + className + "()), null, null, null, 0, null, null, jdbcService.whereSql(classInfo, reqVo, null, false));");
            content.add("        PageInfo<" + className + "> page = page(pageVo.map(), runner, classInfo, null, Comparator.comparing(" + className + "::getCreateTime));");
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
            imports.add("import " + tgp.entityGenInfo.packageName + "." + className + ";");
            imports.add("import shz.jdbc.JdbcService;");
            imports.add("import shz.core.model.PageInfo;");
            imports.add("import shz.spring.model.PageVo;");
            imports.add("import shz.orm.ClassInfo;");
            imports.add("import shz.core.function.ActionRunner;");
            imports.add("import shz.core.FieldSetter;");
            imports.add("import java.util.Comparator;");
            imports.add("import java.util.List;");
        }

        return content;
    }
}
