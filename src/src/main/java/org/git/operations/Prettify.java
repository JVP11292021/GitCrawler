package org.git.operations;

import org.git.page.*;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

import static java.util.stream.Collectors.joining;

public class Prettify {
    private Prettify() {}

    public static String pageList(@NotNull Page rootPage) {
        if (!(rootPage instanceof GitHubPage ghPage))
            return pageName(rootPage);

        return ghPage
                .subtree()
                .map(Prettify::pageName)
                .collect(joining("\n"));
    }

    public static String pageName(@NotNull Page page) {
        return switch (page) {
            case ErrorPage(URI url, _) -> "💥 ERROR: " + url.getHost();
            case ExternalPage(URI url, _) -> "💤 EXTERNAL: " + url.getHost();
            case GitHubIssuePage(_, _, _, int nr) -> "🐈 ISSUE #" + nr;
            case GitHubPrPage(_, _, _, int nr) -> {
                yield "🐙 PR #" + nr;
            }
        };
    }
}
