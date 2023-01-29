package shz.jdbc.model;

import java.io.Serializable;

public final class ImportedKey implements Serializable {
    private static final long serialVersionUID = 1191236651789431592L;
    /**
     * PKTABLE_CAT 被导入的主键表类别（可为 null）
     */
    private String pkTableCat;
    /**
     * PKTABLE_SCHEM 被导入的主键表模式（可为 null）
     */
    private String pkTableSchem;
    /**
     * PKTABLE_NAME 被导入的主键表名称
     */
    private String pkTableName;
    /**
     * PKCOLUMN_NAME 被导入的主键列名称
     */
    private String pkColumnName;
    /**
     * PK_NAME 主键的名称（可为 null）
     */
    private String pkName;
    /**
     * FKTABLE_CAT 外键表类别（可为 null）
     */
    private String fkTableCat;
    /**
     * FKTABLE_SCHEM 外键表模式（可为 null）
     */
    private String fkTableSchem;
    /**
     * FKTABLE_NAME 外键表名称
     */
    private String fkTableName;
    /**
     * FKCOLUMN_NAME 外键列名称
     */
    private String fkColumnName;
    /**
     * FK_NAME 外键的名称（可为 null）
     */
    private String fkName;
    /**
     * KEY_SEQ 外键中的序列号（值 1 表示外键中的第一列，值 2 表示外键中的第二列）
     */
    private short keySeq;
    /**
     * UPDATE_RULE 更新主键时外键发生的变化
     */
    private short updateRule;
    /**
     * DELETE_RULE 删除主键时外键发生的变化
     */
    private short deleteRule;
    /**
     * DEFERRABILITY 是否可以将对外键约束的评估延迟到提交时间
     */
    private short deferrability;

    public String getPkTableCat() {
        return pkTableCat;
    }

    public void setPkTableCat(String pkTableCat) {
        this.pkTableCat = pkTableCat;
    }

    public String getPkTableSchem() {
        return pkTableSchem;
    }

    public void setPkTableSchem(String pkTableSchem) {
        this.pkTableSchem = pkTableSchem;
    }

    public String getPkTableName() {
        return pkTableName;
    }

    public void setPkTableName(String pkTableName) {
        this.pkTableName = pkTableName;
    }

    public String getPkColumnName() {
        return pkColumnName;
    }

    public void setPkColumnName(String pkColumnName) {
        this.pkColumnName = pkColumnName;
    }

    public String getPkName() {
        return pkName;
    }

    public void setPkName(String pkName) {
        this.pkName = pkName;
    }

    public String getFkTableCat() {
        return fkTableCat;
    }

    public void setFkTableCat(String fkTableCat) {
        this.fkTableCat = fkTableCat;
    }

    public String getFkTableSchem() {
        return fkTableSchem;
    }

    public void setFkTableSchem(String fkTableSchem) {
        this.fkTableSchem = fkTableSchem;
    }

    public String getFkTableName() {
        return fkTableName;
    }

    public void setFkTableName(String fkTableName) {
        this.fkTableName = fkTableName;
    }

    public String getFkColumnName() {
        return fkColumnName;
    }

    public void setFkColumnName(String fkColumnName) {
        this.fkColumnName = fkColumnName;
    }

    public String getFkName() {
        return fkName;
    }

    public void setFkName(String fkName) {
        this.fkName = fkName;
    }

    public short getKeySeq() {
        return keySeq;
    }

    public void setKeySeq(short keySeq) {
        this.keySeq = keySeq;
    }

    public short getUpdateRule() {
        return updateRule;
    }

    public void setUpdateRule(short updateRule) {
        this.updateRule = updateRule;
    }

    public short getDeleteRule() {
        return deleteRule;
    }

    public void setDeleteRule(short deleteRule) {
        this.deleteRule = deleteRule;
    }

    public short getDeferrability() {
        return deferrability;
    }

    public void setDeferrability(short deferrability) {
        this.deferrability = deferrability;
    }

    @Override
    public String toString() {
        return "ImportedKey{" +
                "pkTableCat='" + pkTableCat + '\'' +
                ", pkTableSchem='" + pkTableSchem + '\'' +
                ", pkTableName='" + pkTableName + '\'' +
                ", pkColumnName='" + pkColumnName + '\'' +
                ", pkName='" + pkName + '\'' +
                ", fkTableCat='" + fkTableCat + '\'' +
                ", fkTableSchem='" + fkTableSchem + '\'' +
                ", fkTableName='" + fkTableName + '\'' +
                ", fkColumnName='" + fkColumnName + '\'' +
                ", fkName='" + fkName + '\'' +
                ", keySeq=" + keySeq +
                ", updateRule=" + updateRule +
                ", deleteRule=" + deleteRule +
                ", deferrability=" + deferrability +
                '}';
    }
}
