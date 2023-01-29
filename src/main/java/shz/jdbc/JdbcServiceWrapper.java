package shz.jdbc;

import shz.orm.OrmServiceWrapper;

@FunctionalInterface
public interface JdbcServiceWrapper<T extends JdbcService> extends OrmServiceWrapper<T> {
}
