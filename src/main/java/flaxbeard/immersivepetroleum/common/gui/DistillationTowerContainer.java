package flaxbeard.immersivepetroleum.common.gui;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import flaxbeard.immersivepetroleum.api.crafting.DistillationTowerRecipe;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.DistillationTowerMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower.DistillationTowerLogic;
import flaxbeard.immersivepetroleum.common.gui.IPSlot.FluidContainer.FluidFilter;
import flaxbeard.immersivepetroleum.common.util.inventory.MultiFluidTankFiltered;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.List;

import static flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower.DistillationTowerLogic.*;

public class DistillationTowerContainer extends MultiblockAwareGuiContainer {

	public final ItemStackHandler handler;
	public final IEnergyStorage energy;
	public final GetterAndSetter<List<FluidStack>> input;
	public final GetterAndSetter<List<FluidStack>> output;

	public static DistillationTowerContainer makeServer(
			MenuType<?> type, int id, Inventory player, MultiblockMenuContext<DistillationTowerLogic.State> ctx)
	{
		State state = ctx.mbContext().getState();

		return new DistillationTowerContainer(
				multiblockCtx(type, id, ctx), player,
				new ItemStackHandler(state.inventory),
				state.getInternalTanks(),
				GetterAndSetter.getterOnly(() -> state.getInternalTanks()[0].fluids),
				GetterAndSetter.getterOnly(() -> state.getInternalTanks()[1].fluids),
				state.getEnergy());
	}

	public static DistillationTowerContainer makeClient(MenuType<?> type, int id, Inventory player)
	{
		return new DistillationTowerContainer(clientCtx(type, id), player,
				new ItemStackHandler(INV_3 + 1),
				new MultiFluidTankFiltered[]{ new MultiFluidTankFiltered(24000), new MultiFluidTankFiltered(24000)},
				GetterAndSetter.standalone(List.of()),
				GetterAndSetter.standalone(List.of()),
				new AveragingEnergyStorage(16000));
	}
	private DistillationTowerContainer(MenuContext ctx, Inventory playerInventory, ItemStackHandler handler, MultiFluidTankFiltered[] tanks, GetterAndSetter<List<FluidStack>> input, GetterAndSetter<List<FluidStack>> output, AveragingEnergyStorage energy){
		super(ctx, DistillationTowerMultiblock.INSTANCE);
		this.handler = handler;
		this.energy = energy;
		this.input = input;
		this.output = output;


		addSlot(new IPSlot(handler, INV_0, 12, 17){
			@Override
			public boolean mayPlace(@Nonnull ItemStack stack){
				return FluidUtil.getFluidHandler(stack).map(h -> {
					if(h.getTanks() <= 0){
						return false;
					}
					
					FluidStack fs = h.getFluidInTank(0);
					if(fs.isEmpty() || (tanks[TANK_INPUT].getFluidAmount() > 0 && !fs.isFluidEqual(tanks[TANK_INPUT].getFluid()))){
						return false;
					}
					
					DistillationTowerRecipe recipe = DistillationTowerRecipe.findRecipe(fs);
					return recipe != null;
				}).orElse(false);
			}
		});
		addSlot(new IPSlot.ItemOutput(handler, INV_1, 12, 53));
		
		addSlot(new IPSlot.FluidContainer(handler, INV_2, 134, 17, FluidFilter.EMPTY));
		addSlot(new IPSlot.ItemOutput(handler, INV_3, 134, 53));
		
		this.ownSlotCount = 4;
		
		addPlayerInventorySlots(playerInventory, 8, 85);
		addPlayerHotbarSlots(playerInventory, 8, 143);

		addGenericData(new GenericContainerData<>(GenericDataSerializers.FLUID_STACKS, input));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.FLUID_STACKS, output));
		addGenericData(GenericContainerData.energy(energy));
	}
}
