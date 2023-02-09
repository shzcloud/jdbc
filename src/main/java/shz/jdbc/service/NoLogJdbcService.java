package shz.jdbc.service;

import org.slf4j.event.Level;
import shz.jdbc.JdbcService;

import java.util.function.Supplier;

public class NoLogJdbcService extends JdbcService {
    @Override
    protected final void log(Level level, Supplier<String> sql) {
        //不进行日志记录
    }
}
