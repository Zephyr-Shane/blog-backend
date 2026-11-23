package com.szenith.ai.infrastructure.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/** 正文 HTML 消毒：白名单标签与属性，防存储型 XSS（评审报告 P1-3） */
public final class HtmlSanitizer {

    private HtmlSanitizer() {}

    private static final Safelist SAFELIST = Safelist.relaxed()
            .addTags("h1", "h2", "h3", "h4", "pre", "code", "blockquote", "hr", "table", "thead", "tbody", "tr", "th", "td")
            .addAttributes("code", "class")
            .addAttributes("a", "target")
            .addAttributes("img", "src", "alt", "title")
            .addProtocols("img", "src", "http", "https", "data")
            .addProtocols("a", "href", "http", "https", "mailto")
            .preserveRelativeLinks(true);

    public static String clean(String html) {
        if (html == null || html.isEmpty()) return "";
        Document.OutputSettings settings = new Document.OutputSettings()
                .prettyPrint(false)
                .outline(false);
        return Jsoup.clean(html, "", SAFELIST, settings);
    }
}
