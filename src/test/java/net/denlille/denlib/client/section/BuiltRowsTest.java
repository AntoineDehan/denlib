// This file was made with Claude Code
package net.denlille.denlib.client.section;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltRowsTest {
    @Test
    void rowsNotShownBySectionAreDropped() {
        // "a" was built by an earlier section; "b" and "c" by the section being built, which only shows "b".
        List<String> rows = new ArrayList<>(List.of("a", "b", "c"));

        BuiltRows.dropUnreachable(rows, 1, Set.of("b")::contains);

        assertEquals(List.of("a", "b"), rows);
    }

    @Test
    void sectionThatShowsNothingDropsAllItsRows() {
        List<String> rows = new ArrayList<>(List.of("a", "b", "c"));

        BuiltRows.dropUnreachable(rows, 1, row -> false);

        assertEquals(List.of("a"), rows);
    }
}
