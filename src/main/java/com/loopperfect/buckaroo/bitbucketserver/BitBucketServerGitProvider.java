package com.loopperfect.buckaroo.bitbucketserver;

import com.google.common.base.Preconditions;
import com.loopperfect.buckaroo.BuckarooConfig;
import com.loopperfect.buckaroo.GitCommitHash;
import com.loopperfect.buckaroo.GitProvider;
import com.loopperfect.buckaroo.Identifier;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Optional;

public final class BitBucketServerGitProvider implements GitProvider {

    private static String sshCloneUrl = "";
    private static String browseUrl = "";

    private BitBucketServerGitProvider() {
    }

    @Override
    public Identifier recipeIdentifierPrefix() {
        return Identifier.of("bitbucketserver");
    }

    @Override
    public String gitURL(final Identifier owner, final Identifier project) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(project);
        return sshCloneUrl + owner.name + "/" + project.name + ".git";
    }

    @Override
    public URI projectURL(final Identifier owner, final Identifier project) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(project);
        try {
            return new URI(browseUrl + owner.name + "/repos/" + project.name);
        } catch (final URISyntaxException e) {
            // Should not happen because identifiers are validated in their constructor.
            throw new RuntimeException(e);
        }
    }

    @Override
    public URI zipURL(final Identifier owner, final Identifier project, final GitCommitHash commit) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(project);
        Preconditions.checkNotNull(commit);
        try {
            return new URI(browseUrl + "rest/api/latest/projects/" + owner.name + "/repos/" + project.name + "/archive?at=" + commit.hash + "&format=zip");
        } catch (final URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Path> zipSubPath(final FileSystem fs, final Identifier owner, final Identifier project, final GitCommitHash commit) {
        Optional<Path> p = Optional.empty();
        return p;
    }

    public static BitBucketServerGitProvider of() {
        return new BitBucketServerGitProvider();
    }

    public static void setConfig(final BuckarooConfig config) {
        if (config.bbsConfig.isPresent()) {
            sshCloneUrl = Preconditions.checkNotNull(config.bbsConfig.get().sshCloneUrl);
            browseUrl = Preconditions.checkNotNull(config.bbsConfig.get().browseUrl);

            if (!sshCloneUrl.endsWith("/")) {
                sshCloneUrl += "/";
            }
            if (!browseUrl.endsWith("/")) {
                browseUrl += "/";
            }
        }
    }
}
