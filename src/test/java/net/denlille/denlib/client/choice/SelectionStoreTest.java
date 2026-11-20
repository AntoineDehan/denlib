// This file was made with Claude Code
package net.denlille.denlib.client.choice;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SelectionStoreTest {
    @Test
    void survivesRebuild() {
        Object screen = new Object();
        Object otherScreen = new Object();
        ResourceLocation id = new ResourceLocation("test", "row");

        SelectionStore.put(screen, id, "a");

        assertEquals(Optional.of("a"), SelectionStore.get(screen, id));
        assertEquals(Optional.empty(), SelectionStore.get(otherScreen, id));
        assertEquals(Optional.empty(), SelectionStore.get(screen, new ResourceLocation("test", "other")));
    }
}
