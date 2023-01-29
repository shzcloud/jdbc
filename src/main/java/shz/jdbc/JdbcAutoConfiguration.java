package shz.jdbc;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
class JdbcAutoConfiguration {
    @Bean
    @ConditionalOnProperty(name = "jdbc.service", havingValue = "true", matchIfMissing = true)
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnMissingBean(JdbcService.class)
    JdbcService jdbcService(DataSource dataSource) {
        JdbcService jdbcService = new JdbcService();
        jdbcService.setDataSource(dataSource);
        return jdbcService;
    }
}
