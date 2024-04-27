package com.luckyone.web.service;

import com.luckyone.web.model.dto.like.LikedCountDTO;
import com.luckyone.web.model.entity.GeneratorLike;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface RedisLikeService {
    /**
     * 点赞
     * @param likedGeneratorId
     * @param likedUserId
     */
    void saveLikeGenerator(Long likedGeneratorId, Long likedUserId);

    /**
     * 取消点赞
     * @param likedGeneratorId
     * @param likedUserId
     */
    void saveUnLikeGenerator(Long likedGeneratorId,Long likedUserId);

    /**
     * 从Redis中删除一条点赞数据
     * @param likedGeneratorId
     * @param likedUserId
     */
    void deleteLikedFromRedis(Long likedGeneratorId, Long likedUserId);

    /**
     * 该生成器的点赞数加1
     * @param likedGeneratorId
     */
    void incrementLikedCount(Long likedGeneratorId);

    /**
     * 该生成器的点赞数减1
     * @param likedGeneratorId
     */
    void decrementLikedCount(Long likedGeneratorId);

    /**
     * 获取Redis中存储的所有点赞数据
     * @return
     */
    List<GeneratorLike> getLikedDataFromRedis();

    /**
     * 获取Redis中存储的所有点赞数量
     * @return
     */
    List<LikedCountDTO> getLikedCountFromRedis();
}
