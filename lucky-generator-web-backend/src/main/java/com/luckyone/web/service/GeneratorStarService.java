package com.luckyone.web.service;

import com.luckyone.web.model.entity.GeneratorStar;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


public interface GeneratorStarService extends IService<GeneratorStar> {

    Integer starGenerator(Long id, List<Long> bookIds, HttpServletRequest request);
}
