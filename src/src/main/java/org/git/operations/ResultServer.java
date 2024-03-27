package org.git.operations;

import com.sun.net.httpserver.SimpleFileServer;
import org.git.page.GitHubPage;
import org.git.page.Page;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import static java.lang.StringTemplate.RAW;
import static java.util.stream.Collectors.joining;
import static org.git.Util.asHTML;
import static org.git.Util.join;

public class ResultServer {
    public static void serve(
            @NotNull Page rootPage,
            @NotNull Path serverDir,
            int port
    ) throws IOException {
        if (!Files.exists(serverDir))
            Files.createDirectory(serverDir);

        var html = asHTML(RAW."""
				<!DOCTYPE html>
				<html lang="en">
					<head>
						<meta charset="utf-8">
						<title>\{Prettify.pageName(rootPage)}</title>
						<link rel="stylesheet" href="style.css">
					</head>
					<body>
						<div class="container">
							\{ResultServer.pageTreeHtml(rootPage)}
						</div>
					</body>
				</html>
				""");
        Files.writeString(serverDir.resolve("index.html"), html.html());

        ResultServer.launchWebServer(serverDir, port);
    }

    private static void launchWebServer(Path serverDir, int port) {
        System.out.printf("Visit localhost:%d%n", port);
        new Thread(() ->
                SimpleFileServer
                        .createFileServer(
                                new InetSocketAddress(port),
                                serverDir.toAbsolutePath(),
                                SimpleFileServer.OutputLevel.INFO)
                        .start())
                .start();
    }

    private static String pageTreeHtml(@NotNull Page rootPage) {
        var printedPages = new HashSet<Page>();
        return ResultServer.appendPageTreeHtml(printedPages, rootPage, 0);
    }

    private static String appendPageTreeHtml(
            @NotNull Set<Page> printedPages,
            @NotNull Page page,
            int level
    ) {
        String pageHtml = ResultServer.pageHtml(page, printedPages.contains(page), level);
        if (printedPages.contains(page)) {
            printedPages.add(page);
            return pageHtml;
        } else {
            printedPages.add(page);
            var descendantsHtml = page instanceof GitHubPage
                    ghPage ? ghPage
                        .links()
                        .stream()
                        .map(linkedPage -> appendPageTreeHtml(
                                printedPages,
                                linkedPage,
                                level + 1))
                        .collect(joining("\n"))
                    : "";
            return join(RAW."""
					\{pageHtml}
					\{descendantsHtml}
					""");
        }
    }

    private static String pageHtml(@NotNull Page page, boolean reference, int level) {
        return join(RAW."""
				<div class="page level-\{level}">
					<a href="\{page.uri().toString()}">\{Prettify.pageName(page)}</a>
					\{reference ? "<span class=\"ref\"></span>" : ""}
				</div>
				""");
    }
}
