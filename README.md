# Micronaut RxJava2

[![Maven Central](https://img.shields.io/maven-central/v/io.micronaut.rxjava2/micronaut-rxjava2.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.micronaut.rxjava2%22%20AND%20a:%22micronaut-rxjava2%22)
[![Build Status](https://github.com/micronaut-projects/micronaut-rxjava2/workflows/Java%20CI/badge.svg)](https://github.com/micronaut-projects/micronaut-rxjava2/actions)
[![Revved up by Develocity](https://img.shields.io/badge/Revved%20up%20by-Develocity-06A0CE?logo=Gradle&labelColor=02303A)](https://ge.micronaut.io/scans)

Integration between Micronaut and RxJava 2

## Documentation

See the [Documentation](https://micronaut-projects.github.io/micronaut-rxjava2/latest/guide/) for more information. 

See the [Snapshot Documentation](https://micronaut-projects.github.io/micronaut-rxjava2/snapshot/guide/) for the current development docs.

## Snapshots and Releases

Snaphots are automatically published to [Sonatype Snapshots](https://oss.sonatype.org/content/repositories/snapshots/) using [Github Actions](https://github.com/micronaut-projects/micronaut-rxjava2/actions).

See the documentation in the [Micronaut Docs](https://docs.micronaut.io/latest/guide/index.html#usingsnapshots) for how to configure your build to use snapshots.

Releases are published to Maven Central via [Github Actions](https://github.com/micronaut-projects/micronaut-rxjava2/actions).

Releases are completely automated. To perform a release use the following steps:

* [Publish the draft release](https://github.com/micronaut-projects/micronaut-rxjava2/releases). There should be already a draft release created, edit and publish it. The Git Tag should start with `v`. For example `v1.0.0`.
* [Monitor the Workflow](https://github.com/micronaut-projects/micronaut-rxjava2/actions?query=workflow%3ARelease) to check it passed successfully.
* If everything went fine, [publish to Maven Central](https://github.com/micronaut-projects/micronaut-rxjava2/actions?query=workflow%3A"Maven+Central+Sync").
* Celebrate!