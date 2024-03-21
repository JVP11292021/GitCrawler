package org.git;

public sealed interface SuccessfulPage extends Page permits ExternalPage, GitHubPage {
    String content();
}
