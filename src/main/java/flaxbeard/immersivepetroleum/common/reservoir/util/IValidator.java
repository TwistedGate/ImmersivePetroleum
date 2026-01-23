package flaxbeard.immersivepetroleum.common.reservoir.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public interface IValidator{
	ResourceLocation location();
	
	default <T> boolean test(ResourceKey<T> key){
		return key != null && location().equals(key.location());
	}
	
	default String getString(){
		return location().toString();
	}
}
