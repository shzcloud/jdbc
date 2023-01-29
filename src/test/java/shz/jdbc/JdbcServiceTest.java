package shz.jdbc;

import org.junit.jupiter.api.Test;
import shz.core.InterfaceProxy;
import shz.jdbc.record.JdbcConsistentHashRepository;

class JdbcServiceTest {
    @Test
    void test() {
        JdbcService jdbcService = new JdbcService();
        jdbcService.setDataSource(
                "com.mysql.cj.jdbc.Driver",
                "jdbc:mysql://192.168.1.105:3306/xxx_demo?useUnicode=true&characterEncoding=utf8&useSSL=false&autoReconnect=true&allowMultiQueries=true&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true",
                "root",
                "root"
        );

        //获取代理类
        JdbcConsistentHashRepository proxy = InterfaceProxy.getProxy(JdbcConsistentHashRepository.class, p -> jdbcService.proxyExecute(p.method, p.args));

        Long dsId = proxy.selectDsId("sys_visit_record", "0_0");
        System.out.println(dsId);
    }
}