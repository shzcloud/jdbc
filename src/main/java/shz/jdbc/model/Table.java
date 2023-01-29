package shz.jdbc.model;

import java.io.Serializable;
import java.util.List;

public final class Table implements Serializable {
    private static final long serialVersionUID = -969186504157153292L;
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
     * TABLE_TYPE 表类型。典型的类型是 "TABLE"、"VIEW"、"SYSTEM TABLE"、"GLOBAL TEMPORARY"、"LOCAL TEMPORARY"、"ALIAS" 和 "SYNONYM"
     */
    private String tableType;
    /**
     * REMARKS 表的解释性注释
     */
    private String remarks;
    /**
     * TYPE_CAT 类型的类别（可为 null）
     */
    private String typeCat;
    /**
     * TYPE_SCHEM 类型模式（可为 null）
     */
    private String typeSchem;
    /**
     * TYPE_NAME 类型名称（可为 null）
     */
    private String typeName;
    /**
     * SELF_REFERENCING_COL_NAME 有类型表的指定 "identifier" 列的名称（可为 null）
     */
    private String selfReferencingColName;
    /**
     * REF_GENERATION 指定在 SELF_REFERENCING_COL_NAME 中创建值的方式。这些值为 "SYSTEM"、"USER" 和 "DERIVED"。（可能为 null）
     */
    private String refGeneration;
    /**
     * 主键
     */
    private List<PrimaryKey> primaryKeys;
    /**
     * 外键
     */
    private List<ImportedKey> importedKeys;
    /**
     * 列
     */
    private List<Column> columns;

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

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getTypeCat() {
        return typeCat;
    }

    public void setTypeCat(String typeCat) {
        this.typeCat = typeCat;
    }

    public String getTypeSchem() {
        return typeSchem;
    }

    public void setTypeSchem(String typeSchem) {
        this.typeSchem = typeSchem;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getSelfReferencingColName() {
        return selfReferencingColName;
    }

    public void setSelfReferencingColName(String selfReferencingColName) {
        this.selfReferencingColName = selfReferencingColName;
    }

    public String getRefGeneration() {
        return refGeneration;
    }

    public void setRefGeneration(String refGeneration) {
        this.refGeneration = refGeneration;
    }

    public List<PrimaryKey> getPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(List<PrimaryKey> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    public List<ImportedKey> getImportedKeys() {
        return importedKeys;
    }

    public void setImportedKeys(List<ImportedKey> importedKeys) {
        this.importedKeys = importedKeys;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return "Table{" +
                "tableCat='" + tableCat + '\'' +
                ", tableSchem='" + tableSchem + '\'' +
                ", tableName='" + tableName + '\'' +
                ", tableType='" + tableType + '\'' +
                ", remarks='" + remarks + '\'' +
                ", typeCat='" + typeCat + '\'' +
                ", typeSchem='" + typeSchem + '\'' +
                ", typeName='" + typeName + '\'' +
                ", selfReferencingColName='" + selfReferencingColName + '\'' +
                ", refGeneration='" + refGeneration + '\'' +
                ", primaryKeys=" + primaryKeys +
                ", importedKeys=" + importedKeys +
                ", columns=" + columns +
                '}';
    }
}
