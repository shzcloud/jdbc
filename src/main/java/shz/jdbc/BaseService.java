package shz.jdbc;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseService<T, P> extends SimpleService<T> {
    @Autowired
    protected P repository;

    public BaseService(JdbcServiceWrapper<?> serviceWrapper) {
        super(serviceWrapper);
    }

    public BaseService() {
    }
}
