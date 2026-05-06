package com.marmot.qilu.modules.hot.controller;

import com.marmot.qilu.modules.hot.service.HotPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hot/posts")
public class HotPostController {

    private final HotPostService hotPostService;


}
