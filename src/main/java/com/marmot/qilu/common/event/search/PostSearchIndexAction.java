package com.marmot.qilu.common.event.search;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostSearchIndexAction {

    SYNC("sync"),
    DELETE("delete");

    private final String value;
}
