package com.xxxx.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.seckill.mapper.GoodsMapper;
import com.xxxx.seckill.pojo.Goods;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author kanghaiquan
 * @since 2022-06-23
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    @Autowired
    //操作Goods表相关的数据库操作
    private GoodsMapper goodsMapper;

    @Override
    public List<GoodsVo> findGoodsVo() {

        return goodsMapper.findGoodsVo();
    }

    @Override
    public GoodsVo findGoodsVoByGoodsId(Long goodsId) {

        return goodsMapper.findGoodsVoByGoodsId(goodsId);
    }
}
