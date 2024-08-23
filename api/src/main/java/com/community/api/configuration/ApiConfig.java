/*-
 * #%L
 * Community Demo API
 * %%
 * Copyright (C) 2009 - 2023 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 *
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package com.community.api.configuration;

import org.apache.catalina.connector.Connector;
import org.broadleafcommerce.common.extensibility.context.merge.Merge;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.community.core.config.CoreConfig;
import com.community.core.config.StringFactoryBean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Configuration
@Import({CoreConfig.class, ApiSecurityConfig.class})
public class ApiConfig {

    @Bean
    @ConditionalOnProperty("jmx.app.name")
    public StringFactoryBean blJmxNamingBean() {
        return new StringFactoryBean();
    }

    /**
     * Spring Boot does not support the configuration of both an HTTP connector and an HTTPS connector via properties.
     * In order to have both, we’ll need to configure one of them programmatically (HTTP).
     * Below is the recommended approach according to the Spring docs:
     * {@link https://github.com/spring-projects/spring-boot/blob/1.5.x/spring-boot-docs/src/main/asciidoc/howto.adoc#configure-ssl}
     *
     * @param httpServerPort
     * @return EmbeddedServletContainerFactory
     */
    @Bean
    public TomcatServletWebServerFactory tomcatEmbeddedServletContainerFactory(@Value("${http.server.port:8082}") int httpServerPort) {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(createStandardConnector(httpServerPort));
        return tomcat;
    }

    @Merge("blMergedCacheConfigLocations")
    public List<String> adminOverrideCache() {
        return Collections.singletonList("classpath:bl-override-ehcache.xml");
    }

    private Connector createStandardConnector(int port) {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(port);
        return connector;
    }

    @Bean
    @Primary
    public JavaMailSender blMailSender() {
        // Configuration properties
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // Set your mail server host and port
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        // Set your mail server credentials
        mailSender.setUsername("simranjit.kaur@avisoft.io");
        mailSender.setPassword("uP@8cCe!");

        // Configure additional mail properties if needed
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true"); // Enable to see mail sending logs
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");


        return mailSender;
    }


}
