package com.study.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.study.reggie.common.BaseContext;
import com.study.reggie.common.R;
import com.study.reggie.entity.ShoppingCart;
import com.study.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){

        log.info("shoppingCart:{}",shoppingCart);
        //目的：添加购物车时，将数据存储到shoppingCart表中
        //1.获取当前登录用户Id，封装进shoppingCart
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        //2.判断当前是菜品还是套餐
        Long dishId = shoppingCart.getDishId();
        //条件构造器，通过条件查询
        LambdaQueryWrapper<ShoppingCart> queryWrapper = Wrappers.lambdaQuery(ShoppingCart.class);
        queryWrapper.eq(ShoppingCart::getUserId,shoppingCart.getUserId());//保证是登录用户
        //进一步构造查询条件，根据当前用户id和dishId或setmealId唯一确定一个对象，
        if (dishId!=null){
            //说明添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            //添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //查询当前菜品或者套餐是否存在购物车中,返回ShoppingCart对象
        ShoppingCart shoppingCartServiceOne = shoppingCartService.getOne(queryWrapper);
        if (shoppingCartServiceOne!=null){
            //说明数据库存在菜品或者套餐，直接在原数据的基础上number+1
            Integer number = shoppingCartServiceOne.getNumber();
            //执行数据库更新操作
            shoppingCartServiceOne.setNumber(number+1);
            shoppingCartService.updateById(shoppingCartServiceOne);
        }else {
            //数据库不存在该数据对象，执行添加操作，且初始值为1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            shoppingCartServiceOne=shoppingCart;
        }
        //shoppingCartServiceOne与数据库数据保持一致
        return R.success(shoppingCartServiceOne);
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车...");
        //根据用户Id进行查询
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        //排序，后添加的在上面显示
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return R.success(list);
    }
    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        //SQL:delete from shopping_cart where user_id = ?
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }
}
