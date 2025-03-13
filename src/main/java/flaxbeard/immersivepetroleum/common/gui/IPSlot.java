package flaxbeard.immersivepetroleum.common.gui;

import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public class IPSlot extends SlotItemHandler {
	private final Predicate<ItemStack> consumer;

	public IPSlot(IItemHandler inventoryIn, int index, int xPosition, int yPosition){
		super(inventoryIn, index, xPosition, yPosition);
		this.consumer = null;
	}
	
	public IPSlot(IItemHandler inventoryIn, int index, int xPosition, int yPosition, Predicate<ItemStack> placeCheck){
		super(inventoryIn, index, xPosition, yPosition);
		this.consumer = placeCheck;
	}
	
	@Override
	public boolean mayPlace(ItemStack pStack){
		if(this.consumer != null){
			return this.consumer.test(pStack);
		}
		return super.mayPlace(pStack);
	}
	
	public static class ItemOutput extends IPSlot{
		public ItemOutput(IItemHandler inventoryIn, int index, int xPosition, int yPosition){
			super(inventoryIn, index, xPosition, yPosition);
		}
		
		@Override
		public boolean mayPlace(@Nonnull ItemStack stack){
			return false;
		}
	}
	
	public static class CokerInput extends IPSlot{
		public CokerInput(AbstractContainerMenu container, IItemHandler inv, int id, int x, int y){
			super(inv, id, x, y);
		}
		
		@Override
		public boolean mayPlace(ItemStack stack){
			return !stack.isEmpty() && CokerUnitRecipe.hasRecipeWithInput(stack, true);
		}
	}
	
	public static class FluidContainer extends IPSlot{
		FluidFilter filter;
		public FluidContainer(IItemHandler inv, int id, int x, int y, FluidFilter filter){
			super(inv, id, x, y);
			this.filter = filter;
		}
		
		@Override
		public boolean mayPlace(@Nonnull ItemStack itemStack){
			LazyOptional<IFluidHandlerItem> handlerCap = FluidUtil.getFluidHandler(itemStack);
			return handlerCap.map(handler -> {
				if(handler.getTanks() <= 0)
					return false;
				
				return switch(filter){
					case FULL -> !handler.getFluidInTank(0).isEmpty();
					case EMPTY -> handler.getFluidInTank(0).isEmpty();
					case ANY -> true;
				};
			}).orElse(false);
		}
		
		public enum FluidFilter{
			ANY, EMPTY, FULL
		}
	}
}
