package com.luckyone.web.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luckyone.web.common.BeanCopyUtils;
import com.luckyone.web.common.ErrorCode;
import com.luckyone.web.common.IMResponse;
import com.luckyone.web.exception.BusinessException;
import com.luckyone.web.im.IMServer;
import com.luckyone.web.mapper.GeneratorMapper;
import com.luckyone.web.model.dto.comment.PostCommentRequest;
import com.luckyone.web.model.dto.user.UsernameAndAvtarDto;
import com.luckyone.web.model.entity.Generator;
import com.luckyone.web.model.entity.GeneratorComment;
import com.luckyone.web.model.dto.comment.ChildrenCommentVo;
import com.luckyone.web.model.dto.comment.RootCommentVo;
import com.luckyone.web.model.entity.User;
import com.luckyone.web.service.GeneratorCommentService;
import com.luckyone.web.mapper.GeneratorCommentMapper;
import com.luckyone.web.service.MsgUnreadService;
import com.luckyone.web.service.UserService;
import com.luckyone.web.utils.RedisKeyUtils;
import com.luckyone.web.utils.RedisUtil;
import io.netty.channel.Channel;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class GeneratorCommentServiceImpl extends ServiceImpl<GeneratorCommentMapper, GeneratorComment>
    implements GeneratorCommentService{

    @Resource
    private GeneratorMapper generatorMapper;

    @Resource
    private UserService userService;

    @Resource
    private MsgUnreadService msgUnreadService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public List<RootCommentVo> getRootCommentsOfGenerator(Long id) {
        List<GeneratorComment> rootComments = lambdaQuery().eq(GeneratorComment::getGeneratorId, id)
                .eq(GeneratorComment::getRootId, -1)
                .list();
        if (rootComments.isEmpty()) {
            return new ArrayList<>();
        }
        // 查询发表用户的信息
        List<Long> userids = rootComments.stream().map(GeneratorComment::getFromId).collect(Collectors.toList());
        if (userids.isEmpty()) {
            return new ArrayList<>();
        }
        List<UsernameAndAvtarDto> dtos = userService.listUserNameAndAvatarByUids(userids);
        Map<Long, UsernameAndAvtarDto> dtoMap = dtos.stream().collect(Collectors.toMap(UsernameAndAvtarDto::getId, dto -> dto));

        return rootComments.stream().map(rootComment -> {
            RootCommentVo vo = BeanCopyUtils.copyBean(rootComment, RootCommentVo.class);
            vo.setFromUsername(dtoMap.get(vo.getFromId()).getUserName());
            vo.setUserAvatar(dtoMap.get(vo.getFromId()).getUserAvatar());

            // 查询子评论
            Integer count = Math.toIntExact(lambdaQuery().eq(GeneratorComment::getRootId, rootComment.getId()).count());
            vo.setReplyCount(count);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ChildrenCommentVo> getChildrenOfRoot(Long id) {
        List<GeneratorComment> childrenComments = lambdaQuery().eq(GeneratorComment::getRootId, id).list();
        if(childrenComments.isEmpty()){
            return new ArrayList<>();
        }
        Set<Long> fromUids = childrenComments.stream().map(GeneratorComment::getFromId).collect(Collectors.toSet());
        Set<Long> toUids = childrenComments.stream().map(GeneratorComment::getToId).collect(Collectors.toSet());
        Set<Long> uids = (Set<Long>) CollectionUtil.addAll(fromUids, toUids);
        //获取用户信息
        List<UsernameAndAvtarDto> dtos = userService.listUserNameAndAvatarByUids(uids);
        Map<Long, UsernameAndAvtarDto> dtoMap = dtos.stream().collect(Collectors.toMap(UsernameAndAvtarDto::getId, dto -> dto));
        return childrenComments.stream().map(childrenComment -> {
            ChildrenCommentVo vo = BeanCopyUtils.copyBean(childrenComment, ChildrenCommentVo.class);
            vo.setFromUsername(dtoMap.get(vo.getFromId()).getUserName());
            vo.setUserAvatar(dtoMap.get(vo.getFromId()).getUserAvatar());
            if(vo.getToId() != null){
                vo.setToUsername(dtoMap.get(vo.getToId()).getUserName());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void likeComment(Long id) {

    }

    @Override
    public void publishComment(PostCommentRequest postCommentRequest, HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        
        //1.首先要查询文章是否存在
        Generator generator = generatorMapper.selectOne(new LambdaQueryWrapper<Generator>()
                .select(Generator::getUserId).eq(Generator::getId, postCommentRequest.getGeneratorId()));
        if(generator == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求的资源不存在");
        }

        GeneratorComment comment = BeanCopyUtils.copyBean(postCommentRequest, GeneratorComment.class);
        CompletableFuture<Void> saveFuture = CompletableFuture.runAsync(() -> {
            //2.保存评论
            if (postCommentRequest.getToId() == null) {
                comment.setToId(generator.getUserId());
            }
            comment.setFromId(currentUser.getId());
            save(comment);
        }, threadPoolExecutor);

        CompletableFuture<Void> countFuture = CompletableFuture.runAsync(() -> {
            //3.generator表的comment_count+1
            generatorMapper.update(null, new LambdaUpdateWrapper<Generator>()
                    .setSql("commentCount = commentCount + 1").eq(Generator::getId, postCommentRequest.getGeneratorId()));
        }, threadPoolExecutor);

        //todo: pipeline优化
        CompletableFuture<Void> cacheFuture = CompletableFuture.runAsync(() -> {
            //4.增加redis中的计数
            String UserId = String.valueOf(currentUser.getId());
            stringRedisTemplate.opsForHash().increment(RedisKeyUtils.MAP_KEY_USER_COMMENT_COUNT,UserId,1);
            // 如果不是回复自己
            if (!Objects.equals(comment.getToId(), comment.getFromId())){
                redisUtil.zset("reply_zset:" + comment.getToId(),comment.getId());
                msgUnreadService.addOneUnread(Math.toIntExact(comment.getToId()), "reply");

                // netty 通知未读消息
                HashMap<String, Object> map = new HashMap<>();
                map.put("type", "接收");
                Set<Channel> channels = IMServer.userChannel.get(comment.getToId());
                if (channels != null) {
                    for (Channel channel : channels) {
                        channel.writeAndFlush(IMResponse.message("reply", map));
                    }
                }
            }
        }, threadPoolExecutor);

        CompletableFuture.allOf(saveFuture, countFuture, cacheFuture).join();
    }

    @Override
    public Long getUserCommentCount(Long UserId) {

        return null;
    }
}




