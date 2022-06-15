package com.study.reggie.controller;


import com.study.reggie.common.R;
import com.study.reggie.entity.Orders;
import com.study.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    /**
     * 提交订单操作
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("orders:{}",orders);
        //需要保存订单(orders、ordersDetail)、清空购物车、根据菜品或者套餐计算价格
        return orderService.saveOrdersAndOrdersDetail(orders);
    }
}