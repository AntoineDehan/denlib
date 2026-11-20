// This file was made with Claude Code
package net.denlille.denlib.client.choice;

/** Splits a number of buttons into lines of at most {@code perRow}. */
@org.jetbrains.annotations.ApiStatus.Internal
public final class ChoiceGrid {
    private ChoiceGrid() {
    }

    public static int[] rows(int count, int perRow) {
        int lines = (count + perRow - 1) / perRow;
        int[] sizes = new int[lines];
        for (int i = 0; i < lines; i++) {
            sizes[i] = Math.min(perRow, count - i * perRow);
        }
        return sizes;
    }
}
