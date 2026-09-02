package io.github.pilzi.henrikdev.beans;

import org.jspecify.annotations.NonNull;

import java.util.List;

public record Response<T> (@NonNull Integer status,
                       @NonNull List<T> data) {
}
