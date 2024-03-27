package org.git.crawler;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;
import java.util.regex.Pattern;


@ToString
@EqualsAndHashCode
public class Patterns {
    public static final Set<String> GITHUB_HOSTS = Set.of("github.com", "user-images.githubusercontent.com");
    public static final Pattern GITHUB_TRACKED_PAGE = Pattern.compile("/issues/\\d+/?$|/pull/\\d+/?$");
    public static final Pattern GITHUB_ISSUE_NUMBER = Pattern.compile(".*/issues/(\\d+)/?.*");
    public static final Pattern GITHUB_PR_NUMBER = Pattern.compile(".*/pull/(\\d+)/?.*");

    public static final String GITHUB_ISSUE_CONTENT_SELECTOR = "#show_issue";
    public static final String GITHUB_PR_CONTENT_SELECTOR = ".clearfix.js-issues-results";
}
