package com.pm.scheduler.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pm.scheduler.common.constant.Constants;
import com.pm.scheduler.common.exception.BusinessException;
import com.pm.scheduler.dto.LoginDTO;
import com.pm.scheduler.entity.SysUser;
import com.pm.scheduler.mapper.SysUserMapper;
import com.pm.scheduler.service.AuthService;
import com.pm.scheduler.vo.LoginVO;
import com.pm.scheduler.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        String md5Password = md5(dto.getPassword());
        if (!user.getPassword().equalsIgnoreCase(md5Password)) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getEnabled() != 1) {
            throw new BusinessException("用户已被禁用");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(Constants.TOKEN_PREFIX + token, user.getId().toString(), 24, TimeUnit.HOURS);

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUser(userVO);
        return loginVO;
    }

    @Override
    public UserVO getCurrentUser() {
        return new UserVO();
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return new BigInteger(1, digest).toString(16);
        } catch (Exception e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }
}
