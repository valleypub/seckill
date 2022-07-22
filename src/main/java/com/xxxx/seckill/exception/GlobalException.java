package com.xxxx.seckill.exception;


import com.xxxx.seckill.vo.RespBeanEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

//
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalException extends RuntimeException{

    private RespBeanEnum respBeanEnum;


}
