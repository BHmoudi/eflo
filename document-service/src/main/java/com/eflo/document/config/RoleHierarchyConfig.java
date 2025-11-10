package com.eflo.document.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

@Configuration
public class RoleHierarchyConfig {

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(String.join("\n",
                "ROLE_SUPER_ADMIN > ROLE_ADMIN",
                "ROLE_SUPER_ADMIN > ROLE_ADMIN_LOCAL",
                "ROLE_SUPER_ADMIN > ROLE_SALES_MANAGER",
                "ROLE_SUPER_ADMIN > ROLE_SALESPERSON",
                "ROLE_SUPER_ADMIN > ROLE_VIEWER",
                "ROLE_SUPER_ADMIN > ROLE_DOCUMENT_MANAGER",
                "ROLE_SUPER_ADMIN > ROLE_DOCUMENT_UPLOADER",
                "ROLE_SUPER_ADMIN > ROLE_VALIDATOR",
                "ROLE_SUPER_ADMIN > ROLE_DOCUMENT_VALIDATOR",
                "ROLE_SUPER_ADMIN > ROLE_ANALYTICS_VIEWER",
                "ROLE_SUPER_ADMIN > ROLE_COMPLIANCE_OFFICER",
                "ROLE_SUPER_ADMIN > ROLE_SYSTEM_IMPORT",
                "ROLE_SUPER_ADMIN > ROLE_EXTERNAL_API_READ",
                "ROLE_SUPER_ADMIN > ROLE_EXTERNAL_API_WRITE",
                "ROLE_SUPER_ADMIN > ROLE_USER",
                "ROLE_SUPER_ADMIN > ROLE_SECRETARY",
                "ROLE_SUPER_ADMIN > ROLE_ACCOUNTANT"
        ));
        return hierarchy;
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        return handler;
    }
}

