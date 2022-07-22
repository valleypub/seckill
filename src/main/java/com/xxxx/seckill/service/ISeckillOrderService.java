package com.xxxx.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckill.pojo.SeckillOrder;
import com.xxxx.seckill.pojo.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author kanghaiquan
 * @since 2022-06-23
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {

    Long getResult(User user, Long goodsId);
}
