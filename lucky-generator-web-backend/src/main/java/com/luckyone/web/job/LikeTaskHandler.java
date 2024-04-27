package com.luckyone.web.job;

import com.luckyone.web.constant.RedisConstant;
import com.luckyone.web.service.GeneratorLikeService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Slf4j
public class LikeTaskHandler {

    @Resource
    private GeneratorLikeService generatorLikeService;

    @Resource
    private RedissonClient redissonClient;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 将 Redis 中的数据同步到数据库中
     */
    @XxlJob("UpdateGeneratorLike")
    public void updateGeneratorLikeJob() {
        log.info("LikeTask--------- {}", simpleDateFormat.format(new Date()));
        RLock lock = redissonClient.getLock(RedisConstant.ASYNC_LIKE_LOCK);
        try {
            // 将 reids 里的点赞信息同步到数据库中
            generatorLikeService.transLikedFromRedis2DB();
            generatorLikeService.transLikedCountFromRedis2DB();
        }catch (Exception e) {
            log.error("更新热点文章失败！", e.getMessage());
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }
}
