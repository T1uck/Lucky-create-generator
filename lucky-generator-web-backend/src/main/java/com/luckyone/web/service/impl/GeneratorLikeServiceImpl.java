package com.luckyone.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luckyone.web.common.ErrorCode;
import com.luckyone.web.exception.BusinessException;
import com.luckyone.web.model.entity.Generator;
import com.luckyone.web.model.entity.GeneratorLike;
import com.luckyone.web.model.entity.User;
import com.luckyone.web.service.GeneratorLikeService;
import com.luckyone.web.mapper.GeneratorLikeMapper;
import com.luckyone.web.service.GeneratorService;
import com.luckyone.web.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Service
public class GeneratorLikeServiceImpl extends ServiceImpl<GeneratorLikeMapper, GeneratorLike>
    implements GeneratorLikeService {

    @Resource
    private GeneratorService generatorService;

    @Resource
    private UserService userService;

    @Override
    public void likeGenerator(Long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求id不存在！");
        }
        // 获取当前生成器信息
        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"找不到id对应的生成器数据");
        }
        try {
            // 将生成器类点赞数 + 1
            Long likeCount = generator.getLikeCount();
            generator.setLikeCount(likeCount + 1);
            generatorService.updateById(generator);
            // 获取当前登陆用户
            User loginUser = userService.getLoginUser(request);
            // 将当前用户和当前生成器id传入生成器点赞表中
            GeneratorLike generatorLike = new GeneratorLike();
            generatorLike.setGeneratorId(id);
            generatorLike.setCreateBy(loginUser.getId());
            this.save(generatorLike);
        }
        catch (Exception error) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"保存数据库失败！");
        }
    }
}




