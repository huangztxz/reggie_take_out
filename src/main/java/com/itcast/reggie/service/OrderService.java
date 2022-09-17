package com.itcast.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itcast.reggie.entity.Orders;

public interface OrderService extends IService<Orders> {
    void submit(Orders orders);
}
