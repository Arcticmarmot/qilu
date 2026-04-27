package com.marmot.qilu.common.util;

public final class ContentUtils {

    public static final int POST_TITLE_PREVIEW_LENGTH = 16;
    public static final int POST_CONTENT_PREVIEW_LENGTH = 16;
    public static final int COMMENT_CONTENT_PREVIEW_LENGTH = 16;

    private ContentUtils() {
    }

    public static String normalizeContent(String content) {
        return content == null ? "" : content.trim();
    }

    public static String buildPostTitlePreview(String content) {
        return buildContentSnippet(content, POST_TITLE_PREVIEW_LENGTH);
    }

    public static String buildPostContentSnippet(String content) {
        return buildContentSnippet(content, POST_CONTENT_PREVIEW_LENGTH);
    }

    public static String buildCommentContentSnippet(String content) {
        return buildContentSnippet(content, COMMENT_CONTENT_PREVIEW_LENGTH);
    }

    private static String buildContentSnippet(String content, int length) {
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