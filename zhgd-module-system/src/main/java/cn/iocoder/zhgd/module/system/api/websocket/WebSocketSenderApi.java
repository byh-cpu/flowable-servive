package cn.iocoder.zhgd.module.system.api.websocket;

/**
 * WebSocket 消息发送 API
 *
 * @author 芋道源码
 */
public interface WebSocketSenderApi {

    /**
     * 发送消息给指定用户类型
     *
     * @param userType 用户类型
     * @param messageType 消息类型
     * @param message 消息内容
     */
    void sendObject(Integer userType, String messageType, Object message);

}
