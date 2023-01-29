package shz.jdbc.record;

import shz.core.type.TypeHelp;
import shz.jdbc.JdbcService;
import shz.orm.record.OrmConsistentHash;
import shz.orm.record.OrmConsistentHashRecordEntity;
import shz.spring.BeanContainer;

public abstract class JdbcConsistentHashRecordEntity<T extends JdbcConsistentHashRecordEntity<T, S>, S extends JdbcConsistentHash<S, T>> extends OrmConsistentHashRecordEntity<JdbcService, T> {
    protected final Class<S> cls = TypeHelp.getParameterizedType(getClass(), JdbcConsistentHashRecordEntity.class, "S");

    @Override
    protected OrmConsistentHash<JdbcService, T> consistentHash() {
        return BeanContainer.get(cls);
    }
}
