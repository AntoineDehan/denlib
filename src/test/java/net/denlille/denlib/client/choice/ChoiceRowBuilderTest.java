// This file was made with Claude Code
package net.denlille.denlib.client.choice;

import net.denlille.denlib.client.section.SectionContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ChoiceRowBuilderTest {
    private static ChoiceRow.Builder<String> builder() {
        return ChoiceRow.builder(new SectionContext(null, null), new ResourceLocation("test", "row"),
                Component.empty());
    }

    @Test
    void iconBeforeAnyOptionFails() {
        assertThrows(IllegalStateException.class, () -> builder().icon(() -> null));
    }

    @Test
    void tooltipBeforeAnyOptionFails() {
        assertThrows(IllegalStateException.class, () -> builder().tooltip(Component.empty()));
    }
}
