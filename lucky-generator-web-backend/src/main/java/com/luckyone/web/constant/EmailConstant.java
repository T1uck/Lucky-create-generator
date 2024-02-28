package com.luckyone.web.constant;

/**
 *  电子邮件常量
 */
public interface EmailConstant {

    /**
     * 电子邮件html内容路径 resources目录下
     */
    String EMAIL_HTML_CONTENT_PATH = "email.html";

    /**
     * 电子邮件html支付成功路径
     */
    String EMAIL_HTML_PAY_SUCCESS_PATH = "pay.html";

    /**
     * captcha缓存键
     */
    String CAPTCHA_CACHE_KEY = "lucky:captcha:";

    /**
     * 电子邮件主题
     */
    String EMAIL_SUBJECT = "验证码邮件";

    /**
     * 电子邮件标题
     */
    String EMAIL_TITLE = "lucky 代码生成器";

    /**
     * 电子邮件标题英语
     */
    String EMAIL_TITLE_ENGLISH = "lucky code generator";

    /**
     * 平台负责人
     */
    String PLATFORM_RESPONSIBLE_PERSON = "luckyone";

    /**
     * 平台地址
     */
    String PLATFORM_ADDRESS = "<a href=''>请联系我们</a>";

    String PATH_ADDRESS = "";
}
