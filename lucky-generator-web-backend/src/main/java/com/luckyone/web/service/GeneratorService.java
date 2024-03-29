package com.luckyone.web.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luckyone.web.model.dto.generator.GeneratorQueryRequest;
import com.luckyone.web.model.entity.Generator;
import com.luckyone.web.model.vo.GeneratorVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 帖子服务
 */
public interface GeneratorService extends IService<Generator> {

    /**
     * 校验
     *
     * @param generator
     * @param add
     */
    void validGenerator(Generator generator, boolean add);

    /**
     * 获取查询条件
     *
     * @param generatorQueryRequest
     * @return
     */
    QueryWrapper<Generator> getQueryWrapper(GeneratorQueryRequest generatorQueryRequest);


    /**
     * 获取帖子封装
     *
     * @param generator
     * @param request
     * @return
     */
    GeneratorVO getGeneratorVO(Generator generator, HttpServletRequest request);

    /**
     * 获取生成器封装列表
     * @param generators
     * @param request
     * @return
     */
    List<GeneratorVO> getGeneratorVOS(List<Generator> generators,HttpServletRequest request);

    /**
     * 分页获取帖子封装
     *
     * @param generatorPage
     * @param request
     * @return
     */
    Page<GeneratorVO> getGeneratorVOPage(Page<Generator> generatorPage, HttpServletRequest request);

    /**
     * 根据ids获取生成器
     * @param generatorIds
     * @param request
     * @return
     */
    List<GeneratorVO> getGeneratorsByIds(List<Long> generatorIds, HttpServletRequest request);
}
