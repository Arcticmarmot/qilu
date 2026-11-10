package com.marmot.qilu.modules.search.model;

/** 候选顺序即召回排名，保留实际命中的树节点。 */
public record PostSearchCandidate(Long postId, Long rootId) {
}
