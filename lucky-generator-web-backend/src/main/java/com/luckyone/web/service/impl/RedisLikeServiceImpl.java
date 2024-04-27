package com.luckyone.web.service.impl;

import cn.hutool.core.util.StrUtil;
import com.luckyone.web.common.ErrorCode;
import com.luckyone.web.common.IMResponse;
import com.luckyone.web.exception.BusinessException;
import com.luckyone.web.im.IMServer;
import com.luckyone.web.model.dto.like.LikedCountDTO;
import com.luckyone.web.model.entity.Generator;
import com.luckyone.web.model.entity.GeneratorLike;
import com.luckyone.web.model.enums.LikedStatusEnum;
import com.luckyone.web.service.GeneratorService;
import com.luckyone.web.service.MsgUnreadService;
import com.luckyone.web.service.RedisLikeService;
import com.luckyone.web.service.UserService;
import com.luckyone.web.utils.RedisKeyUtils;
import com.luckyone.web.utils.RedisUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.Date;

@Service
@Slf4j
public class RedisLikeServiceImpl implements RedisLikeService {

    @Resource
    private UserService userService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private GeneratorService generatorService;

    @Resource
    private MsgUnreadService msgUnreadService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 使用redis存储点赞信息，点赞
     * @param likedGeneratorId
     * @param likedUserId
     */
    public void saveLikeGenerator(Long likedGeneratorId, Long likedUserId){
        String likedKey = RedisKeyUtils.getLikedKey(likedGeneratorId, likedUserId);
        // 判断 redis 中是否存在点赞信息
        Object key= stringRedisTemplate.opsForHash().get(RedisKeyUtils.MAP_KEY_USER_LIKED, likedKey);
        // 点赞抛出
        if (Objects.equals((String) key, LikedStatusEnum.LIKE.getCode())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"已经点赞成功，Redis中存在点赞信息");
        }
        stringRedisTemplate.opsForHash().put(RedisKeyUtils.MAP_KEY_USER_LIKED, likedKey, LikedStatusEnum.LIKE.getCode());
        // 通知生成器作者被赞了
        CompletableFuture.runAsync(() -> {
            // 查询作者id
            Generator generator = generatorService.getById(likedGeneratorId);
            // 如过作者id和登陆id相同则不通知
            if (!Objects.equals(generator.getUserId(), likedUserId)) {
                // 更新最新被点赞的生成器
                redisUtil.zset("be_loved-zset:" + generator.getUserId(),likedGeneratorId);
                msgUnreadService.addOneUnread(Math.toIntExact(generator.getUserId()),"love");
                // netty 通知未读消息
                HashMap<String, Object> map = new HashMap<>();
                map.put("type", "接收");
                Set<Channel> channels = IMServer.userChannel.get(Math.toIntExact(generator.getUserId()));
                if (channels != null) {
                    for (Channel channel : channels) {
                        channel.writeAndFlush(IMResponse.message("love", map));
                    }
                }
            }
        }, threadPoolExecutor);
    }

    /**
     * 使用redis存储点赞信息，取消点赞
     * @param likedGeneratorId
     * @param likedUserId
     */
    public void saveUnLikeGenerator(Long likedGeneratorId,Long likedUserId){
        String likedKey = RedisKeyUtils.getLikedKey(likedGeneratorId, likedUserId);
        // 判断 redis 中是否存在点赞信息
        Object key= stringRedisTemplate.opsForHash().get(RedisKeyUtils.MAP_KEY_USER_LIKED, likedKey);
        // 点赞抛出
        if (key == LikedStatusEnum.UNLIKE.getCode() && key == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"不需要取消点赞,该用户未点赞过");
        }
        stringRedisTemplate.opsForHash().put(RedisKeyUtils.MAP_KEY_USER_LIKED, likedKey, LikedStatusEnum.UNLIKE.getCode());
    }

    /**
     * 从Redis中删除一条点赞数据
     * @param likedGeneratorId
     * @param likedUserId
     */
    @Override
    public void deleteLikedFromRedis(Long likedGeneratorId, Long likedUserId) {
        String likedKey = RedisKeyUtils.getLikedKey(likedGeneratorId, likedUserId);
        // 是否存在点赞信息
        Boolean hasKey = stringRedisTemplate.opsForHash().hasKey(RedisKeyUtils.MAP_KEY_USER_LIKED, likedKey);
        if (!hasKey) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"Redis中不存在点赞数据");
        }
        stringRedisTemplate.opsForHash().delete(RedisKeyUtils.MAP_KEY_USER_LIKED,likedKey);
    }

    /**
     * 该生成器的点赞数加1
     * @param likedGeneratorId
     */
    @Override
    public void incrementLikedCount(Long likedGeneratorId) {
        String string = String.valueOf(likedGeneratorId);
        stringRedisTemplate.opsForHash().increment(RedisKeyUtils.MAP_KEY_USER_LIKED_COUNT, string, 1);
    }

    /**
     * 该生成器的点赞数减1
     * @param likedGeneratorId
     */
    @Override
    public void decrementLikedCount(Long likedGeneratorId) {
        String string = String.valueOf(likedGeneratorId);
        Object key = stringRedisTemplate.opsForHash().get(RedisKeyUtils.MAP_KEY_USER_LIKED_COUNT, string);
        if (key == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"未点赞过，不需要减少点赞数");
        }
        stringRedisTemplate.opsForHash().increment(RedisKeyUtils.MAP_KEY_USER_LIKED_COUNT, string, -1);
    }

    /**
     * 获取Redis中存储的所有点赞数据
     * @return
     */
    @Override
    public List<GeneratorLike> getLikedDataFromRedis() {
        Cursor<Map.Entry<Object, Object>> scan = stringRedisTemplate.opsForHash().scan(RedisKeyUtils.MAP_KEY_USER_LIKED, ScanOptions.NONE);
        ArrayList<GeneratorLike> list = new ArrayList<>();
        while (scan.hasNext()) {
            Map.Entry<Object, Object> entry = scan.next();
            String key = (String) entry.getKey();
            // 分离出 likedGeneratorId , likedUserId
            String[] split = key.split("::");
            String likedGeneratorId = split[0];
            String likedUserId = split[1];
            Integer value = Integer.valueOf((String) entry.getValue());

            // 组装成 GeneratorLike 对象
            GeneratorLike generatorLike = new GeneratorLike();
            generatorLike.setGeneratorId(Long.valueOf(likedGeneratorId));
            generatorLike.setCreateBy(Long.valueOf(likedUserId));
            generatorLike.setStatus(value);
            list.add(generatorLike);

            // 存到 list 后从 Redis 中删除
            stringRedisTemplate.opsForHash().delete(RedisKeyUtils.MAP_KEY_USER_LIKED, key);
        }
        return list;
    }

    /**
     * 获取Redis中存储的所有点赞数量
     * @return
     */
    @Override
    public List<LikedCountDTO> getLikedCountFromRedis() {
        Cursor<Map.Entry<Object, Object>> cursor = stringRedisTemplate.opsForHash().scan(RedisKeyUtils.MAP_KEY_USER_LIKED_COUNT, ScanOptions.NONE);
        ArrayList<LikedCountDTO> list = new ArrayList<>();
        while (cursor.hasNext()) {
            Map.Entry<Object, Object> map = cursor.next();
            // 将点赞数量存储在 LikedCountDTO 中
            Long key = Long.valueOf((String) map.getKey());
            Integer valueOf = Integer.valueOf((String) map.getValue());
            LikedCountDTO likedCountDTO = new LikedCountDTO(key, valueOf);
            list.add(likedCountDTO);
            // 从 Redis 中删除这条记录
            String stringKey = StrUtil.toString(key);
            stringRedisTemplate.opsForHash().delete(RedisKeyUtils.MAP_KEY_USER_LIKED_COUNT, stringKey);
        }
        return list;
    }
}
