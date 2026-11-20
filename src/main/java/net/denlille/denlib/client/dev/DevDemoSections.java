// This file was made with Claude Code
package net.denlille.denlib.client.dev;

import net.denlille.denlib.DenLib;
import net.denlille.denlib.client.choice.ChoiceRow;
import net.denlille.denlib.client.section.CreateWorldSections;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Two demo sections to exercise the lib in the dev client only (never registered in production). */
@Mod.EventBusSubscriber(modid = DenLib.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DevDemoSections {
    private static final ResourceLocation DIFFICULTY = new ResourceLocation(DenLib.MODID, "demo_difficulty");
    private static final ResourceLocation SIZE = new ResourceLocation(DenLib.MODID, "demo_size");
    private static final String[] DIFFICULTIES = {"peaceful", "easy", "normal", "hard", "nightmare"};
    private static final String[] SIZES = {"small", "medium", "large"};

    private DevDemoSections() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        if (FMLEnvironment.production) {
            return;
        }
        CreateWorldSections.register(DIFFICULTY, 0, ctx -> {
            ChoiceRow.Builder<String> row = ChoiceRow.<String>builder(ctx, DIFFICULTY,
                    Component.translatable("denlib.demo.difficulty"));
            for (String id : DIFFICULTIES) {
                row.option(id, Component.translatable("denlib.demo.difficulty." + id));
            }
            return row.description(id -> Component.translatable("denlib.demo.difficulty." + id + ".desc"))
                    .onCreate(id -> DenLib.LOGGER.info("[demo] difficulty armed: {}", id))
                    .build();
        });
        CreateWorldSections.register(SIZE, 10, ctx -> {
            ChoiceRow.Builder<String> row = ChoiceRow.<String>builder(ctx, SIZE,
                    Component.translatable("denlib.demo.size"));
            for (String id : SIZES) {
                row.option(id, Component.translatable("denlib.demo.size." + id))
                        .tooltip(Component.translatable("denlib.demo.size." + id + ".tooltip"));
            }
            return row.onCreate(id -> DenLib.LOGGER.info("[demo] size armed: {}", id)).build();
        });
        DenLib.LOGGER.info("denlib dev demo sections registered");
    }
}
