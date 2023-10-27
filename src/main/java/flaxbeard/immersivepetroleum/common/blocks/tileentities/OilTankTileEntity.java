package flaxbeard.immersivepetroleum.common.blocks.tileentities;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.fluid.IPressurizedFluidOutput;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartBlockEntity;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.immersivepetroleum.common.blocks.ticking.IPCommonTickableTile;
import flaxbeard.immersivepetroleum.common.multiblocks.OilTankMultiblock;
import flaxbeard.immersivepetroleum.common.util.AABBUtils;
import flaxbeard.immersivepetroleum.common.util.FluidHelper;
import flaxbeard.immersivepetroleum.common.util.LayeredComparatorOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class OilTankTileEntity extends MultiblockPartBlockEntity<OilTankTileEntity> implements IPCommonTickableTile, IEBlockInterfaces.IPlayerInteraction, IEBlockInterfaces.IBlockOverlayText, IEBlockInterfaces.IBlockBounds, IEBlockInterfaces.IHammerInteraction, IEBlockInterfaces.IComparatorOverride, IPressurizedFluidOutput{
	
	public enum PortState implements StringRepresentable{
		INPUT, OUTPUT;
		
		@Override
		@Nonnull
		public String getSerializedName(){
			return this.toString().toLowerCase(Locale.ENGLISH);
		}
		
		public Component getText(){
			return new TranslatableComponent("desc.immersivepetroleum.info.oiltank." + getSerializedName());
		}
		
		public PortState next(){
			return this == INPUT ? OUTPUT : INPUT;
		}
	}
	
	public enum Port implements StringRepresentable{
		TOP(new BlockPos(2, 2, 3)),
		BOTTOM(new BlockPos(2, 0, 3)),
		DYNAMIC_A(new BlockPos(0, 1, 2)),
		DYNAMIC_B(new BlockPos(4, 1, 2)),
		DYNAMIC_C(new BlockPos(0, 1, 4)),
		DYNAMIC_D(new BlockPos(4, 1, 4));
		
		public static final Port[] DYNAMIC_PORTS = {DYNAMIC_A, DYNAMIC_B, DYNAMIC_C, DYNAMIC_D};
		
		public final BlockPos posInMultiblock;
		Port(BlockPos posInMultiblock){
			this.posInMultiblock = posInMultiblock;
		}
		
		public boolean matches(BlockPos posInMultiblock){
			return posInMultiblock.equals(this.posInMultiblock);
		}
		
		@Override
		@Nonnull
		public String getSerializedName(){
			return this.toString().toLowerCase(Locale.ENGLISH);
		}
		
		static Set<BlockPos> toSet(Port... ports){
			ImmutableSet.Builder<BlockPos> builder = ImmutableSet.builder();
			for(Port port:ports){
				builder.add(port.posInMultiblock);
			}
			return builder.build();
		}
	}
	
	/**
	 * Template-Location of the Redstone Input Port. (2 2 5 & 2 2 2)<br>
	 */
	public static final Set<BlockPos> Redstone_IN = ImmutableSet.of(new BlockPos(2, 2, 5), new BlockPos(2, 2, 2));
	
	public final FluidTank tank = new FluidTank(1024 * FluidAttributes.BUCKET_VOLUME, f -> !f.getFluid().getAttributes().isGaseous());
	public final EnumMap<Port, PortState> portConfig = new EnumMap<>(Port.class);
	public OilTankTileEntity(BlockEntityType<OilTankTileEntity> type, BlockPos pWorldPosition, BlockState pBlockState){
		super(OilTankMultiblock.INSTANCE, type, true, pWorldPosition, pBlockState);
		this.redstoneControlInverted = false;
		for(Port port:Port.values()){
			if(port == Port.DYNAMIC_B || port == Port.DYNAMIC_C || port == Port.BOTTOM){
				portConfig.put(port, PortState.OUTPUT);
			}else{
				portConfig.put(port, PortState.INPUT);
			}
		}
	}
	
	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket){
		super.readCustomNBT(nbt, descPacket);
		this.tank.readFromNBT(nbt.getCompound("tank"));
		
		for(Port port:Port.DYNAMIC_PORTS){
			portConfig.put(port, PortState.values()[nbt.getInt(port.getSerializedName())]);
		}
	}
	
	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket){
		super.writeCustomNBT(nbt, descPacket);
		nbt.put("tank", this.tank.writeToNBT(new CompoundTag()));
		
		for(Port port:Port.DYNAMIC_PORTS){
			nbt.putInt(port.getSerializedName(), getPortStateFor(port).ordinal());
		}
	}
	
	@Override
	public void tickClient(){
	}
	
	@Override
	public void tickServer(){
		if(isDummy()){
			return;
		}
		
		int threshold = 1;
		
		PortState portStateA = getPortStateFor(Port.DYNAMIC_A),
				portStateB = getPortStateFor(Port.DYNAMIC_B),
				portStateC = getPortStateFor(Port.DYNAMIC_C),
				portStateD = getPortStateFor(Port.DYNAMIC_D);
		
		boolean wasBalancing = false;
		if((portStateA == PortState.OUTPUT && portStateC == PortState.INPUT) || (portStateA == PortState.INPUT && portStateC == PortState.OUTPUT)){
			wasBalancing |= equalize(Port.DYNAMIC_A, threshold, FluidAttributes.BUCKET_VOLUME);
		}
		
		if((portStateB == PortState.OUTPUT && portStateD == PortState.INPUT) || (portStateB == PortState.INPUT && portStateD == PortState.OUTPUT)){
			wasBalancing |= equalize(Port.DYNAMIC_B, threshold, FluidAttributes.BUCKET_VOLUME);
		}
		
		if(isRSDisabled()){
			for(Port port:Port.values()){
				if((!wasBalancing && getPortStateFor(port) == PortState.OUTPUT) || (wasBalancing && port == Port.BOTTOM)){
					Direction facing = getPortDirection(port);
					BlockPos pos = getBlockPosForPos(port.posInMultiblock).relative(facing);
					
					FluidUtil.getFluidHandler(this.level, pos, facing.getOpposite()).map(out -> {
						if(this.tank.getFluidAmount() > 0){
							FluidStack fs = FluidHelper.copyFluid(this.tank.getFluid(), Math.min(tank.getFluidAmount(), 432), false);
							int accepted = out.fill(fs, FluidAction.SIMULATE);
							if(accepted > 0){
								int drained = out.fill(FluidHelper.copyFluid(fs, Math.min(fs.getAmount(), accepted), false), FluidAction.EXECUTE);
								this.tank.drain(Utils.copyFluidStackWithAmount(this.tank.getFluid(), drained, false), FluidAction.EXECUTE);
								this.setChanged();
								this.markContainingBlockForUpdate(null);
								return true;
							}
						}
						return false;
					}).orElse(false);
				}
			}
		}
		
		this.comparatorHelper.update(this.tank.getFluidAmount());
	}
	
	private boolean equalize(Port port, int threshold, int maxTransfer){
		Direction facing = getPortDirection(port);
		BlockPos pos = getBlockPosForPos(port.posInMultiblock).relative(facing);
		BlockEntity te = getLevel().getBlockEntity(pos);
		
		if(te instanceof OilTankTileEntity otherMaster){
			otherMaster = otherMaster.master();
			
			int diff = otherMaster.tank.getFluidAmount() - this.tank.getFluidAmount();
			int amount = Math.min(Math.abs(diff) / 2, maxTransfer);
			
			return (diff <= -threshold && transfer(this, otherMaster, amount)) || (diff >= threshold && transfer(otherMaster, this, amount));
		}
		
		return false;
	}
	
	private boolean transfer(OilTankTileEntity src, OilTankTileEntity dst, int amount){
		FluidStack fs = new FluidStack(src.tank.getFluid(), amount);
		int accepted = dst.tank.fill(fs, FluidAction.SIMULATE);
		if(accepted > 0){
			fs = new FluidStack(src.tank.getFluid(), accepted);
			dst.tank.fill(fs, FluidAction.EXECUTE);
			src.tank.drain(fs, FluidAction.EXECUTE);
			
			src.setChanged();
			dst.setChanged();
			src.markContainingBlockForUpdate(null);
			dst.markContainingBlockForUpdate(null);
			return true;
		}
		
		return false;
	}
	
	private Direction getPortDirection(Port port){
		switch(port){
			case DYNAMIC_B:
			case DYNAMIC_D:{
				return getIsMirrored() ? getFacing().getCounterClockWise() : getFacing().getClockWise();
			}
			case DYNAMIC_A:
			case DYNAMIC_C:{
				return getIsMirrored() ? getFacing().getClockWise() : getFacing().getCounterClockWise();
			}
			case TOP:{
				return Direction.UP;
			}
			default:
				return Direction.DOWN;
		}
	}
	
	@Override
	public boolean isRSDisabled(){
		Set<BlockPos> rsPositions = getRedstonePos();
		if(rsPositions == null || rsPositions.isEmpty())
			return false;
		MultiblockPartBlockEntity<?> master = master();
		if(master == null)
			master = this;
		if(master.computerControl.isAttached())
			return !master.computerControl.isEnabled();
		
		boolean ret = false;
		for(BlockPos rsPos:rsPositions){
			OilTankTileEntity tile = this.getEntityForPos(rsPos);
			if(tile != null){
				ret |= tile.isRSPowered();
			}
		}
		return this.redstoneControlInverted != ret;
	}
	
	@Override
	public boolean interact(@Nonnull Direction side, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ){
		OilTankTileEntity master = this.master();
		if(master != null){
			if(FluidUtils.interactWithFluidHandler(player, hand, master.tank)){
				this.updateMasterBlock(null, true);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean hammerUseSide(@Nonnull Direction side, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull Vec3 hitVec){
		Level level = this.getLevelNonnull();
		if(!level.isClientSide){
			for(Port port:Port.DYNAMIC_PORTS){
				if(port.posInMultiblock.equals(this.posInMultiblock)){
					OilTankTileEntity master = master();
					if(master != null){
						PortState portState = master.getPortStateFor(port);
						master.portConfig.put(port, portState.next());
						this.updateMasterBlock(null, true);
						return true;
					}
					break;
				}
			}
		}
		return false;
	}
	
	public PortState getPortStateFor(Port port){
		return this.portConfig.get(port);
	}
	
	static final int MAX_FLUID_IO = FluidAttributes.BUCKET_VOLUME * 10;
	@Override
	public int getMaxAcceptedFluidAmount(FluidStack resource){
		return MAX_FLUID_IO;
	}
	
	@Override
	public Set<BlockPos> getRedstonePos(){
		return Redstone_IN;
	}
	
	private final MultiblockCapability<IFluidHandler> inputHandler = MultiblockCapability.make(
			this, be -> be.inputHandler, OilTankTileEntity::master, registerFluidInput(tank)
	);
	private final MultiblockCapability<IFluidHandler> outputHandler = MultiblockCapability.make(
			this, be -> be.outputHandler, OilTankTileEntity::master, registerFluidOutput(tank)
	);
	
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side){
		if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
			for(Port port:Port.values()){
				if(port.matches(this.posInMultiblock)){
					OilTankTileEntity master = isDummy() ? master() : this;
					if(master == null){
						return LazyOptional.empty();
					}
					return switch(master.portConfig.get(port)){
						case INPUT -> inputHandler.getAndCast();
						case OUTPUT -> outputHandler.getAndCast();
					};
				}
			}
		}
		return super.getCapability(cap, side);
	}
	
	public boolean isLadder(){
		int x = posInMultiblock.getX();
		int z = posInMultiblock.getZ();
		
		return x == 3 && z == 0;
	}
	
	@Override
	public Component[] getOverlayText(Player player, @Nonnull HitResult mop, boolean hammer){
		if(Utils.isFluidRelatedItemStack(player.getItemInHand(InteractionHand.MAIN_HAND))){
			OilTankTileEntity master = master();
			FluidStack fs = master != null ? master.tank.getFluid() : this.tank.getFluid();
			return new Component[]{TextUtils.formatFluidStack(fs)};
		}
		return null;
	}
	
	@Override
	public boolean useNixieFont(@Nonnull Player player, @Nonnull HitResult mop){
		return false;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public AABB getRenderBoundingBox(){
		BlockPos pos = getBlockPos();
		return new AABB(pos.offset(-3, -1, -3), pos.offset(3, 4, 3));
	}
	
	private final LayeredComparatorOutput comparatorHelper = new LayeredComparatorOutput(
			this.tank.getCapacity(),
			3,
			() -> this.level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock()),
			layer -> {
				BlockPos masterPos = this.worldPosition.subtract(this.offsetToMaster);
				for(int z = -2;z <= 2;z++){
					for(int x = -2;x <= 2;x++){
						BlockPos pos = masterPos.offset(x, layer, z);
						this.level.updateNeighborsAt(pos, this.level.getBlockState(pos).getBlock());
					}
				}
			});
	@Override
	public int getComparatorInputOverride(){
		OilTankTileEntity master = master();
		if(master != null && this.offsetToMaster.getY() >= 0 && this.offsetToMaster.getY() < this.comparatorHelper.getLayers()){
			return master.comparatorHelper.getLayerOutput(this.offsetToMaster.getY());
		}
		return 0;
	}
	
	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES = CachedShapesWithTransform.createForMultiblock(OilTankTileEntity::getShape);
	
	@Override
	@Nonnull
	public VoxelShape getBlockBounds(CollisionContext ctx){
		return SHAPES.get(this.posInMultiblock, Pair.of(getFacing(), getIsMirrored()));
	}
	
	private static List<AABB> getShape(BlockPos posInMultiblock){
		int x = posInMultiblock.getX();
		int y = posInMultiblock.getY();
		int z = posInMultiblock.getZ();
		
		List<AABB> main = new ArrayList<>();
		
		if(y == 0){
			// Corner Supports
			if(x == 0 && z == 1){
				AABBUtils.box16(main, 0, 0, 0, 4, 16, 4);
				AABBUtils.box16(main, 8, 0, 8, 16, 8, 16);
			}
			if(x == 4 && z == 1){
				AABBUtils.box16(main, 12, 0, 0, 16, 16, 4);
				AABBUtils.box16(main, 0, 0, 8, 8, 8, 16);
			}
			if(x == 0 && z == 5){
				AABBUtils.box16(main, 0, 0, 12, 4, 16, 16);
				AABBUtils.box16(main, 8, 0, 0, 16, 8, 8);
			}
			if(x == 4 && z == 5){
				AABBUtils.box16(main, 12, 0, 12, 16, 16, 16);
				AABBUtils.box16(main, 0, 0, 0, 8, 8, 8);
			}
			
			// Edge and Corner Angles
			
			if(!(x >= 1 && z >= 2 && x <= 3 && z <= 4))
				AABBUtils.box16(main, 0, 8, 0, 16, 16, 16);
			
			if(z >= 2 && z <= 4){
				if(x == 0) AABBUtils.box16(main, 8, 0, 0, 16, 8, 16);
				if(x == 4) AABBUtils.box16(main, 0, 0, 0, 8, 8, 16);
			}
			if(x >= 1 && x <= 3){
				if(z == 1) AABBUtils.box16(main, 0, 0, 8, 16, 8, 16);
				if(z == 5) AABBUtils.box16(main, 0, 0, 0, 16, 8, 8);
			}
		}
		
		// Easy Access Ladders™
		if(x == 3 && z == 0){
			if(y == 1 || y == 2){
				AABBUtils.box16(main, 2, 0, 15, 14, 16, 16);
			}
		}
		
		// Easy Access Slabs™
		if(y == 2){
			if(z == 0 && (x == 2 || x == 4)){
				AABBUtils.box16(main, 0, 8, 0, 16, 16, 16);
			}
		}
		
		// Railings
		if(y == 3){
			if(z >= 1 && z <= 5){
				if(x == 0){
					AABBUtils.box16(main, 0, 0, 0, 1, 16, 16);
				}else if(x == 4){
					AABBUtils.box16(main, 15, 0, 0, 16, 16, 16);
				}
			}
			if(x >= 0 && x <= 4){
				if(z == 5){
					AABBUtils.box16(main, 0, 0, 15, 16, 16, 16);
				}else if(z == 1 && x != 4){
					AABBUtils.box16(main, 0, 0, 0, 16, 16, 1);
				}
			}
		}
		
		// Use default cube shape if nessesary
		if(main.isEmpty()){
			main.add(AABBUtils.FULL);
		}
		return main;
	}
}
