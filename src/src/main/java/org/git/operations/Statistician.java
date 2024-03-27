package org.git.operations;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.git.page.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Data
public class Statistician {
    private final Set<Page> evaluatedPages;

    private int numOfIssues;
    private int numOfPrs;
    private int numOfExternalLinks;
    private int numOfErrors;

    private Statistician() {
        this.evaluatedPages = new HashSet<>();
    }

    public static @NotNull Stats evaluate(@NotNull Page rootPage) {
        Statistician statistician = new Statistician();
        statistician.evaluateTree(rootPage);
        return statistician.result();
    }

    private void evaluateTree(Page page) {
        if (page instanceof GitHubPage ghPage)
            ghPage.subtree().forEach(this::evaluatePage);
        else
            evaluatePage(page);
    }

    private void evaluatePage(Page page) {
        if (evaluatedPages.contains(page))
            return;
        evaluatedPages.add(page);

        switch (page) {
            case ErrorPage _ -> this.numOfErrors++;
            case ExternalPage _ -> this.numOfExternalLinks++;
            case GitHubIssuePage _ -> this.numOfIssues++;
            case GitHubPrPage _ -> this.numOfPrs++;
        }
    }

    @Contract(" -> new")
    private @NotNull Stats result() {
        return new Stats(this.numOfIssues, this.numOfPrs, this.numOfExternalLinks, this.numOfErrors);
    }

    public record Stats(
            int numberOfIssues,
            int numberOfPrs,
            int numberOfExternalLinks,
            int numberOfErrors)
    { }

}
