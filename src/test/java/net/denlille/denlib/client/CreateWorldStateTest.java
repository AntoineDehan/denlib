// This file was made with Claude Code
package net.denlille.denlib.client;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CreateWorldStateTest {
    private static final ResourceLocation ROW = new ResourceLocation("test", "row");

    /** A creation screen stand-in: the real one gets the state through a mixin-added field. */
    private static final class FakeScreen implements CreateWorldStateHolder {
        private final CreateWorldState state = new CreateWorldState();

        @Override
        public CreateWorldState denlib$state() {
            return this.state;
        }
    }

    @Test
    void selectionIsPerScreen() {
        FakeScreen screen = new FakeScreen();
        CreateWorldState.of(screen).select(ROW, "a");

        assertEquals(Optional.of("a"), CreateWorldState.of(screen).selection(ROW));
        assertEquals(Optional.empty(), CreateWorldState.of(new FakeScreen()).selection(ROW));
        assertEquals(Optional.empty(), CreateWorldState.of(new Object()).selection(ROW));
    }

    @Test
    void invalidSelectionIsForgotten() {
        CreateWorldState state = new CreateWorldState();
        state.select(ROW, "removed");

        assertEquals(Optional.empty(), state.<String>validSelection(ROW, value -> !value.equals("removed")));
        assertEquals(Optional.empty(), state.selection(ROW));

        state.select(ROW, "kept");
        assertEquals(Optional.of("kept"), state.<String>validSelection(ROW, value -> true));
    }

    @Test
    void screenIsCollectableEvenWhenItsStateReferencesIt() throws InterruptedException {
        FakeScreen screen = new FakeScreen();
        // Consumer callbacks commonly capture the screen: simulate a state that references its screen.
        CreateWorldState.of(screen).select(ROW, screen);
        WeakReference<FakeScreen> ref = new WeakReference<>(screen);
        screen = null;

        for (int i = 0; i < 50 && ref.get() != null; i++) {
            System.gc();
            Thread.sleep(10);
        }
        assertNull(ref.get(), "the creation screen must not be retained by denlib");
    }
}
