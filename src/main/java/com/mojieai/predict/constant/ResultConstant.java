package com.mojieai.predict.constant;

/**
 * Created by Singal
 */
public class ResultConstant {
    /* 通用结果*/
    public static final int SUCCESS = 0;
    public static final int ERROR = -1;
    public static final int TOKEN_ERROR = -2;//未登录认证
    public static final int SIGN_ERROR = -3;//验签有误
    public static final int PERIOD_CHANGED = -4; //选择期次已经过期
    public static final int GAME_NOT_EXIST = -5;
    public static final int REPEAT = -6;

    public static final String GAME_NOT_EXIST_INFO = "玩法不存在";

    public static final String SUCCESS_SAVE_MSG = "保存成功";
    public static final String SUCCESS_SAVE_ERROR = "保存失败";

    public static final String SUCCESS_CODE = "0";
    public static final String NEED_RELOGIN = "2007";
    public static final String TOKEN_ISSUE = "2003";
    public static final String PERIOD_CHANGED_MSG = "periodChanged";

    public static final String PARAMS_ERROR = "参数错误";

    public static final String PARAMS_ERR_MSG = "请求参数错误";
    public static final String PARAMS_MOBILE_IS_NOT_REGISTER = "该手机号未注册";
    public static final String USER_ERR_MSG = "用户不存在";

    public static final String PASSWORD_FORMAT_ERR_MSG = "密码格式错误";
    public static final String NICKNAME_FORMAT_ERR_MSG = "有特殊字符不能识别哦";
    public static final String MOBILE_FORMAT_ERR_MSG = "手机号码格式错误";
    public static final String VERIFY_CODE_FORMAT_ERR_MSG = "验证码格式错误";

    public static final String VALIDATE_VERIFY_CODE_EXPIRE_MSG = "验证码已失效，请重新发送";
    public static final String VALIDATE_VERIFY_CODE_ERR_MSG = "验证码错误，请重新输入";

    public static final String SEND_VERIFY_CODE_ERR_MSG = "请求发送验证码频繁，请稍后再试";

    public static final String VALIDATE_VERIFY_CODE_FAILURE_MSG = "您输入的验证码有误，请重新输入";
    public static final String VALIDATE_VERIFY_CODE_TIMES_FAILURE_MSG = "验证码验证次数过多，请稍后再试";
    public static final String VALIDATE_USER_IS_EXIST_MSG = "该手机号已经注册";
    public static final String VALIDATE_PASSWORD_TIMES_FAILURE_MSG = "密码码验证次数过多，请稍后再试";
    public static final String VALIDATE_PASSWORD_FAILURE_MSG = "密码错误，可以尝试验证码登录";
    public static final String OLD_PASSWORD_FAILURE_MSG = "旧密码有误";
    public static final int VALIDATE_TOKEN_FAILURE_CODE = 2001;
    public static final String VALIDATE_TOKEN_FAILURE_MSG = "登录信息过期，请重新登录";
    public static final String SEND_VERIFY_CODE_SUCCESS_MSG = "验证码发送成功";
    public static final String SET_PASSWORD_SUCCESS_MSG = "密码设置成功";
    public static final String MODIFY_PROTOTYPE_SUCCESS_MSG = "修改成功";

    public static final int LOTTERY_NUMBER_TYPE_SINGLE_NOT = 0;//不够单式但是红球篮球都有
    public static final int LOTTERY_NUMBER_TYPE_ONLY_RED = 1;//只有红球
    public static final int LOTTERY_NUMBER_TYPE_ONLY_BLUE = 2;//只有篮球
    public static final int LOTTERY_NUMBER_TYPE_SINGLE = 3;//单式
    public static final int LOTTERY_NUMBER_TYPE_MULTIPLE = 4;//单式

    public static final Integer REPEAT_CODE = 3;
    public static final String REPEAT_MSG = "重复支付";
    public static final Integer PAY_SUCCESS_CODE = 1;//单式
    public static final String PAY_GOLD_COIN_SUCCESS = "金币支付成功";//单式
    public static final Integer PAY_FAILED_CODE = -1;//单式
    public static final String PAY_GOLD_COIN_FAILED = "金币余额不足";//单式

    public static final String COUPON_DISTRIBUTE_FAIL_STATUS = "FAIL";
    public static final String COUPON_DISTRIBUTE_SUCCESS_STATUS = "SUCCESS";


}
