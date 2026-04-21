package com.hongshu.idle.controller;

import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.idle.domain.vo.IdleProductVO;
import com.hongshu.idle.service.IRecommendProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 推荐
 *
 * @author: hongshu
 */
@RestController
@RequestMapping("/es/recommend")
public class RecommendController {

    @Autowired
    private IRecommendProductService recommendProductService;


    /**
     * 推荐商品
     *
     * @param userId 用户ID
     */
    @NoLoginIntercept
    @GetMapping("recommendProduct/{userId}")
    public Result<?> getRecommendProduct(@PathVariable long userId) {
        List<IdleProductVO> productVOList = recommendProductService.getRecommendProduct(userId);
        return Result.ok(productVOList);
    }

}
