package org.git.page;

public sealed interface SuccessfulPage extends Page permits ExternalPage, GitHubPage {
    String content();
}
