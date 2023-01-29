package shz.jdbc.model;

import java.io.Serializable;

public final class PrimaryKey implements Serializable {
    private static final long serialVersionUID = -1418658668605440647L;
    /**
     * TABLE_CAT 表类别（可为 null）
     */
    private String tableCat;
    /**
     * TABLE_SCHEM 表模式（可为 null）
     */
    private String tableSchem;
    /**
     * TABLE_NAME 表名称
     */
    private String tableName;
    /**
     * COLUMN_NAME 列名称
     */
    private String columnName;
    /**
     * KEY_SEQ 主键中的序列号（值 1 表示主键中的第一列，值 2 表示主键中的第二列）
     */
    private short keySeq;
    /**
     * PK_NAME 主键的名称（可为 null）
     */
    private String pkName;

    public String getTableCat() {
        return tableCat;
    }

    public void setTableCat(String tableCat) {
        this.tableCat = tableCat;
    }

    public String getTableSchem() {
        return tableSchem;
    }

    public void setTableSchem(String tableSchem) {
        this.tableSchem = tableSchem;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public short getKeySeq() {
        return keySeq;
    }

    public void setKeySeq(short keySeq) {
        this.keySeq = keySeq;
    }

    public String getPkName() {
        return pkName;
    }

    public void setPkName(String pkName) {
        this.pkName = pkName;
    }

    @Override
    public String toString() {
        return "PrimaryKey{" +
                "tableCat='" + tableCat + '\'' +
                ", tableSchem='" + tableSchem + '\'' +
                ", tableName='" + tableName + '\'' +
                ", columnName='" + columnName + '\'' +
                ", keySeq=" + keySeq +
                ", pkName='" + pkName + '\'' +
                '}';
    }
}
