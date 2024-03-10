package com.luckyone.web.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luckyone.web.common.ErrorCode;
import com.luckyone.web.constant.RedisConstant;
import com.luckyone.web.exception.BusinessException;
import com.luckyone.web.mapper.GeneratorMapper;
import com.luckyone.web.mapper.StarBookMapper;
import com.luckyone.web.model.entity.Generator;
import com.luckyone.web.model.entity.GeneratorStar;
import com.luckyone.web.model.entity.StarBook;
import com.luckyone.web.model.entity.User;
import com.luckyone.web.service.GeneratorStarService;
import com.luckyone.web.mapper.GeneratorStarMapper;
import com.luckyone.web.service.UserService;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Service
public class GeneratorStarServiceImpl extends ServiceImpl<GeneratorStarMapper, GeneratorStar>
    implements GeneratorStarService {

    @Resource
    private StarBookMapper starBookMapper;
    
    @Resource
    private UserService userService;
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Resource
    private GeneratorMapper generatorMapper;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public Integer starGenerator(Long id, List<Long> bookIds, HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);

        String cacheKey = RedisConstant.ARTICLE_STAR_PREFIX + id + RedisConstant.ARTICLE_STAR_SUFFIX;
        SetOperations<String, String> operations = stringRedisTemplate.opsForSet();
        Boolean isStared = operations.isMember(cacheKey, currentUser.getId().toString());
        //1. 取消收藏
        if(isStared && CollectionUtils.isEmpty(bookIds)){
            cancelStar(id, currentUser, cacheKey, operations);
            return 0;
        } else {
            //2. 新收藏或更改收藏
            //2.1 校验收藏夹参数
            //首先将所有收藏夹查出来
            List<StarBook> starBooks = starBookMapper.selectBatchIds(bookIds);
            //2.1.1 某个收藏夹不存在
            if(starBooks.size() != bookIds.size()){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"收藏夹不存在");
            }
            //2.1.2 接着检查这些收藏夹是否都属于这个用户
            for (StarBook starBook : starBooks) {
                //某个收藏夹不属于当前用户
                if(!starBook.getCreateBy().equals(currentUser.getId())){
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限查看收藏夹");
                }
            }

            // 更改收藏
            if(isStared) {
                changeStar(id, bookIds, currentUser);
                return 1;
            } else {
                // 新收藏
                newStar(id, bookIds, currentUser, cacheKey, operations);
                return 2;
            }
        }
    }

    private void newStar(Long id, List<Long> bookIds, User currentUser, String cacheKey, SetOperations<String, String> operations) {
        CompletableFuture<Void> future0 = CompletableFuture.runAsync(() -> {
            //2.3 generator表的star_count+1
            generatorMapper.update(null, new LambdaUpdateWrapper<Generator>()
                    .setSql("starCount = starCount + 1").eq(Generator::getId, id));
        }, threadPoolExecutor);

        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            //2.4 generator_star表添加记录
            List<GeneratorStar> generatorStars = bookIdsToGeneratorStars(bookIds, id, currentUser.getId());
            this.saveBatch(generatorStars);
        }, threadPoolExecutor);

        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            //2.5 对应的star_book count+1
            starBookMapper.update(null, new LambdaUpdateWrapper<StarBook>()
                    .setSql("count = count + 1").in(StarBook::getId, bookIds));
        }, threadPoolExecutor);

        CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
            //2.6 redis的set中添加用户
            operations.add(cacheKey, currentUser.getId().toString());
        }, threadPoolExecutor);

        CompletableFuture.allOf(future0, future1, future2, future3);
    }

    private void changeStar(Long id, List<Long> bookIds, User currentUser) {
        //查询当前的所有收藏了该文章的收藏夹id
        Set<Long> curBookIds = lambdaQuery().select(GeneratorStar::getBookId)
                .eq(GeneratorStar::getId, currentUser.getId())
                .eq(GeneratorStar::getGeneratorId, id).list()
                .stream().map(GeneratorStar::getBookId).collect(Collectors.toSet());
        //要修改的收藏夹id
        Set<Long> updateBookIds = new HashSet<>(bookIds);

        //将要删除的：cur中有，update中没有
        Set<Long> remove = (Set<Long>) CollectionUtil.subtract(curBookIds, updateBookIds);
        //将要添加的：update中有，cur中无
        Set<Long> add = (Set<Long>) CollectionUtil.subtract(updateBookIds, curBookIds);

        CompletableFuture<Void> future0 = CompletableFuture.runAsync(() -> {
            //对于要删除的，删除generator_star表中的记录，star_book中的count-1
            if (!remove.isEmpty()) {
                remove(new LambdaQueryWrapper<GeneratorStar>().eq(GeneratorStar::getGeneratorId, id).in(GeneratorStar::getBookId, remove));
                starBookMapper.update(null, new LambdaUpdateWrapper<StarBook>()
                        .setSql("count = count - 1").in(StarBook::getId, remove));
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            //对于要添加的，插入generators_star记录，star_book中的count+1
            if (!add.isEmpty()) {
                List<GeneratorStar> generatorStars = bookIdsToGeneratorStars(add, id, currentUser.getId());
                saveBatch(generatorStars);
                starBookMapper.update(null, new LambdaUpdateWrapper<StarBook>()
                        .setSql("count = count + 1").in(StarBook::getId, add));
            }
        }, threadPoolExecutor);

        CompletableFuture.allOf(future0, future1);
    }

    private void cancelStar(Long id, User currentUser, String cacheKey, SetOperations<String, String> operations) {
        CompletableFuture<Void> future0 = CompletableFuture.runAsync(() -> {
            //1.1 generator表的star_count-1
            generatorMapper.update(null, new LambdaUpdateWrapper<Generator>()
                    .setSql("starCount = starCount - 1").eq(Generator::getId, id));
        }, threadPoolExecutor);

        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            //1.4 redis的set中删除用户
            operations.remove(cacheKey, currentUser.getId().toString());
        }, threadPoolExecutor);

        List<GeneratorStar> generatorStars = lambdaQuery().select(GeneratorStar::getId, GeneratorStar::getBookId)
                .eq(GeneratorStar::getId, currentUser.getId()).eq(GeneratorStar::getGeneratorId, id).list();
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            //1.2 generator_star删除记录
            List<Long> ids = generatorStars.stream().map(GeneratorStar::getId).collect(Collectors.toList());
            removeByIds(ids);
        }, threadPoolExecutor);

        CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
            //1.3 star_book更新count-1
            List<Long> starBookIds = generatorStars.stream().map(GeneratorStar::getBookId).collect(Collectors.toList());
            starBookMapper.update(null, new LambdaUpdateWrapper<StarBook>()
                    .setSql("count = count - 1").in(StarBook::getId, starBookIds));
        }, threadPoolExecutor);

        CompletableFuture.allOf(future0, future1, future2, future3).join();
    }

    private List<GeneratorStar> bookIdsToGeneratorStars(Collection<Long> bookIds, Long generatorId, Long uid){
        List<GeneratorStar> generatorStars = bookIds.stream().map(bookId -> {
            GeneratorStar generatorStar = new GeneratorStar();
            generatorStar.setGeneratorId(generatorId);
            generatorStar.setBookId(bookId);
            generatorStar.setCreateBy(uid);
            return generatorStar;
        }).collect(Collectors.toList());
        return generatorStars;
    }
}




