package flaxbeard.immersivepetroleum.common.fluids;

import com.google.common.collect.ImmutableList;
import flaxbeard.immersivepetroleum.common.IPRegisters;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class IPFluid extends FlowingFluid{
	public static final List<IPFluidEntry> FLUIDS = new ArrayList<>();
	
	public static IPFluidEntry makeFluid(String name, int density, int viscosity){
		return makeFluid(name, density, viscosity, 0.014D);
	}
	
	public static <B extends IPFluidBlock> IPFluidEntry makeFluid(String name, int density, int viscosity, BiFunction<IPFluidEntry, Block.Properties, B> blockFactory){
		return makeFluid(name, density, viscosity, 0.014D, blockFactory);
	}
	
	public static <S extends IPFluid> IPFluidEntry makeFluid(String name, int density, int viscosity, Function<IPFluidEntry, S> sourceFactory){
		return makeFluid(name, density, viscosity, 0.014D, sourceFactory);
	}
	
	public static <S extends IPFluid, B extends IPFluidBlock> IPFluidEntry makeFluid(String name, int density, int viscosity, Function<IPFluidEntry, S> sourceFactory, BiFunction<IPFluidEntry, Block.Properties, B> blockFactory){
		return makeFluid(name, density, viscosity, 0.014D, sourceFactory, blockFactory);
	}
	
	
	public static IPFluidEntry makeFluid(String name, int density, int viscosity, double motionScale){
		IPFluidEntry entry = IPFluidEntry.make(name, IPFluid::new, IPFluid.Flowing::new, IPFluid.IPFluidBlock::new, builder(density, viscosity, motionScale));
		return entry;
	}
	
	public static <B extends IPFluidBlock> IPFluidEntry makeFluid(String name, int density, int viscosity, double motionScale, BiFunction<IPFluidEntry, Block.Properties, B> blockFactory){
		IPFluidEntry entry = IPFluidEntry.make(name, IPFluid::new, IPFluid.Flowing::new, blockFactory, builder(density, viscosity, motionScale));
		return entry;
	}
	
	public static <S extends IPFluid> IPFluidEntry makeFluid(String name, int density, int viscosity, double motionScale, Function<IPFluidEntry, S> sourceFactory){
		IPFluidEntry entry = IPFluidEntry.make(name, sourceFactory, IPFluid.Flowing::new, IPFluid.IPFluidBlock::new, builder(density, viscosity, motionScale));
		return entry;
	}
	
	public static <S extends IPFluid, B extends IPFluidBlock> IPFluidEntry makeFluid(String name, int density, int viscosity, double motionScale, Function<IPFluidEntry, S> sourceFactory, BiFunction<IPFluidEntry, Block.Properties, B> blockFactory){
		IPFluidEntry entry = IPFluidEntry.make(name, sourceFactory, IPFluid.Flowing::new, blockFactory, builder(density, viscosity, motionScale));
		return entry;
	}
	
	
	/*protected static Material createMaterial(MaterialColor color){
		return new Material(color, true, false, false, false, false, true, PushReaction.DESTROY);
	}*/
	
	private static IPFluidEntry staticEntry;
	private static IPFluid makeFluid(Function<IPFluidEntry, ? extends IPFluid> make, IPFluid.IPFluidEntry entry){
		staticEntry = entry;
		IPFluid ret = make.apply(entry);
		staticEntry = null;
		return ret;
	}
	
	protected final IPFluidEntry entry;
	protected IPFluid(IPFluidEntry entry){
		this.entry = entry;
	}
	
	@Override
	protected void createFluidStateDefinition(Builder<Fluid, FluidState> builder){
		super.createFluidStateDefinition(builder);
		for(Property<?> p:(entry == null ? staticEntry : entry).properties()){
			builder.add(p);
		}
	}
	
	@Override
	protected void beforeDestroyingBlock(@Nonnull LevelAccessor arg0, @Nonnull BlockPos arg1, @Nonnull BlockState arg2){
	}
	
	@Override
	public boolean canConvertToSource(FluidState state, Level level, BlockPos pos){
		return false;
	}

	@Override
	protected boolean canConvertToSource(Level pLevel) {
		return false;
	}

	public boolean hasCustomSlowdown(){
		return false;
	}
	
	public double getEntitySlowdown(){
		return 0.8;
	}
	
	@Override
	@Nonnull
	public Fluid getFlowing(){
		return this.entry.flowing.get();
	}
	
	@Override
	@Nonnull
	public Fluid getSource(){
		return this.entry.source.get();
	}
	
	@Override
	@Nonnull
	public Item getBucket(){
		return this.entry.bucket.get();
	}
	
	@Override
	public FluidType getFluidType(){
		return this.entry.type.get();
	}
	
	@Override
	protected int getDropOff(@Nonnull LevelReader arg0){
		return 1;
	}
	
	@Override
	protected int getSlopeFindDistance(@Nonnull LevelReader arg0){
		return 4;
	}
	
	@Override
	protected boolean canBeReplacedWith(FluidState fluidState, BlockGetter blockReader, BlockPos pos, Fluid fluid, Direction direction){
		return direction == Direction.DOWN && !isSame(fluid);
	}
	
	@Override
	public int getTickDelay(@Nonnull LevelReader p_205569_1_){
		return 5;
	}
	
	@Override
	protected float getExplosionResistance(){
		return 100;
	}
	
	@Override
	protected @Nonnull BlockState createLegacyBlock(@Nonnull FluidState state){
		return this.entry.block.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
	}
	
	@Override
	public boolean isSource(FluidState state){
		return state.is(getSource());
	}
	
	@Override
	public int getAmount(@Nonnull FluidState state){
		return isSource(state) ? 8 : state.getValue(LEVEL);
	}
	
	@Override
	public boolean isSame(Fluid fluidIn){
		return fluidIn == getSource() || fluidIn == getFlowing();
	}
	
	private static Consumer<FluidType.Properties> builder(int density, int viscosity, double motionScale){
		return builder -> builder.viscosity(viscosity).density(density).motionScale(motionScale);
	}
	
	// STATIC CLASSES
	
	public record IPFluidEntry(RegistryObject<IPFluid> source, RegistryObject<IPFluid> flowing, RegistryObject<IPFluidBlock> block, RegistryObject<BucketItem> bucket, RegistryObject<FluidType> type, List<Property<?>> properties){
		
		public Fluid get(){
			return source().get();
		}
		
		public void setEffect(MobEffect effect, int duration, int level){
			block().get().setEffect(effect, duration, level);
		}
		
		protected static <S extends IPFluid, F extends IPFluid, B extends IPFluidBlock> IPFluidEntry make(
			String name,
			Function<IPFluidEntry, S> makeSource, Function<IPFluidEntry, F> makeFlowing, BiFunction<IPFluidEntry, BlockBehaviour.Properties, B> makeBlock,
			@Nullable Consumer<FluidType.Properties> buildAttributes
		){
			return make(name, 0, makeSource, makeFlowing, makeBlock, buildAttributes, ImmutableList.of());
		}
		
		protected static <S extends IPFluid, F extends IPFluid, B extends IPFluidBlock> IPFluidEntry make(
			String name,
			Function<IPFluidEntry, S> makeSource, Function<IPFluidEntry, F> makeFlowing, BiFunction<IPFluidEntry, BlockBehaviour.Properties, B> makeBlock,
			@Nullable Consumer<FluidType.Properties> buildAttributes, List<Property<?>> properties
		){
			return make(name, 0, makeSource, makeFlowing, makeBlock, buildAttributes, properties);
		}
		
		protected static <S extends IPFluid, F extends IPFluid, B extends IPFluidBlock> IPFluidEntry make(
			String name, int burnTime,
			Function<IPFluidEntry, S> makeSource, Function<IPFluidEntry, F> makeFlowing, BiFunction<IPFluidEntry, BlockBehaviour.Properties, B> makeBlock,
			@Nullable Consumer<FluidType.Properties> buildAttributes, List<Property<?>> properties
		){
			FluidType.Properties builder = FluidType.Properties.create();
			if(buildAttributes != null){
				buildAttributes.accept(builder);
			}
			
			RegistryObject<FluidType> type = IPRegisters.FLUID_TYPE.register(name, () -> new CustomFluidType(name, builder));
			
			Mutable<IPFluidEntry> thisMutable = new MutableObject<>();
			
			RegistryObject<IPFluid> source = IPRegisters.registerFluid(name, () -> IPFluid.makeFluid(makeSource, thisMutable.getValue()));
			RegistryObject<IPFluid> flow = IPRegisters.registerFluid(name + "_flowing", () -> IPFluid.makeFluid(makeFlowing, thisMutable.getValue()));
			RegistryObject<IPFluidBlock> block = IPRegisters.registerBlock(name + "_fluid_block", () -> makeBlock.apply(thisMutable.getValue(), Properties.copy(Blocks.WATER).noLootTable()));
			RegistryObject<BucketItem> bucket = IPRegisters.registerItem(name + "_bucket", () -> new IPBucketItem(source, burnTime));
			
			IPFluidEntry entry = new IPFluidEntry(source, flow, block, bucket, type, properties);
			thisMutable.setValue(entry);
			FLUIDS.add(entry);
			return entry;
		}
	}
	
	private static class CustomFluidType extends FluidType{
		final ResourceLocation stillTexture, flowTexture;
		public CustomFluidType(String name, Properties properties){
			super(properties);
			this.stillTexture = ResourceUtils.ip("block/fluid/" + name + "_still");
			this.flowTexture = ResourceUtils.ip("block/fluid/" + name + "_flow");
		}
		
		@Override
		public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer){
			consumer.accept(new IClientFluidTypeExtensions(){
				@Override
				public ResourceLocation getStillTexture(){
					return stillTexture;
				}
				
				@Override
				public ResourceLocation getFlowingTexture(){
					return flowTexture;
				}
			});
		}
		
		@Override
		public boolean move(FluidState state, LivingEntity entity, Vec3 movementVector, double gravity){
			if(!(state.getType() instanceof IPFluid)){
				return false;
			}
			
			IPFluid ipFluid = (IPFluid) state.getType();
			if(!ipFluid.hasCustomSlowdown()){
				return false;
			}
			
			double drag = ipFluid.getEntitySlowdown();
			boolean isFalling = entity.getDeltaMovement().y <= 0.0D;
			double y = entity.getY();
			double walkSpeed = entity.isSprinting() ? drag * 1.125F : drag;
			double swimSpeed = 0.02F;
			
			swimSpeed *= entity.getAttribute(ForgeMod.SWIM_SPEED.get()).getValue();
			entity.moveRelative((float) swimSpeed, movementVector);
			entity.move(MoverType.SELF, entity.getDeltaMovement());
			Vec3 deltaMovment = entity.getDeltaMovement();
			if(entity.horizontalCollision && entity.onClimbable()){
				deltaMovment = new Vec3(deltaMovment.x, 0.2D, deltaMovment.z);
			}
			
			entity.setDeltaMovement(deltaMovment.multiply(walkSpeed, drag, walkSpeed));
			Vec3 vec32 = entity.getFluidFallingAdjustedMovement(gravity, isFalling, entity.getDeltaMovement());
			entity.setDeltaMovement(vec32);
			if(entity.horizontalCollision && entity.isFree(vec32.x, vec32.y + 0.6 - entity.getY() + y, vec32.z)){
				entity.setDeltaMovement(vec32.x, 0.3, vec32.z);
			}
			
			return true;
		}
	}
	
	public static class IPFluidBlock extends LiquidBlock{
		private static IPFluidEntry staticEntry;
		
		protected final IPFluidEntry entry;
		@Nullable
		private MobEffect effect;
		private int duration;
		private int level;
		public IPFluidBlock(IPFluidEntry entry, Block.Properties props){
			super(entry.source(), Util.make(props, p -> staticEntry = entry));
			this.entry = entry;
			staticEntry = null;
		}
		
		public void setEffect(@Nullable MobEffect effect, int duration, int level){
			this.effect = effect;
			this.duration = duration;
			this.level = level;
		}
		
		@Override
		protected void createBlockStateDefinition(Builder<Block, BlockState> builder){
			super.createBlockStateDefinition(builder);
			for(Property<?> p:(this.entry == null ? staticEntry : this.entry).properties())
				builder.add(p);
		}
		
		@Override
		public FluidState getFluidState(BlockState pState){
			FluidState state = super.getFluidState(pState);
			for(Property<?> prop:state.getProperties()){
				if(pState.hasProperty(prop))
					state = copyValue(prop, state, pState);
			}
			return state;
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity){
			super.entityInside(pState, pLevel, pPos, pEntity);
			if(this.effect != null && pEntity instanceof LivingEntity living)
				living.addEffect(new MobEffectInstance(this.effect, this.duration, this.level));
		}
		
		public static <S extends StateHolder<?, S>, P extends Comparable<P>> S copyValue(Property<P> prop, S oldState, StateHolder<?, ?> from){
			return oldState.setValue(prop, from.getValue(prop));
		}
	}
	
	public static class IPBucketItem extends BucketItem{
		private static final Item.Properties PROPS = new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET);
		
		private int burnTime;
		public IPBucketItem(Supplier<? extends Fluid> fluid, int burnTime){
			super(fluid, PROPS);
			this.burnTime = burnTime;
		}
		
		public IPBucketItem(Supplier<? extends Fluid> fluid, Function<Item.Properties, Item.Properties> props){
			super(fluid, props.apply(PROPS));
		}
		
		@Override
		public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable CompoundTag nbt){
			return new FluidBucketWrapper(stack);
		}
		
		@Override
		public int getBurnTime(ItemStack itemStack, @org.jetbrains.annotations.Nullable RecipeType<?> recipeType){
			return this.burnTime;
		}
	}
	
	public static class Flowing extends IPFluid{
		public Flowing(IPFluid.IPFluidEntry entry){
			super(entry);
		}
		
		@Override
		protected void createFluidStateDefinition(@Nonnull Builder<Fluid, FluidState> builder){
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}
	}
}
