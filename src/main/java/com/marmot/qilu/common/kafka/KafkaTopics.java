package com.marmot.qilu.common.kafka;

public final class KafkaTopics {
    private KafkaTopics() { }

    public static final String TOPIC_LIKE_EVENTS = "like-events";
    public static final String TOPIC_COMMENT_EVENTS = "comment-events";
    public static final String TOPIC_REPLY_EVENTS = "reply-events";

    public static final String TOPIC_POST_SEARCH_INDEX_EVENTS = "post-search-index-events";

    public static final String TOPIC_VOUCHER_ORDER_EVENTS = "voucher-order-events";
}
