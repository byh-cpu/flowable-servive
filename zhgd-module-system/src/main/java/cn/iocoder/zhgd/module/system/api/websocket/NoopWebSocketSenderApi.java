package cn.iocoder.zhgd.module.system.api.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * WebSocketSenderApi 空实现，避免无 WebSocket 模块时启动失败
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class NoopWebSocketSenderApi implements WebSocketSenderApi {

    @Override
    public void sendObject(Integer userType, String messageType, Object message) {
        log.debug("[WebSocket] ignore sendObject userType={}, messageType={}", userType, messageType);
    }

}
