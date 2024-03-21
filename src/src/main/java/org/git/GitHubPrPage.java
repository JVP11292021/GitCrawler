package org.git;

import java.net.URI;
import java.util.Objects;
import java.util.Set;

public record GitHubPrPage(URI url, String content, Set<URI> links, int prNumber) implements GitHubPage {
    public GitHubPrPage {
        // TODO add verification
        Objects.requireNonNull(url);
        Objects.requireNonNull(content);
        links = Set.copyOf(links);
        if (prNumber <= 0)
            throw new IllegalArgumentException("PR number must be 1 or greater - was '%s' at '%s'.".formatted(prNumber, url));
    }

    public GitHubPrPage(URI url, String content, int prNumber) {
        this(url, content, Set.of(), prNumber);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || other instanceof GitHubPrPage page
                && this.url.equals(page.url());
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

}
