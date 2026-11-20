// This file was made with Claude Code
package net.denlille.denlib;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(DenLib.MODID)
public final class DenLib {
    public static final String MODID = "denlib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DenLib() {
        LOGGER.info("denlib loaded");
    }
}
