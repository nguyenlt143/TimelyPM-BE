package com.CapstoneProject.capstone.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public class HtmlSanitizerUtil {
    public static String sanitize(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return "";
        }

        PolicyFactory policy = new HtmlPolicyBuilder()
                .allowElements("p", "a", "img")
                .allowAttributes("href", "rel", "target", "style").onElements("a")
                .allowAttributes("src", "alt").onElements("img")
                .allowAttributes("style").onElements("p")
                .toFactory();

        return policy.sanitize(htmlContent);
    }
}
