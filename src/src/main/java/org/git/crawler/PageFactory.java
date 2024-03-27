package org.git.crawler;

import org.git.page.ExternalPage;
import org.git.page.GitHubIssuePage;
import org.git.page.GitHubPrPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.stream.Stream;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toSet;
import static org.git.crawler.Patterns.*;

public class PageFactory {
    private PageFactory() {}

    public static PageWithLinks parsePage(URI uri, String html) {
        return switch (uri) {
            case URI u when u.getHost().equals("github.com") && u.getPath().contains("/issues/") -> PageFactory.parseIssuePage(uri, html);
            case URI u when u.getHost().equals("github.com") && u.getPath().contains("/pull/") -> PageFactory.parsePrPage(uri, html);
            default -> PageFactory.parseExternalPage(uri, html);
        };
    }

    static PageWithLinks parseIssuePage(URI uri, String html) {
        Document document = Jsoup.parse(html);
        var content = PageFactory.extractContent(document, GITHUB_ISSUE_CONTENT_SELECTOR);
        var links = PageFactory.extractLinks(uri, document, GITHUB_ISSUE_CONTENT_SELECTOR);
        var issueNr = PageFactory.getFirstMatchAsNumber(GITHUB_ISSUE_NUMBER, uri);
        return new PageWithLinks(new GitHubIssuePage(uri, content, issueNr), links);
    }

    static PageWithLinks parsePrPage(URI uri, String html) {
        Document document = Jsoup.parse(html);
        var content = PageFactory.extractContent(document, GITHUB_PR_CONTENT_SELECTOR);
        var links = PageFactory.extractLinks(uri, document, GITHUB_PR_CONTENT_SELECTOR);
        var issueNr = PageFactory.getFirstMatchAsNumber(GITHUB_PR_NUMBER, uri);
        return new PageWithLinks(new GitHubPrPage(uri, content, issueNr), links);
    }

    private static PageWithLinks parseExternalPage(URI uri, String html) {
        return new PageWithLinks(new ExternalPage(uri, html), Set.of());
    }

    private static String extractContent(Document document, String cssContentSelector) {
        var selectedElements = document.select(cssContentSelector);
        if (selectedElements.size() != 1)
            throw new IllegalArgumentException("The CSS selector '%s' yielded %d elements".formatted(cssContentSelector, selectedElements.size()));
        return selectedElements.getFirst().toString();
    }

    private static Set<URI> extractLinks(URI url, Document document, String cssContentSelector) {
        return document
                .select(cssContentSelector + " a[href]").stream()
                .map(element -> element.attribute("href").getValue())
                .flatMap(href -> normalizePotentialLink(url, href))
                .filter(PageFactory::shouldRegisterLink)
                .collect(toSet());
    }

    private static Stream<URI> normalizePotentialLink(URI pageUrl, String href) {
        if (href == null || href.isBlank())
            return Stream.empty();

        try {
            var url = pageUrl.resolve(new URI(href));
            var isCyclicLink = url.equals(pageUrl);
            if (isCyclicLink)
                return Stream.empty();
            return Stream.of(url);
        } catch (URISyntaxException ex) {
            return Stream.empty();
        }
    }

    private static boolean shouldRegisterLink(URI url) {
        if (url.getHost() == null)
            return false;

        var isExternalUrl = !GITHUB_HOSTS.contains(url.getHost());
        return isExternalUrl || GITHUB_TRACKED_PAGE.matcher(url.toString()).find();
    }

    private static int getFirstMatchAsNumber(Pattern pattern, URI url) {
        var issueNumberMatcher = pattern.matcher(url.toString());
        var found = issueNumberMatcher.find();
        if (!found)
            throw new IllegalStateException("Alleged issue/PR URL %s does not seem to contain a number.".formatted(url));
        return Integer.parseInt(issueNumberMatcher.group(1));
    }
}
