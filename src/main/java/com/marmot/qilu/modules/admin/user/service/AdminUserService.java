package com.marmot.qilu.modules.admin.user.service;

import com.marmot.qilu.modules.admin.user.dto.AdminUserPageQueryDTO;
import com.marmot.qilu.modules.admin.user.vo.AdminUserPageVO;
import com.marmot.qilu.modules.admin.user.vo.AdminUserPageItemVO;

public interface AdminUserService {

    AdminUserPageVO<AdminUserPageItemVO> getUserPage(AdminUserPageQueryDTO dto);

    void banUser(String userUuid);

    void unbanUser(String userUuid);
}
