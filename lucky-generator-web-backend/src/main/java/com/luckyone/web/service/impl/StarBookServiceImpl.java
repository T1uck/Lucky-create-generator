package com.luckyone.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luckyone.web.common.BeanCopyUtils;
import com.luckyone.web.mapper.GeneratorStarMapper;
import com.luckyone.web.model.dto.star.StarBookQueryRequest;
import com.luckyone.web.model.entity.GeneratorStar;
import com.luckyone.web.model.entity.StarBook;
import com.luckyone.web.model.entity.User;
import com.luckyone.web.model.vo.GeneratorVO;
import com.luckyone.web.model.vo.star.StarBookBoolVo;
import com.luckyone.web.service.GeneratorService;
import com.luckyone.web.service.StarBookService;
import com.luckyone.web.mapper.StarBookMapper;
import com.luckyone.web.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StarBookServiceImpl extends ServiceImpl<StarBookMapper, StarBook>
    implements StarBookService{

    @Resource
    private GeneratorStarMapper generatorStarMapper;
    @Resource
    private GeneratorService generatorService;

    @Resource
    private UserService userService;

    @Override
    public List<StarBookBoolVo> getStarBooks(Long generatorId, HttpServletRequest request) {
        // 获取当前用户
        User currentUser = userService.getLoginUser(request);

        //首先查询出所有的收藏夹
        List<StarBookBoolVo> vos = lambdaQuery()
                .select(StarBook::getId, StarBook::getName, StarBook::getCount)
                .eq(StarBook::getCreateBy, currentUser.getId()).list()
                .stream().map(starBook -> BeanCopyUtils.copyBean(starBook, StarBookBoolVo.class))
                .collect(Collectors.toList());

        if(generatorId.equals(-1L)){
            return vos;
        }

        List<Long> bookIds = vos.stream().map(StarBookBoolVo::getId).collect(Collectors.toList());
        if(bookIds.isEmpty()){
            return new ArrayList<>();
        }
        //查询收藏记录
        Set<Long> containBookIds = generatorStarMapper.selectList(new LambdaQueryWrapper<GeneratorStar>().select(GeneratorStar::getBookId)
                        .eq(GeneratorStar::getGeneratorId, generatorId).in(GeneratorStar::getBookId, bookIds))
                .stream().map(GeneratorStar::getBookId).collect(Collectors.toSet());

        for (StarBookBoolVo vo : vos) {
            if (containBookIds.contains(vo.getId())){
                vo.setIsContain(true);
            }
        }
        return vos;
    }

    @Override
    public List<StarBookBoolVo> getStarBooksById(Long id) {
        return lambdaQuery()
                .select(StarBook::getId, StarBook::getName, StarBook::getCount)
                .eq(StarBook::getCreateBy, id).list()
                .stream().map(starBook -> BeanCopyUtils.copyBean(starBook, StarBookBoolVo.class))
                .collect(Collectors.toList());
    }

    @Override
    public Long createStarBook(String name, HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        StarBook starBook = new StarBook();
        starBook.setName(name);
        starBook.setCreateBy(currentUser.getId());
        save(starBook);
        return starBook.getId();
    }

    @Override
    public Page<GeneratorVO> getGeneratorsInStarBook(StarBookQueryRequest starBookQueryRequest) {
        long pageNum = starBookQueryRequest.getCurrent();
        long pageSize = starBookQueryRequest.getPageSize();
        Long bookId = starBookQueryRequest.getBookId();

        //数据库中的数据除了访问量其他数据都可以确保是最新的
        //1. 构造查询条件
        QueryWrapper<GeneratorStar> queryWrapper = new QueryWrapper<GeneratorStar>()
                .select("generatorId")
                .eq("bookId", bookId)
                .orderBy(true, false, "createTime");

        //2. 查询数据库中的信息
        long start = System.currentTimeMillis();
        Page<GeneratorStar> articleLikePage = generatorStarMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        long end = System.currentTimeMillis();

        //3. 转换为vos
        List<Long> generatorIds = articleLikePage.getRecords().stream().map(GeneratorStar::getGeneratorId).collect(Collectors.toList());
        List<GeneratorVO> vos = generatorService.getGeneratorsByIds(generatorIds, null);
        Page<GeneratorVO> generatorVoPage = new Page<>(pageNum, pageSize, articleLikePage.getTotal());
        generatorVoPage.setRecords(vos);
        return generatorVoPage;
    }
}




