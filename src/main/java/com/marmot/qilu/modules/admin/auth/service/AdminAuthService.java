package com.marmot.qilu.modules.admin.auth.service;

import com.marmot.qilu.modules.admin.auth.dto.OperatorLoginDTO;
import com.marmot.qilu.modules.admin.auth.vo.OperatorLoginVO;

public interface AdminAuthService {

    OperatorLoginVO login(OperatorLoginDTO dto);
}
