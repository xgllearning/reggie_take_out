package com.study.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.reggie.entity.ShoppingCart;
import com.study.reggie.mapper.ShoppingCartMapper;
import com.study.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
