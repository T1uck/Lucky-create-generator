package com.luckyone.web.job;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luckyone.web.common.ErrorCode;
import com.luckyone.web.config.CacheConfig;
import com.luckyone.web.constant.RedisConstant;
import com.luckyone.web.exception.BusinessException;
import com.luckyone.web.manager.CacheManager;
import com.luckyone.web.manager.CosManager;
import com.luckyone.web.model.entity.Generator;
import com.luckyone.web.service.GeneratorService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class GetHotAndScore {

    @Resource
    private CosManager cosManager;

    @Resource
    private GeneratorService generatorService;

    @Resource
    private CacheManager cacheManager;

    @Resource
    private RedissonClient redissonClient;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 更新文章的score和hot值，小时一次
     */
    @XxlJob("updateScoreAndHot")
    public void updateScoreAndHotJob() {
        RLock lock = redissonClient.getLock(RedisConstant.ASYNC_SCORE_LOCK);
        try {
            if (lock.tryLock(0, 30, TimeUnit.SECONDS)) {
                log.info("updateScoreAndHot---------- {}", simpleDateFormat.format(new Date()));
                // 首先获取所有文章(只获取id,score,hot)
                List<Generator> generators = generatorService.lambdaQuery()
                        .select(Generator::getViewCount, Generator::getLikeCount, Generator::getStarCount, Generator::getCommentCount,
                                Generator::getId, Generator::getScore, Generator::getHot)
                        .list();
                for (Generator generator : generators) {
                    // score = 浏览量 + 点赞量*8 + 收藏量*16 + 评论量*8
                    int newScore = (int) (generator.getViewCount() + (generator.getLikeCount() << 3) + (generator.getStarCount() << 4) + (generator.getCommentCount() << 3));
                    // hot = hot*0.9+newScore-ScoreOld
                    int newHot = (int) (generator.getHot() * 0.9 + newScore - generator.getScore());
                    generator.setScore(newScore);
                    generator.setHot(newHot);


                }
                generatorService.updateBatchById(generators);

                // 将hot前8的文章缓存起来
                Page<Generator> page = generatorService.lambdaQuery().orderBy(true, false, Generator::getHot).page(new Page<>(1, 8));
                cacheManager.put(RedisConstant.HOT_ARTICLES, page);
                // 将hot前8的压缩包缓存
                List<Generator> generatorList = page.getRecords();
                for (Generator generator : generatorList) {
                    // 缓存空间
                    String distPath = generator.getDistPath();
                    String zipFilePath = CacheConfig.getCacheFilePath(generator.getId(), distPath);
                    try {
                        cosManager.download(distPath, zipFilePath);
                    } catch (Exception e) {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成器下载失败");
                    }
                }
            }
        } catch (Exception e) {
            log.error("更新热点文章失败！", e.getMessage());
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
