package shz.jdbc.generate;

import shz.jdbc.model.Table;

public final class Tgp {
    public final Table table;
    public final GenInfo entityGenInfo;

    public final GenInfo addVoGenInfo;
    public final GenInfo updateVoGenInfo;
    public final GenInfo queryVoGenInfo;
    public final GenInfo detailVoGenInfo;

    public final GenInfo apiGenInfo;

    public final GenInfo serviceGenInfo;
    public final GenInfo serviceImplGenInfo;
    public final GenInfo repositoryGenInfo;
    public final GenInfo controllerGenInfo;
    public final GenInfo enumGenInfo;

    public final GenInfo apiClientGenInfo;
    public final GenInfo apiClientFallbackGenInfo;

    Tgp(
            Table table,
            GenInfo entityGenInfo,

            GenInfo addVoGenInfo,
            GenInfo updateVoGenInfo,
            GenInfo queryVoGenInfo,
            GenInfo detailVoGenInfo,

            GenInfo apiGenInfo,

            GenInfo serviceGenInfo,
            GenInfo serviceImplGenInfo,
            GenInfo repositoryGenInfo,
            GenInfo controllerGenInfo,
            GenInfo enumGenInfo,

            GenInfo apiClientGenInfo,
            GenInfo apiClientFallbackGenInfo
    ) {
        this.table = table;
        this.entityGenInfo = entityGenInfo;

        this.addVoGenInfo = addVoGenInfo;
        this.updateVoGenInfo = updateVoGenInfo;
        this.queryVoGenInfo = queryVoGenInfo;
        this.detailVoGenInfo = detailVoGenInfo;

        this.apiGenInfo = apiGenInfo;

        this.serviceGenInfo = serviceGenInfo;
        this.serviceImplGenInfo = serviceImplGenInfo;
        this.repositoryGenInfo = repositoryGenInfo;
        this.controllerGenInfo = controllerGenInfo;
        this.enumGenInfo = enumGenInfo;

        this.apiClientGenInfo = apiClientGenInfo;
        this.apiClientFallbackGenInfo = apiClientFallbackGenInfo;
    }
}
