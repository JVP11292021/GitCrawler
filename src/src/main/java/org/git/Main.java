package org.git;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.git.crawler.PageTreeFactory;
import org.git.operations.Prettify;
import org.git.operations.ResultServer;
import org.git.operations.Statistician;
import org.git.page.ErrorPage;
import org.git.page.Page;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.nio.file.Path;

import static java.lang.StringTemplate.RAW;
import static org.git.Util.join;

//https://github.com/nipafx/modern-java-demo/blob/main/src/main/java/dev/nipafx/demo/modern/page/GitHubPrPage.java

@Slf4j
public class Main {
    /**
     * @param args 0: path to GitHub issue or PR page
     *             1: depth of tree that will be built
     */
    @SneakyThrows // URISyntaxException, InterruptedException, IOException
    public static void main(String[] args) {
        Configuration config = Configuration.parse(args);

        System.out.printf("%nTo see virtual threads in action, run this while the app is resolving a bunch of links:%n");
        System.out.printf("jcmd %s Thread.dump_to_file -format=json -overwrite threads.json%n%n", ProcessHandle.current().pid());

        HttpClient client = HttpClient.newHttpClient();
        PageTreeFactory factory = new PageTreeFactory(client);
        Page rootPage = factory.createPage(config.seedUrl(), config.depth());

        System.out.println(join(RAW."""

				---

				\{Statistician.evaluate(rootPage)}

				\{Prettify.pageList(rootPage)}

        """));

        ResultServer.serve(rootPage, Path.of("serve"), 8085);
    }

    private record Configuration(URI seedUrl, int depth) {

        @Contract("_ -> new")
        static @NotNull Configuration parse(String @NotNull [] args) throws URISyntaxException {
            if (args.length < 2)
                throw new IllegalArgumentException("Please specify the seed URL and depth.");
            URI seedUrl = new URI(args[0]);
            int depth = Integer.parseInt(args[1]);
            return new Configuration(seedUrl, depth);
        }

    }
}
