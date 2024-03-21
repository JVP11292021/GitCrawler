package org.git.page;

import java.net.URI;
import java.util.Objects;

public record ExternalPage(URI url, String content) implements SuccessfulPage {
    public ExternalPage {
        Objects.requireNonNull(url);
        Objects.requireNonNull(content);
        if (!content.isBlank())
            throw new IllegalArgumentException("Illegal argument for content");
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof ExternalPage page && Objects.equals(url, page.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, content);
    }

    @Override
    public URI uri() {
        return this.url;
    }
}
