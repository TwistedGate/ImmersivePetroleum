package flaxbeard.immersivepetroleum.common.util;

import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidHelper{
	
	/** Convenience Method */
	public static FluidStack copyFluid(FluidStack fluid, int amount){
		return copyFluid(fluid, amount, false);
	}
	
	/**
	 * Makes a copy of a FluidStack (excluding NBT) and optionally adds the Pressurized tag
	 * 
	 * @param fluid      {@link FluidStack} to use as the fluid type
	 * @param amount     the amount to use in the copied stack
	 * @param pressurize (optionally)
	 * @return {@link FluidStack}, pressurized if above IE's transfer threshold
	 */
	public static FluidStack copyFluid(FluidStack fluid, int amount, boolean pressurize){
		FluidStack fs = new FluidStack(fluid.getFluid(), amount);
		if(pressurize && amount > IFluidPipe.AMOUNT_UNPRESSURIZED){
			fs.getOrCreateTag().putBoolean(IFluidPipe.NBT_PRESSURIZED, true);
		}
		return fs;
	}
	
	/**
	 * Creates a pressurized FluidStack instance of the given Fluid.<br>
	 * Only pressurizes the fluid if necessary. (amount goes above 50)
	 * 
	 * @param fluid  {@link Fluid} to use as the fluid type
	 * @param amount the amount to use in the created stack
	 * @return {@link FluidStack}, with pressurized tag as needed.
	 */
	public static FluidStack makePressurizedFluid(Fluid fluid, int amount){
		FluidStack fs = new FluidStack(fluid, amount);
		if(amount > IFluidPipe.AMOUNT_UNPRESSURIZED){
			fs.getOrCreateTag().putBoolean(IFluidPipe.NBT_PRESSURIZED, true);
		}
		return fs;
	}
	
	/**
	 * Originally in IE as {@link blusunrize.immersiveengineering.common.util.Utils#isFluidContainerFull(ItemStack)}
	 */
	public static boolean isFluidContainerFull(ItemStack stack){
		return FluidUtil.getFluidHandler(stack).map(handler -> {
			for(int t = 0;t < handler.getTanks();++t)
				if(handler.getFluidInTank(t).getAmount() < handler.getTankCapacity(t))
					return false;
			return true;
		}).orElse(true);
	}
	
	/**
	 * Originally in IE as
	 * {@link blusunrize.immersiveengineering.common.util.Utils#fillFluidContainer(IFluidHandler, ItemStack, ItemStack, Player)}
	 */
	public static ItemStack fillFluidContainer(IFluidHandler handler, ItemStack containerIn, ItemStack containerOut, @Nullable Player player){
		if(containerIn == null || containerIn.isEmpty())
			return ItemStack.EMPTY;
		
		FluidActionResult result = FluidUtil.tryFillContainer(containerIn, handler, Integer.MAX_VALUE, player, false);
		if(result.isSuccess()){
			final ItemStack full = result.getResult();
			if((containerOut.isEmpty() || ItemHandlerHelper.canItemStacksStack(containerOut, full))){
				if(!containerOut.isEmpty() && containerOut.getCount() + full.getCount() > containerOut.getMaxStackSize())
					return ItemStack.EMPTY;
				result = FluidUtil.tryFillContainer(containerIn, handler, Integer.MAX_VALUE, player, true);
				if(result.isSuccess()){
					return result.getResult();
				}
			}
		}
		return ItemStack.EMPTY;
	}
	
	/**
	 * FluidStack based version of
	 * {@link blusunrize.immersiveengineering.common.util.Utils#fillFluidContainer(IFluidHandler, ItemStack, ItemStack, Player)} minus the
	 * useless bits :D
	 */
	public static ItemStack fillFluidContainer(IFluidTank tank, FluidStack fluid, ItemStack containerIn, ItemStack containerOut){
		if(containerIn == null || containerIn.isEmpty())
			return ItemStack.EMPTY;
		
		FluidActionResult result = tryFillContainer(tank, fluid, containerIn, false);
		if(result.isSuccess()){
			final ItemStack full = result.getResult();
			if((containerOut.isEmpty() || ItemHandlerHelper.canItemStacksStack(containerOut, full))){
				if(!containerOut.isEmpty() && containerOut.getCount() + full.getCount() > containerOut.getMaxStackSize()){
					return ItemStack.EMPTY;
				}
				
				result = tryFillContainer(tank, fluid, containerIn, true);
				if(result.isSuccess()){
					return result.getResult();
				}
			}
		}
		
		return ItemStack.EMPTY;
	}
	
	/**
	 * FluidStack based version of {@link net.minecraftforge.fluids.FluidUtil#tryFillContainer(ItemStack, IFluidHandler, int, Player, boolean)}
	 * minus the useless bits :D
	 */
	static FluidActionResult tryFillContainer(IFluidTank tank, FluidStack fluidSource, @Nonnull ItemStack container, boolean doFill){
		ItemStack containerCopy = ItemHandlerHelper.copyStackWithSize(container, 1);
		return FluidUtil.getFluidHandler(containerCopy).map(containerFluidHandler -> {
			
			int fillableAmount = containerFluidHandler.fill(fluidSource, FluidAction.SIMULATE);
			if(fillableAmount > 0){
				if(doFill){
					FluidStack fs = new FluidStack(fluidSource, Math.min(fluidSource.getAmount(), fillableAmount));
					containerFluidHandler.fill(fs, FluidAction.EXECUTE);
					tank.drain(fs, FluidAction.EXECUTE);
				}
				
				ItemStack resultContainer = containerFluidHandler.getContainer();
				return new FluidActionResult(resultContainer);
			}
			
			return FluidActionResult.FAILURE;
		}).orElse(FluidActionResult.FAILURE);
	}
	
	private FluidHelper(){
	}
}
