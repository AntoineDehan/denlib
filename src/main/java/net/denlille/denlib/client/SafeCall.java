// This file was made with Claude Code
package net.denlille.denlib.client;

import net.denlille.denlib.DenLib;
import org.jetbrains.annotations.ApiStatus;

/** Runs a consumer mod's callback without letting its bug crash the creation screen. */
@ApiStatus.Internal
public final class SafeCall {
    private SafeCall() {
    }

    /** Returns false (and logs) if {@code action} threw. */
    public static boolean run(String what, Runnable action) {
        try {
            action.run();
            return true;
        } catch (RuntimeException e) {
            DenLib.LOGGER.error("{} threw; ignored so the world creation screen keeps working", what, e);
            return false;
        }
    }
}
