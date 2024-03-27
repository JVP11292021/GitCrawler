package org.git;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Util {
    public static String join(@NotNull StringTemplate template) {
        return template.interpolate();
    }

    public static Document asHTML(@NotNull StringTemplate template) {
        return Jsoup.parse(template.interpolate());
    }
}
