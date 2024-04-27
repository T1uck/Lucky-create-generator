package com.luckyone.web.model.dto.chat;


import cn.hutool.json.JSONUtil;
import com.luckyone.web.model.enums.WebSocketCodeEnum;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class Response extends HashMap<String, Object> implements Serializable {
    private static final long serialVersionUID = -5073219672329618243L;

    public static Response initialize(){
        Response response = new Response();
        response.put("code", WebSocketCodeEnum.SUCCESS.getCode());
        response.put("msg", WebSocketCodeEnum.SUCCESS.getMsg());
        return response;
    }

    public static TextWebSocketFrame ok() {
        return new TextWebSocketFrame(JSONUtil.toJsonStr(initialize()));
    }

    public static TextWebSocketFrame ok(Object data){
        Response response = initialize();
        response.put("data", data);
        return new TextWebSocketFrame(JSONUtil.toJsonStr(response));
    }

    public static TextWebSocketFrame error(WebSocketCodeEnum webSocketCodeEnum, String msg){
        return error(webSocketCodeEnum.getCode(), msg);
    }

    public static TextWebSocketFrame error(WebSocketCodeEnum webSocketCodeEnum){
        return error(webSocketCodeEnum.getCode(), webSocketCodeEnum.getMsg());
    }

    public static TextWebSocketFrame error(int code, String msg) {
        Response response = new Response();
        response.put("code", code);
        response.put("msg", msg);
        return new TextWebSocketFrame(JSONUtil.toJsonStr(response));
    }
}
