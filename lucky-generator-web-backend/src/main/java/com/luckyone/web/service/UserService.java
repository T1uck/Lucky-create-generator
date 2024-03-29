package com.luckyone.web.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luckyone.web.model.dto.email.UserBindEmailRequest;
import com.luckyone.web.model.dto.email.UserEmailLoginRequest;
import com.luckyone.web.model.dto.email.UserEmailRegisterRequest;
import com.luckyone.web.model.dto.email.UserUnBindEmailRequest;
import com.luckyone.web.model.dto.phone.UserBindPhoneRequest;
import com.luckyone.web.model.dto.user.UserQueryRequest;
import com.luckyone.web.model.dto.user.UserUpdatePwdRequest;
import com.luckyone.web.model.dto.user.UsernameAndAvtarDto;
import com.luckyone.web.model.entity.User;
import com.luckyone.web.model.vo.LoginUserVO;
import com.luckyone.web.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param userName 用户名
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword,String userName);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 用户电子邮件登录
     *
     * @param userEmailLoginRequest 用户电子邮件登录请求
     * @param request               要求
     * @return {@link UserVO}
     */
    UserVO userEmailLogin(UserEmailLoginRequest userEmailLoginRequest, HttpServletRequest request);

    /**
     * 用户绑定电子邮件
     *
     * @param userEmailLoginRequest 用户电子邮件登录请求
     * @param request               要求
     * @return {@link UserVO}
     */
    UserVO userBindEmail(UserBindEmailRequest userEmailLoginRequest, HttpServletRequest request);

    /**
     * 用户取消绑定电子邮件
     *
     * @param request                要求
     * @param userUnBindEmailRequest 用户取消绑定电子邮件请求
     * @return {@link UserVO}
     */
    UserVO userUnBindEmail(UserUnBindEmailRequest userUnBindEmailRequest, HttpServletRequest request);

    /**
     * 用户电子邮件注册
     *
     * @param userEmailRegisterRequest 用户电子邮件注册请求
     * @return long
     */
    long userEmailRegister(UserEmailRegisterRequest userEmailRegisterRequest);

    /**
     * 更新用户 secretKey
     * @param id
     * @return
     */
    boolean updateSecretKey(Long id);

    /**
     * 用户绑定电话号码
     * @param userBindPhoneRequest
     * @param request
     * @return
     */
    UserVO userBindPhone(UserBindPhoneRequest userBindPhoneRequest, HttpServletRequest request);

    /**
     * 更新用户密码
     * @param userUpdatePwdRequest
     * @param request
     */
    void updatePassword(UserUpdatePwdRequest userUpdatePwdRequest, HttpServletRequest request);

    /**
     * 获取用户集合
     * @param ids
     * @return
     */
    List<UsernameAndAvtarDto> listUserNameAndAvatarByUids(Collection<Long> ids);

    /**
     * 获取用户头像和昵称
     * @param id
     * @return
     */
    UsernameAndAvtarDto getUsernameAndAvatar(Long id);
}
