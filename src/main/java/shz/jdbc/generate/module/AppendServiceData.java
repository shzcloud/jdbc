package shz.jdbc.generate.module;

import shz.jdbc.generate.AppendData;
import shz.jdbc.generate.Tgp;
import shz.jdbc.model.Table;
import shz.orm.entity.RecordEntity;
import shz.orm.entity.TreeEntity;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AppendServiceData extends AppendData {
    @Override
    protected String comment(Table table) {
        return "/**\n" +
                " * " + desc(table) + "服务接口\n" +
                " */";
    }

    @Override
    protected List<String> annotations(Tgp tgp, Set<String> imports) {
        return super.annotations(tgp, imports);
    }

    @Override
    protected String cls(Tgp tgp, Set<String> imports) {
        String className = className(tgp.table);
        Class<?> superEntity = superEntity(tgp.table);

        if (superEntity != null && RecordEntity.class.isAssignableFrom(superEntity))
            return "public interface " + className + "Service";

        imports.add("import shz.jdbc.IService;");
        imports.add("import " + tgp.entityGenInfo.packageName + "." + className + ";");

        return "public interface " + className + "Service extends IService<" + className + ">";
    }

    @Override
    protected List<String> content(Tgp tgp, Set<String> imports) {
        List<String> content = new LinkedList<>();
        String className = className(tgp.table);
        Class<?> superEntity = superEntity(tgp.table);

        if (superEntity != null && RecordEntity.class.isAssignableFrom(superEntity)) {
            content.add("    PageInfo<Query" + className + "Vo.Vo> page(PageVo<Query" + className + "Vo, Query" + className + "Vo.Vo> pageVo);\n");
            content.add("    void record(" + className + " record);\n");
            content.add("    void batchRecord(List<" + className + "> records);");

            imports.add("import " + tgp.queryVoGenInfo.packageName + ".Query" + className + "Vo;");
            imports.add("import " + tgp.entityGenInfo.packageName + "." + className + ";");
            imports.add("import shz.core.model.PageInfo;");
            imports.add("import shz.spring.model.PageVo;");
            imports.add("import java.util.List;");
        } else if (superEntity != null && TreeEntity.class.isAssignableFrom(superEntity)) {
            content.add("    List<" + className + "> list();\n");
            content.add("    " + className + "DetailVo detail(Long id);");

            imports.add("import " + tgp.detailVoGenInfo.packageName + "." + className + "DetailVo;");
            imports.add("import java.util.List;");
        } else {
            content.add("    PageInfo<Query" + className + "Vo.Vo> page(PageVo<Query" + className + "Vo, Query" + className + "Vo.Vo> pageVo);\n");
            content.add("    " + className + "DetailVo detail(Long id);");

            imports.add("import " + tgp.queryVoGenInfo.packageName + ".Query" + className + "Vo;");
            imports.add("import " + tgp.detailVoGenInfo.packageName + "." + className + "DetailVo;");
            imports.add("import shz.core.model.PageInfo;");
            imports.add("import shz.spring.model.PageVo;");
        }

        return content;
    }
}
