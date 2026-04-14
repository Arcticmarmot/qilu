package com.marmot.qilu.modules.auth.service;

import com.marmot.qilu.modules.auth.dto.LoginDTO;
import com.marmot.qilu.modules.auth.vo.LoginVO;

public interface AuthService {
    LoginVO login(LoginDTO dto);
}
