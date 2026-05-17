package com.marmot.qilu.modules.admin.post.service.impl;

import com.marmot.qilu.modules.admin.post.service.AdminPostService;
import com.marmot.qilu.modules.post.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPostServiceImpl implements AdminPostService {

    private final PostMapper postMapper;


}
