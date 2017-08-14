package com.loopperfect.buckaroo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;

public final class BuckarooConfig {

    public final ImmutableList<RemoteCookbook> cookbooks;
    public final Optional<URL> analyticsServer;
    public final Optional<BitBucketServerConfig> bbsConfig;

    private BuckarooConfig(final ImmutableList<RemoteCookbook> cookbooks, final Optional<URL> analyticsServer) {
        this(cookbooks, analyticsServer, Optional.empty());
    }

    private BuckarooConfig(final ImmutableList<RemoteCookbook> cookbooks, final Optional<URL> analyticsServer, final Optional<BitBucketServerConfig> bbsConfig) {
        this.cookbooks = Preconditions.checkNotNull(cookbooks);
        this.analyticsServer = Preconditions.checkNotNull(analyticsServer);
        this.bbsConfig = Preconditions.checkNotNull(bbsConfig);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof BuckarooConfig)) {
            return false;
        }
        final BuckarooConfig other = (BuckarooConfig) obj;
        return Objects.equals(cookbooks, other.cookbooks) &&
            Objects.equals(analyticsServer, other.analyticsServer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cookbooks, analyticsServer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("cookbooks", cookbooks)
            .add("analyticsServer", analyticsServer)
            .toString();
    }

    public static BuckarooConfig of(final ImmutableList<RemoteCookbook> cookBooks, final Optional<URL> analyticsServer, final Optional<BitBucketServerConfig> bbsConfig) {
        return new BuckarooConfig(cookBooks, analyticsServer, bbsConfig);
    }

    public static BuckarooConfig of(final ImmutableList<RemoteCookbook> cookBooks, final Optional<URL> analyticsServer) {
        return new BuckarooConfig(cookBooks, analyticsServer, Optional.empty());
    }

    public static BuckarooConfig of(final ImmutableList<RemoteCookbook> cookBooks, final URL analyticsServer) {
        return new BuckarooConfig(cookBooks, Optional.of(analyticsServer));
    }

    public static BuckarooConfig of(final ImmutableList<RemoteCookbook> cookBooks) {
        return new BuckarooConfig(cookBooks, Optional.empty());
    }
}
