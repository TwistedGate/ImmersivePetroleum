package flaxbeard.immersivepetroleum.common;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistrationBuilder;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockItem;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.MultiblockBEType;
import blusunrize.immersiveengineering.common.blocks.multiblocks.component.MultiblockGui;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import com.google.common.collect.ImmutableSet;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.blocks.IPBlockBase;
import flaxbeard.immersivepetroleum.common.blocks.IPMultiblockBase;
import flaxbeard.immersivepetroleum.common.util.IPEffects.IPEffect;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class IPRegisters{
	public static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, ImmersivePetroleum.MODID);
	public static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, ImmersivePetroleum.MODID);
	private static final DeferredRegister<Fluid> FLUID_REGISTER = DeferredRegister.create(ForgeRegistries.FLUIDS, ImmersivePetroleum.MODID);
	private static final DeferredRegister<BlockEntityType<?>> TE_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ImmersivePetroleum.MODID);
	private static final DeferredRegister<EntityType<?>> ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ImmersivePetroleum.MODID);
	public static final DeferredRegister<MenuType<?>> MENU_REGISTER = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ImmersivePetroleum.MODID);
	private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ImmersivePetroleum.MODID);
	private static final DeferredRegister<MobEffect> MOB_EFFECT = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ImmersivePetroleum.MODID);
	private static final DeferredRegister<SoundEvent> SOUND_EVENT = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ImmersivePetroleum.MODID);
	private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPE = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ImmersivePetroleum.MODID);
	private static final DeferredRegister<EntityType<?>> ENTITY_TYPE = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ImmersivePetroleum.MODID);
	public static final DeferredRegister<FluidType> FLUID_TYPE = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, ImmersivePetroleum.MODID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ImmersivePetroleum.MODID);
	public static final DeferredRegister<Feature<?>> FEATURE_REGISTER = DeferredRegister.create(ForgeRegistries.FEATURES, ImmersivePetroleum.MODID);

	private static final List<Consumer<IEventBus>> MOD_BUS_CALLBACKS = new ArrayList<>();
	
	public static void addRegistersToEventBus(IEventBus eventBus){
		FLUID_REGISTER.register(eventBus);
		BLOCK_REGISTER.register(eventBus);
		ITEM_REGISTER.register(eventBus);
		TE_REGISTER.register(eventBus);
		ENTITY_REGISTER.register(eventBus);
		MENU_REGISTER.register(eventBus);
		RECIPE_SERIALIZERS.register(eventBus);
		MOB_EFFECT.register(eventBus);
		SOUND_EVENT.register(eventBus);
		PARTICLE_TYPE.register(eventBus);
		ENTITY_TYPE.register(eventBus);
		FLUID_TYPE.register(eventBus);
		CREATIVE_TABS.register(eventBus);
		FEATURE_REGISTER.register(eventBus);

		//MOD_BUS_CALLBACKS.forEach(e -> e.accept(eventBus));
	}

	public static <S extends IMultiblockState> MultiblockRegistration<S> registerMetalMultiblock(String name, IMultiblockLogic<S> logic, Supplier<TemplateMultiblock> structure){
		return registerMetalMultiblock(name, logic, structure, null);
	}

	public static <S extends IMultiblockState> MultiblockRegistration<S> registerMetalMultiblock(String name, IMultiblockLogic<S> logic, Supplier<TemplateMultiblock> structure, @Nullable Consumer<MultiblockBuilder<S>> extras){
		// @formatter:off
		BlockBehaviour.Properties prop = BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL)
				.strength(3, 15)
				.requiresCorrectToolForDrops()
				.isViewBlocking((state, blockReader, pos) -> false)
				.noOcclusion()
				.dynamicShape()
				.pushReaction(PushReaction.BLOCK);
		// @formatter:on

		return registerMultiblock(name, logic, structure, extras, prop);
	}

	public static <S extends IMultiblockState> MultiblockRegistration<S> registerMultiblock(String name, IMultiblockLogic<S> logic, Supplier<TemplateMultiblock> structure, @Nullable Consumer<MultiblockBuilder<S>> extras, BlockBehaviour.Properties prop){
		MultiblockBuilder<S> builder = new MultiblockBuilder<>(logic, name)
				.structure(structure)
				.defaultBEs(TE_REGISTER)
				.customBlock(BLOCK_REGISTER, ITEM_REGISTER,
						mb -> new IPMultiblockBase<>(prop, mb),
						MultiblockItem::new);
				//.defaultBlock(BLOCK_REGISTER, ITEM_REGISTER, prop);

		if(extras != null){
			extras.accept(builder);
		}

		return builder.build();//builder.build(MOD_BUS_CALLBACKS::add);
	}

	protected static class MultiblockBuilder<S extends IMultiblockState> extends MultiblockRegistrationBuilder<S, MultiblockBuilder<S>> {
		public MultiblockBuilder(IMultiblockLogic<S> logic, String name){
			super(logic, ResourceUtils.ip(name));
		}

		public MultiblockBuilder<S> redstone(IMultiblockComponent.StateWrapper<S, RedstoneControl.RSState> getState, BlockPos... positions){
			redstoneAware();
			return selfWrappingComponent(new RedstoneControl<>(getState, positions));
		}

		public MultiblockBuilder<S> comparator(ComparatorManager<S> comparator){
			withComparator();
			return super.selfWrappingComponent(comparator);
		}

		public MultiblockBuilder<S> gui(IEMenuTypes.MultiblockContainer<S, ?> menu)
		{
			return component(new MultiblockGui<>(menu));
		}

		@Override
		protected MultiblockBuilder<S> self(){
			return this;
		}
	}

	/*@Deprecated
	public static <T extends Block> RegistryObject<Block, T> registerMultiblockBlock(String name, Supplier<T> blockConstructor){
		throw new UnsupportedOperationException();
	}*/

	public static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> blockConstructor){
		return registerBlock(name, blockConstructor, null);
	}
	
	public static <T extends Block> RegistryObject<T> registerMultiblockBlock(String name, Supplier<T> blockConstructor){
		return registerBlock(name, blockConstructor, block -> new BlockItem(block, new Item.Properties()));
	}
	
	public static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> blockConstructor, @Nullable Function<T, ? extends BlockItem> blockItem){
		RegistryObject<T> block = BLOCK_REGISTER.register(name, blockConstructor);
		if(blockItem != null){
			registerItem(name, () -> blockItem.apply(block.get()));
		}
		return block;
	}
	
	public static <T extends IPBlockBase> RegistryObject<T> registerIPBlock(String name, Supplier<T> blockConstructor){
		RegistryObject<T> block = BLOCK_REGISTER.register(name, blockConstructor);
		
		registerItem(name, () -> block.get().blockItemSupplier().get());
		
		return block;
	}
	
	public static <T extends Item> RegistryObject<T> registerItem(String name, Supplier<T> itemConstructor){
		return ITEM_REGISTER.register(name, itemConstructor);
	}
	
	public static <T extends Fluid> RegistryObject<T> registerFluid(String name, Supplier<T> fluidConstructor){
		return FLUID_REGISTER.register(name, fluidConstructor);
	}
	
	public static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> registerTE(String name, BlockEntityType.BlockEntitySupplier<T> factory, Supplier<? extends Block> valid){
		return TE_REGISTER.register(name, () -> new BlockEntityType<>(factory, ImmutableSet.of(valid.get()), null));
	}
	
	public static <T extends BlockEntity & IEBlockInterfaces.IGeneralMultiblock> MultiblockBEType<T> registerMultiblockTE(String name, MultiblockBEType.BEWithTypeConstructor<T> factory, Supplier<? extends Block> valid){
		return new MultiblockBEType<>(name, TE_REGISTER, factory, valid, state -> state.hasProperty(IEProperties.MULTIBLOCKSLAVE) && !state.getValue(IEProperties.MULTIBLOCKSLAVE));
	}
	
	public static <T extends EntityType<?>> RegistryObject<T> registerEntity(String name, Supplier<T> entityConstructor){
		return ENTITY_REGISTER.register(name, entityConstructor);
	}
	
	public static <T extends RecipeSerializer<?>> RegistryObject<T> registerSerializer(String name, Supplier<T> serializer){
		return RECIPE_SERIALIZERS.register(name, serializer);
	}
	
	public static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenu(String name, Supplier<MenuType<T>> factory){
		return MENU_REGISTER.register(name, factory);
	}
	
	public static <T extends IPEffect> RegistryObject<T> registerMobEffect(String name, Supplier<T> constructor){
		return MOB_EFFECT.register(name, constructor);
	}
	
	public static RegistryObject<SoundEvent> registerSoundEvent(String name){
		return SOUND_EVENT.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceUtils.ip(name)));
	}
	
	public static <PType extends ParticleType<?>> RegistryObject<PType> registerParticleType(String name, Supplier<PType> particleType){
		return PARTICLE_TYPE.register(name, particleType);
	}
	
	public static <EType extends EntityType<?>> RegistryObject<EType> registerEntityType(String name, Function<ResourceLocation, EType> entityType){
		return ENTITY_TYPE.register(name, () -> entityType.apply(ResourceUtils.ip(name)));
	}

	public static RegistryObject<CreativeModeTab> registerCreativeTab(String name, Supplier<CreativeModeTab> tab)
	{
		return CREATIVE_TABS.register(name, tab);
	}
	
	private IPRegisters(){
	}
}
