package flaxbeard.immersivepetroleum;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import flaxbeard.immersivepetroleum.api.crafting.IPRecipeTypes;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.client.ClientProxy;
import flaxbeard.immersivepetroleum.common.CommonEventHandler;
import flaxbeard.immersivepetroleum.common.CommonProxy;
import flaxbeard.immersivepetroleum.common.ExternalModContent;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPContent.Fluids;
import flaxbeard.immersivepetroleum.common.IPRegisters;
import flaxbeard.immersivepetroleum.common.IPSaveData;
import flaxbeard.immersivepetroleum.common.IPToolShaders;
import flaxbeard.immersivepetroleum.common.cfg.IPClientConfig;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.crafting.RecipeReloadListener;
import flaxbeard.immersivepetroleum.common.network.IPPacketHandler;
import flaxbeard.immersivepetroleum.common.util.commands.IslandCommand;
import flaxbeard.immersivepetroleum.common.util.compat.computer.cctweaked.IPPeripheralProvider;
import flaxbeard.immersivepetroleum.common.util.loot.IPLootFunctions;
import flaxbeard.immersivepetroleum.common.world.IPWorldGen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(ImmersivePetroleum.MODID)
public class ImmersivePetroleum{
	public static final String MODID = "immersivepetroleum";
	
	public static final Logger log = LogManager.getLogger(MODID);
	
	public static final CreativeModeTab creativeTab = new CreativeModeTab(MODID){
		@Override
		@Nonnull
		public ItemStack makeIcon(){
			return new ItemStack(Fluids.CRUDEOIL.bucket().get());
		}
	};
	
	// Complete hack: DistExecutor::safeRunForDist intentionally tries to access the "wrong" supplier in dev, which
	// throws an error (rather than an exception) on J16 due to trying to load a client-only class. So we need to
	// replace the error with an exception in dev.
	public static <T> Supplier<T> bootstrapErrorToXCPInDev(Supplier<T> in){
		if(FMLLoader.isProduction())
			return in;
		return () -> {
			try{
				return in.get();
			}catch(BootstrapMethodError e){
				throw new RuntimeException(e);
			}
		};
	}
	
	public static final CommonProxy proxy = DistExecutor.safeRunForDist(bootstrapErrorToXCPInDev(() -> ClientProxy::new), bootstrapErrorToXCPInDev(() -> CommonProxy::new));
	
	public ImmersivePetroleum(){
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, IPServerConfig.ALL);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, IPClientConfig.ALL);
		
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
		
		MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
		MinecraftForge.EVENT_BUS.addListener(this::registerCommand);
		MinecraftForge.EVENT_BUS.addListener(this::addReloadListeners);
		
		IEventBus eBus = FMLJavaModLoadingContext.get().getModEventBus();
		IPRegisters.addRegistersToEventBus(eBus);
		
		IPContent.modConstruction();
		IPLootFunctions.modConstruction();
		IPRecipeTypes.modConstruction();
		
		MinecraftForge.EVENT_BUS.register(new IPWorldGen());
		IPWorldGen.init(eBus);
	}
	
	public void setup(FMLCommonSetupEvent event){
		proxy.setup();
		
		// ---------------------------------------------------------------------------------------------------------------------------------------------
		
		proxy.preInit();
		
		IPContent.preInit();
		IPPacketHandler.preInit();
		IPToolShaders.preInit();
		
		proxy.preInitEnd();
		
		// ---------------------------------------------------------------------------------------------------------------------------------------------
		
		IPContent.init(event);
		
		MinecraftForge.EVENT_BUS.register(new CommonEventHandler());
		
		proxy.init();
		
		if(ModList.get().isLoaded("computercraft")){
			IPPeripheralProvider.init();
		}
		
		// ---------------------------------------------------------------------------------------------------------------------------------------------
		
		proxy.postInit();
		
		ReservoirHandler.recalculateChances();
		ExternalModContent.init();
		
		proxy.registerContainersAndScreens();
	}
	
	public void loadComplete(FMLLoadCompleteEvent event){
		proxy.completed(event);
	}
	
	public void registerCommand(RegisterCommandsEvent event){
		LiteralArgumentBuilder<CommandSourceStack> ip = Commands.literal("ip");
		
		ip.then(IslandCommand.create());
		
		event.getDispatcher().register(ip);
	}
	
	public void addReloadListeners(AddReloadListenerEvent event){
		event.addListener(new RecipeReloadListener(event.getServerResources()));
	}
	
	public void serverStarted(ServerStartedEvent event){
		ServerLevel world = event.getServer().getLevel(Level.OVERWORLD);
		if(!world.isClientSide){
			IPSaveData worldData = world.getDataStorage().computeIfAbsent(IPSaveData::new, IPSaveData::new, IPSaveData.dataName);
			IPSaveData.setInstance(worldData);
		}
		
		ReservoirHandler.recalculateChances();
	}
}
