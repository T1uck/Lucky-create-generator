package com.luckyone.web.controller.generator;

import com.luckyone.web.common.BaseResponse;
import com.luckyone.web.common.ErrorCode;
import com.luckyone.web.common.ResultUtils;
import com.luckyone.web.exception.BusinessException;
import com.luckyone.web.model.dto.like.GeneratorLikeQueryRequest;
import com.luckyone.web.model.entity.GeneratorLike;
import com.luckyone.web.service.GeneratorLikeService;
import com.luckyone.web.service.RedisLikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
@Slf4j
public class GeneratorLikeController {

    @Resource
    private GeneratorLikeService generatorLikeService;

    @Resource
    private RedisLikeService redisLikeService;

    /**
     * 根据生成器id点赞
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/generator/like/{id}")
    public BaseResponse<Boolean> likeGenerator(@PathVariable("id") Long id , HttpServletRequest request){
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求数据不存在！");
        }
        generatorLikeService.likeGenerator(id, request);
        return ResultUtils.success(true);
    }

    /**
     * 点赞操作,点赞状态持续化在redis中
     * @param generatorLikeQueryRequest
     * @return
     */
    @PostMapping("/generator/like")
    public BaseResponse<Boolean> likeGeneratorRedis(GeneratorLikeQueryRequest generatorLikeQueryRequest) {
        Long generatorId = generatorLikeQueryRequest.getGeneratorId();
        Long userId = generatorLikeQueryRequest.getCreateBy();
        // 先把数据存到Redis里，在定时存回数据
        redisLikeService.saveLikeGenerator(generatorId, userId);
        redisLikeService.incrementLikedCount(generatorId);
        return ResultUtils.success(true);
    }

    /**
     * 取消点赞操作
     * @param generatorLikeQueryRequest
     * @return
     */
    @PostMapping("/generator/unlike")
    public BaseResponse<Boolean> unlikeGeneratorRedis(GeneratorLikeQueryRequest generatorLikeQueryRequest) {
        Long generatorId = generatorLikeQueryRequest.getGeneratorId();
        Long userId = generatorLikeQueryRequest.getCreateBy();
        // 先把数据存到Redis里，在定时存回数据
        redisLikeService.saveUnLikeGenerator(generatorId, userId);
        redisLikeService.decrementLikedCount(generatorId);
        return ResultUtils.success(true);
    }

    /**
     * 根据生成器id和用户id获取当前点赞状态
     * @param generatorId
     * @param userId
     * @return
     */
    @GetMapping("/generator/likeStatus")
    public BaseResponse<Boolean> getLikeGeneratorStatus(Long generatorId,Long userId) {
        GeneratorLike generatorLike = generatorLikeService.getByLikedGeneratorIdAndLikedUserId(generatorId, userId);
        return ResultUtils.success(generatorLike != null);
    }

    @PostMapping("/generator/likeCount")
    public BaseResponse<Long> getGeneratorLikeCount() {
        // todo: 获取点赞数

        return null;
    }

    @PostMapping("/generator")
    public BaseResponse<Boolean> TestRedisToMySql() {
        // 将 reids 里的点赞信息同步到数据库中
        generatorLikeService.transLikedFromRedis2DB();
        generatorLikeService.transLikedCountFromRedis2DB();
        return ResultUtils.success(true);
    }
}
