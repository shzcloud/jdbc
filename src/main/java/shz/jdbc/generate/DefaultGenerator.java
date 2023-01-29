package shz.jdbc.generate;

import shz.core.AccessibleHelp;
import shz.core.NullHelp;
import shz.core.PRException;
import shz.core.ToList;
import shz.core.cl.ClassLoaderHelp;
import shz.jdbc.generate.module.*;
import shz.jdbc.model.Table;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.Collator;
import java.util.*;

public abstract class DefaultGenerator extends Generator {
    protected final Properties properties;

    protected DefaultGenerator(Properties properties) {
        this.properties = properties;
    }

    @Override
    protected final Connection conn() {
        Properties prop = new Properties();
        prop.putAll(properties);

        prop.put("useInformationSchema", "true");
        prop.put("remarksReporting", "true");

        String url = prop.getProperty("url");
        try {
            return DriverManager.getConnection(url, prop);
        } catch (SQLException e) {
            ClassLoaderHelp.load(prop.getProperty("driverClassName"));
        }

        try {
            return DriverManager.getConnection(url, prop);
        } catch (SQLException e) {
            throw PRException.of(e);
        }
    }

    protected abstract String path();

    protected abstract String basePackage();

    @Override
    protected GenInfo entityGenInfo(Table table) {
        return GenInfo.of(basePackage() + ".entity").path(path());
    }

    @Override
    protected GenInfo addVoGenInfo(Table table) {
        return GenInfo.of(basePackage() + ".vo.base").path(path());
    }

    @Override
    protected GenInfo updateVoGenInfo(Table table) {
        return GenInfo.of(basePackage() + ".vo.base").path(path());
    }

    @Override
    protected GenInfo queryVoGenInfo(Table table) {
        return GenInfo.of(basePackage() + ".vo.base").path(path());
    }

    @Override
    protected GenInfo detailVoGenInfo(Table table) {
        return GenInfo.of(basePackage() + ".vo.base").path(path());
    }

    @Override
    protected GenInfo apiGenInfo(Table table) {
        return null;
    }

    @Override
    protected GenInfo serviceGenInfo(Table table) {
        return GenInfo.of(basePackage() + ".service").path(path());
    }

    @Override
    protected GenInfo serviceImplGenInfo(Table table) {
        return GenInfo.of(basePackage() + ".service.impl").path(path());
    }

    @Override
    protected GenInfo repositoryGenInfo(Table table) {
        return GenInfo.of(basePackage() + ".repository").path(path());
    }

    @Override
    protected GenInfo controllerGenInfo(Table table) {
        return GenInfo.of(basePackage() + ".controller").path(path());
    }

    @Override
    protected GenInfo enumGenInfo(Table table) {
        return GenInfo.of(basePackage() + ".enums").path(path());
    }

    @Override
    protected GenInfo apiClientGenInfo(Table table) {
        return null;
    }

    @Override
    protected GenInfo apiClientFallbackGenInfo(Table table) {
        return null;
    }

    /**
     * 拼接java文件内容
     */
    private void append(Tgp tgp, GenInfo genInfo, List<String> data, Class<? extends AppendData> cls) {
        AppendData appendData = AccessibleHelp.newInstance(cls);
        appendData.generator = this;

        data.add("package " + genInfo.packageName + ";\n");

        Set<String> imports = new HashSet<>();
        List<String> annotations = appendData.annotations(tgp, imports);
        String classStr = appendData.cls(tgp, imports);
        List<String> content = appendData.content(tgp, imports);

        if (!imports.isEmpty()) {
            List<String> sort = sort(imports);
            sort.set(sort.size() - 1, sort.get(sort.size() - 1) + "\n");
            data.addAll(sort);
        }

        String comment = appendData.comment(tgp.table);
        if (NullHelp.nonBlank(comment)) data.add(comment);

        data.addAll(annotations);
        data.add(classStr + " {");
        data.addAll(content);
        data.add("}");
    }

    /**
     * 对imports行进行排序
     */
    private List<String> sort(Collection<String> collection) {
        List<String> list = ToList.explicitCollect(collection.stream(), collection.size());
        list.sort(Collator.getInstance(Locale.ENGLISH));
        return list;
    }

    @Override
    protected final void appendEntityData(Tgp tgp, List<String> data) {
        append(tgp, tgp.entityGenInfo, data, appendEntityCls());
    }

    protected Class<? extends AppendEntityData> appendEntityCls() {
        return AppendEntityData.class;
    }

    @Override
    protected final void appendAddVoData(Tgp tgp, List<String> data) {
        append(tgp, tgp.addVoGenInfo, data, appendAddVoCls());
    }

    protected Class<? extends AppendAddVoData> appendAddVoCls() {
        return AppendAddVoData.class;
    }

    @Override
    protected final void appendUpdateVoData(Tgp tgp, List<String> data) {
        append(tgp, tgp.updateVoGenInfo, data, appendUpdateVoCls());
    }

    protected Class<? extends AppendUpdateVoData> appendUpdateVoCls() {
        return AppendUpdateVoData.class;
    }

    @Override
    protected final void appendQueryVoData(Tgp tgp, List<String> data) {
        append(tgp, tgp.queryVoGenInfo, data, appendQueryVoCls());
    }

    protected Class<? extends AppendQueryVoData> appendQueryVoCls() {
        return AppendQueryVoData.class;
    }

    @Override
    protected final void appendDetailVoData(Tgp tgp, List<String> data) {
        append(tgp, tgp.detailVoGenInfo, data, appendDetailVoCls());
    }

    protected Class<? extends AppendDetailVoData> appendDetailVoCls() {
        return AppendDetailVoData.class;
    }

    @Override
    protected final void appendApiData(Tgp tgp, List<String> data) {
        append(tgp, tgp.apiGenInfo, data, appendApiCls());
    }

    protected Class<? extends AppendApiData> appendApiCls() {
        return AppendApiData.class;
    }

    @Override
    protected final void appendServiceData(Tgp tgp, List<String> data) {
        append(tgp, tgp.serviceGenInfo, data, appendServiceCls());
    }

    protected Class<? extends AppendServiceData> appendServiceCls() {
        return AppendServiceData.class;
    }

    @Override
    protected final void appendServiceImplData(Tgp tgp, List<String> data) {
        append(tgp, tgp.serviceImplGenInfo, data, appendServiceImplCls());
    }

    protected Class<? extends AppendServiceImplData> appendServiceImplCls() {
        return AppendServiceImplData.class;
    }

    @Override
    protected final void appendRepositoryData(Tgp tgp, List<String> data) {
        append(tgp, tgp.repositoryGenInfo, data, appendRepositoryCls());
    }

    protected Class<? extends AppendRepositoryData> appendRepositoryCls() {
        return AppendRepositoryData.class;
    }

    @Override
    protected final void appendControllerData(Tgp tgp, List<String> data) {
        append(tgp, tgp.controllerGenInfo, data, appendControllerCls());
    }

    protected Class<? extends AppendControllerData> appendControllerCls() {
        return AppendControllerData.class;
    }

    @Override
    protected final void appendApiClientData(Tgp tgp, List<String> data) {
        append(tgp, tgp.apiClientGenInfo, data, appendApiClientCls());
    }

    protected Class<? extends AppendApiClientData> appendApiClientCls() {
        return AppendApiClientData.class;
    }

    @Override
    protected final void appendApiClientFallbackData(Tgp tgp, List<String> data) {
        append(tgp, tgp.apiClientFallbackGenInfo, data, appendApiClientFallbackCls());
    }

    protected Class<? extends AppendApiClientFallbackData> appendApiClientFallbackCls() {
        return AppendApiClientFallbackData.class;
    }
}
