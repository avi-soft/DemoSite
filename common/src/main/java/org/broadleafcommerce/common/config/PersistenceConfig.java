//package org.broadleafcommerce.common.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
//import javax.persistence.EntityManagerFactory;
//import javax.sql.DataSource;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//
//@Configuration
//@EnableTransactionManagement
//public class PersistenceConfig {
//
//    @Bean
//    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
//        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
//        emf.setPersistenceUnitName("blPU");  // Ensure this matches your persistence.xml
//        emf.setDataSource(dataSource);
//        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
//        return emf;
//    }
//
//    @Bean
//    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
//        return new JpaTransactionManager(entityManagerFactory);
//    }
//
//    @Bean
//    public DataSource dataSource() {
//        return DataSourceBuilder.create()
//                .url("jdbc:hsqldb:mem:testdb")  // Update this to match your DB connection
//                .username("sa")
//                .password("")
//                .driverClassName("org.hsqldb.jdbc.JDBCDriver")
//                .build();
//    }
//}
//
