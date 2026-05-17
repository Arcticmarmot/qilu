package com.marmot.qilu.modules.admin.service;

import com.marmot.qilu.modules.admin.dto.AdminLoginDTO;
import com.marmot.qilu.modules.admin.vo.AdminLoginVO;
import com.marmot.qilu.modules.admin.vo.AdminVO;

public interface AdminAuthService {

    AdminLoginVO login(AdminLoginDTO dto);

    AdminVO getCurrentAdminProfile();
}
