package shz.jdbc;

import com.alibaba.druid.pool.DruidDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import shz.core.*;
import shz.core.constant.ArrayConstant;
import shz.core.st.tst.LTST;
import shz.core.structure.limiter.LongLimiter;
import shz.core.type.TypeHelp;
import shz.jdbc.handler.DefaultSqlHandler;
import shz.jdbc.handler.MysqlSqlHandler;
import shz.jdbc.handler.OracleSqlHandler;
import shz.jdbc.handler.PostgresSqlHandler;
import shz.jdbc.model.Column;
import shz.jdbc.model.ImportedKey;
import shz.jdbc.model.PrimaryKey;
import shz.jdbc.model.Table;
import shz.orm.ClassInfo;
import shz.orm.OrmService;
import shz.orm.Tnp;
import shz.orm.enums.DataType;
import shz.orm.exception.OrmClassNoFieldException;
import shz.orm.param.OrmMapConsumer;
import shz.orm.param.OrmMapFilter;
import shz.orm.param.OrmMapping;
import shz.spring.BeanContainer;

import javax.sql.DataSource;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 常用方法：
 * createStatement()：创建向数据库发送 SQL 的 statement 对象。
 * prepareStatement(SQL) ：创建向数据库发送预编译 SQL 的 PrepareSatement 对象。
 * prepareCall(SQL)：创建执行存储过程的 callableStatement 对象。
 * setAutoCommit(boolean autoCommit)：设置事务是否自动提交。
 * commit() ：在链接上提交事务。
 * rollback() ：在此链接上回滚事务。
 * <p>
 * Statement 接口
 * 用于执行静态 SQL 语句并返回它所生成结果的对象。
 * 三种 Statement 类：
 * Statement：由 createStatement 创建，用于发送简单的 SQL 语句（不带参数）。
 * PreparedStatement ：继承自 Statement 接口，由 preparedStatement 创建，用于发送含有一个或多个参数的 SQL 语句。PreparedStatement 对象比 Statement 对象的效率更高，并且可以防止 SQL 注入，所以我们一般都使用 PreparedStatement。
 * CallableStatement：继承自 PreparedStatement 接口，由方法 prepareCall 创建，用于调用存储过程。
 * 常用 Statement 方法：
 * execute(String SQL):运行语句，返回是否有结果集
 * executeQuery(String SQL)：运行 select 语句，返回 ResultSet 结果集。
 * executeUpdate(String SQL)：运行 insert/update/delete 操作，返回更新的行数。
 * addBatch(String SQL) ：把多条 SQL 语句放到一个批处理中。
 * executeBatch()：向数据库发送一批 SQL 语句执行。
 * <p>
 * ResultSet 接口
 * ResultSet 提供检索不同类型字段的方法，常用的有：
 * getString(int index)、getString(String columnName)：获得在数据库里是 varchar、char 等类型的数据对象。
 * getFloat(int index)、getFloat(String columnName)：获得在数据库里是 Float 类型的数据对象。
 * getDate(int index)、getDate(String columnName)：获得在数据库里是 Date 类型的数据。
 * getBoolean(int index)、getBoolean(String columnName)：获得在数据库里是 Boolean 类型的数据。
 * getObject(int index)、getObject(String columnName)：获取在数据库里任意类型的数据。
 * <p>
 * ResultSet 还提供了对结果集进行滚动的方法：
 * next()：移动到下一行
 * Previous()：移动到前一行
 * absolute(int row)：移动到指定行
 * beforeFirst()：移动 resultSet 的最前面。
 * afterLast() ：移动到 resultSet 的最后面。
 * 使用后依次关闭对象及连接：ResultSet → Statement → Connection
 * <p>
 * 数据库事务拥有以下四个特性，习惯上被称之为ACID 特性。
 * 原子性（Atomicity）：事务作为一个整体被执行，包含在其中的对数据库的操作要么全部被执行，要么都不执行。
 * 一致性（Consistency）：事务应确保数据库的状态从一个一致状态转变为另一个一致状态。一致状态的含义是数据库中的数据应满足完整性约束。
 * 隔离性（Isolation）：多个事务并发执行时，一个事务的执行不应影响其他事务的执行。
 * 持久性（Durability）：已被提交的事务对数据库的修改应该永久保存在数据库中
 */
public class JdbcService extends OrmService {
    public static final JdbcService NULL = new JdbcService();

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected static final class EP {
        public Connection conn;
        private Savepoint sp;
        private volatile boolean rollback;

        EP(Connection conn) {
            this.conn = conn;
        }

        public void setSavepoint() {
            if (sp == null) try {
                sp = conn.setSavepoint();
            } catch (SQLException e) {
                throw PRException.of(e);
            }
        }

        public void releaseSavepoint() {
            try {
                if (sp != null) conn.releaseSavepoint(sp);
                sp = null;
            } catch (SQLException ignored) {
            }
        }
    }

    private DataSource writeDataSource;
    private DataSource readDataSource;
    private DefaultSqlHandler defaultSqlHandler;
    private ThreadLocal<EP> writeThreadLocal;
    private ThreadLocal<Connection> readThreadLocal;
    private ThreadLocal<AtomicInteger> referenceThreadLocal;

    public final void setDataSource(DataSource writeDataSource, DataSource readDataSource) {
        Objects.requireNonNull(writeDataSource);
        this.writeDataSource = writeDataSource;
        this.readDataSource = readDataSource;
        defaultSqlHandler = sqlHandler(this.writeDataSource);
        sqlHandler = defaultSqlHandler;
        writeThreadLocal = InheritableThreadLocal.withInitial(() -> {
            try {
                return new EP(this.writeDataSource.getConnection());
            } catch (SQLException e) {
                throw PRException.of(e);
            }
        });
        if (this.readDataSource != null) readThreadLocal = InheritableThreadLocal.withInitial(() -> {
            try {
                return this.readDataSource.getConnection();
            } catch (SQLException e) {
                throw PRException.of(e);
            }
        });
        referenceThreadLocal = InheritableThreadLocal.withInitial(AtomicInteger::new);
    }

    public final void setDataSource(DataSource dataSource) {
        setDataSource(dataSource, null);
    }

    public final void setDataSource(String writeDriverClassName, String writeUrl, String writeUsername, String writePassword, String readDriverClassName, String readUrl, String readUsername, String readPassword) {
        setDataSource(defaultDruidDataSource(writeDriverClassName, writeUrl, writeUsername, writePassword), defaultDruidDataSource(readDriverClassName, readUrl, readUsername, readPassword));
    }

    public final void setDataSource(String driverClassName, String url, String username, String password) {
        setDataSource(defaultDruidDataSource(driverClassName, url, username, password), null);
    }

    protected DruidDataSource defaultDruidDataSource(String driverClassName, String url, String username, String password) {
        DruidDataSource dataSource = new DruidDataSource();

        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        //初始化时建立物理连接的个数。初始化发生在显示调用init方法，或者第一次getConnection时
        dataSource.setInitialSize(1);
        //最大连接池数量
        dataSource.setMaxActive(64);
        //最小连接池数量
        dataSource.setMinIdle(2);
        //获取连接时最大等待时间，单位毫秒。配置了maxWait之后，缺省启用公平锁，并发效率会有所下降，如果需要可以通过配置useUnfairLock属性为true使用非公平锁。
        dataSource.setMaxWait(60000);
        //是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle。在mysql下建议关闭
        dataSource.setPoolPreparedStatements(false);
        //要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true。在Druid中，不会存在Oracle下PSCache占用内存过多的问题，可以把这个数值配置大一些，比如说100
        dataSource.setMaxOpenPreparedStatements(-1);
        //用来检测连接是否有效的sql，要求是一个查询语句。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会其作用
        dataSource.setValidationQuery("SELECT 1 FROM DUAL");
        //申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
        dataSource.setTestOnBorrow(true);
        //归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
        dataSource.setTestOnReturn(false);
        //建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效
        dataSource.setTestWhileIdle(true);
        //有两个含义： 1) Destroy线程会检测连接的间隔时间2) testWhileIdle的判断依据，详细看testWhileIdle属性的说明
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        //配置一个连接在池中最小生存的时间，单位是毫秒
        dataSource.setMinEvictableIdleTimeMillis(300000);

        Properties connectProperties = new Properties();
        //打开mergeSql功能
        connectProperties.setProperty("druid.stat.mergeSql", "true");
        //慢SQL记录
        connectProperties.setProperty("druid.stat.slowSqlMillis", "5000");
        dataSource.setConnectProperties(connectProperties);

        return dataSource;
    }

    private static final Map<DataSource, DefaultSqlHandler> SQL_HANDLERS = new ConcurrentHashMap<>(4);

    @Override
    protected DefaultSqlHandler sqlHandler(DataSource dataSource) {
        return SQL_HANDLERS.computeIfAbsent(dataSource, k -> {
            Connection conn = null;
            String productName;
            try {
                conn = dataSource.getConnection();
                productName = conn.getMetaData().getDatabaseProductName();
            } catch (SQLException e) {
                throw PRException.of(e);
            } finally {
                if (conn != null) try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
            NullHelp.requireNonBlank(productName);
            switch (productName) {
                case "MySQL":
                    return BeanContainer.get(MysqlSqlHandler.class, MysqlSqlHandler::new);
                case "Oracle":
                    return BeanContainer.get(OracleSqlHandler.class, OracleSqlHandler::new);
                case "PostgreSQL":
                    return BeanContainer.get(PostgresSqlHandler.class, PostgresSqlHandler::new);
                default:
                    throw new UnsupportedOperationException();
            }
        });
    }

    protected final EP ep() {
        EP ep = writeThreadLocal.get();
        if (ep == null || ep.rollback) return null;
        referenceThreadLocal.get().incrementAndGet();
        return ep;
    }

    protected final Connection conn() {
        if (readThreadLocal == null) {
            EP ep = ep();
            return ep == null ? null : ep.conn;
        }
        Connection conn = readThreadLocal.get();
        referenceThreadLocal.get().incrementAndGet();
        return conn;
    }

    protected final void catchThrow(EP ep) {
        ep.rollback = true;
        if (ep.sp != null && ep.conn != null) try {
            ep.conn.rollback(ep.sp);
            ep.conn.releaseSavepoint(ep.sp);
            ep.sp = null;
        } catch (SQLException ignored) {
        }
    }

    protected final void close(ResultSet rst) {
        if (rst != null) try {
            rst.close();
        } catch (SQLException ignored) {
        }
    }

    protected final void close(PreparedStatement pst) {
        if (pst != null) try {
            pst.close();
        } catch (SQLException ignored) {
        }
    }

    protected final void close(Statement st) {
        if (st != null) try {
            st.close();
        } catch (SQLException ignored) {
        }
    }

    protected final void close(Connection conn) {
        if (conn != null) try {
            conn.close();
        } catch (SQLException ignored) {
        }
        writeThreadLocal.remove();
        if (readThreadLocal != null) readThreadLocal.remove();
        referenceThreadLocal.remove();
    }

    protected final void close(ResultSet rst, PreparedStatement pst, EP ep) {
        close(rst);
        close(pst);
        int count = referenceThreadLocal.get().decrementAndGet();
        if (count < 0) throw new IllegalStateException();
        if (count == 0) {
            if (!ep.rollback) try {
                if (!ep.conn.getAutoCommit()) ep.conn.commit();
            } catch (SQLException ignored) {
            }
            close(ep.conn);
        }
    }

    protected final void close(ResultSet rst, PreparedStatement pst, Connection conn) {
        close(rst);
        close(pst);
        int count = referenceThreadLocal.get().decrementAndGet();
        if (count < 0) throw new IllegalStateException();
        if (count == 0) close(conn);
    }

    private boolean logEnabled = true;

    public final void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    protected void log(Level level, Supplier<String> sql) {
        if (!logEnabled) return;
        switch (level) {
            case TRACE:
                if (log.isTraceEnabled()) log.trace(sql.get());
                break;
            case DEBUG:
                if (log.isDebugEnabled()) log.debug(sql.get());
                break;
            case INFO:
                if (log.isInfoEnabled()) log.info(sql.get());
                break;
            case WARN:
                if (log.isWarnEnabled()) log.warn(sql.get());
                break;
            case ERROR:
                if (log.isErrorEnabled()) log.error(sql.get());
                break;
            default:
                break;
        }
    }

    protected final String logSql(String sql, Object... params) {
        if (NullHelp.isEmpty(params)) return sql;
        else {
            int len = sql.length();
            StringBuilder sb = new StringBuilder(len + 100);
            int plen = params.length;
            char c;
            int i = 0;
            for (int j = 0; i < len; ++i) {
                c = sql.charAt(i);
                if (c == '?') {
                    sqlHandler.escape(sb, params[j]);
                    if (++j == plen) {
                        ++i;
                        break;
                    }
                } else sb.append(c);
            }
            if (i < len) sb.append(sql, i, len);
            return sb.toString();
        }
    }

    protected final void setPst(PreparedStatement pst, Object[] params) throws SQLException {
        for (int i = 1; i <= params.length; ++i) {
            Object val = params[i - 1];
            if (val == null) pst.setNull(i, Types.NULL);
            else if (val instanceof CharSequence) pst.setString(i, val.toString());
            else if (val instanceof Integer) pst.setInt(i, (Integer) val);
            else if (val instanceof Long) pst.setLong(i, (Long) val);
            else if (val instanceof Byte) pst.setByte(i, (Byte) val);
            else if (val instanceof Boolean) pst.setBoolean(i, (Boolean) val);
            else if (val instanceof BigDecimal) pst.setBigDecimal(i, (BigDecimal) val);
            else if (val instanceof Date) pst.setObject(i, val, Types.TIMESTAMP);
            else if (val instanceof ZonedDateTime)
                pst.setObject(i, Timestamp.from(((ZonedDateTime) val).toInstant()), Types.TIMESTAMP);
            else if (val instanceof LocalDateTime)
                pst.setObject(i, Timestamp.valueOf((LocalDateTime) val), Types.TIMESTAMP);
            else if (val instanceof Instant) pst.setObject(i, Timestamp.from((Instant) val), Types.TIMESTAMP);
            else if (val instanceof LocalDate)
                pst.setObject(i, Timestamp.valueOf(LocalDateTime.of((LocalDate) val, LocalTime.MIN)), Types.DATE);
            else if (val instanceof LocalTime) pst.setObject(i, Time.valueOf((LocalTime) val), Types.TIME);
            else if (val instanceof Short) pst.setShort(i, (Short) val);
            else if (val instanceof Double) pst.setDouble(i, (Double) val);
            else if (val instanceof Character) pst.setObject(i, val, Types.CHAR);
            else if (val instanceof BigInteger) pst.setLong(i, ((BigInteger) val).longValue());
            else if (val instanceof Float) pst.setFloat(i, (Float) val);
            else if (val instanceof Number) pst.setObject(i, val, Types.NUMERIC);
            else pst.setObject(i, val);
        }
    }

    protected final void preSet(EP ep) throws SQLException {
        if (ep.conn.getAutoCommit()) ep.conn.setAutoCommit(false);
        if (ep.conn.isReadOnly()) ep.conn.setReadOnly(false);
    }

    protected final void preSet(Connection conn) throws SQLException {
        if (!conn.isReadOnly()) conn.setReadOnly(true);
    }

    public final boolean execute(String sql) {
        EP ep = ep();
        if (ep == null) return false;
        PreparedStatement pst = null;
        try {
            preSet(ep);
            pst = ep.conn.prepareStatement(sql);
            ep.setSavepoint();
            log(Level.DEBUG, () -> logSql(sql));
            return pst.execute();
        } catch (Throwable t) {
            catchThrow(ep);
            log(Level.ERROR, () -> logSql(sql));
            throw PRException.of(t);
        } finally {
            close(null, pst, ep);
        }
    }

    public final boolean execute(String sql, Object... params) {
        EP ep = ep();
        if (ep == null) return false;
        PreparedStatement pst = null;
        try {
            preSet(ep);
            pst = ep.conn.prepareStatement(sql);
            setPst(pst, params);
            ep.setSavepoint();
            log(Level.DEBUG, () -> logSql(sql, params));
            return pst.execute();
        } catch (Throwable t) {
            catchThrow(ep);
            log(Level.ERROR, () -> logSql(sql, params));
            throw PRException.of(t);
        } finally {
            close(null, pst, ep);
        }
    }

    public final void executeBatch(int batchSize, String... sqls) {
        if (sqls.length == 1) {
            execute(sqls[0]);
            return;
        }
        EP ep = ep();
        if (ep == null) return;
        Statement st = null;
        try {
            preSet(ep);
            st = ep.conn.createStatement();
            int batch = 0;
            for (String sql : sqls) {
                st.addBatch(sql);
                log(Level.DEBUG, () -> logSql("batch:" + sql));
                if (batchSize > 0 && ++batch % batchSize == 0) {
                    executeBatch(ep, st);
                    batch = 0;
                }
            }
            if (batchSize <= 0 || batch > 0) executeBatch(ep, st);
        } catch (Throwable t) {
            catchThrow(ep);
            throw PRException.of(t);
        } finally {
            close(st);
            close(null, null, ep);
        }
    }

    private void executeBatch(EP ep, Statement st) {
        try {
            st.executeBatch();
            st.clearBatch();
        } catch (SQLException e) {
            catchThrow(ep);
            throw PRException.of(e);
        }
    }

    public final void executeBatch(int batchSize, String sql, Collection<Object[]> values) {
        if (values.size() == 1) {
            execute(sql, values.iterator().next());
            return;
        }
        EP ep = ep();
        if (ep == null) return;
        PreparedStatement pst = null;
        try {
            preSet(ep);
            pst = ep.conn.prepareStatement(sql);
            int batch = 0;
            for (Object[] params : values) {
                setPst(pst, params);
                pst.addBatch();
                log(Level.DEBUG, () -> logSql("batch:" + sql, params));
                if (batchSize > 0 && ++batch % batchSize == 0) {
                    executeBatch(ep, pst);
                    batch = 0;
                }
            }
            if (batchSize <= 0 || batch > 0) executeBatch(ep, pst);
        } catch (Throwable t) {
            catchThrow(ep);
            throw PRException.of(t);
        } finally {
            close(null, pst, ep);
        }
    }

    @Override
    protected final String humpToUnderline(String name) {
        return sqlHandler.humpToUnderline(name);
    }

    private static final Map<Type, DataType> DATA_TYPE_CACHE = new HashMap<>(128);

    @Override
    protected final DataType dataType(Type type) {
        Class<?> cls = TypeHelp.toClass(type);
        if (cls == null || Map.class.isAssignableFrom(cls)) return DataType.MAP;
        if (Collection.class.isAssignableFrom(cls)) return DataType.LIST;
        if (cls.isArray()) return DataType.ARRAY;
        if (TypeHelp.likeCommon(cls)) return DataType.COMMON;
        return DATA_TYPE_CACHE.computeIfAbsent(type, k -> {
            boolean nested = false;
            for (Field f : AccessibleHelp.fields(TypeHelp.toClass(k))) {
                shz.orm.annotation.Column column = f.getAnnotation(shz.orm.annotation.Column.class);
                if (column != null && column.ignoreNested()) continue;
                Class<?> fCls = TypeHelp.fieldClass(f, k);
                if (Map.class.isAssignableFrom(fCls) || Collection.class.isAssignableFrom(fCls) || fCls.isArray())
                    return DataType.MERGE;
                if (!nested && !TypeHelp.likeCommon(fCls)) nested = true;
            }
            return nested ? DataType.NESTED : DataType.DEFAULT;
        });
    }

    @Override
    public final <T> T apply(Supplier<? extends T> action) {
        EP ep = ep();
        if (ep == null) return null;
        try {
            return action.get();
        } catch (Throwable t) {
            catchThrow(ep);
            throw PRException.of(t);
        } finally {
            close(null, null, ep);
        }
    }

    @Override
    public final int[] executeBatch(int batchSize, boolean commit, String sql, List<Object[]> values) {
        EP ep = ep();
        if (ep == null) return ArrayConstant.EMPTY_INT_ARRAY;
        PreparedStatement pst = null;
        try {
            preSet(ep);
            pst = ep.conn.prepareStatement(sql);
            int[] result = new int[values.size()];
            int batch = 0, destPos = 0;
            for (Object[] params : values) {
                setPst(pst, params);
                pst.addBatch();
                log(Level.INFO, () -> logSql("batch:" + sql, params));
                if (batchSize > 0 && ++batch % batchSize == 0) {
                    executeBatch(ep, commit, pst, result, destPos);
                    destPos += batch;
                    batch = 0;
                }
            }
            if (batchSize <= 0) executeBatch(ep, commit, pst, result, 0);
            else if (batch > 0) executeBatch(ep, commit, pst, result, destPos);
            return result;
        } catch (Throwable t) {
            catchThrow(ep);
            throw PRException.of(t);
        } finally {
            close(null, pst, ep);
        }
    }

    private void executeBatch(EP ep, boolean commit, Statement st, int[] result, int destPos) {
        try {
            if (commit) {
                ep.setSavepoint();
                int[] rows = st.executeBatch();
                System.arraycopy(rows, 0, result, destPos, rows.length);
                ep.conn.commit();
                st.clearBatch();
                ep.releaseSavepoint();
            } else {
                int[] rows = st.executeBatch();
                System.arraycopy(rows, 0, result, destPos, rows.length);
                st.clearBatch();
            }
        } catch (SQLException e) {
            catchThrow(ep);
            throw PRException.of(e);
        }
    }

    @Override
    public final int[] executeBatch(int batchSize, boolean commit, String... sqls) {
        EP ep = ep();
        if (ep == null) return ArrayConstant.EMPTY_INT_ARRAY;
        Statement st = null;
        try {
            preSet(ep);
            st = ep.conn.createStatement();
            int[] result = new int[sqls.length];
            int batch = 0, destPos = 0;
            for (String sql : sqls) {
                st.addBatch(sql);
                log(Level.INFO, () -> logSql("batch:" + sql));
                if (batchSize > 0 && ++batch % batchSize == 0) {
                    executeBatch(ep, commit, st, result, destPos);
                    destPos += batch;
                    batch = 0;
                }
            }
            if (batchSize <= 0) executeBatch(ep, commit, st, result, 0);
            else if (batch > 0) executeBatch(ep, commit, st, result, destPos);
            return result;
        } catch (Throwable t) {
            catchThrow(ep);
            throw PRException.of(t);
        } finally {
            close(st);
            close(null, null, ep);
        }
    }

    @Override
    protected final int insert(Consumer<Object> idSetter, String sql, Object... params) {
        EP ep = ep();
        if (ep == null) return 0;
        PreparedStatement pst = null;
        ResultSet rst = null;
        try {
            preSet(ep);
            pst = ep.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            setPst(pst, params);
            ep.setSavepoint();
            log(Level.INFO, () -> logSql(sql, params));
            int row = pst.executeUpdate();
            if (idSetter != null) {
                rst = pst.getGeneratedKeys();
                if (rst.next()) idSetter.accept(rst.getObject(1));
            }
            return row;
        } catch (SQLException e) {
            catchThrow(ep);
            log(Level.ERROR, () -> logSql(sql, params));
            throw PRException.of(e);
        } finally {
            close(rst, pst, ep);
        }
    }

    @Override
    protected final int[] batchInsert(BiConsumer<Integer, Object> idSetter, int batchSize, boolean commit, String sql, List<Object[]> values) {
        EP ep = ep();
        if (ep == null) return ArrayConstant.EMPTY_INT_ARRAY;
        PreparedStatement pst = null;
        try {
            preSet(ep);
            pst = ep.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int[] result = new int[values.size()];
            int batch = 0, destPos = 0;
            for (Object[] params : values) {
                setPst(pst, params);
                pst.addBatch();
                log(Level.INFO, () -> logSql("batch:" + sql, params));
                if (batchSize > 0 && ++batch % batchSize == 0) {
                    batchInsert0(idSetter, ep, commit, pst, result, destPos);
                    destPos += batch;
                    batch = 0;
                }
            }
            if (batchSize <= 0) batchInsert0(idSetter, ep, commit, pst, result, 0);
            else if (batch > 0) batchInsert0(idSetter, ep, commit, pst, result, destPos);
            return result;
        } catch (Throwable t) {
            catchThrow(ep);
            throw PRException.of(t);
        } finally {
            close(null, pst, ep);
        }
    }

    private void batchInsert0(BiConsumer<Integer, Object> idSetter, EP ep, boolean commit, Statement st, int[] result, int destPos) {
        ResultSet rst = null;
        try {
            if (commit) {
                ep.setSavepoint();
                int[] rows = st.executeBatch();
                if (idSetter != null) rst = st.getGeneratedKeys();
                System.arraycopy(rows, 0, result, destPos, rows.length);
                ep.conn.commit();
                st.clearBatch();
                ep.releaseSavepoint();
            } else {
                int[] rows = st.executeBatch();
                if (idSetter != null) rst = st.getGeneratedKeys();
                System.arraycopy(rows, 0, result, destPos, rows.length);
                st.clearBatch();
            }
            if (rst != null) while (rst.next()) idSetter.accept(destPos++, rst.getObject(1));
        } catch (SQLException e) {
            catchThrow(ep);
            throw PRException.of(e);
        } finally {
            close(rst);
        }
    }

    @Override
    public final int update(String sql, Object... params) {
        EP ep = ep();
        if (ep == null) return 0;
        PreparedStatement pst = null;
        try {
            preSet(ep);
            pst = ep.conn.prepareStatement(sql);
            setPst(pst, params);
            ep.setSavepoint();
            log(Level.INFO, () -> logSql(sql, params));
            return pst.executeUpdate();
        } catch (SQLException e) {
            catchThrow(ep);
            log(Level.ERROR, () -> logSql(sql, params));
            throw PRException.of(e);
        } finally {
            close(null, pst, ep);
        }
    }

    @Override
    public final int delete(String sql, Object... params) {
        return update(sql, params);
    }

    @Override
    public final int count(String sql, Object... params) {
        Connection conn = conn();
        if (conn == null) return 0;
        PreparedStatement pst = null;
        ResultSet rst = null;
        try {
            preSet(conn);
            pst = conn.prepareStatement(sql);
            setPst(pst, params);
            log(Level.DEBUG, () -> logSql(sql, params));
            rst = pst.executeQuery();
            return rst.next() ? rst.getInt(1) : 0;
        } catch (Throwable t) {
            log(Level.ERROR, () -> logSql(sql, params));
            throw PRException.of(t);
        } finally {
            close(rst, pst, conn);
        }
    }

    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<ddl
    public final void createTable(Table table) {
        accept(() -> {
            try {
                execute(defaultSqlHandler.dropTable(Tnp.of().tableSchema(table.getTableSchem()).tableName(table.getTableName())));
            } catch (Throwable ignored) {
            }
            executeBatch(0, defaultSqlHandler.createTable(table).split(defaultSqlHandler.multiSqlSep()));
        });
    }

    public final void createTables(List<Table> tables) {
        executeBatch(0, tables.stream().map(table -> {
            try {
                execute(defaultSqlHandler.dropTable(Tnp.of().tableSchema(table.getTableSchem()).tableName(table.getTableName())));
            } catch (Throwable ignored) {
            }
            return defaultSqlHandler.createTable(table);
        }).filter(Objects::nonNull).flatMap(sqls -> Arrays.stream(sqls.split(defaultSqlHandler.multiSqlSep()))).toArray(String[]::new));
    }

    public final void dropTable(Tnp tnp) {
        execute(defaultSqlHandler.dropTable(tnp));
    }

    public final void dropTables(List<Tnp> tnps) {
        if (NullHelp.nonEmpty(tnps))
            executeBatch(0, tnps.stream().map(defaultSqlHandler::dropTable).toArray(String[]::new));
    }

    public final void copyTableStructure(Tnp src, Tnp des) {
        execute(defaultSqlHandler.copyTableStructure(src, des));
    }

    public final List<Table> getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) {
        Connection conn = conn();
        if (conn == null) return Collections.emptyList();

        List<Table> result = new LinkedList<>();
        ResultSet tables = null;
        ResultSet primaryKeys = null;
        ResultSet importedKeys = null;
        ResultSet columns = null;
        try {
            catalog = NullHelp.isBlank(catalog) ? conn.getCatalog() : catalog;
            schemaPattern = NullHelp.isBlank(schemaPattern) ? "%" : schemaPattern;
            types = NullHelp.isBlank(types) ? new String[]{"TABLE"} : types;
            DatabaseMetaData dbMetaData = conn.getMetaData();
            tables = dbMetaData.getTables(catalog, schemaPattern, tableNamePattern, types);
            while (tables.next()) {
                Table table = new Table();
                table.setTableCat(getString(tables, "TABLE_CAT"));
                table.setTableSchem(getString(tables, "TABLE_SCHEM"));
                table.setTableName(getString(tables, "TABLE_NAME"));
                table.setTableType(getString(tables, "TABLE_TYPE"));
                table.setRemarks(getString(tables, "REMARKS"));

                table.setTypeCat(getString(tables, "TYPE_CAT"));
                table.setTypeSchem(getString(tables, "TYPE_SCHEM"));
                table.setTypeName(getString(tables, "TYPE_NAME"));
                table.setSelfReferencingColName(getString(tables, "SELF_REFERENCING_COL_NAME"));
                table.setRefGeneration(getString(tables, "REF_GENERATION"));

                //主键
                primaryKeys = dbMetaData.getPrimaryKeys(catalog, schemaPattern, table.getTableName());
                List<PrimaryKey> primaryKeyList = new LinkedList<>();
                while (primaryKeys.next()) {
                    PrimaryKey primaryKey = new PrimaryKey();
                    primaryKey.setTableCat(getString(primaryKeys, "TABLE_CAT"));
                    primaryKey.setTableSchem(getString(primaryKeys, "TABLE_SCHEM"));
                    primaryKey.setTableName(getString(primaryKeys, "TABLE_NAME"));
                    primaryKey.setColumnName(getString(primaryKeys, "COLUMN_NAME"));
                    primaryKey.setKeySeq(getShort(primaryKeys, "KEY_SEQ"));
                    primaryKey.setPkName(getString(primaryKeys, "PK_NAME"));

                    primaryKeyList.add(primaryKey);
                }
                table.setPrimaryKeys(primaryKeyList.isEmpty() ? Collections.emptyList() : ToList.explicitCollect(primaryKeyList.stream().distinct(), primaryKeyList.size()));
                close(primaryKeys);

                //外键
                importedKeys = dbMetaData.getImportedKeys(catalog, schemaPattern, table.getTableName());
                List<ImportedKey> importedKeyList = new LinkedList<>();
                while (importedKeys.next()) {
                    ImportedKey importedKey = new ImportedKey();
                    importedKey.setPkTableCat(getString(importedKeys, "PKTABLE_CAT"));
                    importedKey.setPkTableSchem(getString(importedKeys, "PKTABLE_SCHEM"));
                    importedKey.setPkTableName(getString(importedKeys, "PKTABLE_NAME"));
                    importedKey.setPkColumnName(getString(importedKeys, "PKCOLUMN_NAME"));
                    importedKey.setPkName(getString(importedKeys, "PK_NAME"));

                    importedKey.setFkTableCat(getString(importedKeys, "FKTABLE_CAT"));
                    importedKey.setFkTableSchem(getString(importedKeys, "FKTABLE_SCHEM"));
                    importedKey.setFkTableName(getString(importedKeys, "FKTABLE_NAME"));
                    importedKey.setFkColumnName(getString(importedKeys, "FKCOLUMN_NAME"));
                    importedKey.setFkName(getString(importedKeys, "FK_NAME"));

                    importedKey.setKeySeq(getShort(importedKeys, "KEY_SEQ"));
                    importedKey.setUpdateRule(getShort(importedKeys, "UPDATE_RULE"));
                    importedKey.setDeleteRule(getShort(importedKeys, "DELETE_RULE"));
                    importedKey.setDeferrability(getShort(importedKeys, "DEFERRABILITY"));

                    importedKeyList.add(importedKey);
                }
                table.setImportedKeys(importedKeyList.isEmpty() ? Collections.emptyList() : ToList.explicitCollect(importedKeyList.stream().distinct(), importedKeyList.size()));
                close(importedKeys);

                //列
                columns = dbMetaData.getColumns(catalog, schemaPattern, table.getTableName(), "%");
                List<Column> columnList = new LinkedList<>();
                while (columns.next()) {
                    Column column = new Column();
                    column.setColumnName(getString(columns, "COLUMN_NAME"));
                    column.setDataType(getInt(columns, "DATA_TYPE"));
                    column.setTypeName(getString(columns, "TYPE_NAME"));
                    column.setColumnSize(getInt(columns, "COLUMN_SIZE"));
                    column.setDecimalDigits(getInt(columns, "DECIMAL_DIGITS"));
                    column.setNumPrecRadix(getInt(columns, "NUM_PREC_RADIX"));
                    column.setNullable(getInt(columns, "NULLABLE"));
                    column.setRemarks(getString(columns, "REMARKS"));
                    column.setColumnDef(getString(columns, "COLUMN_DEF"));
                    column.setCharOctetLength(getInt(columns, "CHAR_OCTET_LENGTH"));
                    column.setOrdinalPosition(getInt(columns, "ORDINAL_POSITION"));
                    column.setIsNullable(getString(columns, "IS_NULLABLE"));
                    column.setSourceDataType(getShort(columns, "SOURCE_DATA_TYPE"));
                    column.setIsAutoIncrement(getString(columns, "IS_AUTOINCREMENT"));
                    column.setScopeCatlog(getString(columns, "SCOPE_CATLOG"));
                    column.setScopeSchem(getString(columns, "SCOPE_SCHEMA"));
                    column.setScopeTable(getString(columns, "SCOPE_TABLE"));

                    columnList.add(column);
                }
                table.setColumns(columnList.isEmpty() ? Collections.emptyList() : ToList.explicitCollect(columnList.stream().distinct(), columnList.size()));
                close(columns);

                result.add(table);
            }
        } catch (Exception e) {
            throw PRException.of(e);
        } finally {
            close(columns);
            close(importedKeys);
            close(primaryKeys);
            close(tables, null, conn);
        }
        return result.isEmpty() ? Collections.emptyList() : result;
    }

    private String getString(ResultSet resultSet, String columnLabel) {
        try {
            return resultSet.getString(columnLabel);
        } catch (SQLException e) {
            return null;
        }
    }

    private int getInt(ResultSet resultSet, String columnLabel) {
        try {
            return resultSet.getInt(columnLabel);
        } catch (SQLException e) {
            return 0;
        }
    }

    private short getShort(ResultSet resultSet, String columnLabel) {
        try {
            return resultSet.getShort(columnLabel);
        } catch (SQLException e) {
            return (short) 0;
        }
    }

    public final List<Table> getTables(String schemaPattern, String tableNamePattern) {
        return getTables(null, schemaPattern, tableNamePattern, null);
    }

    public final List<Table> getTables(String tableNamePattern) {
        return getTables(null, tableNamePattern);
    }

    public final List<String> fromIs(InputStream is) {
        return defaultSqlHandler.fromIs(is);
    }

    public final void createDatabases(String... databases) {
        NullHelp.requireNonAnyBlank(databases);
        executeBatch(0, Arrays.stream(databases).map(defaultSqlHandler::createDatabase).toArray(String[]::new));
    }

    public final void dropDatabases(String... databases) {
        NullHelp.requireNonAnyBlank(databases);
        executeBatch(0, Arrays.stream(databases).map(defaultSqlHandler::dropDatabase).toArray(String[]::new));
    }

    public final void backupDatabases(String disk, String... databases) {
        NullHelp.requireNonBlank(disk);
        NullHelp.requireNonAnyBlank(databases);
        executeBatch(0, Arrays.stream(databases).map(database -> defaultSqlHandler.backupDatabase(database, disk)).toArray(String[]::new));
    }

    public final void restoreDatabases(String disk, String... databases) {
        NullHelp.requireNonBlank(disk);
        NullHelp.requireNonAnyBlank(databases);
        executeBatch(0, Arrays.stream(databases).map(database -> defaultSqlHandler.restoreDatabase(database, disk)).toArray(String[]::new));
    }
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<ddl

    @Override
    public final void query(OrmMapping mapping, Type type, LongLimiter limiter, OrmMapFilter mapFilter, OrmMapConsumer mapConsumer, int fetchSize, String sql, Object... params) {
        NullHelp.requireNon(mapFilter == null && mapConsumer == null);
        Connection conn = conn();
        if (conn == null) return;
        PreparedStatement pst = null;
        ResultSet rst = null;
        try {
            preSet(conn);
            pst = conn.prepareStatement(sql);
            if (fetchSize != Integer.MAX_VALUE)
                pst.setFetchSize(fetchSize == 0 ? 3000 : fetchSize < 0 || fetchSize > 9000 ? 9000 : fetchSize);
            setPst(pst, params);
            log(Level.DEBUG, () -> logSql(sql, params));
            rst = pst.executeQuery();

            ResultSetMetaData metaData = rst.getMetaData();
            int count = metaData.getColumnCount();
            int capacity = (int) Math.ceil(count / 0.75f);

            Map<String, String> columnMapping = columnMapping(mapping, type, metaData, sql);
            while (rst.next()) {
                if (limiter != null && limiter.isLimit()) break;
                Map<String, Object> map = new LinkedHashMap<>(capacity, 1.0f);
                for (Map.Entry<String, String> entry : columnMapping.entrySet())
                    map.put(entry.getValue(), sqlHandler.valueMap(rst.getObject(entry.getKey())));
                //mapFilter不为null时的过滤,即映射为对象之前就过滤
                if (mapFilter != null && !mapFilter.test(map)) continue;
                if (mapConsumer == null) {
                    if (limiter != null && limiter.isLimitAfter()) break;
                    continue;
                }
                //进行数据的消费
                mapConsumer.accept(map);
            }
        } catch (Exception e) {
            log(Level.ERROR, () -> logSql(sql, params));
            throw PRException.of(e);
        } finally {
            close(rst, pst, conn);
        }
    }

    /**
     * 根据sql缓存对象映射关系(当使用select * 时,禁止手动修改或删除数据库字段,否则应当禁用该缓存)
     */
    protected static final LTST<Map<String, String>> SQL_COLUMN_MAPPING_CACHE = LTST.of();

    protected Map<String, String> columnMapping(OrmMapping mapping, Type type, ResultSetMetaData metaData, String sql) throws SQLException {
        char[] chars = sql.toCharArray();
        Map<String, String> columnMapping = SQL_COLUMN_MAPPING_CACHE.get(chars);
        if (columnMapping != null) return columnMapping;
        columnMapping = ToMap.get(metaData.getColumnCount()).build();
        DataType dataType = dataType(type);
        if (dataType == DataType.MAP || dataType == DataType.LIST || dataType == DataType.ARRAY || dataType == DataType.COMMON)
            simpleColumnMapping(mapping, metaData, columnMapping);
        else if (dataType == DataType.DEFAULT) defaultColumnMapping(mapping, type, metaData, columnMapping);
        else nestedColumMapping(mapping, type, metaData, columnMapping);
        SQL_COLUMN_MAPPING_CACHE.put(chars, columnMapping);
        return columnMapping;
    }

    private void simpleColumnMapping(OrmMapping mapping, ResultSetMetaData metaData, Map<String, String> columnMapping) throws SQLException {
        int count = metaData.getColumnCount();
        if (mapping == null) {
            for (int i = 0; i < count; ++i) {
                String columnLabel = metaData.getColumnLabel(i + 1);
                columnMapping.put(columnLabel, columnLabel);
            }
        } else {
            for (int i = 0; i < count; ++i) {
                String columnLabel = metaData.getColumnLabel(i + 1);
                columnMapping.put(columnLabel, NullHelp.nullOrElse(mapping.apply(columnLabel), columnLabel));
            }
        }
    }

    private void defaultColumnMapping(OrmMapping mapping, Type type, ResultSetMetaData metaData, Map<String, String> columnMapping) throws SQLException {
        Class<?> cls = TypeHelp.toClass(type);
        List<Field> fields = AccessibleHelp.fields(cls);
        if (fields.isEmpty()) throw new OrmClassNoFieldException(cls, "");
        ClassInfo classInfo = ClassInfo.get(cls);
        int count = metaData.getColumnCount();
        if (mapping == null) {
            for (int i = 0; i < count; ++i) {
                String columnLabel = metaData.getColumnLabel(i + 1);
                columnMapping.put(columnLabel, defaultColumnMapping(classInfo, fields, columnLabel));
            }
        } else {
            for (int i = 0; i < count; ++i) {
                String columnLabel = metaData.getColumnLabel(i + 1);
                columnMapping.put(columnLabel, defaultColumnMapping(classInfo, fields, NullHelp.nullOrElse(mapping.apply(columnLabel), columnLabel)));
            }
        }
    }

    private String defaultColumnMapping(ClassInfo classInfo, List<Field> fields, String column) {
        for (Field field : fields)
            if ((classInfo != null && column.equals(classInfo.toColumnName(field.getName()))) || sqlHandler.aliasToField(column).equals(field.getName()))
                return field.getName();
        return null;
    }

    private void nestedColumMapping(OrmMapping mapping, Type type, ResultSetMetaData metaData, Map<String, String> columnMapping) throws SQLException {
        Class<?> cls = TypeHelp.toClass(type);
        List<Field> fields = AccessibleHelp.fields(cls);
        if (fields.isEmpty()) throw new OrmClassNoFieldException(cls, "");
        ClassInfo classInfo = ClassInfo.get(cls);
        Map<Field, Type> fieldTypeMap = TypeHelp.fieldType(type);
        int count = metaData.getColumnCount();
        if (mapping == null) {
            for (int i = 0; i < count; ++i) {
                String columnLabel = metaData.getColumnLabel(i + 1);
                columnMapping.put(columnLabel, nestedColumMapping(classInfo, fieldTypeMap, fields, columnLabel, ""));
            }
        } else {
            for (int i = 0; i < count; ++i) {
                String columnLabel = metaData.getColumnLabel(i + 1);
                columnMapping.put(columnLabel, nestedColumMapping(classInfo, fieldTypeMap, fields, NullHelp.nullOrElse(mapping.apply(columnLabel), columnLabel), ""));
            }
        }
    }

    private String nestedColumMapping(ClassInfo classInfo, Map<Field, Type> fieldTypeMap, List<Field> fields, String column, String mapField) {
        if (fields.isEmpty()) return null;
        Type[] types;
        Class<?> cls;
        String key;
        for (Field field : fields) {
            if (classInfo != null && column.equals(classInfo.toColumnName(field.getName()))) return field.getName();
            String fieldName = sqlHandler.aliasToField(column);
            if (fieldName.equals(field.getName()) || fieldName.equals(mapField + field.getName())) return fieldName;
            Class<?> fCls = TypeHelp.fieldClass(field, fieldTypeMap);
            if (Map.class.isAssignableFrom(fCls) || TypeHelp.likeCommon(fCls)) continue;
            if (Collection.class.isAssignableFrom(fCls) || fCls.isArray()) {
                if ((types = TypeHelp.getActualTypeArguments(TypeHelp.fieldType(field, fieldTypeMap))) == null || types.length != 1)
                    continue;
                if (!TypeHelp.likeModel(cls = TypeHelp.toClass(types[0]))) continue;
                key = nestedColumMapping(ClassInfo.get(cls), fieldTypeMap, AccessibleHelp.fields(cls), column, mapField + field.getName() + ".");
                if (key == null) continue;
                return key;
            }
            key = nestedColumMapping(ClassInfo.get(fCls), fieldTypeMap, AccessibleHelp.fields(fCls), column, mapField + field.getName() + ".");
            if (key != null) return key;
        }
        return null;
    }
}
