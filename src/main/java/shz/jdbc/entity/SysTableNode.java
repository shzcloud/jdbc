package shz.jdbc.entity;

import shz.orm.annotation.Id;
import shz.orm.annotation.Table;

@Table("sys_table_node")
public class SysTableNode {
    @Id
    private Long id;
    private String tableName;
    private String node;
    private String dsName;

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

    public String getDsName() {
        return dsName;
    }

    public void setDsName(String dsName) {
        this.dsName = dsName;
    }

    @Override
    public String toString() {
        return "SysTableNode{" +
                "id=" + id +
                ", tableName='" + tableName + '\'' +
                ", node='" + node + '\'' +
                ", dsName='" + dsName + '\'' +
                '}';
    }
}
