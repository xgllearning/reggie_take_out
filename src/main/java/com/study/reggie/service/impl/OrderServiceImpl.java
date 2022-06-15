package com.study.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.study.reggie.common.BaseContext;
import com.study.reggie.common.CustomException;
import com.study.reggie.common.R;
import com.study.reggie.entity.*;
import com.study.reggie.mapper.OrderMapper;
import com.study.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;
    /**
     * 用户下单
     * @param orders
     */
    public R<String> saveOrdersAndOrdersDetail(Orders orders) {
        //1.先获取当前登录用户Id
        Long currentId = BaseContext.getCurrentId();
        //2.根据登录用户Id查询当前用户的购物车信息,需要shoppingCart表
        LambdaQueryWrapper<ShoppingCart> queryWrapper = Wrappers.lambdaQuery(ShoppingCart.class);
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);

        if (shoppingCarts==null || shoppingCarts.size()==0){
            throw new CustomException("购物车为空,不能下单");
        }

        //3.1封装订单信息,先通过雪花算法生成订单ID
        long orderId = IdWorker.getId();
        //3.2获取用户信息
        User user = userService.getById(currentId);
        //3.3获取地址信息--先通过orders获取地址信息Id,再根据id查询地址详情
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);

        if (addressBook==null){
            throw new CustomException("用户地址信息有误，不能下单");
        }
        //3.4 组装订单明细信息，在订单明细表中需要计算amount，通过原子操作AtomicInteger--多个订单明细
        AtomicInteger amount=new AtomicInteger();
        List<OrderDetail> orderDetailList = shoppingCarts.stream().map(s -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setName(s.getName());
            orderDetail.setImage(s.getImage());
            orderDetail.setDishId(s.getDishId());
            orderDetail.setSetmealId(s.getSetmealId());
            orderDetail.setDishFlavor(s.getDishFlavor());
            orderDetail.setNumber(s.getNumber());
            orderDetail.setAmount(s.getAmount());
            amount.addAndGet(s.getAmount().multiply(new BigDecimal(s.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        //组装订单数据
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(currentId);
        orders.setNumber(String.valueOf(orderId));//将订单id转为number
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        //向订单表插入订单数据
        this.save(orders);

        //向订单明细表插入数据
        orderDetailService.saveBatch(orderDetailList);

        //清空购物车
        shoppingCartService.remove(queryWrapper);
        return R.success("下单成功");
    }
}