package cn.iocoder.zhgd.framework.web.config;

import cn.pinming.core.cookie.AuthUserHelper;
import cn.pinming.core.cookie.AuthUser;
import cn.pinming.core.cookie.support.AuthUserHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 当前登录用户拦截器自动配置
 */
@AutoConfiguration
public class AuthUserAutoConfiguration implements WebMvcConfigurer {

    @Resource
    private AuthUserHelper authUserHelper;
    @Resource
    @Qualifier("siteContextHolder")
    private AuthUserHolder authUserHolder;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        org.slf4j.LoggerFactory.getLogger(AuthUserAutoConfiguration.class)
                .info("[AuthUser] register interceptor: PinmingAuthUserHandlerInterceptor");
        registry.addInterceptor(new PinmingAuthUserHandlerInterceptor(authUserHelper, authUserHolder))
                .addPathPatterns("/**")
                .excludePathPatterns("/actuator/**", "/login/**", "/static/**");
    }

    @AutoConfiguration
    static class AuthUserComponentConfiguration {

        @Value("${cookie.domain:}")
        private String cookieDomain;

        @org.springframework.context.annotation.Bean
        public AuthUserHelper authUserHelper() {
            AuthUserHelper helper = new AuthUserHelper();
            helper.setDomain(cookieDomain);
            org.slf4j.LoggerFactory.getLogger(AuthUserAutoConfiguration.class)
                    .info("[AuthUser] AuthUserHelper initialized, cookie.domain={}", cookieDomain);
            return helper;
        }
    }

    @Slf4j
    static class PinmingAuthUserHandlerInterceptor implements HandlerInterceptor {
        private final AuthUserHelper authUserHelper;
        private final AuthUserHolder authUserHolder;
        private final AtomicBoolean loggedMethods = new AtomicBoolean(false);

        PinmingAuthUserHandlerInterceptor(AuthUserHelper authUserHelper, AuthUserHolder authUserHolder) {
            this.authUserHelper = authUserHelper;
            this.authUserHolder = authUserHolder;
        }

        @Override
        public boolean preHandle(jakarta.servlet.http.HttpServletRequest request,
                                 jakarta.servlet.http.HttpServletResponse response,
                                 Object handler) {
            logAuthUserHelperMethodsOnce();
            log.info("[AuthUser] preHandle uri={}, tokenPresent={}, cookies={}",
                    request.getRequestURI(),
                    hasToken(request),
                    getCookieNames(request));
            AuthUser authUser = resolveAuthUser(request);
            if (authUser != null) {
                authUserHolder.setCurrentUser(authUser);
                setRequestLoginUserId(request, authUser);
                log.info("[AuthUser] resolved authUserId={}, class={}, uri={}",
                        authUser.getId(),
                        authUser.getClass().getName(),
                        request.getRequestURI());
            } else {
                log.info("[AuthUser] resolve failed, uri={}, tokenPresent={}, cookies={}",
                        request.getRequestURI(),
                        hasToken(request),
                        getCookieNames(request));
            }
            return true;
        }

        @Override
        public void afterCompletion(jakarta.servlet.http.HttpServletRequest request,
                                    jakarta.servlet.http.HttpServletResponse response,
                                    Object handler,
                                    Exception ex) {
            authUserHolder.removeCurrentUser();
        }

        private AuthUser resolveAuthUser(jakarta.servlet.http.HttpServletRequest request) {
            if (authUserHelper == null) {
                log.debug("[AuthUser] authUserHelper is null");
                return null;
            }
            // 1. 优先使用 token 解析（避免 javax/jakarta 兼容问题）
            AuthUser authUser = resolveAuthUserByToken(request);
            if (authUser != null) {
                log.debug("[AuthUser] resolved by token");
                return authUser;
            }
            // 2. 尝试用 request 解析
            authUser = resolveAuthUserByRequest(request);
            if (authUser != null) {
                log.debug("[AuthUser] resolved by request");
                return authUser;
            }
            return null;
        }

        private AuthUser resolveAuthUserByToken(jakarta.servlet.http.HttpServletRequest request) {
            String token = getCookieValue(request, "userToken");
            if (token == null) {
                token = getCookieValue(request, "_site3_f_ue_");
            }
            if (token == null) {
                token = request.getHeader("userToken");
            }
            if (token == null) {
                token = request.getHeader("_site3_f_ue_");
            }
            if (token == null) {
                return null;
            }
            String decodedToken = decodeToken(token);
            log.debug("[AuthUser] token length: raw={}, decoded={}", token.length(), decodedToken.length());
            for (java.lang.reflect.Method method : authUserHelper.getClass().getMethods()) {
                if (method.getParameterCount() == 1
                        && method.getParameterTypes()[0] == String.class
                        && AuthUser.class.isAssignableFrom(method.getReturnType())) {
                    try {
                        log.info("[AuthUser] try token method={}", method.getName());
                        AuthUser authUser = (AuthUser) method.invoke(authUserHelper, decodedToken);
                        if (authUser == null) {
                            log.info("[AuthUser] token parse returned null, method={}", method.getName());
                            continue;
                        }
                        return authUser;
                    } catch (Exception ex) {
                        log.info("[AuthUser] token parse error, method={}", method.getName(), ex);
                        continue;
                    }
                }
            }
            return null;
        }

        private void logAuthUserHelperMethodsOnce() {
            if (!loggedMethods.compareAndSet(false, true)) {
                return;
            }
            try {
                for (java.lang.reflect.Method method : authUserHelper.getClass().getMethods()) {
                    if (AuthUser.class.isAssignableFrom(method.getReturnType())) {
                        log.info("[AuthUser] helper method: {}({})",
                                method.getName(),
                                java.util.Arrays.toString(method.getParameterTypes()));
                    }
                }
            } catch (Exception ex) {
                log.info("[AuthUser] list helper methods failed", ex);
            }
        }

        private String decodeToken(String token) {
            if (token == null) {
                return null;
            }
            if (token.indexOf('%') < 0 && token.indexOf('+') < 0) {
                return token;
            }
            try {
                return URLDecoder.decode(token, StandardCharsets.UTF_8);
            } catch (Exception ex) {
                log.debug("[AuthUser] token decode failed", ex);
                return token;
            }
        }

        private AuthUser resolveAuthUserByRequest(jakarta.servlet.http.HttpServletRequest request) {
            for (java.lang.reflect.Method method : authUserHelper.getClass().getMethods()) {
                if (method.getParameterCount() == 1
                        && AuthUser.class.isAssignableFrom(method.getReturnType())) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    // jakarta servlet
                    if (jakarta.servlet.http.HttpServletRequest.class.isAssignableFrom(paramType)) {
                        try {
                            log.info("[AuthUser] try request(jakarta) method={}", method.getName());
                            AuthUser authUser = (AuthUser) method.invoke(authUserHelper, request);
                            if (authUser == null) {
                                log.info("[AuthUser] request(jakarta) returned null, method={}", method.getName());
                                continue;
                            }
                            return authUser;
                        } catch (Exception ex) {
                            log.info("[AuthUser] request(jakarta) error, method={}", method.getName(), ex);
                            continue;
                        }
                    }
                    // javax servlet
                    if ("javax.servlet.http.HttpServletRequest".equals(paramType.getName())) {
                        Object javaxRequest = createJavaxRequestProxy(request);
                        if (javaxRequest == null) {
                            return null;
                        }
                        try {
                            log.info("[AuthUser] try request(javax) method={}", method.getName());
                            AuthUser authUser = (AuthUser) method.invoke(authUserHelper, javaxRequest);
                            if (authUser == null) {
                                log.info("[AuthUser] request(javax) returned null, method={}", method.getName());
                                continue;
                            }
                            return authUser;
                        } catch (Exception ex) {
                            log.info("[AuthUser] request(javax) error, method={}", method.getName(), ex);
                            continue;
                        }
                    }
                    // javax cookie array
                    if (paramType.isArray()
                            && "javax.servlet.http.Cookie".equals(paramType.getComponentType().getName())) {
                        Object javaxCookies = createJavaxCookies(request);
                        if (javaxCookies == null) {
                            return null;
                        }
                        try {
                            log.info("[AuthUser] try cookie(javax) method={}", method.getName());
                            AuthUser authUser = (AuthUser) method.invoke(authUserHelper, javaxCookies);
                            if (authUser == null) {
                                log.info("[AuthUser] cookie(javax) returned null, method={}", method.getName());
                                continue;
                            }
                            return authUser;
                        } catch (Exception ex) {
                            log.info("[AuthUser] cookie(javax) error, method={}", method.getName(), ex);
                            continue;
                        }
                    }
                }
            }
            return null;
        }

        private String getCookieValue(jakarta.servlet.http.HttpServletRequest request, String name) {
            jakarta.servlet.http.Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                return null;
            }
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
            return null;
        }

        private boolean hasToken(jakarta.servlet.http.HttpServletRequest request) {
            return getCookieValue(request, "userToken") != null
                    || getCookieValue(request, "_site3_f_ue_") != null
                    || request.getHeader("userToken") != null
                    || request.getHeader("_site3_f_ue_") != null;
        }

        private String getCookieNames(jakarta.servlet.http.HttpServletRequest request) {
            jakarta.servlet.http.Cookie[] cookies = request.getCookies();
            if (cookies == null || cookies.length == 0) {
                return "[]";
            }
            StringBuilder builder = new StringBuilder("[");
            for (int i = 0; i < cookies.length; i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(cookies[i].getName());
            }
            builder.append("]");
            return builder.toString();
        }

        private void setRequestLoginUserId(jakarta.servlet.http.HttpServletRequest request, AuthUser authUser) {
            try {
                if (authUser != null && cn.hutool.core.util.StrUtil.isNotBlank(authUser.getId())) {
                    cn.iocoder.zhgd.framework.web.core.util.WebFrameworkUtils
                            .setLoginUserId(request, authUser.getId());
                }
            } catch (Exception ignored) {
                // ignore
            }
        }

        private Object createJavaxRequestProxy(jakarta.servlet.http.HttpServletRequest request) {
            try {
                Class<?> javaxRequestClass = Class.forName("javax.servlet.http.HttpServletRequest");
                return java.lang.reflect.Proxy.newProxyInstance(
                        javaxRequestClass.getClassLoader(),
                        new Class<?>[]{javaxRequestClass},
                        (proxy, method, args) -> {
                            String methodName = method.getName();
                            if ("getCookies".equals(methodName)) {
                                return createJavaxCookies(request);
                            }
                            if ("getHeader".equals(methodName) && args != null && args.length == 1) {
                                return request.getHeader(String.valueOf(args[0]));
                            }
                            if ("getParameter".equals(methodName) && args != null && args.length == 1) {
                                return request.getParameter(String.valueOf(args[0]));
                            }
                            if ("getRequestURI".equals(methodName)) {
                                return request.getRequestURI();
                            }
                            if ("getMethod".equals(methodName)) {
                                return request.getMethod();
                            }
                            return null;
                        });
            } catch (Exception ignored) {
                return null;
            }
        }

        private Object createJavaxCookies(jakarta.servlet.http.HttpServletRequest request) {
            try {
                Class<?> javaxCookieClass = Class.forName("javax.servlet.http.Cookie");
                jakarta.servlet.http.Cookie[] cookies = request.getCookies();
                if (cookies == null) {
                    return java.lang.reflect.Array.newInstance(javaxCookieClass, 0);
                }
                Object array = java.lang.reflect.Array.newInstance(javaxCookieClass, cookies.length);
                for (int i = 0; i < cookies.length; i++) {
                    Object javaxCookie = javaxCookieClass
                            .getConstructor(String.class, String.class)
                            .newInstance(cookies[i].getName(), cookies[i].getValue());
                    java.lang.reflect.Array.set(array, i, javaxCookie);
                }
                return array;
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
