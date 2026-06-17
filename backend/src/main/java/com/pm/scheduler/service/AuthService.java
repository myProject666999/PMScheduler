package com.pm.scheduler.service;

import com.pm.scheduler.dto.LoginDTO;
import com.pm.scheduler.vo.LoginVO;
import com.pm.scheduler.vo.UserVO;

public interface AuthService {

    LoginVO login(LoginDTO dto);

    UserVO getCurrentUser();
}
