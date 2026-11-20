// This file was made with Claude Code
package net.denlille.denlib.client.choice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ChoiceGridTest {
    @Test
    void splitsIntoRowsOfThree() {
        assertArrayEquals(new int[]{3}, ChoiceGrid.rows(3, 3));
        assertArrayEquals(new int[]{3, 2}, ChoiceGrid.rows(5, 3));
        assertArrayEquals(new int[]{1}, ChoiceGrid.rows(1, 3));
        assertArrayEquals(new int[]{}, ChoiceGrid.rows(0, 3));
    }
}
