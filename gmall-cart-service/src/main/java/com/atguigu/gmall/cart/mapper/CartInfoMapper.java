package com.atguigu.gmall.cart.mapper;

import com.atguigu.gmall.bean.CartInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {

    /**
     * 根据用户Id查询购物车数据
     * @param userId
     * @return
     */
    List<CartInfo> selectCartListWithCurPrice(@Param("userId") String userId);
}
