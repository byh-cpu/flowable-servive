package cn.iocoder.zhgd.module.system.convert.auth;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.zhgd.framework.common.util.object.BeanUtils;
import cn.iocoder.zhgd.module.system.api.sms.dto.code.SmsCodeSendReqDTO;
import cn.iocoder.zhgd.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import cn.iocoder.zhgd.module.system.api.social.dto.SocialUserBindReqDTO;
import cn.iocoder.zhgd.module.system.controller.admin.auth.vo.*;
import cn.iocoder.zhgd.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.iocoder.zhgd.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.zhgd.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.zhgd.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.zhgd.module.system.enums.permission.MenuTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.slf4j.LoggerFactory;

import java.util.*;

import static cn.iocoder.zhgd.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.zhgd.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.zhgd.module.system.dal.dataobject.permission.MenuDO.ID_ROOT;

@Mapper
public interface AuthConvert {

    AuthConvert INSTANCE = Mappers.getMapper(AuthConvert.class);

    // 关键修复：显式指定字段映射，适配@Builder注解的VO
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "accessToken", target = "accessToken")
    @Mapping(source = "refreshToken", target = "refreshToken")
    @Mapping(source = "expiresTime", target = "expiresTime")
    AuthLoginRespVO convert(OAuth2AccessTokenDO bean);

    // 以下原有代码不变
    default AuthPermissionInfoRespVO convert(AdminUserDO user, List<RoleDO> roleList, List<MenuDO> menuList) {
        return AuthPermissionInfoRespVO.builder()
                .user(BeanUtils.toBean(user, AuthPermissionInfoRespVO.UserVO.class))
                .roles(convertSet(roleList, RoleDO::getCode))
                .permissions(convertSet(menuList, MenuDO::getPermission))
                .menus(buildMenuTree(menuList))
                .build();
    }

    AuthPermissionInfoRespVO.MenuVO convertTreeNode(MenuDO menu);

    default List<AuthPermissionInfoRespVO.MenuVO> buildMenuTree(List<MenuDO> menuList) {
        if (CollUtil.isEmpty(menuList)) {
            return Collections.emptyList();
        }
        menuList.removeIf(menu -> menu.getType().equals(MenuTypeEnum.BUTTON.getType()));
        menuList.sort(Comparator.comparing(MenuDO::getSort));

        Map<Long, AuthPermissionInfoRespVO.MenuVO> treeNodeMap = new LinkedHashMap<>();
        menuList.forEach(menu -> treeNodeMap.put(menu.getId(), AuthConvert.INSTANCE.convertTreeNode(menu)));
        treeNodeMap.values().stream().filter(node -> !node.getParentId().equals(ID_ROOT)).forEach(childNode -> {
            AuthPermissionInfoRespVO.MenuVO parentNode = treeNodeMap.get(childNode.getParentId());
            if (parentNode == null) {
                LoggerFactory.getLogger(getClass()).error("[buildRouterTree][resource({}) 找不到父资源({})]",
                        childNode.getId(), childNode.getParentId());
                return;
            }
            if (parentNode.getChildren() == null) {
                parentNode.setChildren(new ArrayList<>());
            }
            parentNode.getChildren().add(childNode);
        });
        return filterList(treeNodeMap.values(), node -> ID_ROOT.equals(node.getParentId()));
    }

    SocialUserBindReqDTO convert(Long userId, Integer userType, AuthSocialLoginReqVO reqVO);

    SmsCodeSendReqDTO convert(AuthSmsSendReqVO reqVO);

    SmsCodeUseReqDTO convert(AuthSmsLoginReqVO reqVO, Integer scene, String usedIp);

}