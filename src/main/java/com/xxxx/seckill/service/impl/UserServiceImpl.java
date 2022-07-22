package com.xxxx.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.seckill.exception.GlobalException;
import com.xxxx.seckill.mapper.UserMapper;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IUserService;
import com.xxxx.seckill.utils.CookieUtil;
import com.xxxx.seckill.utils.MD5Util;
import com.xxxx.seckill.utils.UUIDUtil;
import com.xxxx.seckill.utils.ValidationUtil;
import com.xxxx.seckill.vo.LoginVo;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author kanghaiquan
 * @since 2022-06-20
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired //为了使用UserMapper。xml中的
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        //先获取前端发过来的数据
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        //对数据进行判断
        if(StringUtils.isEmpty(mobile)||StringUtils.isEmpty(password)){
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
            //return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }
        if(!ValidationUtil.isMobile(mobile)){
            throw new GlobalException(RespBeanEnum.MOBILE_ERROR);
            //return RespBean.error(RespBeanEnum.MOBILE_ERROR);
        }
        //运行到这里才进行数据库查询：
        User user = userMapper.selectById(mobile);//mobile电话号码就是t_user表的id列
        if(null == user){//没查询到
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
            //return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }
        //运行到这：
        if(!MD5Util.formPassToDBPass(password, user.getSalt()).equals(user.getPassword())){
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
            //return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }

        /*//生成Cookie
        String ticket = UUIDUtil.uuid();
        //将ticket和用户对象存到session里面
        request.getSession().setAttribute(ticket, user);
        CookieUtil.setCookie(request, response, "userTicket", ticket);*/

        //将用户信息存入redis
        String ticket = UUIDUtil.uuid();//这个怎么保证每次生成的都一样的？？？
        redisTemplate.opsForValue().set("user:" + ticket, user);
        //使用ticket生成cookie
        CookieUtil.setCookie(request, response, "userTicket", ticket);

        //运行到这说明：一切正确，返回SUCCESS
        return RespBean.success(ticket); //一定要把ticket传进全参构造中，这个在UsetUtils中需要用到
    }

    @Override
    public User getUserByCookies(String userTicket, HttpServletRequest request, HttpServletResponse response) {
        //判断ticket是否为空
        if(StringUtils.isEmpty(userTicket)){
            return null;
        }
        User user = (User) redisTemplate.opsForValue().get("user:"+ userTicket); //因为存进redis时key是："user:" + ticket
        //判断根据ticket从redis获取的用户对象是否为空
        if(null == user){
            return null;
        }else{
            CookieUtil.setCookie(request, response, "userTicket", userTicket);
        }
        return user;
    }


    /**
     * 用户更新密码
     * @param userTicket
     * @param password
     * @param request
     * @param response
     * @return
     */
    @Override
    //这个没有被用到，只是用来演示数据库和redis的数据一致性
    public RespBean updatePassword(String userTicket, String password,
                                   HttpServletRequest request, HttpServletResponse response) {
        //1.先判断原用户存不存在
        User user = getUserByCookies(userTicket, request, response);
        if(null == user){
            //不存在，直接抛出异常给前端显示
            throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXIT);
        }
        //2.若存在，执行密码修改操作
        //二次md5加密后存入数据库
        user.setPassword(MD5Util.inputPassToDBPass(password,user.getSalt()));
        //使用Mybatis的mapper方式操作数据库
        int result = userMapper.updateById(user);
        //每次操作完数据库后，进行清空redis、存入redis操作
        if(1 == result){
            //删除redis
            redisTemplate.delete("user:"+userTicket);
            //不在这里进行redis更新，而是需要下次用户login/toLogin的时候会存进redis中。这个和QQ很像，QQ也是改完密码后，需要重新登陆的
            return  RespBean.success();
        }
        return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_FAIL);
    }


}
