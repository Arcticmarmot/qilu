package com.marmot.qilu.common.util;

public final class ContentUtils {

    private static final int POST_PREVIEW_LENGTH = 16;
    private static final int COMMENT_PREVIEW_LENGTH = 16;

    private ContentUtils() {
    }

    public static String normalizeContent(String content) {
        return content == null ? "" : content.trim();
    }

    public static String buildPostContentPreview(String content) {
        return buildContentPreview(content, POST_PREVIEW_LENGTH);
    }

    public static String buildCommentContentPreview(String content) {
        return buildContentPreview(content, COMMENT_PREVIEW_LENGTH);
    }

    private static String buildContentPreview(String content, int length) {
        String normalized = content == null ? "" : content
                .replace("\r", " ")
                .replace("\n", " ")
                .trim();

        if (normalized.isEmpty()) {
            return "";
        }

        return normalized.substring(0, Math.min(normalized.length(), length));
    }
}