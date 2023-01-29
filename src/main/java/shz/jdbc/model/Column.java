package shz.jdbc.model;

import java.io.Serializable;

public final class Column implements Serializable {
    private static final long serialVersionUID = 1677450272533146324L;
    /**
     * COLUMN_NAME 列名称
     */
    private String columnName;
    /**
     * DATA_TYPE {@link java.sql.Types}
     */
    private int dataType;
    /**
     * TYPE_NAME 数据源依赖的类型名称，对于 UDT，该类型名称是完全限定的
     */
    private String typeName;
    /**
     * COLUMN_SIZE 列的大小
     * 列表示给定列的指定列大小
     * 对于数值数据，这是最大精度
     * 对于字符数据，这是字符长度
     * 对于日期时间数据类型，这是 String 表示形式的字符长度（假定允许的最大小数秒组件的精度）
     * 对于二进制数据，这是字节长度
     * 对于 ROWID 数据类型，这是字节长度
     * 对于列大小不适用的数据类型，则返回 Null
     */
    private int columnSize;
    /**
     * DECIMAL_DIGITS 小数部分的位数。对于 DECIMAL_DIGITS 不适用的数据类型，则返回 Null
     */
    private int decimalDigits;
    /**
     * NUM_PREC_RADIX 基数（通常为 10 或 2）
     */
    private int numPrecRadix;
    /**
     * NULLABLE 是否允许使用 NULL
     */
    private int nullable;
    /**
     * REMARKS 描述列的注释（可为 null）
     */
    private String remarks;
    /**
     * COLUMN_DEF 该列的默认值，当值在单引号内时应被解释为一个字符串（可为 null）
     */
    private String columnDef;
    /**
     * CHAR_OCTET_LENGTH 对于 char 类型，该长度是列中的最大字节数
     */
    private int charOctetLength;
    /**
     * ORDINAL_POSITION 表中的列的索引（从 1 开始）
     */
    private int ordinalPosition;
    /**
     * IS_NULLABLE ISO 规则用于确定列是否包括 null (YES,NO,空字符串)
     */
    private String isNullable;
    /**
     * SOURCE_DATA_TYPE 不同类型或用户生成 Ref 类型{@link java.sql.Types}（如果 DATA_TYPE 不是 DISTINCT 或用户生成的 REF，则为 null）
     */
    private short sourceDataType;
    /**
     * IS_AUTOINCREMENT 指示此列是否自动增加 (YES,NO,空字符串)
     */
    private String isAutoIncrement;
    /**
     * SCOPE_CATLOG 表的类别，它是引用属性的作用域（如果 DATA_TYPE 不是 REF，则为 null）
     */
    private String scopeCatlog;
    /**
     * SCOPE_SCHEMA 表的模式，它是引用属性的作用域（如果 DATA_TYPE 不是 REF，则为 null）
     */
    private String scopeSchem;
    /**
     * SCOPE_TABLE 表名称，它是引用属性的作用域（如果 DATA_TYPE 不是 REF，则为 null）
     */
    private String scopeTable;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }

    public int getDecimalDigits() {
        return decimalDigits;
    }

    public void setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    public int getNumPrecRadix() {
        return numPrecRadix;
    }

    public void setNumPrecRadix(int numPrecRadix) {
        this.numPrecRadix = numPrecRadix;
    }

    public int getNullable() {
        return nullable;
    }

    public void setNullable(int nullable) {
        this.nullable = nullable;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getColumnDef() {
        return columnDef;
    }

    public void setColumnDef(String columnDef) {
        this.columnDef = columnDef;
    }

    public int getCharOctetLength() {
        return charOctetLength;
    }

    public void setCharOctetLength(int charOctetLength) {
        this.charOctetLength = charOctetLength;
    }

    public int getOrdinalPosition() {
        return ordinalPosition;
    }

    public void setOrdinalPosition(int ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }

    public String getIsNullable() {
        return isNullable;
    }

    public void setIsNullable(String isNullable) {
        this.isNullable = isNullable;
    }

    public short getSourceDataType() {
        return sourceDataType;
    }

    public void setSourceDataType(short sourceDataType) {
        this.sourceDataType = sourceDataType;
    }

    public String getIsAutoIncrement() {
        return isAutoIncrement;
    }

    public void setIsAutoIncrement(String isAutoIncrement) {
        this.isAutoIncrement = isAutoIncrement;
    }

    public String getScopeCatlog() {
        return scopeCatlog;
    }

    public void setScopeCatlog(String scopeCatlog) {
        this.scopeCatlog = scopeCatlog;
    }

    public String getScopeSchem() {
        return scopeSchem;
    }

    public void setScopeSchem(String scopeSchem) {
        this.scopeSchem = scopeSchem;
    }

    public String getScopeTable() {
        return scopeTable;
    }

    public void setScopeTable(String scopeTable) {
        this.scopeTable = scopeTable;
    }

    @Override
    public String toString() {
        return "Column{" +
                "columnName='" + columnName + '\'' +
                ", dataType=" + dataType +
                ", typeName='" + typeName + '\'' +
                ", columnSize=" + columnSize +
                ", decimalDigits=" + decimalDigits +
                ", numPrecRadix=" + numPrecRadix +
                ", nullable=" + nullable +
                ", remarks='" + remarks + '\'' +
                ", columnDef='" + columnDef + '\'' +
                ", charOctetLength=" + charOctetLength +
                ", ordinalPosition=" + ordinalPosition +
                ", isNullable='" + isNullable + '\'' +
                ", sourceDataType=" + sourceDataType +
                ", isAutoIncrement='" + isAutoIncrement + '\'' +
                ", scopeCatlog='" + scopeCatlog + '\'' +
                ", scopeSchem='" + scopeSchem + '\'' +
                ", scopeTable='" + scopeTable + '\'' +
                '}';
    }
}
