package org.warnickwar.components;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

	public static final String MODID = "componentapi";
	public static final String MOD_NAME = "ComponentAPI";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	public static boolean isServerRunning = false;

	public enum PacketIDs {
		TEMP("TEMP");

		private final ResourceLocation id;

		PacketIDs(String name) {
			id = new ResourceLocation(MODID, name);
		}

		PacketIDs(ResourceLocation loc) {
			id = loc;
		}

		public ResourceLocation getLocation() {
			return id;
		}
	}
}