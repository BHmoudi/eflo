package com.eflo.document.config;

import com.eflo.document.web.filter.RequestResponseLoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Configuration class for registering servlet filters.
 * Registers the RequestResponseLoggingFilter with highest priority.
 */
@Configuration
public class FilterConfig {

    /**
     * Registers the RequestResponseLoggingFilter with the servlet container.
     * This filter will log all incoming HTTP requests and outgoing responses.
     *
     * @param loggingFilter the RequestResponseLoggingFilter instance
     * @return FilterRegistrationBean configured with the logging filter
     */
    @Bean
    public FilterRegistrationBean<RequestResponseLoggingFilter> loggingFilter(
            RequestResponseLoggingFilter loggingFilter) {

        FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean =
            new FilterRegistrationBean<>();

        registrationBean.setFilter(loggingFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registrationBean.setName("requestResponseLoggingFilter");

        return registrationBean;
    }
}
