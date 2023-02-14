package shz.jdbc;

import shz.core.model.PageInfo;
import shz.orm.annotation.Where;
import shz.orm.enums.Condition;

import java.util.Collection;
import java.util.List;

public interface IService<T> {
    int add(T entity);

    int update(T entity);

    int delete(Collection<?> ids);

    /**
     * @param fieldNames 指定需要插入的列对应字段，其余列忽略不插入并且指定的列不管是否为空都会强制插入
     */
    int insert(Object entity, List<String> fieldNames);

    /**
     * 没有指定插入列默认会选择非空的列插入{@link Where 中strategy}
     */
    int insert(Object entity);

    /**
     * @param batchSize 批次大小
     * @param commit    每批次完是否提交
     */
    int[] batchInsert(List<?> entities, List<String> fieldNames, int batchSize, boolean commit);

    int[] batchInsert(List<?> entities, List<String> fieldNames);

    int[] batchInsert(List<?> entities);

    /**
     * @param fieldNames 指定需要更新的列对应字段，其余列忽略不更新并且指定的列不管是否为空都会强制更新
     */
    int updateById(Object entity, List<String> fieldNames);

    /**
     * 没有指定更新列默认会选择非空的列更新
     */
    int updateById(Object entity);

    int[] batchUpdateById(List<?> entities, List<String> fieldNames, int batchSize, boolean commit);

    int[] batchUpdateById(List<?> entities, List<String> fieldNames);

    int[] batchUpdateById(List<?> entities);

    int updateByColumn(Object entity, List<String> fieldNames, String fieldName, Object fieldValue, Condition condition);

    int updateByColumn(Object entity, String fieldName, Object fieldValue, Condition condition);

    int updateByColumn(Object entity, String fieldName, Object fieldValue);

    /**
     * @param uniqueFields 唯一列，插入时会校验是否存在该列的值
     */
    int insertOrUpdate(Object entity, List<String> fieldNames, String... uniqueFields);

    int insertOrUpdate(Object entity, String... uniqueFields);

    int[] batchInsertOrUpdate(List<?> entities, List<String> fieldNames, int batchSize, boolean commit, String... uniqueFields);

    int[] batchInsertOrUpdate(List<?> entities, List<String> fieldNames, String... uniqueFields);

    int[] batchInsertOrUpdate(List<?> entities, String... uniqueFields);

    int deleteById(Object id);

    int[] batchDeleteById(List<?> ids, int batchSize, boolean commit);

    int[] batchDeleteById(List<?> ids);

    int deleteByColumn(String fieldName, Object fieldValue, Condition condition);

    int deleteByColumn(String fieldName, Object fieldValue);

    List<T> selectList(Object obj, List<String> fieldNames);

    List<T> selectList(Object obj);

    List<T> selectListByColumn(List<String> fieldNames, String fieldName, Object fieldValue, Condition condition);

    List<T> selectListByColumn(String fieldName, Object fieldValue, Condition condition);

    List<T> selectListByColumn(String fieldName, Object fieldValue);

    PageInfo<T> selectPage(PageInfo<T> pageInfo, Object obj, List<String> fieldNames);

    PageInfo<T> selectPage(PageInfo<T> pageInfo, Object obj);

    T selectById(Object id, List<String> fieldNames);

    T selectById(Object id);

    List<T> selectByIds(Collection<?> ids, List<String> fieldNames);

    List<T> selectByIds(Collection<?> ids);

    T selectOne(Object obj, List<String> fieldNames);

    T selectOne(Object obj);

    T selectOneByColumn(List<String> fieldNames, String fieldName, Object fieldValue, Condition condition);

    T selectOneByColumn(String fieldName, Object fieldValue, Condition condition);

    T selectOneByColumn(String fieldName, Object fieldValue);
}
