package cn.iocoder.zhgd.framework.web.config;

import cn.pinming.core.cookie.AuthUserHelper;
import cn.pinming.core.cookie.support.AuthUserHolder;

import cn.pinming.core.cookie.support.spring.AuthUserInterceptor;
import cn.pinming.core.web.exception.UnauthorizedException;
import jakarta.annotation.Resource;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 当前登录用户拦截器自动配置
 */
@AutoConfiguration
public class AuthUserAutoConfiguration implements WebMvcConfigurer {

    @Resource
    private AuthUserInterceptor authUserInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DelegatingHandlerInterceptor(authUserInterceptor))
                .addPathPatterns("/**")
                .excludePathPatterns("/actuator/**", "/login/**", "/static/**");
    }

    @AutoConfiguration
    static class AuthUserComponentConfiguration {

        @Value("${cookie.domain:}")
        private String cookieDomain;

        @Resource
        @Qualifier("siteContextHolder")
        private AuthUserHolder authUserHolder;

        @org.springframework.context.annotation.Bean
        public AuthUserHelper authUserHelper() {
            AuthUserHelper helper = new AuthUserHelper();
            helper.setDomain(cookieDomain);
            return helper;
        }

        @org.springframework.context.annotation.Bean
        public AuthUserInterceptor authUserInterceptor(AuthUserHelper authUserHelper) {
            return new AuthUserInterceptor(authUserHolder, authUserHelper, errorMessage -> {
                if (errorMessage == null) {
                    throw new UnauthorizedException();
                }
                throw new UnauthorizedException(errorMessage);
            });
        }
    }

    static class DelegatingHandlerInterceptor implements HandlerInterceptor {
        private final Object delegate;
        private final java.lang.reflect.Method preHandle;
        private final java.lang.reflect.Method postHandle;
        private final java.lang.reflect.Method afterCompletion;

        DelegatingHandlerInterceptor(Object delegate) {
            this.delegate = delegate;
            this.preHandle = findMethod("preHandle", jakarta.servlet.http.HttpServletRequest.class,
                    jakarta.servlet.http.HttpServletResponse.class, Object.class);
            this.postHandle = findMethod("postHandle", jakarta.servlet.http.HttpServletRequest.class,
                    jakarta.servlet.http.HttpServletResponse.class, Object.class,
                    org.springframework.web.servlet.ModelAndView.class);
            this.afterCompletion = findMethod("afterCompletion", jakarta.servlet.http.HttpServletRequest.class,
                    jakarta.servlet.http.HttpServletResponse.class, Object.class, Exception.class);
        }

        private java.lang.reflect.Method findMethod(String name, Class<?>... parameterTypes) {
            try {
                return delegate.getClass().getMethod(name, parameterTypes);
            } catch (NoSuchMethodException ex) {
                return null;
            }
        }

        @Override
        public boolean preHandle(jakarta.servlet.http.HttpServletRequest request,
                                 jakarta.servlet.http.HttpServletResponse response,
                                 Object handler) throws Exception {
            if (preHandle == null) {
                return true;
            }
            Object result;
            try {
                result = preHandle.invoke(delegate, request, response, handler);
            } catch (java.lang.reflect.InvocationTargetException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof Exception) {
                    throw (Exception) cause;
                }
                throw new RuntimeException(cause);
            }
            return !(result instanceof Boolean) || (Boolean) result;
        }

        @Override
        public void postHandle(jakarta.servlet.http.HttpServletRequest request,
                               jakarta.servlet.http.HttpServletResponse response,
                               Object handler,
                               org.springframework.web.servlet.ModelAndView modelAndView) throws Exception {
            if (postHandle == null) {
                return;
            }
            try {
                postHandle.invoke(delegate, request, response, handler, modelAndView);
            } catch (java.lang.reflect.InvocationTargetException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof Exception) {
                    throw (Exception) cause;
                }
                throw new RuntimeException(cause);
            }
        }

        @Override
        public void afterCompletion(jakarta.servlet.http.HttpServletRequest request,
                                    jakarta.servlet.http.HttpServletResponse response,
                                    Object handler,
                                    Exception ex) throws Exception {
            if (afterCompletion == null) {
                return;
            }
            try {
                afterCompletion.invoke(delegate, request, response, handler, ex);
            } catch (java.lang.reflect.InvocationTargetException invokeEx) {
                Throwable cause = invokeEx.getCause();
                if (cause instanceof Exception) {
                    throw (Exception) cause;
                }
                throw new RuntimeException(cause);
            }
        }
    }
}
