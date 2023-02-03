package shz.jdbc.generate.module;

import shz.jdbc.generate.AppendData;
import shz.jdbc.generate.Tgp;
import shz.jdbc.model.Table;

import java.util.List;
import java.util.Set;

public class AppendApiData extends AppendData {
    @Override
    protected String comment(Table table) {
        return "/**\n" +
                " * " + desc(table) + "API接口\n" +
                " */";
    }

    @Override
    protected List<String> annotations(Tgp tgp, Set<String> imports) {
        return super.annotations(tgp, imports);
    }

    @Override
    protected String cls(Tgp tgp, Set<String> imports) {
        return "public interface " + className(tgp.table) + "Api";
    }

    @Override
    protected List<String> content(Tgp tgp, Set<String> imports) {
        return super.content(tgp, imports);
    }
}
