package com.luckyone.web.controller;

import com.luckyone.web.common.BaseResponse;
import com.luckyone.web.common.ErrorCode;
import com.luckyone.web.common.ResultUtils;
import com.luckyone.web.exception.BusinessException;
import com.luckyone.web.service.GeneratorLikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
@Slf4j
public class GeneratorLikeController {

    @Resource
    private GeneratorLikeService generatorLikeService;

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
}
