package org.git;

import java.net.URI;

public sealed interface Page permits ErrorPage, SuccessfulPage {
    URI uri();
}

