package com.xxxx.seckill.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.xxxx.seckill.pojo.Goods;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsVo extends Goods{

    //秒杀相关的
    private BigDecimal seckillPrice;
    private Integer stockCount;
    private Date startDate;
    private Date endDate;

}
