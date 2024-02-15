package com.luckyone.web.config;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.json.JSONUtil;
import com.luckyone.web.model.dto.generator.GeneratorQueryRequest;
import org.springframework.context.annotation.Configuration;

/**
 * 获取缓存操作
 */
@Configuration
public class CacheConfig {

    /**
     * 获取缓存文件路径
     * @param id
     * @param distPath
     * @return
     */
    public static String getCacheFilePath(long id, String distPath) {
        String projectPath = System.getProperty("user.dir");
        String tempDirPath = String.format("%s/.temp/cache/%s", projectPath, id);
        return tempDirPath + "/" + distPath;
    }

    /**
     * 获取分页缓存 key
     * @param generatorQueryRequest
     * @return
     */
    public static String getPageCacheKey(GeneratorQueryRequest generatorQueryRequest) {
        String jsonStr = JSONUtil.toJsonStr(generatorQueryRequest);
        String base64 = Base64Encoder.encode(jsonStr);
        String key = "generator:page:" + base64;
        return key;
    }

}
