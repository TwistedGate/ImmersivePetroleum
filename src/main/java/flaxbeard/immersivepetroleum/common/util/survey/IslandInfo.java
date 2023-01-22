package flaxbeard.immersivepetroleum.common.util.survey;

import javax.annotation.Nonnull;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirIsland;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class IslandInfo implements ISurveyInfo{
	public static final String TAG_KEY = "islandscan";
	
	private int x, z;
	private byte status;
	private long amount;
	private FluidStack fluidStack = FluidStack.EMPTY;
	private int expected;
	
	public IslandInfo(CompoundTag tag){
		this.x = tag.getInt("x");
		this.z = tag.getInt("z");
		this.status = tag.getByte("status");
		this.amount = tag.getLong("amount");
		this.expected = tag.getInt("expected");
		
		if(tag.contains("fluid")){
			try{
				ResourceLocation fluidRL = new ResourceLocation(tag.getString("fluid"));
				
				Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidRL);
				if(fluid != null){
					this.fluidStack = new FluidStack(fluid, 1);
				}
			}catch(ResourceLocationException e){
				// Technicaly don't care, but made it log this just incase.
				ImmersivePetroleum.log.debug("IslandInfo invalid ResourceLocation. Ignoring.");
			}
		}
	}
	
	public IslandInfo(Level world, BlockPos pos, ReservoirIsland island){
		this.x = pos.getX();
		this.z = pos.getZ();
		
		this.status = (byte) (island.getAmount() / (float) island.getCapacity() * 100);
		this.amount = island.getAmount();
		this.fluidStack = new FluidStack(island.getFluid(), 1);
		this.expected = ReservoirIsland.getFlow(island.getPressure(world, pos.getX(), pos.getZ()));
	}
	
	@Override
	public int getX(){
		return this.x;
	}
	
	@Override
	public int getZ(){
		return this.z;
	}
	
	public byte getStatus(){
		return this.status;
	}
	
	public int getExpected(){
		return this.expected;
	}
	
	public long getAmount(){
		return this.amount;
	}
	
	@Nonnull
	public FluidStack getFluidStack(){
		return this.fluidStack;
	}
	
	public Fluid getFluid(){
		return this.fluidStack.getFluid();
	}
	
	@Override
	public CompoundTag writeToStack(ItemStack stack){
		return writeToTag(stack.getOrCreateTagElement(TAG_KEY));
	}
	
	@Override
	public CompoundTag writeToTag(CompoundTag tag){
		tag.putInt("x", this.x);
		tag.putInt("z", this.z);
		tag.putByte("status", this.status);
		tag.putLong("amount", this.amount);
		tag.putInt("expected", this.expected);
		
		if(!this.fluidStack.isEmpty()){
			tag.putString("fluid", this.fluidStack.getFluid().getRegistryName().toString());
		}
		
		return tag;
	}
}
