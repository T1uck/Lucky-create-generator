package com.luckyone.web.controller;


import com.luckyone.web.common.BaseResponse;
import com.luckyone.web.common.ErrorCode;
import com.luckyone.web.common.ResultUtils;
import com.luckyone.web.exception.BusinessException;
import com.luckyone.web.service.GeneratorStarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/")
@Slf4j
@Validated
public class GeneratorStarController {

    @Resource
    private GeneratorStarService generatorStarService;

    /**
     * 收藏生成器
     * @param id
     * @param bookIds
     * @param request
     * @return
     */
    @PostMapping("/generator/{id}/star")
    public BaseResponse<Integer> starGenerator(@PathVariable("id") Long id, @RequestBody List<Long> bookIds, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"获取不到id");
        }
        if (bookIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"获取不到收藏夹");
        }
        Integer result = generatorStarService.starGenerator(id, bookIds, request);
        return ResultUtils.success(result);
    }
}
