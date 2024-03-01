package com.luckyone.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luckyone.web.model.entity.StarBook;
import com.luckyone.web.service.StarBookService;
import com.luckyone.web.mapper.StarBookMapper;
import org.springframework.stereotype.Service;

/**
* @author 小飞的电脑
* @description 针对表【star_book(用户收藏夹)】的数据库操作Service实现
* @createDate 2024-03-01 10:46:00
*/
@Service
public class StarBookServiceImpl extends ServiceImpl<StarBookMapper, StarBook>
    implements StarBookService{

}




