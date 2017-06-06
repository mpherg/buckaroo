package com.loopperfect.buckaroo.tasks;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.loopperfect.buckaroo.*;
import com.loopperfect.buckaroo.io.EvenMoreFiles;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class InstallExistingTasks {

    private InstallExistingTasks() {

    }

    private static Observable<DownloadProgress> downloadResolvedDependency(final FileSystem fs, final ResolvedDependency resolvedDependency, final Path target) {

        Preconditions.checkNotNull(fs);
        Preconditions.checkNotNull(resolvedDependency);
        Preconditions.checkNotNull(target);

        final Observable<DownloadProgress> downloadSourceCode = resolvedDependency.source.join(
            gitCommit -> Observable.error(new IOException("Git commit not supported yet. ")),
            remoteArchive -> CommonTasks.downloadRemoteArchive(fs, remoteArchive, target));

        final Path buckFilePath = fs.getPath(target.toString(), "BUCK");
        final Observable<DownloadProgress> downloadBuckFile = Files.exists(buckFilePath) ?
            Observable.empty() :
            resolvedDependency.buckResource
                .map(x -> CommonTasks.downloadRemoteFile(fs, x, buckFilePath))
                .orElse(Observable.empty());

        final Path buckarooDepsFilePath = fs.getPath(target.toString(), "BUCKAROO_DEPS");
        final Observable<DownloadProgress> writeBuckarooDeps = MoreObservables.fromAction(() -> {
            EvenMoreFiles.writeFile(
                buckarooDepsFilePath,
                CommonTasks.generateBuckarooDeps(resolvedDependency.dependencies),
                Charset.defaultCharset(),
                true);
        });

        return Observable.concat(
            downloadSourceCode,
            downloadBuckFile,
            writeBuckarooDeps);
    }

    private static Observable<DownloadProgress> installDependencyLock(final FileSystem fs, final DependencyLock lock) {

        Preconditions.checkNotNull(fs);
        Preconditions.checkNotNull(lock);

        final Path dependencyFolder = fs.getPath(
            "buckaroo", CommonTasks.toFolderName(lock.identifier)).toAbsolutePath();

        return downloadResolvedDependency(fs, lock.origin, dependencyFolder);
    }

    public static Observable<Event> installExistingDependenciesInWorkingDirectory(final FileSystem fs) {

        Preconditions.checkNotNull(fs);

        final Path lockFilePath = fs.getPath("buckaroo.lock.json").toAbsolutePath();

        return Observable.concat(

            // Do we have a lock file?
            Single.fromCallable(() -> Files.exists(lockFilePath)).flatMapObservable(hasBuckarooLockFile -> {
                if (hasBuckarooLockFile) {
                    // No need to generate one
                    return Observable.empty();
                }
                // Generate a lock file
                return ResolveTasks.resolveDependenciesInWorkingDirectory(fs);
            }),

            MoreSingles.chainObservable(

                // Read the lock file
                CommonTasks.readLockFile(fs.getPath("buckaroo.lock.json").toAbsolutePath())
                    .map(ReadLockFileEvent::of),

                (ReadLockFileEvent event) -> {

                    // Install each entry
                    final ImmutableList<Observable<Map.Entry<DependencyLock, DownloadProgress>>> installs = event.locks.entries()
                        .stream()
                        .map(dependencyLock -> installDependencyLock(fs, dependencyLock)
                            .map(x -> Maps.immutableEntry(dependencyLock, x)))
                        .collect(ImmutableList.toImmutableList());

                    return MoreObservables.parallel(installs)
                        .map(xs -> DependencyInstallationProgress.of(
                            xs.stream()
                                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue))));
                }
            ));
    }
}