package shz.jdbc.generate.module;

import shz.jdbc.generate.AppendData;
import shz.jdbc.generate.Tgp;
import shz.jdbc.model.Table;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AppendApiClientFallbackData extends AppendData {
    @Override
    protected String comment(Table table) {
        return null;
    }

    @Override
    protected List<String> annotations(Tgp tgp, Set<String> imports) {
        List<String> annotations = new LinkedList<>();

        annotations.add("@Slf4j");
        annotations.add("@Component");

        imports.add("import lombok.extern.slf4j.Slf4j;");
        imports.add("import org.springframework.stereotype.Component;");

        return annotations;
    }

    @Override
    protected String cls(Tgp tgp, Set<String> imports) {
        String className = className(tgp.table);

        imports.add("import feign.hystrix.FallbackFactory;");
        imports.add("import " + tgp.apiClientGenInfo.packageName + "." + className + "Client;");

        return "public class " + className + "ClientFallbackFactory implements FallbackFactory<" + className + "Client>";
    }

    @Override
    protected List<String> content(Tgp tgp, Set<String> imports) {
        List<String> content = new LinkedList<>();

        String className = className(tgp.table);

        content.add("    @Override");
        content.add("    public " + className + "Client create(Throwable cause) {");
        content.add("        return InterfaceProxy.getProxy(" + className + "Client.class, p -> {");
        content.add("            log.error(cause.getMessage(), cause);");
        content.add("            return Response.fail(ServerFailure.GATEWAY_TIMEOUT);");
        content.add("        });");
        content.add("    }");


        imports.add("import shz.core.model.Response;");
        imports.add("import shz.core.InterfaceProxy;");
        imports.add("import shz.core.msg.ServerFailure;");

        return content;
    }
}
