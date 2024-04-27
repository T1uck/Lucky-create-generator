package com.luckyone.web.mapper;

import com.luckyone.web.model.entity.Generator;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* 针对表【generator(代码生成器)】的数据库操作Mapper
*/
public interface GeneratorMapper extends BaseMapper<Generator> {

    @Select("SELECT id, distPath from generator_db.generator where isDelete = 1")
    List<Generator> listDeleteGenerator();
}




