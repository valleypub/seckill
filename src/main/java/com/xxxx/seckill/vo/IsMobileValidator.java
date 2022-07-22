package com.xxxx.seckill.vo;

import com.xxxx.seckill.utils.ValidationUtil;
import com.xxxx.seckill.validator.isMobile;
import org.thymeleaf.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsMobileValidator implements ConstraintValidator<isMobile, String> {

    private boolean required = false;

    @Override
    public void initialize(isMobile constraintAnnotation) {
        //初始化时先获取这个required flag用来表示这个值是否非必填，后面的isValid方法需要根据这个来进行不同的逻辑处理
        required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if(required){
            return ValidationUtil.isMobile(value);
        }
        else{
            if(StringUtils.isEmpty(value)){
                return true; //因为是非必填
            }else{
                return  ValidationUtil.isMobile(value);//如果是非必填，却填了，那也是要校验一下的
            }
        }
    }
}
