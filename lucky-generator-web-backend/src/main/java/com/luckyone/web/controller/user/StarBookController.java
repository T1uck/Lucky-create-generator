package com.luckyone.web.controller.user;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luckyone.web.common.BaseResponse;
import com.luckyone.web.common.ResultUtils;
import com.luckyone.web.model.dto.star.StarBookQueryRequest;
import com.luckyone.web.model.vo.GeneratorVO;
import com.luckyone.web.model.vo.star.StarBookBoolVo;
import com.luckyone.web.service.StarBookService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/starBook")
@Validated
public class StarBookController {
    @Resource
    private StarBookService starBookService;

    /**
     * 获取当前用户的所有收藏夹
     * @param request
     * @return
     */
    @GetMapping("/{generatorId}")
    public BaseResponse<List<StarBookBoolVo>> getStarBooks(@PathVariable("generatorId")Long generatorId, HttpServletRequest request){
        List<StarBookBoolVo> vos = starBookService.getStarBooks(generatorId, request);
        return ResultUtils.success(vos);
    }

    /**
     * 获取某用户的所有收藏夹
     * @return
     */
    @GetMapping("/of/{id}")
    public BaseResponse<List<StarBookBoolVo>> getStarBooksById(@PathVariable("id")Long id){
        List<StarBookBoolVo> vos = starBookService.getStarBooksById(id);
        return ResultUtils.success(vos);
    }

    /**
     * 创建收藏夹
     * @param name
     * @param request
     * @return
     */
    @PostMapping
    public BaseResponse<Long> createStarBook(@RequestParam("name") String name, HttpServletRequest request){
        Long starBookId = starBookService.createStarBook(name, request);
        return ResultUtils.success(starBookId);
    }

    /**
     * 获取某收藏夹下的文章
     * @param starBookQueryRequest
     * @return
     */
    @PostMapping("/generators")
    public BaseResponse<Page<GeneratorVO>> getGeneratorsInStarBook(@RequestBody StarBookQueryRequest starBookQueryRequest){
        Page<GeneratorVO> vos = starBookService.getGeneratorsInStarBook(starBookQueryRequest);
        return ResultUtils.success(vos);
    }
}
