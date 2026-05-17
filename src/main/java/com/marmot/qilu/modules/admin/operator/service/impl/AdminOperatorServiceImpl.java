package com.marmot.qilu.modules.admin.operator.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.marmot.qilu.common.context.OperatorContext;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.modules.admin.operator.entity.Operator;
import com.marmot.qilu.modules.admin.operator.mapper.OperatorMapper;
import com.marmot.qilu.modules.admin.operator.service.AdminOperatorService;
import com.marmot.qilu.modules.admin.operator.vo.OperatorVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOperatorServiceImpl implements AdminOperatorService {

    private final OperatorMapper operatorMapper;

    @Override
    public OperatorVO getCurrentAdminProfile() {
        String adminUuid = OperatorContext.requireUuid();
        Operator operator = operatorMapper.selectOne(
                Wrappers.<Operator>lambdaQuery()
                        .eq(Operator::getUuid, adminUuid)
        );

        OperatorVO vo = new OperatorVO();
        vo.setUuid(operator.getUuid());
        vo.setUsername(operator.getUsername());
        vo.setRole(operator.getRole());
        vo.setStatus(operator.getStatus());
        vo.setCreateAt(operator.getCreatedAt());
        return vo;
    }

    @Override
    public Operator getOperatorByUsername(String username) {
        validateUsername(username);

        return operatorMapper.selectOne(
                Wrappers.<Operator>lambdaQuery()
                        .eq(Operator::getUsername, username)
        );
    }

    private void validateUsername(String username) {
        if(username == null || username.isBlank()) {
            throw new BadRequestException("username is invalid");
        }
    }
}
