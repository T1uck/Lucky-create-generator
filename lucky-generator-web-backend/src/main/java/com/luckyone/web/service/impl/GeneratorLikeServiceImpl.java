package com.luckyone.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luckyone.web.common.ErrorCode;
import com.luckyone.web.constant.RedisConstant;
import com.luckyone.web.exception.BusinessException;
import com.luckyone.web.mapper.GeneratorMapper;
import com.luckyone.web.model.dto.like.GeneratorLikeQueryRequest;
import com.luckyone.web.model.dto.like.LikedCountDTO;
import com.luckyone.web.model.entity.Generator;
import com.luckyone.web.model.entity.GeneratorLike;
import com.luckyone.web.model.entity.User;
import com.luckyone.web.service.GeneratorLikeService;
import com.luckyone.web.mapper.GeneratorLikeMapper;
import com.luckyone.web.service.GeneratorService;
import com.luckyone.web.service.RedisLikeService;
import com.luckyone.web.service.UserService;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class GeneratorLikeServiceImpl extends ServiceImpl<GeneratorLikeMapper, GeneratorLike>
    implements GeneratorLikeService {

    @Resource
    private GeneratorMapper generatorMapper;

    @Resource
    private GeneratorService generatorService;

    @Resource
    private RedisLikeService redisLikeService;

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void likeGenerator(Long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求id不存在！");
        }
        // 获取当前生成器信息
        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"找不到id对应的生成器数据");
        }
        try {
            // 将生成器类点赞数 + 1
            Long likeCount = generator.getLikeCount();
            generator.setLikeCount(likeCount + 1);
            generatorService.updateById(generator);
            // 获取当前登陆用户
            User loginUser = userService.getLoginUser(request);
            // 将当前用户和当前生成器id传入生成器点赞表中
            GeneratorLike generatorLike = new GeneratorLike();
            generatorLike.setGeneratorId(id);
            generatorLike.setCreateBy(loginUser.getId());
            this.save(generatorLike);
        }
        catch (Exception error) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"保存数据库失败！");
        }
    }


    /**
     * id点赞authorId的articleId
     * @param id
     * @param generatorId
     * @param authorId
     */
    @Transactional
    @Override
    public void likeGenerator(Long id, Long generatorId, Long authorId) {
        String likeCacheKey = RedisConstant.ARTICLE_LIKE_PREFIX + generatorId + RedisConstant.ARTICLE_LIKE_SUFFIX;
        String likeMsgCacheKey = RedisConstant.NOTIFICATION_PREFIX + authorId + RedisConstant.LIKE_NOTIFICATION_SUFFIX;

        SetOperations<String, String> setOperations = stringRedisTemplate.opsForSet();
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();

        Boolean isLiked = setOperations.isMember(likeCacheKey, id.toString());
        //1.已经点赞了，取消
        if(isLiked){
            CompletableFuture.runAsync(() -> {
                //1.1 article表的like_count-1
                generatorMapper.update(null, new LambdaUpdateWrapper<Generator>()
                        .setSql("like_count = like_count - 1").eq(Generator::getId, generatorId));
            }, threadPoolExecutor);

            //todo: pipeline优化
            CompletableFuture.runAsync(() -> {
                //1.2 redis的set中删除用户
                setOperations.remove(likeCacheKey, id.toString());
            }, threadPoolExecutor);

            CompletableFuture.runAsync(() -> {
                //1.3 article_like表删除记录
                remove(new LambdaQueryWrapper<GeneratorLike>()
                        .eq(GeneratorLike::getId, id)
                        .eq(GeneratorLike::getGeneratorId, generatorId));
            }, threadPoolExecutor);

            CompletableFuture.runAsync(() -> {
                //1.4 redis中点赞消息数-1
                if(!id.equals(authorId)){
                    valueOperations.decrement(likeMsgCacheKey);
                }
            }, threadPoolExecutor);
        } else {
            //todo: 异步编排优化
            //2. 没点赞，添加
            CompletableFuture.runAsync(() -> {
                //2.1 article表的like_count+1
                generatorMapper.update(null, new LambdaUpdateWrapper<Generator>()
                        .setSql("like_count = like_count + 1").eq(Generator::getId, generatorId));
            }, threadPoolExecutor);

            CompletableFuture.runAsync(() -> {
                //2.2 redis的set中添加用户
                setOperations.add(likeCacheKey, id.toString());
            }, threadPoolExecutor);

            CompletableFuture.runAsync(() -> {
                //2.3 增加记录
                GeneratorLike articleLike = new GeneratorLike();
                articleLike.setGeneratorId(generatorId);
                articleLike.setId(id);
                save(articleLike);
            }, threadPoolExecutor);

            CompletableFuture.runAsync(() -> {
                //2.4 redis中点赞消息+1
                if(!id.equals(authorId)){
                    valueOperations.increment(likeMsgCacheKey);
                }
            }, threadPoolExecutor);
        }
    }

    @Override
    public boolean save(GeneratorLike generatorLike) {
        return this.save(generatorLike);
    }

    @Override
    @Transactional
    public List<GeneratorLike> saveAll(List<GeneratorLike> list) {
        return this.saveAll(list);
    }

    @Override
    public Page<GeneratorLike> getLikedListByLikedUserId(GeneratorLikeQueryRequest generatorLikeQueryRequest) {
        long current = generatorLikeQueryRequest.getCurrent();
        long pageSize = generatorLikeQueryRequest.getPageSize();
        Page<GeneratorLike> page = lambdaQuery().eq(GeneratorLike::getCreateBy, generatorLikeQueryRequest.getCreateBy()).page(new Page<>(current, pageSize));
        return page;
    }

    @Override
    public Page<GeneratorLike> getLikedListByLikedGeneratorId(GeneratorLikeQueryRequest generatorLikeQueryRequest) {
        long current = generatorLikeQueryRequest.getCurrent();
        long pageSize = generatorLikeQueryRequest.getPageSize();
        Page<GeneratorLike> page = lambdaQuery().eq(GeneratorLike::getGeneratorId, generatorLikeQueryRequest.getGeneratorId()).page(new Page<>(current, pageSize));
        return page;
    }

    @Override
    public GeneratorLike getByLikedGeneratorIdAndLikedUserId(Long likedGeneratorId, Long likedUserId) {
        LambdaQueryWrapper<GeneratorLike> LambdaQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper.eq(GeneratorLike::getGeneratorId, likedGeneratorId)
                .eq(GeneratorLike::getCreateBy, likedUserId);
        return getOne(LambdaQueryWrapper);
    }

    /**
     * 将Redis里的点赞数据存入数据库中
     */
    @Override
    @Transactional
    public void transLikedFromRedis2DB() {
        List<GeneratorLike> list = redisLikeService.getLikedDataFromRedis();
        for (GeneratorLike like : list) {
            GeneratorLike ul = getByLikedGeneratorIdAndLikedUserId(like.getGeneratorId(), like.getCreateBy());
            if (ul == null){
                //没有记录，直接存入
                save(like);
            }else{
                //有记录，需要更新
                ul.setStatus(like.getStatus());
                save(ul);
            }
        }
    }

    /**
     * 将Redis中的点赞数量数据存入数据库
     */
    @Override
    @Transactional
    public void transLikedCountFromRedis2DB() {
        List<LikedCountDTO> list = redisLikeService.getLikedCountFromRedis();
        for (LikedCountDTO dto : list) {
            Generator generator = generatorService.getById(dto.getId());
            //点赞数量属于无关紧要的操作，出错无需抛异常
            if (generator != null){
                Long likeNum = (long) Math.toIntExact(generator.getLikeCount() + dto.getCount());
                generator.setLikeCount(likeNum);
                //更新点赞数量
                generatorService.updateById(generator);
            }
        }
    }
}




