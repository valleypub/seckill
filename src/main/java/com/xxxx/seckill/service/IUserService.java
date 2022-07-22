package com.xxxx.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.vo.LoginVo;
import com.xxxx.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author kanghaiquan
 * @since 2022-06-20
 */
public interface IUserService extends IService<User> {

    RespBean doLogin(LoginVo loginVo,
                     HttpServletRequest request, HttpServletResponse response);

    /**
     * 根据Cookie获取用户
     * @param userTicket
     * @return
     */
    User getUserByCookies(String userTicket,
                          HttpServletRequest request, HttpServletResponse response);


    /**
     * 用户更新密码
     * @param userTicket
     * @param password
     * @param request
     * @param response
     * @return
     */
    public RespBean updatePassword(String userTicket, String password,
                                   HttpServletRequest request, HttpServletResponse response);
}
