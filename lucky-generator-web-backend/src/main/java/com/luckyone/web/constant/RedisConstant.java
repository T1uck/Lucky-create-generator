package com.luckyone.web.constant;

public interface RedisConstant {
    /**
     * 验证码
     */
    public static final String CODE_SMS_CACHE_PREFIX = "user:code:sms:";
    public static final String MAIL_CODE_CACHE_PREFIX = "user:code:mail:";

    /**
     * 用户标签
     */
    public static final String USER_TAGS_CATEGORY = "user:tags:category";
    public static final String USER_TAGS_PREFIX = "user:tags:id:";

    /**
     * 用户登录态
     */
    public static final String USER_SESSION_PREFIX = "user:session:";
    public static final Integer USER_SESSION_TTL = 30;

    /**
     * 用户信息
     */
    public static final String USER_INFO_PREFIX = "user:info:id:";
    public static final Integer USER_INFO_TTL = 1;

    /**
     * 用户推荐
     */
    public static final String USER_RECOMMEND_PREFIX = "user:recommend:id:";
    

    /**
     * 文章
     */
    public static final Integer ARTICLE_TTL = 4;

    public static final String ARTICLE_COVER_PREFIX = "generator:id:";
    public static final String ARTICLE_COVER_SUFFIX = ":cover";

    public static final String ARTICLE_CONTENT_PREFIX = "generator:id:";
    public static final String ARTICLE_CONTENT_SUFFIX = ":content";

    //浏览量
    public static final String ARTICLE_VIEW_PREFIX = "generator:id:";
    public static final String ARTICLE_VIEW_SUFFIX = ":view";
    //评论量
    public static final String ARTICLE_COMMENT_PREFIX = "generator:id:";
    public static final String ARTICLE_COMMENT_SUFFIX = ":comment";

    public static final String ARTICLE_LIKE_PREFIX = "generator:id:";
    public static final String ARTICLE_LIKE_SUFFIX = ":like";

    public static final String ARTICLE_STAR_PREFIX = "generator:id:";
    public static final String ARTICLE_STAR_SUFFIX = ":star";

    /**
     * 通知
     */
    public static final String NOTIFICATION_PREFIX = "notification:uid:";
    //点赞
    public static final String LIKE_NOTIFICATION_SUFFIX = ":like";
    //评论
    public static final String COMMENT_NOTIFICATION_SUFFIX = ":comment";
    //消息
    public static final String MSG_NOTIFICATION_SUFFIX = ":msg";
    //系统通知
    public static final String NOTICE_NOTIFICATION_SUFFIX = ":notice";

    public static final String ARTICLE_PUBLISHED = "generator:published";


    /**
     * 同步锁
     */
    public static final String ASYNC_CLEAR_LOCK = "generator:lock:clear";

    // 同步点赞
    public static final String ASYNC_LIKE_LOCK = "generator:lock:like";

    // 同步浏览量
    public static final String ASYNC_COUNT_LOCK = "generator:lock:count";

    // 更新文章score和hot的锁
    public static final String ASYNC_SCORE_LOCK = "generator:lock:score";

    /**
     * 文章热榜
     */
    public static final String HOT_ARTICLES = "generator:hot";

    /**
     * 全局只当文章
     */
    public static final String GLOBAL_TOP = "generator:top";
}
