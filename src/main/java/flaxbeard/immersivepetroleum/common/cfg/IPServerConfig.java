package flaxbeard.immersivepetroleum.common.cfg;

import java.lang.reflect.Field;
import java.util.List;

import com.electronwill.nightconfig.core.Config;
import com.google.common.base.Preconditions;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.energy.FuelHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = ImmersivePetroleum.MODID, bus = Bus.MOD)
public class IPServerConfig{
	public static final Extraction EXTRACTION;
	public static final Refining REFINING;
	public static final Generation GENERATION;
	public static final Miscellaneous MISCELLANEOUS;
	
	public static final ForgeConfigSpec ALL;
	
	static{
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		
		EXTRACTION = new Extraction(builder);
		REFINING = new Refining(builder);
		GENERATION = new Generation(builder);
		MISCELLANEOUS = new Miscellaneous(builder);
		
		ALL = builder.build();
	}
	
	private static Config rawConfig;
	public static Config getRawConfig(){
		if(rawConfig == null){
			try{
				Field childConfig = ForgeConfigSpec.class.getDeclaredField("childConfig");
				childConfig.setAccessible(true);
				rawConfig = (Config) childConfig.get(ALL);
				Preconditions.checkNotNull(rawConfig);
			}catch(Exception x){
				throw new RuntimeException(x);
			}
		}
		return rawConfig;
	}
	
	public static class Extraction{
		public final ConfigValue<Integer> pumpjack_consumption;
		public final ConfigValue<Integer> pumpjack_speed;
		public final ConfigValue<Integer> derrick_consumption;
		Extraction(ForgeConfigSpec.Builder builder){
			builder.push("Extraction");
			
			pumpjack_consumption = builder
				.comment("The Flux the Pumpjack requires each tick to pump", "Default: 1024")
				.define("pumpjack_consumption", 1024);
			
			pumpjack_speed = builder
				.comment("The amount of mB of oil a Pumpjack extracts per tick", "Default: 15")
				.define("pumpjack_speed", 15);
			
			derrick_consumption = builder
				.comment("The Flux the Derrick requires each tick to operate", "Default: 512")
				.define("derrick_consumption", 512);
			
			builder.pop();
		}
	}
	
	public static class Refining{
		public final ConfigValue<Double> distillationTower_energyModifier;
		public final ConfigValue<Double> distillationTower_timeModifier;
		public final ConfigValue<Double> cokerUnit_energyModifier;
		public final ConfigValue<Double> cokerUnit_timeModifier;
		public final ConfigValue<Double> hydrotreater_energyModifier;
		public final ConfigValue<Double> hydrotreater_timeModifier;
		Refining(ForgeConfigSpec.Builder builder){
			builder.push("Refining");
			
			distillationTower_energyModifier = builder
				.comment("A modifier to apply to the energy costs of every Distillation Tower recipe", "Default: 1.0")
				.define("distillationTower_energyModifier", 1.0);
	
			distillationTower_timeModifier = builder
				.comment("A modifier to apply to the time of every Distillation recipe. Can't be lower than 1", "Default: 1.0")
				.define("distillationTower_timeModifier", 1.0);
	
			cokerUnit_energyModifier = builder
				.comment("A modifier to apply to the energy costs of every Coker Tower recipe", "Default: 1.0")
				.define("cokerUnit_energyModifier", 1.0);
	
			cokerUnit_timeModifier = builder
				.comment("A modifier to apply to the time of every Coker recipe. Can't be lower than 1", "Default: 1.0")
				.define("cokerUnit_timeModifier", 1.0);
	
			hydrotreater_energyModifier = builder
				.comment("A modifier to apply to the energy costs of every High-Pressure Refinery Unit recipe", "Default: 1.0")
				.define("hydrotreater_energyModifier", 1.0);
	
			hydrotreater_timeModifier = builder
				.comment("A modifier to apply to the time of every High-Pressure Refinery Unit recipe. Can't be lower than 1", "Default: 1.0")
				.define("hydrotreater_timeModifier", 1.0);
			
			builder.pop();
		}
	}
	
	public static class Generation{
		public final ConfigValue<List<? extends String>> fuels;
		Generation(ForgeConfigSpec.Builder builder){
			builder.push("Generation");
			
			fuels = builder
				.comment("List of Portable Generator fuels. Format: fluid_name, mb_used_per_second, flux_produced_per_tick")
				.defineList("generator_fuels",
				    List.of("immersivepetroleum:naphtha, 9, 256",
						    "immersivepetroleum:gasoline, 6, 256",
							"immersivepetroleum:benzene, 6, 256"), o -> true);
			
			builder.pop();
		}
	}
	
	public static class Miscellaneous{
		public final ConfigValue<List<? extends String>> boat_fuels;
		public final BooleanValue autounlock_recipes;
		public final BooleanValue asphalt_speed;
		Miscellaneous(ForgeConfigSpec.Builder builder){
			builder.push("Miscellaneous");
			
			boat_fuels = builder
					.comment("List of Motorboat fuels. Format: fluid_name, mb_used_per_tick")
					.defineList("boat_fuels",
							List.of("immersivepetroleum:gasoline, 1",
									"immersivepetroleum:naphtha, 2",
									"immersivepetroleum:benzene, 2"), o -> true);
			
			autounlock_recipes = builder
					.comment("Automatically unlock IP recipes for new players", "Default: true")
					.define("autounlock_recipes", true);
			
			asphalt_speed = builder
					.comment("Set to false to disable the asphalt block boosting player speed", "Default: true")
					.define("asphalt_speed", true);
			
			builder.pop();
		}
	}
	
	@SubscribeEvent
	public static void onConfigReload(ModConfigEvent ev){
		FuelHandler.onConfigReload(ev);
	}
}
