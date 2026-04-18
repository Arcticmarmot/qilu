package com.marmot.qilu.modules.user.service;

import com.marmot.qilu.modules.user.dto.UserCreateDTO;
import com.marmot.qilu.modules.user.entity.User;
import com.marmot.qilu.modules.user.vo.UserVO;

public interface UserService {
    UserVO createUser(UserCreateDTO dto);

    UserVO getUserProfile(String uuid);

    UserVO getCurrentUserProfile(String uuid);

    User getUserByUuid(String uuid);

    User getUserByEmail(String email);
}
