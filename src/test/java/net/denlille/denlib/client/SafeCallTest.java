// This file was made with Claude Code
package net.denlille.denlib.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SafeCallTest {
    @Test
    void consumerExceptionIsContained() {
        boolean ok = assertDoesNotThrow(() -> SafeCall.run("test callback", () -> {
            throw new IllegalStateException("consumer bug");
        }));

        assertFalse(ok);
    }

    @Test
    void normalCallRuns() {
        int[] calls = {0};

        assertTrue(SafeCall.run("test callback", () -> calls[0]++));
        assertEquals(1, calls[0]);
    }
}
