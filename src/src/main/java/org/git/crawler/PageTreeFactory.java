package org.git.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.git.page.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

import static java.util.stream.Collectors.toSet;

@Slf4j
@RequiredArgsConstructor
public class PageTreeFactory {

    private final HttpClient client;
    private final ConcurrentMap<URI, Page> resolvedPages = new ConcurrentHashMap<>();

    public Page createPage(URI url, int depth) throws InterruptedException {
        if (resolvedPages.containsKey(url)) {
            System.out.printf("Found cached '%s'%n", url);
            return resolvedPages.get(url);
        }

        System.out.printf("Resolving '%s'...%n", url);
        PageWithLinks pageWithLinks = fetchPageWithLinks(url);
        var page = pageWithLinks.page();
        resolvedPages.computeIfAbsent(page.uri(), __ -> page);
        System.out.printf("Resolved '%s' with children: %s%n", url, pageWithLinks.links());

        return switch (page) {
            case GitHubIssuePage(var isUrl, var content, _, int nr) ->
                    new GitHubIssuePage(isUrl, content, resolveLinks(pageWithLinks.links(), depth - 1), nr);
            case GitHubPrPage(var prUrl, var content, _, int nr) ->
                    new GitHubIssuePage(prUrl, content, resolveLinks(pageWithLinks.links(), depth - 1), nr);
            case ExternalPage _, ErrorPage _ -> page;
        };
    }

    private PageWithLinks fetchPageWithLinks(URI url) throws InterruptedException {
        try {
            var pageBody = fetchPageAsString(url);
            return PageFactory.parsePage(url, pageBody);
        } catch (InterruptedException iex) {
            throw iex;
        } catch (Exception ex) {
            return new PageWithLinks(new ErrorPage(url, ex));
        }
    }

    private String fetchPageAsString(URI url) throws IOException, InterruptedException {
        var request = HttpRequest
                .newBuilder(url)
                .GET()
                .build();
        return client
                .send(request, HttpResponse.BodyHandlers.ofString())
                .body();
    }

    private Set<Page> resolveLinks(Set<URI> links, int depth) throws InterruptedException {
        if (depth < 0)
            return Collections.emptySet();

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var futurePages = new ArrayList<StructuredTaskScope.Subtask<Page>>();
            for (URI link : links)
                futurePages.add(scope.fork(() -> createPage(link, depth)));

            scope.join();
            scope.throwIfFailed();

            return futurePages.stream()
                    .map(StructuredTaskScope.Subtask::get)
                    .collect(toSet());
        } catch (ExecutionException ex) {
            throw new IllegalStateException("Error cases should have been handled during page creation!", ex);
        }
    }

}
