package shz.jdbc.generate.module;

import shz.jdbc.generate.AppendData;
import shz.jdbc.generate.Tgp;
import shz.jdbc.model.Table;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AppendRepositoryData extends AppendData {
    @Override
    protected String comment(Table table) {
        return null;
    }

    @Override
    protected List<String> annotations(Tgp tgp, Set<String> imports) {
        List<String> annotations = new LinkedList<>();

        annotations.add("@Repository");

        imports.add("import shz.orm.annotation.Repository;");

        return annotations;
    }

    @Override
    protected String cls(Tgp tgp, Set<String> imports) {
        return "public interface " + className(tgp.table) + "Repository";
    }

    @Override
    protected List<String> content(Tgp tgp, Set<String> imports) {
        return super.content(tgp, imports);
    }
}
