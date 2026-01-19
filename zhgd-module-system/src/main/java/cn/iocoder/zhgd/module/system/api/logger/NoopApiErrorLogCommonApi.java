package cn.iocoder.zhgd.module.system.api.logger;

import cn.iocoder.zhgd.framework.common.biz.infra.logger.ApiErrorLogCommonApi;
import cn.iocoder.zhgd.framework.common.biz.infra.logger.dto.ApiErrorLogCreateReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * ApiErrorLogCommonApi 空实现，避免无 infra 模块时启动失败
 */
@Service
@Slf4j
public class NoopApiErrorLogCommonApi implements ApiErrorLogCommonApi {

    @Override
    public void createApiErrorLog(ApiErrorLogCreateReqDTO createDTO) {
        log.debug("[ApiErrorLog] ignore createApiErrorLog");
    }

}
