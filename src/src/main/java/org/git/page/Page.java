package org.git.page;

import java.net.URI;

public sealed interface Page permits ErrorPage, SuccessfulPage {
    URI uri();
}

