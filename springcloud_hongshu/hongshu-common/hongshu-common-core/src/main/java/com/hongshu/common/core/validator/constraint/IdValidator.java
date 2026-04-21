package com.hongshu.common.core.validator.constraint;

import com.hongshu.common.core.annotation.IdValid;
import com.hongshu.common.core.constant.Constantss;
import com.hongshu.common.core.utils.StringUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * ID校验器，主要判断是否为空，并且长度是否为32
 *
 * @author: hongshu
 * @date 2019年12月4日22:48:43
 */
public class IdValidator implements ConstraintValidator<IdValid, String> {


    @Override
    public void initialize(IdValid constraintAnnotation) {

    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || StringUtils.isBlank(value) || StringUtils.isEmpty(value.trim()) || value.length() != Constantss.THIRTY_TWO) {
            return false;
        }
        return true;
    }
}
