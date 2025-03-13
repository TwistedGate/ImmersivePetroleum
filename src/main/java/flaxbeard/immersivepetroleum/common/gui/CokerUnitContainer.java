package flaxbeard.immersivepetroleum.common.gui;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.CokerUnitMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.CokerUnitLogic;
import flaxbeard.immersivepetroleum.common.gui.IPSlot.FluidContainer.FluidFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public class CokerUnitContainer extends MultiblockAwareGuiContainer{

	public final AveragingEnergyStorage energy;
	public final CokerUnitLogic.BufferTanks tanks;
	public final ItemStackHandler items;

	public final Level level;
	public final GetterAndSetter<List<String>> pos;


	public static CokerUnitContainer makeServer(
			MenuType<?> type, int id, Inventory player, MultiblockMenuContext<CokerUnitLogic.State> ctx)
	{
		CokerUnitLogic.State state = ctx.mbContext().getState();
		BlockPos pos = ctx.mbContext().getLevel().getAbsoluteOrigin();
		Level level = ctx.mbContext().getLevel().getRawLevel();

		return new CokerUnitContainer(
				multiblockCtx(type, id, ctx), player,
				new ItemStackHandler(state.inventory),
				state.bufferTanks,
				state.energy,
				level,
				new GetterAndSetter<>(() -> List.of(pos.toShortString()), b ->
				{
					String s = b.get(0);
					String[] coords = s.split(", ");
					state.masterPos = new BlockPos(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
				}));
	}

	public static CokerUnitContainer makeClient(MenuType<?> type, int id, Inventory player)
	{
		return new CokerUnitContainer(clientCtx(type, id), player,
				new ItemStackHandler(CokerUnitLogic.Inventory.values().length),
				new CokerUnitLogic.BufferTanks (),
				new AveragingEnergyStorage(24000),
				player.player.level(),
				GetterAndSetter.standalone(List.of("0, 0, 0")));
	}

	public CokerUnitContainer(MenuContext ctx, Inventory playerInventory, ItemStackHandler items, CokerUnitLogic.BufferTanks tanks, AveragingEnergyStorage energy, Level level, GetterAndSetter<List<String>> pos)
	{
		super(ctx, CokerUnitMultiblock.INSTANCE);
		this.items = items;
		this.tanks = tanks;
		this.energy = energy;
		this.level = level;
		this.pos = pos;

		addSlot(new IPSlot.CokerInput(this, this.items, CokerUnitLogic.Inventory.INPUT.id(), 20, 71));
		addSlot(new IPSlot(this.items, CokerUnitLogic.Inventory.INPUT_FILLED.id(), 9, 14, stack -> FluidUtil.getFluidHandler(stack).map(h -> {
			if(h.getTanks() <= 0 || h.getFluidInTank(0).isEmpty()){
				return false;
			}

			FluidStack fs = h.getFluidInTank(0);
			if(fs.isEmpty() || (this.tanks.input().getFluidAmount() > 0 && !fs.isFluidEqual(this.tanks.input().getFluid()))){
				return false;
			}

			return CokerUnitRecipe.hasRecipeWithInput(fs, true);
		}).orElse(false)));
		addSlot(new IPSlot.ItemOutput(this.items, CokerUnitLogic.Inventory.INPUT_EMPTY.id(), 9, 45));
		
		addSlot(new IPSlot.FluidContainer(this.items, CokerUnitLogic.Inventory.OUTPUT_EMPTY.id(), 175, 14, FluidFilter.EMPTY));
		addSlot(new IPSlot.ItemOutput(this.items, CokerUnitLogic.Inventory.OUTPUT_FILLED.id(), 175, 45));
		
		this.ownSlotCount = CokerUnitLogic.Inventory.values().length;
		
		addPlayerInventorySlots(playerInventory, 20, 105);
		addPlayerHotbarSlots(playerInventory, 20, 163);

		addGenericData(GenericContainerData.energy(energy));
		addGenericData(GenericContainerData.fluid(tanks.input()));
		addGenericData(GenericContainerData.fluid(tanks.output()));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.STRINGS, pos));
	}
}
