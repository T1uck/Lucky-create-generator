package com.luckyone.web.job;

import cn.hutool.core.util.StrUtil;
import com.luckyone.web.constant.RedisConstant;
import com.luckyone.web.manager.CosManager;
import com.luckyone.web.mapper.GeneratorMapper;
import com.luckyone.web.model.entity.Generator;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ClearCosJobHandler {
    @Resource
    private CosManager cosManager;

    @Resource
    private GeneratorMapper generatorMapper;

    @Resource
    private RedissonClient redissonClient;



    /**
     * 每天执行清理文件
     *
     * @throws Exception
     */
    @XxlJob("clearCosJobHandler")
    public void clearCosJobHandler() throws Exception {
        RLock lock = redissonClient.getLock(RedisConstant.ASYNC_CLEAR_LOCK);
        try {
            log.info("clearCosJobHandler start =========>");
            // 编写业务逻辑
            // 1.包括用户上传的模版制作文件（generator_make_template）
            cosManager.deleteDir("/generator_make_template/");

            // 2.已删除的代码生成器对应的产物包文件（generator_dist）
            List<Generator> generatorList = generatorMapper.listDeleteGenerator();
            List<String> keyList = generatorList.stream().map(Generator::getDistPath)
                    .filter(StrUtil::isNotBlank)
                    // 移除 '/' 前缀
                    .map(distPath -> distPath.substring(1))
                    .collect(Collectors.toList());

            cosManager.deleteObjects(keyList);
            log.info("cleatCosJobHandler end =========>");
        } catch (Exception e) {
            log.error("清理文件执行器错误", e.getMessage());
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }
}
