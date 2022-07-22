package com.xxxx.seckill.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author kanghaiquan
 * @since 2022-06-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_order")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 商品id
     */
    private Long goodsId;

    /**
     * 收货地址ID
     */
    private Long deliveryAddrId;

    /**
     * 冗余过来的商品名称，方便查询的
     */
    private String goodsName;

    /**
     * 订单数量
     */
    private Integer goodsCount;

    /**
     * 商品单价
     */
    private BigDecimal goodsPrice;

    /**
     * 1 pc 2 android 3 ios
     */
    private Integer orderChannel;

    /**
     * 订单状态：0新建未支付、1已支付 2已发货 3已收货 4已退货 5已完成
     */
    private Integer status;

    /**
     * 当单的创建时间
     */
    private Date createDate;

    /**
     * 订单的支付时间
     */
    private Date payDate;


}
