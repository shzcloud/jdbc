package shz.jdbc.generate.module;

import shz.core.NullHelp;
import shz.jdbc.generate.AppendData;
import shz.jdbc.generate.Tgp;
import shz.jdbc.model.Table;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AppendApiClientData extends AppendData {
    @Override
    protected String comment(Table table) {
        String date = date();
        return "/**\n" +
                " * @author " + user(table) + "\n" +
                (NullHelp.isBlank(date) ? "" : " * @date " + date + "\n") +
                " * @description " + desc(table) + "API客户端接口\n" +
                " */";
    }

    @Override
    protected List<String> annotations(Tgp tgp, Set<String> imports) {
        List<String> annotations = new LinkedList<>();

        String className = className(tgp.table);

        String value = apiClientIdPrefix(tgp.table) + module(tgp.table);
        String path = requestMapping(tgp.table);

        annotations.add("@FeignClient(value = \"" + value + "\", path = \"" + path + "\", fallbackFactory = " + className + "ClientFallbackFactory.class)");

        imports.add("import org.springframework.cloud.openfeign.FeignClient;");
        imports.add("import " + tgp.apiClientFallbackGenInfo.packageName + "." + className + "ClientFallbackFactory;");

        return annotations;
    }

    @Override
    protected String cls(Tgp tgp, Set<String> imports) {
        return "public interface " + className(tgp.table) + "Client";
    }

    @Override
    protected List<String> content(Tgp tgp, Set<String> imports) {
        return super.content(tgp, imports);
    }
}
