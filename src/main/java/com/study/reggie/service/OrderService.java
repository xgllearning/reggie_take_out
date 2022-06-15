package com.study.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.reggie.common.R;
import com.study.reggie.entity.Orders;


public interface OrderService extends IService<Orders> {

    R<String> saveOrdersAndOrdersDetail(Orders orders);
}
