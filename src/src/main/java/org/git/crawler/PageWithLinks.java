package org.git.crawler;

import org.git.page.Page;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public record PageWithLinks(Page page, Set<URI> links) {
    public PageWithLinks {
        requireNonNull(page);
        requireNonNull(links);
        links = Set.copyOf(links);
    }

    public PageWithLinks(@NotNull Page page) {
        this(page, Set.of());
    }
}
