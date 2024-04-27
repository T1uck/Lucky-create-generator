package com.luckyone.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luckyone.web.model.dto.like.GeneratorLikeQueryRequest;
import com.luckyone.web.model.entity.GeneratorLike;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface GeneratorLikeService extends IService<GeneratorLike> {

    void likeGenerator(Long id, HttpServletRequest request);

    void likeGenerator(Long id, Long generatorId, Long authorId);

    /**
     * 保存点赞记录
     *
     * @param generatorLike
     * @return
     */
    boolean save(GeneratorLike generatorLike);

    /**
     * 批量保存或修改
     * @param list
     */
    List<GeneratorLike> saveAll(List<GeneratorLike> list);


    /**
     * 根据被点赞人的id查询点赞列表（即查询都谁给这个人点赞过）
     * @param generatorLikeQueryRequest
     * @return
     */
    Page<GeneratorLike> getLikedListByLikedGeneratorId(GeneratorLikeQueryRequest generatorLikeQueryRequest);

    /**
     * 根据点赞人的id查询点赞列表（即查询这个人都给谁点赞过）
     * @param generatorLikeQueryRequest
     * @return
     */
    Page<GeneratorLike> getLikedListByLikedUserId(GeneratorLikeQueryRequest generatorLikeQueryRequest);

    /**
     * 通过被点赞人和点赞人id查询是否存在点赞记录
     * @param likedGeneratorId
     * @param likedUserId
     * @return
     */
    GeneratorLike getByLikedGeneratorIdAndLikedUserId(Long likedGeneratorId, Long likedUserId);


    /**
     * 将Redis里的点赞数据存入数据库中
     */
    void transLikedFromRedis2DB();

    /**
     * 将Redis中的点赞数量数据存入数据库
     */
    void transLikedCountFromRedis2DB();
}
