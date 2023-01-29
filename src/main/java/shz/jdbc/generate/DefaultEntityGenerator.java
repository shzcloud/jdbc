package shz.jdbc.generate;

import shz.jdbc.generate.module.*;
import shz.jdbc.model.Table;

import java.util.Properties;

public abstract class DefaultEntityGenerator extends DefaultGenerator{
    protected DefaultEntityGenerator(Properties properties) {
        super(properties);
    }

    @Override
    protected final GenInfo addVoGenInfo(Table table) {
        return null;
    }

    @Override
    protected final GenInfo updateVoGenInfo(Table table) {
        return null;
    }

    @Override
    protected final GenInfo queryVoGenInfo(Table table) {
        return null;
    }

    @Override
    protected final GenInfo detailVoGenInfo(Table table) {
        return null;
    }

    @Override
    protected final GenInfo apiGenInfo(Table table) {
        return null;
    }

    @Override
    protected final GenInfo serviceGenInfo(Table table) {
        return null;
    }

    @Override
    protected final GenInfo serviceImplGenInfo(Table table) {
        return null;
    }

    @Override
    protected final GenInfo repositoryGenInfo(Table table) {
        return null;
    }

    @Override
    protected final GenInfo controllerGenInfo(Table table) {
        return null;
    }

    @Override
    protected final GenInfo apiClientGenInfo(Table table) {
        return null;
    }

    @Override
    protected final GenInfo apiClientFallbackGenInfo(Table table) {
        return null;
    }

    @Override
    protected final Class<? extends AppendAddVoData> appendAddVoCls() {
        return super.appendAddVoCls();
    }

    @Override
    protected final Class<? extends AppendUpdateVoData> appendUpdateVoCls() {
        return super.appendUpdateVoCls();
    }

    @Override
    protected final Class<? extends AppendQueryVoData> appendQueryVoCls() {
        return super.appendQueryVoCls();
    }

    @Override
    protected final Class<? extends AppendDetailVoData> appendDetailVoCls() {
        return super.appendDetailVoCls();
    }

    @Override
    protected final Class<? extends AppendApiData> appendApiCls() {
        return super.appendApiCls();
    }

    @Override
    protected final Class<? extends AppendServiceData> appendServiceCls() {
        return super.appendServiceCls();
    }

    @Override
    protected final Class<? extends AppendServiceImplData> appendServiceImplCls() {
        return super.appendServiceImplCls();
    }

    @Override
    protected final Class<? extends AppendRepositoryData> appendRepositoryCls() {
        return super.appendRepositoryCls();
    }

    @Override
    protected final Class<? extends AppendControllerData> appendControllerCls() {
        return super.appendControllerCls();
    }

    @Override
    protected final Class<? extends AppendApiClientData> appendApiClientCls() {
        return super.appendApiClientCls();
    }

    @Override
    protected final Class<? extends AppendApiClientFallbackData> appendApiClientFallbackCls() {
        return super.appendApiClientFallbackCls();
    }

    @Override
    protected final long flags(Table table) {
        return FILE_ENTITY;
    }

    @Override
    protected final String primaryKeyType(Table table) {
        return super.primaryKeyType(table);
    }

    @Override
    protected final String module(Table table) {
        return super.module(table);
    }

    @Override
    protected final String urlSuffix(Table table) {
        return super.urlSuffix(table);
    }

    @Override
    protected final String version() {
        return super.version();
    }

    @Override
    protected final String requestMapping(Table table) {
        return super.requestMapping(table);
    }

    @Override
    protected final String apiTags(Table table) {
        return super.apiTags(table);
    }

    @Override
    protected final String apiClientIdPrefix(Table table) {
        return super.apiClientIdPrefix(table);
    }
}
