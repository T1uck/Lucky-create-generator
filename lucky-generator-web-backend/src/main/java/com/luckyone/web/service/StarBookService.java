package com.luckyone.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luckyone.web.model.dto.star.StarBookQueryRequest;
import com.luckyone.web.model.entity.StarBook;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luckyone.web.model.vo.GeneratorVO;
import com.luckyone.web.model.vo.star.StarBookBoolVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


public interface StarBookService extends IService<StarBook> {

    List<StarBookBoolVo> getStarBooks(Long generatorId, HttpServletRequest request);

    List<StarBookBoolVo> getStarBooksById(Long id);

    Long createStarBook(String name, HttpServletRequest request);

    Page<GeneratorVO> getGeneratorsInStarBook(StarBookQueryRequest starBookQueryRequest);
}
