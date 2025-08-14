package com.mialliance.mind.base;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface NullablePredicate<T> {
    boolean test(@Nullable T val);
}
