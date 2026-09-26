package flaxbeard.immersivepetroleum;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

public final class MinecraftTestBootstrap{
	private static boolean initialized;

	private MinecraftTestBootstrap(){
	}

	public static synchronized void initialize(){
		if(!initialized){
			SharedConstants.tryDetectVersion();
			Bootstrap.bootStrap();
			initialized = true;
		}
	}
}
