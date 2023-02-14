package shz.jdbc.entity;

import shz.orm.annotation.Table;

@Table("sys_table_node_transfer_info")
public class SysTableNodeTransferInfo {
    private Long id;
    private String tableName;
    private String node;
    private Boolean dilatation;
    private Boolean finished;
    private String transferInfo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public Boolean getDilatation() {
        return dilatation;
    }

    public void setDilatation(Boolean dilatation) {
        this.dilatation = dilatation;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public String getTransferInfo() {
        return transferInfo;
    }

    public void setTransferInfo(String transferInfo) {
        this.transferInfo = transferInfo;
    }

    @Override
    public String toString() {
        return "SysTableNodeTransferInfo{" +
                "id=" + id +
                ", tableName='" + tableName + '\'' +
                ", node='" + node + '\'' +
                ", dilatation=" + dilatation +
                ", finished=" + finished +
                ", transferInfo='" + transferInfo + '\'' +
                '}';
    }
}
