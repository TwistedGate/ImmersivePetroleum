package flaxbeard.immersivepetroleum.common.blocks.tileentities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableSet;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.multiblocks.HydroTreaterMultiblock;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class HydroTreaterTileEntity extends PoweredMultiblockTileEntity<HydroTreaterTileEntity, MultiblockRecipe> implements IBlockBounds{
	/**
	 * Do not Touch! Taken care of by
	 * {@link IPContent#registerTile(RegistryEvent.Register, Class, Block...)}
	 */
	public static TileEntityType<HydroTreaterTileEntity> TYPE;

	/** Input Fluid Tank A<br> */
	public static final int TANK_INPUT_A = 0;
	
	/** Input Fluid Tank B<br> */
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
	public static final Set<BlockPos> Energy_IN = ImmutableSet.of(new BlockPos(2, 2, 3));
	
	/** Template-Location of the Redstone Input Port. (0 1 3)<br> */
	public static final Set<BlockPos> Redstone_IN = ImmutableSet.of(new BlockPos(0, 1, 3));
	
	
	public final FluidTank[] tanks = new FluidTank[]{new FluidTank(12000), new FluidTank(12000), new FluidTank(12000)};
	public HydroTreaterTileEntity(){
		super(HydroTreaterMultiblock.INSTANCE, 8000, true, null);
	}
	
	@Override
	public TileEntityType<?> getType(){
		return TYPE;
	}
	
	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket){
		super.readCustomNBT(nbt, descPacket);
		
		this.tanks[TANK_INPUT_A].readFromNBT(nbt.getCompound("tank0"));
		this.tanks[TANK_OUTPUT].readFromNBT(nbt.getCompound("tank1"));
	}
	
	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket){
		super.writeCustomNBT(nbt, descPacket);
		
		nbt.put("tank0", this.tanks[TANK_INPUT_A].writeToNBT(new CompoundNBT()));
		nbt.put("tank1", this.tanks[TANK_OUTPUT].writeToNBT(new CompoundNBT()));
	}
	
	@Override
	protected MultiblockRecipe getRecipeForId(ResourceLocation id){
		return null;
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
	public void doGraphicalUpdates(int slot){
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}
	
	@Override
	public Set<BlockPos> getEnergyPos(){
		return Energy_IN;
	}
	
	@Override
	public Set<BlockPos> getRedstonePos(){
		return Redstone_IN;
	}
	
	@Override
	public IOSideConfig getEnergySideConfig(Direction facing){
		if(this.formed && this.isEnergyPos() && (facing == null || facing == Direction.UP))
			return IOSideConfig.INPUT;
		
		return IOSideConfig.NONE;
	}
	
	@Override
	public IFluidTank[] getInternalTanks(){
		return this.tanks;
	}
	
	@Override
	public MultiblockRecipe findRecipeForInsertion(ItemStack inserting){
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
	public boolean additionalCanProcessCheck(MultiblockProcess<MultiblockRecipe> process){
		return false;
	}
	
	@Override
	public void doProcessOutput(ItemStack output){
	}
	
	@Override
	public void doProcessFluidOutput(FluidStack output){
	}
	
	@Override
	public void onProcessFinish(MultiblockProcess<MultiblockRecipe> process){
	}
	
	@Override
	public void tick(){
		super.tick();
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
	public float getMinProcessDistance(MultiblockProcess<MultiblockRecipe> process){
		return 0;
	}
	
	@Override
	public boolean isInWorldProcessingMachine(){
		return false;
	}
	
	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side){
		HydroTreaterTileEntity master = master();
		if(master != null){
			if(this.posInMultiblock.equals(Fluid_IN_A) && (side == null || side == getFacing().getOpposite())){
				return new IFluidTank[]{master.tanks[TANK_INPUT_A]};
			}
			if(this.posInMultiblock.equals(Fluid_IN_B) && (side == null || side == Direction.UP)){
				return new IFluidTank[]{master.tanks[TANK_INPUT_B]};
			}
			if(this.posInMultiblock.equals(Fluid_OUT) && (side == null || side == Direction.UP)){
				return new IFluidTank[]{master.tanks[TANK_OUTPUT]};
			}
		}
		return new IFluidTank[0];
	}
	
	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resource){
		if(this.posInMultiblock.equals(Fluid_IN_A) && (side == null || side == getFacing().getOpposite())){
			HydroTreaterTileEntity master = master();
			
			if(master != null && master.tanks[TANK_INPUT_A].getFluidAmount() < master.tanks[TANK_INPUT_A].getCapacity()){
				if(master.tanks[TANK_INPUT_A].isEmpty()){
					// TODO Recipe Part
					return false;	
				}else{
					return resource.isFluidEqual(master.tanks[TANK_INPUT_A].getFluid());
				}
			}
		}
		if(this.posInMultiblock.equals(Fluid_IN_B) && (side == null || side == Direction.UP)){
			HydroTreaterTileEntity master = master();
			
			if(master != null && master.tanks[TANK_INPUT_B].getFluidAmount() < master.tanks[TANK_INPUT_B].getCapacity()){
				if(master.tanks[TANK_INPUT_B].isEmpty()){
					// TODO Recipe Part
					return false;
				}else{
					return resource.isFluidEqual(master.tanks[TANK_INPUT_B].getFluid());
				}
			}
		}
		return false;
	}
	
	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side){
		return false;
	}
	
	private static CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES = CachedShapesWithTransform.createForMultiblock(HydroTreaterTileEntity::getShape);
	public static boolean updateShapes = false;
	
	@Override
	public VoxelShape getBlockBounds(ISelectionContext ctx){
		if(updateShapes){
			updateShapes = false;
			SHAPES = CachedShapesWithTransform.createForMultiblock(HydroTreaterTileEntity::getShape);
		}
		
		return SHAPES.get(this.posInMultiblock, Pair.of(getFacing(), getIsMirrored()));
	}
	
	private static List<AxisAlignedBB> getShape(BlockPos posInMultiblock){
		int x = posInMultiblock.getX();
		int y = posInMultiblock.getY();
		int z = posInMultiblock.getZ();
		
		List<AxisAlignedBB> main = new ArrayList<>();
		
		// Baseplate
		if(y == 0 && !(x == 0 && z == 2) && !(z == 3 && (x == 1 || x == 2))){
			main.add(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0));
		}
		
		// Redstone Controller
		if(y == 0 && x == 0 && z == 3){
			main.add(new AxisAlignedBB(0.75, 0.5, 0.625, 0.875, 1.0, 0.875));
			main.add(new AxisAlignedBB(0.125, 0.5, 0.625, 0.25, 1.0, 0.875));
		}else if(y == 1 && x == 0 && z == 3){
			main.add(new AxisAlignedBB(0.0, 0.0, 0.5, 1.0, 1.0, 1.0));
		}
		
		// Small Tank
		if(x == 0){
			// Bottom half
			if(y == 0){
				if(z == 0){
					main.add(new AxisAlignedBB(0.125, 0.75, 0.5, 1.0, 1.0, 1.0));
					main.add(new AxisAlignedBB(0.25, 0.5, 0.75, 0.875, 0.75, 1.0));
				}
				if(z == 1){
					main.add(new AxisAlignedBB(0.125, 0.75, 0.0, 1.0, 1.0, 1.0));
				}
				if(z == 3){
					main.add(new AxisAlignedBB(0.125, 0.75, 0.0, 1.0, 1.0, 0.25));
				}
				
			}
			
			// Top half
			if(y == 1){
				if(z == 0){
					main.add(new AxisAlignedBB(0.125, 0.0, 0.5, 1.0, 0.75, 1.0));
				}
				if(z == 1){
					main.add(new AxisAlignedBB(0.125, 0.0, 0.0, 1.0, 0.75, 1.0));
				}
				if(z == 3){
					main.add(new AxisAlignedBB(0.125, 0.0, 0.0, 1.0, 0.75, 0.25));
				}
			}
		}
		
		// Big tank
		{
			// Support legs
			if(y == 0){
				if(z == 0){
					if(x == 1){
						main.add(new AxisAlignedBB(0.125, 0.3125, 0.0625, 0.375, 1.0, 0.3125));
					}
					if(x == 2){
						main.add(new AxisAlignedBB(0.625, 0.3125, 0.0625, 0.875, 1.0, 0.3125));
					}
				}
				if(z == 1){
					if(x == 1){
						main.add(new AxisAlignedBB(0.125, 0.3125, 0.875, 0.375, 1.0, 1.0));
					}
					if(x == 2){
						main.add(new AxisAlignedBB(0.625, 0.3125, 0.875, 0.875, 1.0, 1.0));
					}
				}
				if(z == 2 && x == 2){
					main.add(new AxisAlignedBB(0.625, 0.3125, 0.0, 0.875, 1.0, 0.125));
				}
			}
		}
		
		// Use default cube shape if nessesary
		if(main.isEmpty()){
			main.add(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
		}
		return main;
	}
}
