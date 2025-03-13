package flaxbeard.immersivepetroleum.common.gui;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.ExternalModContent;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.DerrickMultiblock;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.DerrickLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class DerrickContainer extends MultiblockAwareGuiContainer {

	public final IEnergyStorage energy;
	public final FluidTank tank;
	public final ItemStackHandler items;
	public final Level level;
	public final GetterAndSetter<List<String>> pos;

	public static DerrickContainer makeServer(
			MenuType<?> type, int id, Inventory player, MultiblockMenuContext<DerrickLogic.State> ctx)
	{
		DerrickLogic.State state = ctx.mbContext().getState();
		BlockPos pos = ctx.mbContext().getLevel().getAbsoluteOrigin();
		Level level = ctx.mbContext().getLevel().getRawLevel();

		return new DerrickContainer(
				multiblockCtx(type, id, ctx), player,
				new ItemStackHandler(state.inventory),
				state.tank,
				state.energy,
				level,
				new GetterAndSetter<>(() -> List.of(pos.toShortString()), b ->
				{
					String s = b.get(0);
					String[] coords = s.split(", ");
					state.originPos = new BlockPos(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
				}));
	}

	public static DerrickContainer makeClient(MenuType<?> type, int id, Inventory player)
	{
		return new DerrickContainer(clientCtx(type, id), player,
				new ItemStackHandler(1),
				new FluidTank(8000),
				new AveragingEnergyStorage(16000),
				player.player.level(),
				GetterAndSetter.standalone(List.of("0, 0, 0")));
	}

	private DerrickContainer(MenuContext ctx, Inventory playerInventory, ItemStackHandler items, FluidTank tank, AveragingEnergyStorage energy, Level level, GetterAndSetter<List<String>> pos){
		super(ctx, DerrickMultiblock.INSTANCE);
		this.items = items;
		this.energy = energy;
		this.tank = tank;
		this.level = level;
		this.pos = pos;

		this.addSlot(new SlotItemHandler(this.items, 0, 92, 55){
			@Override
			public boolean mayPlace(@Nonnull ItemStack stack){
				return ExternalModContent.isIEItem_Pipe(stack);
			}
		});
		
		this.ownSlotCount = 1;
		
		addPlayerInventorySlots(playerInventory, 20, 82);
		addPlayerHotbarSlots(playerInventory, 20, 140);

		addGenericData(new GenericContainerData<>(GenericDataSerializers.STRINGS, pos));
		addGenericData(GenericContainerData.energy(energy));
		addGenericData(GenericContainerData.fluid(tank));
	}

	public static BlockPos getPos(List<String> coords)
	{
		String s = coords.get(0);
		String[] splited = s.split(", ");
		return new BlockPos(Integer.parseInt(splited[0]), Integer.parseInt(splited[1]), Integer.parseInt(splited[2]));
	}
}
