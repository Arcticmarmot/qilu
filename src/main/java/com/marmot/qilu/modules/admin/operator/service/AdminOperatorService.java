package com.marmot.qilu.modules.admin.operator.service;

import com.marmot.qilu.modules.admin.operator.entity.Operator;
import com.marmot.qilu.modules.admin.operator.vo.OperatorVO;

public interface AdminOperatorService {

    OperatorVO getCurrentAdminProfile();

    Operator getOperatorByUsername(String username);
}
