package com.luckyone.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luckyone.web.common.BeanCopyUtils;
import com.luckyone.web.common.IMResponse;
import com.luckyone.web.im.IMServer;
import com.luckyone.web.mapper.*;
import com.luckyone.web.model.dto.msgUnread.MsgUnreadQueryRequest;
import com.luckyone.web.model.dto.user.UsernameAndAvtarDto;
import com.luckyone.web.model.entity.*;
import com.luckyone.web.model.vo.msgUnread.CommentMsgUnreadVO;
import com.luckyone.web.model.vo.msgUnread.LoveMsgUnreadVO;
import com.luckyone.web.service.GeneratorCommentService;
import com.luckyone.web.service.MsgUnreadService;
import com.luckyone.web.service.UserService;
import com.luckyone.web.utils.RedisUtil;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class MsgUnreadServiceImpl extends ServiceImpl<MsgUnreadMapper, MsgUnread>
        implements MsgUnreadService {

    @Resource
    private MsgUnreadMapper msgUnreadMapper;

    @Resource
    private UserService userService;

    @Resource
    private GeneratorCommentMapper generatorCommentMapper;

    @Resource
    private GeneratorLikeMapper generatorLikeMapper;

    @Resource
    private GeneratorMapper generatorMapper;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private ChatMapper chatMapper;

    /**
     * 给指定用户的某一列未读消息加一
     *
     * @param id     用户ID
     * @param column msg_unread表列名 "reply"/"love"/"system"
     */
    @Override
    public void addOneUnread(Integer id, String column) {
//        MsgUnread msgUnread = getUnread(id);
//        if (msgUnread != null) {
//            if (column == "reply") {
//                Integer reply = msgUnread.getReply();
//                msgUnread.setReply(reply + 1);
//            }
//            if (column == "love") {
//                Integer love = msgUnread.getLove();
//                msgUnread.setLove(love + 1);
//            }
//            save(msgUnread);
//        }
//        else {
//            msgUnread = new MsgUnread();
//            msgUnread.setId(id);
//            if (column == "reply") {
//                Integer reply = msgUnread.getReply();
//                msgUnread.setReply(reply + 1);
//            }
//            if (column == "love") {
//                Integer love = msgUnread.getLove();
//                msgUnread.setLove(love + 1);
//            }
//            save(msgUnread);
//        }
        UpdateWrapper<MsgUnread> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).setSql(column + " = " + column + " + 1");
        msgUnreadMapper.update(null, updateWrapper);
        redisUtil.delValue("msg_unread:" + id);
    }

    /**
     * 分页查询点赞记录
     *
     * @param msgUnreadQueryRequest
     * @param request
     * @return
     */
    @Override
    public Page<LoveMsgUnreadVO> listLoveMsgUnreadByPage(MsgUnreadQueryRequest msgUnreadQueryRequest, HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        long pageNum = msgUnreadQueryRequest.getCurrent();
        long pageSize = msgUnreadQueryRequest.getPageSize();

        // 1.首先查询该用户的所有文章的id
        List<Long> generatorIds = generatorMapper.selectList(new LambdaQueryWrapper<Generator>()
                        .select(Generator::getId)
                        .eq(Generator::getUserId, currentUser.getId()))
                .stream()
                .map(Generator::getId).collect(Collectors.toList());

        Page<LoveMsgUnreadVO> page = new Page<>(pageNum, pageSize);
        if (generatorIds.isEmpty()) {
            page.setRecords(new ArrayList<>());
            return page;
        }

        // 2.分页查询这些文章的点赞记录（以createTime排序）
        Page<GeneratorLike> generatorLikePage = generatorLikeMapper.selectPage(new Page<>(pageNum, pageSize), new QueryWrapper<GeneratorLike>()
                .ne("createBy", currentUser.getId())
                .in("generatorId", generatorIds)
                .orderBy(true, false, "createTime"));
        if (generatorLikePage.getRecords().isEmpty()) {
            return page;
        }

        // 3. 转换为vo
        // 查询点赞用户信息
        CompletableFuture<Map<Long, UsernameAndAvtarDto>> userMapFuture = CompletableFuture.supplyAsync(() -> {
            Set<Long> uids = generatorLikePage.getRecords().stream().map(GeneratorLike::getCreateBy).collect(Collectors.toSet());
            List<UsernameAndAvtarDto> usernameAndAvtarDtos = userService.listUserNameAndAvatarByUids(uids);
            return usernameAndAvtarDtos.stream().collect(Collectors.toMap(UsernameAndAvtarDto::getId, dto -> dto));
        }, threadPoolExecutor);

        // 查询被点赞的文章信息
        CompletableFuture<Map<Long, Generator>> generatorMapFuture = CompletableFuture.supplyAsync(() -> {
            Set<Long> queryGeneratorIds = generatorLikePage.getRecords().stream().map(GeneratorLike::getGeneratorId).collect(Collectors.toSet());
            return generatorMapper.selectList(new LambdaQueryWrapper<Generator>()
                            .select(Generator::getId, Generator::getName, Generator::getDescription)
                            .in(Generator::getId, queryGeneratorIds)).stream()
                    .collect(Collectors.toMap(Generator::getId, generator -> generator));
        }, threadPoolExecutor);

        // 信息拼装
        CompletableFuture<List<LoveMsgUnreadVO>> vosFuture = userMapFuture.thenCombine(generatorMapFuture, (userMap, generatorMap) -> {
            List<LoveMsgUnreadVO> vos = generatorLikePage.getRecords().stream().map(generatorLike -> {
                LoveMsgUnreadVO vo = BeanCopyUtils.copyBean(generatorLike, LoveMsgUnreadVO.class);
                UsernameAndAvtarDto dto = userMap.get(generatorLike.getCreateBy());
                vo.setFromId(dto.getId());
                vo.setUserAvatar(dto.getUserAvatar());
                vo.setUserName(dto.getUserName());
                Generator generator = generatorMap.get(generatorLike.getGeneratorId());
                vo.setGeneratorName(generator.getName());
                vo.setDescription(generator.getDescription());
                return vo;
            }).collect(Collectors.toList());
            return vos;
        });

        vosFuture.thenAccept(page::setRecords).join();
        return page;
    }

    /**
     * 分页查询评论记录
     *
     * @param msgUnreadQueryRequest
     * @param request
     * @return
     */
    @Override
    public Page<CommentMsgUnreadVO> listCommentMsgUnreadByPage(MsgUnreadQueryRequest msgUnreadQueryRequest, HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        long pageNum = msgUnreadQueryRequest.getCurrent();
        long pageSize = msgUnreadQueryRequest.getPageSize();

        Page<CommentMsgUnreadVO> page = new Page<>(pageNum, pageSize);
        // 1.分页查询生成器的被评论记录
        Page<GeneratorComment> generatorCommentPage = generatorCommentMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new QueryWrapper<GeneratorComment>()
                        .select("id", "toCommentId", "fromId", "content", "generatorId", "createTime")
                        .ne("fromId", currentUser.getId())
                        .eq("toId", currentUser.getId())
                        .orderBy(true, false, "createTime")
        );
        if (generatorCommentPage.getRecords().isEmpty()) {
            page.setRecords(new ArrayList<>());
            return page;
        }

        // 2.转换为vos
        // 查询原始评论信息
        CompletableFuture<Map<Long, GeneratorComment>> commentMapFuture = CompletableFuture.supplyAsync(() -> {
            Set<Long> comments = generatorCommentPage.getRecords().stream()
                    .map(GeneratorComment::getToCommentId)
                    .collect(Collectors.toSet());
            List<GeneratorComment> generatorComments = generatorCommentMapper.selectList(new LambdaQueryWrapper<GeneratorComment>()
                    .select(GeneratorComment::getId, GeneratorComment::getContent)
                    .in(GeneratorComment::getId, comments)
            );
            return generatorComments.stream().collect(Collectors.toMap(GeneratorComment::getId, generatorComment -> generatorComment));
        }, threadPoolExecutor);

        // 查询用户信息
        CompletableFuture<Map<Long, UsernameAndAvtarDto>> userMapFuture = CompletableFuture.supplyAsync(() -> {
            Set<Long> uids = generatorCommentPage.getRecords().stream()
                    .map(GeneratorComment::getFromId)
                    .collect(Collectors.toSet());
            List<UsernameAndAvtarDto> usernameAndAvtarDtos = userService.listUserNameAndAvatarByUids(uids);
            return usernameAndAvtarDtos
                    .stream()
                    .collect(Collectors.toMap(UsernameAndAvtarDto::getId, dto -> dto));
        }, threadPoolExecutor);

        // 查询文章信息
        CompletableFuture<Map<Long, Generator>> generatorMapFuture = CompletableFuture.supplyAsync(() -> {
            Set<Long> generatorIds = generatorCommentPage.getRecords().stream()
                    .map(GeneratorComment::getGeneratorId)
                    .collect(Collectors.toSet());
            List<Generator> generatorList = generatorMapper.selectList(new LambdaQueryWrapper<Generator>()
                    .select(Generator::getId, Generator::getName, Generator::getDescription)
                    .in(Generator::getId, generatorIds)
            );
            return generatorList
                    .stream()
                    .collect(Collectors.toMap(Generator::getId, generator -> generator));
        }, threadPoolExecutor);

        // 3.信息拼装
        CompletableFuture<List<CommentMsgUnreadVO>> vosFuture = CompletableFuture.allOf(commentMapFuture, userMapFuture, generatorMapFuture)
                .thenApply((Void) -> {
                    Map<Long, GeneratorComment> commentMap = commentMapFuture.join();
                    Map<Long, UsernameAndAvtarDto> userMap = userMapFuture.join();
                    Map<Long, Generator> generatorMap = generatorMapFuture.join();

                    List<CommentMsgUnreadVO> vos = generatorCommentPage.getRecords().stream().map(generatorComment -> {
                        CommentMsgUnreadVO vo = BeanCopyUtils.copyBean(generatorComment, CommentMsgUnreadVO.class);
                        if (generatorComment.getToCommentId() != null) {
                            vo.setFromContent(commentMap.get(generatorComment.getToCommentId()).getContent());
                        }
                        UsernameAndAvtarDto dto = userMap.get(generatorComment.getFromId());
                        vo.setFromId(dto.getId());
                        vo.setUserAvatar(dto.getUserAvatar());
                        vo.setUserName(dto.getUserName());
                        Generator generator = generatorMap.get(generatorComment.getGeneratorId());
                        vo.setGeneratorName(generator.getName());
                        vo.setDescription(generator.getDescription());
                        return vo;
                    }).collect(Collectors.toList());
                    return vos;
                });
        vosFuture.thenAccept(page::setRecords).join();
        return page;
    }

    /**
     * 清除指定用户的某一列未读消息
     *
     * @param id     用户ID
     * @param column msg_unread表列名 "reply"/"at"/"love"/"system"/"whisper"/"dynamic"
     */
    @Override
    public void clearUnread(Integer id, String column) {
        QueryWrapper<MsgUnread> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id).ne(column, 0);
        MsgUnread msgUnread = msgUnreadMapper.selectOne(queryWrapper);
        // 如果本身就是0条未读就没必要执行下面的操作了 不过如果有未读的话做这个查询就会带来额外的开销
        if (msgUnread == null) return;

        // 通知用户的全部channel 更新该消息类型未读数为0
        Map<String, Object> map = new HashMap<>();
        map.put("type", "全部已读");
        Set<Channel> myChannels = IMServer.userChannel.get(id);
        if (myChannels != null) {
            for (Channel channel : myChannels) {
                channel.writeAndFlush(IMResponse.message(column, map));
            }
        }

        UpdateWrapper<MsgUnread> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).set(column, 0);
        msgUnreadMapper.update(null, updateWrapper);
        redisUtil.delValue("msg_unread:" + id);
        if (Objects.equals(column, "whisper")) {
            // 如果是清除私聊消息还需要去把chat表的全部未读清掉
            UpdateWrapper<Chat> updateWrapper1 = new UpdateWrapper<>();
            updateWrapper1.eq("anotherId", id).set("unread", 0);
            chatMapper.update(null, updateWrapper1);
        }
    }

    /**
     * 私聊消息特有的减除一定数量的未读数
     *
     * @param id    用户ID
     * @param count 要减多少
     */
    @Override
    public void subtractWhisper(Integer id, Integer count) {
        UpdateWrapper<MsgUnread> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                // 更新后的未读数不能小于0
                .setSql("whisper = CASE WHEN whisper - " + count + " < 0 THEN 0 ELSE whisper - " + count + " END");
        msgUnreadMapper.update(null, updateWrapper);
        redisUtil.delValue("msg_unread:" + id);
    }

    /**
     * 获取某人的全部消息未读数
     *
     * @param id 用户ID
     * @return MsgUnread对象
     */
    @Override
    public MsgUnread getUnread(Integer id) {
        MsgUnread msgUnread = redisUtil.getObject("msg_unread:" + id, MsgUnread.class);
        if (msgUnread == null) {
            msgUnread = getById(id);
            if (msgUnread != null) {
                MsgUnread finalMsgUnread = msgUnread;
                CompletableFuture.runAsync(() -> {
                    redisUtil.setExObjectValue("msg_unread:" + id, finalMsgUnread);    // 异步更新到redis
                }, threadPoolExecutor);
            } else {
                return new MsgUnread(id, 0, 0, 0, 0);
            }
        }
        return msgUnread;
    }
}




