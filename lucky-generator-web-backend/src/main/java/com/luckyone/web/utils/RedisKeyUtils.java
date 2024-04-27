package com.luckyone.web.utils;

/**
 * 用于点赞功能
 */
public class RedisKeyUtils {

    //保存用户点赞数据的key
    public static final String MAP_KEY_USER_LIKED = "MAP_USER_LIKED";
    //保存用户被点赞数量的key
    public static final String MAP_KEY_USER_LIKED_COUNT = "MAP_USER_LIKED_COUNT";

    // 保存用户评论数量
    public static final String MAP_KEY_USER_COMMENT_COUNT = "MAP_USER_COMMENT_COUNT";

    /**
     * 拼接被点赞生成器的id和点赞的人的id作为key。格式 222222::333333
     *
     * @param likedGeneratorId 被点赞的生成器id
     * @param likedUserId 点赞的人的id
     * @return
     */
    public static String getLikedKey(Long likedGeneratorId, Long likedUserId) {
        StringBuilder builder = new StringBuilder();
        builder.append(likedGeneratorId);
        builder.append("::");
        builder.append(likedUserId);
        return builder.toString();
    }

}
