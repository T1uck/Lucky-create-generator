package com.luckyone.web.controller;

import com.luckyone.web.common.BaseResponse;
import com.luckyone.web.common.ErrorCode;
import com.luckyone.web.common.ResultUtils;
import com.luckyone.web.exception.BusinessException;
import com.luckyone.web.model.dto.comment.PostCommentRequest;
import com.luckyone.web.model.dto.comment.ChildrenCommentVo;
import com.luckyone.web.model.dto.comment.RootCommentVo;
import com.luckyone.web.service.GeneratorCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/comment")
@Slf4j
public class GeneratorCommentController {

    @Resource
    private GeneratorCommentService generatorCommentService;

    /**
     * 获取某个生成器的根评论
     * @param id 生成器id
     * @return
     */
    @GetMapping("/root")
    public BaseResponse<List<RootCommentVo>> getRootCommentsOfGenerator(Long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<RootCommentVo> rootCommentsOfGenerator = generatorCommentService.getRootCommentsOfGenerator(id);
        return ResultUtils.success(rootCommentsOfGenerator);
    }

    /**
     * 获取某个根评论下的子评论，后期可以优化成分页，就像B站那样
     * @param id 根评论id
     * @return
     */
    @GetMapping("/children")
    public BaseResponse<List<ChildrenCommentVo>> getChildrenOfRoot(Long id){
        List<ChildrenCommentVo> vos = generatorCommentService.getChildrenOfRoot(id);
        return ResultUtils.success(vos);
    }

    /**
     * 点赞评论
     * @param id: 评论id
     * @return
     */
    @PostMapping("/like")
    public BaseResponse<Boolean> likeComment(Long id){
        generatorCommentService.likeComment(id);
        return ResultUtils.success(true);
    }

    /**
     * 发表评论
     * @param postCommentRequest
     * @param request
     * @return
     */
    @PostMapping
    public BaseResponse<Boolean> publishComment(@Valid @RequestBody PostCommentRequest postCommentRequest, HttpServletRequest request){
        generatorCommentService.publishComment(postCommentRequest, request);
        return ResultUtils.success(true);
    }
}
