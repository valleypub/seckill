package com.xxxx.seckill.vo;


import com.xxxx.seckill.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailVo {
    private User user;
    private GoodsVo goodsVo;
    private int secKillStatus;
    private int remainSeconds;
}
