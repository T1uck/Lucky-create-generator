package com.luckyone.web.service;

import com.luckyone.web.model.dto.comment.PostCommentRequest;
import com.luckyone.web.model.entity.GeneratorComment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luckyone.web.model.dto.comment.ChildrenCommentVo;
import com.luckyone.web.model.dto.comment.RootCommentVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


public interface GeneratorCommentService extends IService<GeneratorComment> {

    List<RootCommentVo> getRootCommentsOfGenerator(Long id);

    List<ChildrenCommentVo> getChildrenOfRoot(Long id);

    void likeComment(Long id);

    void publishComment(PostCommentRequest postCommentRequest, HttpServletRequest request);
}
