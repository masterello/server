package com.masterello.worker.domain;

import java.util.List;

import static java.util.Collections.emptyList;

public record FullWorkerPage(List<FullWorkerProjection> items, long total){

    public static FullWorkerPage emptyPage(long total) {
        return new FullWorkerPage(emptyList(), total);
    }
}
