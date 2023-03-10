package shz.jdbc;

import shz.core.NullHelp;
import shz.core.PRException;
import shz.core.ToString;
import shz.core.model.PageInfo;
import shz.core.msg.ClientFailureMsg;
import shz.core.type.TypeHelp;
import shz.orm.enums.Condition;
import shz.orm.sql.WhereSql;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.*;

public abstract class SimpleService<T> implements IService<T> {
    protected final Class<T> cls = TypeHelp.getParameterizedType(getClass(), SimpleService.class, "T");
    protected final JdbcService jdbcService;

    protected SimpleService(JdbcService jdbcService) {
        this.jdbcService = jdbcService;
    }

    protected SimpleService(String dsName) {
        this(JdbcService.get(dsName));
    }

    protected SimpleService() {
        this("");
    }

    @Override
    public int add(T entity) {
        return insert(entity);
    }

    @Override
    public int update(T entity) {
        return updateById(entity);
    }

    @Override
    public int delete(Collection<?> ids) {
        return jdbcService.deleteByColumn(cls, jdbcService.nonNullClassInfo(cls).idField.getName(), ids, Condition.IN);
    }

    @Override
    public int insert(Object entity, List<String> fieldNames) {
        return jdbcService.insert(entity, fieldNames);
    }

    @Override
    public int insert(Object entity) {
        return jdbcService.insert(entity);
    }

    @Override
    public int[] batchInsert(List<?> entities, List<String> fieldNames, int batchSize, boolean commit) {
        return jdbcService.batchInsert(entities, fieldNames, batchSize, commit);
    }

    @Override
    public int[] batchInsert(List<?> entities, List<String> fieldNames) {
        return jdbcService.batchInsert(entities, fieldNames);
    }

    @Override
    public int[] batchInsert(List<?> entities) {
        return jdbcService.batchInsert(entities);
    }

    @Override
    public int updateById(Object entity, List<String> fieldNames) {
        return jdbcService.updateById(entity, fieldNames);
    }

    @Override
    public int updateById(Object entity) {
        return jdbcService.updateById(entity);
    }

    @Override
    public int[] batchUpdateById(List<?> entities, List<String> fieldNames, int batchSize, boolean commit) {
        return jdbcService.batchUpdateById(entities, fieldNames, batchSize, commit);
    }

    @Override
    public int[] batchUpdateById(List<?> entities, List<String> fieldNames) {
        return jdbcService.batchUpdateById(entities, fieldNames);
    }

    @Override
    public int[] batchUpdateById(List<?> entities) {
        return jdbcService.batchUpdateById(entities);
    }

    @Override
    public int updateByColumn(Object entity, List<String> fieldNames, String fieldName, Object fieldValue, Condition condition) {
        return jdbcService.updateByColumn(entity, fieldNames, fieldName, fieldValue, condition);
    }

    @Override
    public int updateByColumn(Object entity, String fieldName, Object fieldValue, Condition condition) {
        return jdbcService.updateByColumn(entity, fieldName, fieldValue, condition);
    }

    @Override
    public int updateByColumn(Object entity, String fieldName, Object fieldValue) {
        return jdbcService.updateByColumn(entity, fieldName, fieldValue);
    }

    @Override
    public int insertOrUpdate(Object entity, List<String> fieldNames, String... uniqueFields) {
        return jdbcService.insertOrUpdate(entity, fieldNames, uniqueFields);
    }

    @Override
    public int insertOrUpdate(Object entity, String... uniqueFields) {
        return jdbcService.insertOrUpdate(entity, uniqueFields);
    }

    @Override
    public int[] batchInsertOrUpdate(List<?> entities, List<String> fieldNames, int batchSize, boolean commit, String... uniqueFields) {
        return jdbcService.batchInsertOrUpdate(entities, fieldNames, null, batchSize, commit, uniqueFields);
    }

    @Override
    public int[] batchInsertOrUpdate(List<?> entities, List<String> fieldNames, String... uniqueFields) {
        return jdbcService.batchInsertOrUpdate(entities, fieldNames, uniqueFields);
    }

    @Override
    public int[] batchInsertOrUpdate(List<?> entities, String... uniqueFields) {
        return jdbcService.batchInsertOrUpdate(entities, uniqueFields);
    }

    @Override
    public int deleteById(Object id) {
        return jdbcService.deleteById(cls, id);
    }

    @Override
    public int[] batchDeleteById(List<?> ids, int batchSize, boolean commit) {
        return jdbcService.batchDeleteById(cls, ids, null, batchSize, commit);
    }

    @Override
    public int[] batchDeleteById(List<?> ids) {
        return jdbcService.batchDeleteById(cls, ids);
    }

    @Override
    public int deleteByColumn(String fieldName, Object fieldValue, Condition condition) {
        return jdbcService.deleteByColumn(cls, fieldName, fieldValue, condition, null);
    }

    @Override
    public int deleteByColumn(String fieldName, Object fieldValue) {
        return jdbcService.deleteByColumn(cls, fieldName, fieldValue);
    }

    @Override
    public List<T> selectList(Object obj, List<String> fieldNames) {
        return jdbcService.selectList(cls, fieldNames, whereSql(obj, true));
    }

    private WhereSql whereSql(Object obj, boolean orderBy) {
        return jdbcService.whereSql(cls, obj, null, orderBy);
    }

    @Override
    public List<T> selectList(Object obj) {
        return jdbcService.selectList(cls, whereSql(obj, true));
    }

    @Override
    public List<T> selectListByColumn(List<String> fieldNames, String fieldName, Object fieldValue, Condition condition) {
        return jdbcService.selectListByColumn(cls, fieldNames, fieldName, fieldValue, condition);
    }

    @Override
    public List<T> selectListByColumn(String fieldName, Object fieldValue, Condition condition) {
        return jdbcService.selectListByColumn(cls, fieldName, fieldValue, condition);
    }

    @Override
    public List<T> selectListByColumn(String fieldName, Object fieldValue) {
        return jdbcService.selectListByColumn(cls, fieldName, fieldValue);
    }

    @Override
    public PageInfo<T> selectPage(PageInfo<T> pageInfo, Object obj, List<String> fieldNames) {
        return jdbcService.page(pageInfo, cls, fieldNames, whereSql(obj, true));
    }

    @Override
    public PageInfo<T> selectPage(PageInfo<T> pageInfo, Object obj) {
        return jdbcService.page(pageInfo, cls, whereSql(obj, true));
    }

    @Override
    public T selectById(Object id, List<String> fieldNames) {
        return jdbcService.selectById(cls, fieldNames, id);
    }

    @Override
    public T selectById(Object id) {
        return jdbcService.selectById(cls, id);
    }

    @Override
    public List<T> selectByIds(Collection<?> ids, List<String> fieldNames) {
        return jdbcService.selectByIds(cls, fieldNames, ids);
    }

    @Override
    public List<T> selectByIds(Collection<?> ids) {
        return jdbcService.selectByIds(cls, ids);
    }

    @Override
    public T selectOne(Object obj, List<String> fieldNames) {
        return jdbcService.selectOne(cls, fieldNames, whereSql(obj, false));
    }

    @Override
    public T selectOne(Object obj) {
        return jdbcService.selectOne(cls, whereSql(obj, false));
    }

    @Override
    public T selectOneByColumn(List<String> fieldNames, String fieldName, Object fieldValue, Condition condition) {
        return jdbcService.selectOneByColumn(cls, fieldNames, fieldName, fieldValue, condition);
    }

    @Override
    public T selectOneByColumn(String fieldName, Object fieldValue, Condition condition) {
        return jdbcService.selectOneByColumn(cls, fieldName, fieldValue, condition);
    }

    @Override
    public T selectOneByColumn(String fieldName, Object fieldValue) {
        return jdbcService.selectOneByColumn(cls, fieldName, fieldValue, Condition.DEFAULT);
    }

    protected final boolean fail(int row) {
        return jdbcService.fail(row);
    }

    protected final boolean batchFail(int[] rows) {
        return jdbcService.batchFail(rows);
    }

    protected final int[] batchFailIdx(int[] rows) {
        return jdbcService.batchFailIdx(rows);
    }

    /**
     * ????????????????????????????????????
     */
    protected final <E, R> Map<Boolean, List<R>> groupBatchResult(List<E> entities, int[] rows, Function<? super E, ? extends R> mapper) {
        return jdbcService.groupBatchResult(entities, rows, mapper);
    }

    protected void check(T entity) {
    }

    protected void checkId(Object id) {
        ClientFailureMsg.requireNonNull(id, "ID????????????");
        ClientFailureMsg.requireNon(!(id instanceof Long) || ((long) id) <= 0L, "ID?????????");
    }

    protected void checkId(Collection<?> ids) {
        if (NullHelp.nonEmpty(ids)) ids.forEach(this::checkId);
    }

    /**
     * ?????????????????????
     */
    protected final boolean checkUniqueForInsert(Object entity, String... uniqueFields) {
        return jdbcService.checkUniqueForInsert(entity, uniqueFields);
    }

    /**
     * ?????????????????????
     */
    protected final boolean checkUniqueForUpdate(Object entity, String... uniqueFields) {
        return jdbcService.checkUniqueForUpdate(entity, uniqueFields);
    }

    /**
     * ????????????
     */
    @SafeVarargs
    protected final <E> boolean batchEdit(List<E> newDataset, List<E> oldDataset, Function<? super E, ?>... classifiers) {
        return jdbcService.batchEdit(newDataset, oldDataset, classifiers);
    }

    protected void checkBoundData(Class<?> entityClass, String fieldName, Collection<?> fieldValues, String format) {
        Map<?, ?> exists = jdbcService.existsColumn(entityClass, fieldName, fieldValues);
        if (!exists.isEmpty())
            throw PRException.of(ClientFailureMsg.fail(format, ToString.joining(exists.keySet(), ",", "(", ")")));
    }
}
