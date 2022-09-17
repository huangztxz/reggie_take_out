package com.itcast.reggie.dto;

import com.itcast.reggie.entity.OrderDetail;
import com.itcast.reggie.entity.Orders;
import lombok.Data;
import java.util.List;

@Data
public class OrderDto extends Orders {
    private List<OrderDetail> orderDetails;
}
