package com.luckyone.web.service;

import com.luckyone.web.model.entity.GeneratorLike;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

public interface GeneratorLikeService extends IService<GeneratorLike> {

    void likeGenerator(Long id, HttpServletRequest request);

}
