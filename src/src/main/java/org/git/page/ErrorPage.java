package org.git.page;

import java.net.URI;
import java.util.Objects;

public record ErrorPage(URI url, Exception e) implements Page {
    public ErrorPage {
        Objects.requireNonNull(url);
        Objects.requireNonNull(e);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || other instanceof GitHubIssuePage page
                && this.url.equals(page.url());
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public URI uri() {
        return this.url;
    }
}
