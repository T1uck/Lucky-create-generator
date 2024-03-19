package com.luckyone.web.job;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luckyone.web.constant.RedisConstant;
import com.luckyone.web.manager.CacheManager;
import com.luckyone.web.manager.CosManager;
import com.luckyone.web.mapper.GeneratorMapper;
import com.luckyone.web.model.entity.Generator;
import com.luckyone.web.service.GeneratorService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ClearCosJobHandler {
    @Resource
    private CosManager cosManager;

    @Resource
    private GeneratorMapper generatorMapper;

    @Resource
    private GeneratorService generatorService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private CacheManager cacheManager;

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

    /**
     * 更新文章的score和hot值，小时一次
     */
    @XxlJob("updateScoreAndHot")
    public void updateScoreAndHotJob() {
        RLock lock = redissonClient.getLock(RedisConstant.ASYNC_SCORE_LOCK);
        try {
            log.info("updateScoreAndHot start =========>");
            if (lock.tryLock(0, 30, TimeUnit.SECONDS)) {
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
