package flaxbeard.immersivepetroleum.common.blocks.tileentities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.orientation.RelativeBlockFace;
import flaxbeard.immersivepetroleum.api.crafting.HighPressureRefineryRecipe;
import flaxbeard.immersivepetroleum.common.IPMenuTypes;
import flaxbeard.immersivepetroleum.common.blocks.ticking.IPCommonTickableTile;
import flaxbeard.immersivepetroleum.common.gui.IPMenuProvider;
import flaxbeard.immersivepetroleum.common.multiblocks.HydroTreaterMultiblock;
import flaxbeard.immersivepetroleum.common.util.AABBUtils;
import flaxbeard.immersivepetroleum.common.util.FluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class HydrotreaterTileEntity extends PoweredMultiblockBlockEntity<HydrotreaterTileEntity, HighPressureRefineryRecipe> implements IPCommonTickableTile, IPMenuProvider<HydrotreaterTileEntity>, IEBlockInterfaces.IBlockBounds{
	/** Primary Fluid Input Tank<br> */
	public static final int TANK_INPUT_A = 0;
	
	/** Secondary Fluid Input Tank<br> */
	public static final int TANK_INPUT_B = 1;
	
	/** Output Fluid Tank<br> */
	public static final int TANK_OUTPUT = 2;
	
	/** Template-Location of the Fluid Input Port. (1 0 3)<br> */
	public static final BlockPos Fluid_IN_A = new BlockPos(1, 0, 3);
	
	/** Template-Location of the Fluid Input Port. (2 2 1)<br> */
	public static final BlockPos Fluid_IN_B = new BlockPos(2, 2, 1);
	
	/** Template-Location of the Fluid Output Port. (0 1 2)<br> */
	public static final BlockPos Fluid_OUT = new BlockPos(0, 1, 2);
	
	/** Template-Location of the Item Output Port. (0 0 2)<br> */
	public static final BlockPos Item_OUT = new BlockPos(0, 0, 2);
	
	/** Template-Location of the Energy Input Ports. (2 2 3)<br> */
	public static final Set<MultiblockFace> Energy_IN = ImmutableSet.of(new MultiblockFace(2, 2, 3, RelativeBlockFace.UP));
	
	/** Template-Location of the Redstone Input Port. (0 1 3)<br> */
	public static final Set<BlockPos> Redstone_IN = ImmutableSet.of(new BlockPos(0, 1, 3));
	
	public final FluidTank[] tanks = new FluidTank[]{new FluidTank(12000), new FluidTank(12000), new FluidTank(12000)};
	public HydrotreaterTileEntity(BlockEntityType<HydrotreaterTileEntity> type, BlockPos pWorldPosition, BlockState pBlockState){
		super(HydroTreaterMultiblock.INSTANCE, 8000, true, type, pWorldPosition, pBlockState);
		tanks[TANK_INPUT_A].setValidator(fs -> HighPressureRefineryRecipe.hasRecipeWithInput(fs, true));
		tanks[TANK_INPUT_B].setValidator(fs -> HighPressureRefineryRecipe.hasRecipeWithSecondaryInput(fs, true));
	}
	
	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket){
		super.readCustomNBT(nbt, descPacket);
		
		this.tanks[TANK_INPUT_A].readFromNBT(nbt.getCompound("tank0"));
		this.tanks[TANK_INPUT_B].readFromNBT(nbt.getCompound("tank1"));
		this.tanks[TANK_OUTPUT].readFromNBT(nbt.getCompound("tank2"));
	}
	
	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket){
		super.writeCustomNBT(nbt, descPacket);
		
		nbt.put("tank0", this.tanks[TANK_INPUT_A].writeToNBT(new CompoundTag()));
		nbt.put("tank1", this.tanks[TANK_INPUT_B].writeToNBT(new CompoundTag()));
		nbt.put("tank2", this.tanks[TANK_OUTPUT].writeToNBT(new CompoundTag()));
	}
	
	@Override
	protected HighPressureRefineryRecipe getRecipeForId(Level level, ResourceLocation id){
		return HighPressureRefineryRecipe.recipes.get(id);
	}
	
	@Override
	public NonNullList<ItemStack> getInventory(){
		return null;
	}
	
	@Override
	public boolean isStackValid(int slot, ItemStack stack){
		return false;
	}
	
	@Override
	public int getSlotLimit(int slot){
		return 0;
	}
	
	@Override
	public void doGraphicalUpdates(){
		this.setChanged();
		this.markContainingBlockForUpdate(null);
	}
	
	@Override
	public Set<MultiblockFace> getEnergyPos(){
		return Energy_IN;
	}
	
	@Override
	public Set<BlockPos> getRedstonePos(){
		return Redstone_IN;
	}
	
	@Override
	public IFluidTank[] getInternalTanks(){
		return this.tanks;
	}
	
	@Override
	public HighPressureRefineryRecipe findRecipeForInsertion(ItemStack inserting){
		return null;
	}
	
	@Override
	public int[] getOutputSlots(){
		return null;
	}
	
	@Override
	public int[] getOutputTanks(){
		return new int[]{TANK_OUTPUT};
	}
	
	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<HighPressureRefineryRecipe> process){
		int outputAmount = 0;
		for(FluidStack outputFluid:process.getRecipe(this.level).getFluidOutputs()){
			outputAmount += outputFluid.getAmount();
		}
		
		return this.tanks[TANK_OUTPUT].getCapacity() >= (this.tanks[TANK_OUTPUT].getFluidAmount() + outputAmount);
	}
	
	@Override
	public void doProcessOutput(ItemStack output){
		if(output == null || output.isEmpty())
			return;
		
		Direction outputdir = (getIsMirrored() ? getFacing().getClockWise() : getFacing().getCounterClockWise());
		BlockPos outputpos = getBlockPosForPos(Item_OUT).relative(outputdir);
		
		BlockEntity te = level.getBlockEntity(outputpos);
		if(te != null){
			IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, outputdir.getOpposite()).orElse(null);
			if(handler != null){
				output = ItemHandlerHelper.insertItem(handler, output, false);
			}
		}
		
		if(!output.isEmpty()){
			double x = outputpos.getX() + 0.5;
			double y = outputpos.getY() + 0.25;
			double z = outputpos.getZ() + 0.5;
			
			Direction facing = getIsMirrored() ? getFacing().getOpposite() : getFacing();
			if(facing != Direction.EAST && facing != Direction.WEST){
				x = outputpos.getX() + (facing == Direction.SOUTH ? 0.15 : 0.85);
			}
			if(facing != Direction.NORTH && facing != Direction.SOUTH){
				z = outputpos.getZ() + (facing == Direction.WEST ? 0.15 : 0.85);
			}
			
			ItemEntity ei = new ItemEntity(level, x, y, z, output.copy());
			ei.setDeltaMovement(0.075 * outputdir.getStepX(), 0.025, 0.075 * outputdir.getStepZ());
			level.addFreshEntity(ei);
		}
	}
	
	@Override
	public void doProcessFluidOutput(FluidStack output){
	}
	
	@Override
	public void onProcessFinish(MultiblockProcess<HighPressureRefineryRecipe> process){
	}
	
	@Override
	public void tickClient(){
	}
	
	@Override
	public void tickServer(){
		boolean update = false;
		
		if(!isRSDisabled()){
			if(this.energyStorage.getEnergyStored() > 0 && this.processQueue.size() < getProcessQueueMaxLength()){
				if(this.tanks[TANK_INPUT_A].getFluidAmount() > 0 || this.tanks[TANK_INPUT_B].getFluidAmount() > 0){
					HighPressureRefineryRecipe recipe = HighPressureRefineryRecipe.findRecipe(this.tanks[TANK_INPUT_A].getFluid(), this.tanks[TANK_INPUT_B].getFluid());
					
					if(recipe != null && this.energyStorage.getEnergyStored() >= recipe.getTotalProcessEnergy()/recipe.getTotalProcessTime()){
						if(this.tanks[TANK_INPUT_A].getFluidAmount() >= recipe.getInputFluid().getAmount() && (recipe.getSecondaryInputFluid() == null || (this.tanks[TANK_INPUT_B].getFluidAmount() >= recipe.getSecondaryInputFluid().getAmount()))){
							int[] inputs, inputAmounts;
							
							if(recipe.getSecondaryInputFluid() != null){
								inputs = new int[]{TANK_INPUT_A, TANK_INPUT_B};
								inputAmounts = new int[]{recipe.getInputFluid().getAmount(), recipe.getSecondaryInputFluid().getAmount()};
							}else{
								inputs = new int[]{TANK_INPUT_A};
								inputAmounts = new int[]{recipe.getInputFluid().getAmount()};
							}
							
							MultiblockProcessInMachine<HighPressureRefineryRecipe> process = new MultiblockProcessInMachine<>(recipe, this::getRecipeForId)
									.setInputTanks(inputs)
									.setInputAmounts(inputAmounts);
							if(addProcessToQueue(process, true)){
								addProcessToQueue(process, false);
								update = true;
							}
						}
					}
				}
			}
		}
		
		if(!this.processQueue.isEmpty()){
			update = true;
		}
		
		super.tickServer();
		
		if(this.tanks[TANK_OUTPUT].getFluidAmount() > 0){
			BlockPos outPos = getBlockPosForPos(Fluid_OUT).above();
			update |= FluidUtil.getFluidHandler(this.level, outPos, Direction.DOWN).map(output -> {
				boolean ret = false;
				FluidStack target = this.tanks[TANK_OUTPUT].getFluid();
				target = FluidHelper.copyFluid(target, Math.min(target.getAmount(), 1000));
				
				int accepted = output.fill(target, FluidAction.SIMULATE);
				if(accepted > 0){
					int drained = output.fill(FluidHelper.copyFluid(target, Math.min(target.getAmount(), accepted)), FluidAction.EXECUTE);
					
					this.tanks[TANK_OUTPUT].drain(new FluidStack(target.getFluid(), drained), FluidAction.EXECUTE);
					ret = true;
				}
				
				return ret;
			}).orElse(false);
		}
		
		if(update){
			updateMasterBlock(null, true);
		}
	}
	
	@Override
	public int getMaxProcessPerTick(){
		return 1;
	}
	
	@Override
	public int getProcessQueueMaxLength(){
		return 1;
	}
	
	@Override
	public float getMinProcessDistance(MultiblockProcess<HighPressureRefineryRecipe> process){
		return 1.0F;
	}
	
	@Override
	public boolean isInWorldProcessingMachine(){
		return false;
	}
	
	private final MultiblockCapability<IFluidHandler> inputAHandler = MultiblockCapability.make(
			this, be -> be.inputAHandler, HydrotreaterTileEntity::master, registerFluidInput(this.tanks[TANK_INPUT_A])
	);
	private final MultiblockCapability<IFluidHandler> inputBHandler = MultiblockCapability.make(
			this, be -> be.inputBHandler, HydrotreaterTileEntity::master, registerFluidInput(this.tanks[TANK_INPUT_B])
	);
	private final MultiblockCapability<IFluidHandler> outputHandler = MultiblockCapability.make(
			this, be -> be.outputHandler, HydrotreaterTileEntity::master, registerFluidOutput(this.tanks[TANK_OUTPUT])
	);
	
	@Nonnull
	@Override
	public <C> LazyOptional<C> getCapability(@Nonnull Capability<C> capability, @Nullable Direction side){
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
			if(this.posInMultiblock.equals(Fluid_IN_A) && (side == null || side == getFacing().getOpposite())){
				return this.inputAHandler.getAndCast();
			}else if(this.posInMultiblock.equals(Fluid_IN_B) && (side == null || side == Direction.UP)){
				return this.inputBHandler.getAndCast();
			}else if(this.posInMultiblock.equals(Fluid_OUT) && (side == null || side == Direction.UP)){
				return this.outputHandler.getAndCast();
			}
		}
		return super.getCapability(capability, side);
	}
	
	@Override
	public HydrotreaterTileEntity getGuiMaster(){
		return master();
	}
	
	@Nonnull
	@Override
	public BEContainerIP<? super HydrotreaterTileEntity, ?> getContainerTypeIP(){
		return IPMenuTypes.HYDROTREATER;
	}
	
	@Override
	public boolean canUseGui(@Nonnull Player player){
		return this.formed;
	}
	
	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES = CachedShapesWithTransform.createForMultiblock(HydrotreaterTileEntity::getShape);
	
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
		
		// Baseplate
		if(y == 0 && !(x == 0 && z == 2) && !(z == 3 && (x == 1 || x == 2))){
			AABBUtils.box16(main, 0, 0, 0, 16, 8, 16);
		}
		
		// Redstone Controller
		if(y == 0 && x == 0 && z == 3){
			AABBUtils.box16(main, 12, 8, 10, 14, 16, 14);
			AABBUtils.box16(main, 2, 8, 10, 4, 16, 14);
		}else if(y == 1 && x == 0 && z == 3){
			AABBUtils.box16(main, 0, 0, 8, 16, 16, 16);
		}
		
		// Small Tank
		if(x == 0){
			// Bottom half
			if(y == 0){
				if(z == 0){
					AABBUtils.box16(main, 2, 12, 8, 16, 16, 16);
					AABBUtils.box16(main, 4, 8, 12, 14, 12, 16);
				}
				if(z == 1){
					AABBUtils.box16(main, 2, 12, 0, 16, 16, 16);
				}
				if(z == 3){
					AABBUtils.box16(main, 2, 12, 0, 16, 16, 4);
				}
				
			}
			
			// Top half
			if(y == 1){
				if(z == 0){
					AABBUtils.box16(main, 2, 0, 8, 16, 12, 16);
				}
				if(z == 1){
					AABBUtils.box16(main, 2, 0, 0, 16, 12, 16);
				}
				if(z == 3){
					AABBUtils.box16(main, 2, 0, 0, 16, 12, 4);
				}
			}
		}
		
		// Big tank
		{
			// Support legs
			if(y == 0){
				if(z == 0){
					if(x == 1){
						AABBUtils.box16(main, 2, 5, 1, 6, 16, 5);
					}
					if(x == 2){
						AABBUtils.box16(main, 10, 5, 1, 14, 16, 5);
					}
				}
				if(z == 1){
					if(x == 1){
						AABBUtils.box16(main, 2, 5, 16, 6, 16, 16);
					}
					if(x == 2){
						AABBUtils.box16(main, 10, 5, 16, 16, 16, 16);
					}
				}
				if(x == 2){
					if(z == 1) AABBUtils.box16(main, 10, 5, 14, 14, 16, 16);
					if(z == 2) AABBUtils.box16(main, 10, 5, 0, 14, 16, 2);
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
