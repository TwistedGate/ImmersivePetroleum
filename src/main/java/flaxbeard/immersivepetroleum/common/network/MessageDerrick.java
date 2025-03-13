package flaxbeard.immersivepetroleum.common.network;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import flaxbeard.immersivepetroleum.client.gui.elements.PipeConfig;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.DerrickLogic;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.WellTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class MessageDerrick implements INetMessage{
	
	public static void sendToServer(BlockPos derrickPos, PipeConfig.Grid grid){
		IPPacketHandler.sendToServer(new MessageDerrick(derrickPos, grid));
	}
	
	BlockPos derrickPos;
	CompoundTag nbt;
	
	private MessageDerrick(BlockPos derrick, PipeConfig.Grid grid){
		this.derrickPos = derrick;
		this.nbt = grid.toCompound();
	}
	
	public MessageDerrick(FriendlyByteBuf buf){
		this.nbt = buf.readNbt();
		this.derrickPos = buf.readBlockPos();
	}
	
	@Override
	public void toBytes(FriendlyByteBuf buf){
		buf.writeNbt(this.nbt);
		buf.writeBlockPos(this.derrickPos);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void process(Supplier<NetworkEvent.Context> context){
		context.get().enqueueWork(() -> {
			NetworkEvent.Context con = context.get();
			
			if(con.getDirection().getReceptionSide() == LogicalSide.SERVER){
				ServerLevel world = Objects.requireNonNull(con.getSender()).serverLevel();
				if(world.isAreaLoaded(this.derrickPos, 2)){
					BlockEntity te = world.getBlockEntity(this.derrickPos);

					if(te instanceof IMultiblockBE<?> derrick && derrick.getHelper().getContext().getState() instanceof DerrickLogic.State){

						DerrickLogic.State state = derrick.getHelper().asType(IPContent.Multiblock.DERRICK).getState();
						IMultiblockContext<DerrickLogic.State> ctx = derrick.getHelper().asType(IPContent.Multiblock.DERRICK).getContext();



						state.gridStorage = PipeConfig.Grid.fromCompound(this.nbt);
						ctx.markDirtyAndSync();
						ctx.requestMasterBESync();
						
						WellTileEntity well = state.getWell(ctx.getLevel(), ctx.getLevel().getAbsoluteOrigin());
						DerrickLogic.transferGridDataToWell(this.derrickPos, state, well);
					}
				}
			}
		});
	}
}
