package flaxbeard.immersivepetroleum.common.gui;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.HydroTreaterMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.hydro_treater.HydroTreaterLogic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class HydrotreaterContainer extends MultiblockAwareGuiContainer{

	public final AveragingEnergyStorage energy;

	public final HydroTreaterLogic.Tanks tanks;

	public static HydrotreaterContainer makeServer(
			MenuType<?> type, int id, Inventory player, MultiblockMenuContext<HydroTreaterLogic.State> ctx)
	{
		HydroTreaterLogic.State state = ctx.mbContext().getState();

		return new HydrotreaterContainer(
				multiblockCtx(type, id, ctx), player,
				state.energy,
				state.tanks);
	}

	public static HydrotreaterContainer makeClient(MenuType<?> type, int id, Inventory player)
	{
		return new HydrotreaterContainer(clientCtx(type, id), player,
				new AveragingEnergyStorage(8000),
				new HydroTreaterLogic.Tanks());
	}
	public HydrotreaterContainer(MenuContext ctx, Inventory playerInventory, AveragingEnergyStorage energy, HydroTreaterLogic.Tanks tanks){
		super(ctx, HydroTreaterMultiblock.INSTANCE);

		this.energy = energy;
		this.tanks = tanks;

		addGenericData(GenericContainerData.energy(energy));
		addGenericData(GenericContainerData.fluid(tanks.primary()));
		addGenericData(GenericContainerData.fluid(tanks.secondary()));
		addGenericData(GenericContainerData.fluid(tanks.output()));
	}
}
