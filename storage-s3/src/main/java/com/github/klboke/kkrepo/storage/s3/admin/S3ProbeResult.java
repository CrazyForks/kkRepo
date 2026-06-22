package com.github.klboke.kkrepo.storage.s3.admin;

public record S3ProbeResult(
    boolean ok,
    String objectKey,
    String message,
    S3BucketSummary summary) {
}
