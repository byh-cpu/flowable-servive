package cn.iocoder.zhgd.framework.web.core.user;

import cn.pinming.core.cookie.AuthUser;
import cn.pinming.core.cookie.support.AuthUserHolder;
import cn.pinming.core.web.exception.UnauthorizedException;
import org.springframework.stereotype.Component;

/**
 * 当前登录用户上下文
 */
@Component("siteContextHolder")
public class SiteContextHolder implements AuthUserHolder {

    private final ThreadLocal<AuthUser> authUserThreadLocal = new ThreadLocal<>();

    public AuthUser getNonNullCurrentUser() {
        AuthUser currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedException();
        }
        return currentUser;
    }

    @Override
    public AuthUser getCurrentUser() {
        return authUserThreadLocal.get();
    }

    @Override
    public void setCurrentUser(AuthUser user) {
        authUserThreadLocal.set(user);
    }

    @Override
    public void removeCurrentUser() {
        authUserThreadLocal.remove();
    }
}
