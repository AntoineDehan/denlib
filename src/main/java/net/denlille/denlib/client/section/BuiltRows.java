// This file was made with Claude Code
package net.denlille.denlib.client.section;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.Predicate;

/** Keeps the choice rows of a section only if the section actually shows them. */
@ApiStatus.Internal
public final class BuiltRows {
    private BuiltRows() {
    }

    /**
     * Drops the rows registered from index {@code from} on (those built by the section just built) that
     * the section does not show: a required row that is shown nowhere would block creation invisibly.
     */
    public static <R> void dropUnreachable(List<R> rows, int from, Predicate<R> reachable) {
        rows.subList(from, rows.size()).removeIf(reachable.negate());
    }
}
