package com.luckyone.maker.meta;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;

public class MetaManager {

    private static volatile Meta meta;

    public static Meta getMetaObject(){
        if (meta == null) {
            synchronized (MetaManager.class) {
                if (meta == null) {
                    meta = initMeta();
                }
            }
        }
        return meta;
    }

    // 初始化 Meta，在获取到数据后，利用JSONUtil。toBean将字符串转换为对象
    private static Meta initMeta() {
        String metaJson = ResourceUtil.readUtf8Str("meta.json");
//        String metaJson = ResourceUtil.readUtf8Str("springboot-init-meta.json");
        Meta newMeta = JSONUtil.toBean(metaJson, Meta.class);
        // 校验和处理默认值
        MetaValidator.doValidAndFill(newMeta);
        return newMeta;
    }
}
