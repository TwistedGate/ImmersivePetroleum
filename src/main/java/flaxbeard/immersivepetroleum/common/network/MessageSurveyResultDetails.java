package flaxbeard.immersivepetroleum.common.network;

import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirIsland;
import flaxbeard.immersivepetroleum.client.gui.SeismicSurveyScreen;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.util.survey.SurveyScan;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.*;
import java.util.function.Supplier;

import static flaxbeard.immersivepetroleum.common.util.survey.SurveyScan.SCAN_RADIUS;
import static flaxbeard.immersivepetroleum.common.util.survey.SurveyScan.SCAN_SIZE;

public class MessageSurveyResultDetails{
	
	/** Request the server to give more information on the provided scan */
	public static void sendRequestToServer(SurveyScan scan){
		IPPacketHandler.sendToServer(new MessageSurveyResultDetails.ClientToServer(scan));
	}
	
	private static void sendReply(Player player, UUID scanId, BitSet replyBitSet){
		IPPacketHandler.sendToPlayer(player, new MessageSurveyResultDetails.ServerToClient(scanId, replyBitSet));
	}
	
	public static class ClientToServer implements INetMessage{
		private int x, z;
		private UUID scanId;
		public ClientToServer(SurveyScan scan){
			this.x = scan.getX();
			this.z = scan.getZ();
			this.scanId = scan.getUuid();
		}
		
		public ClientToServer(FriendlyByteBuf buf){
			this.x = buf.readInt();
			this.z = buf.readInt();
			this.scanId = buf.readUUID();
		}
		
		@Override
		public void toBytes(FriendlyByteBuf buf){
			buf.writeInt(this.x);
			buf.writeInt(this.z);
			buf.writeUUID(this.scanId);
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void process(Supplier<Context> context){
			context.get().enqueueWork(() -> {
				final ServerPlayer sPlayer = Objects.requireNonNull(context.get().getSender());
				final ServerLevel sLevel = sPlayer.serverLevel();
				
				if(sLevel.isAreaLoaded(new BlockPos(this.x, 0, this.z), SCAN_RADIUS)){
					final BitSet set = compileBitSet(sLevel);
					sendReply(sPlayer, this.scanId, set);
				}
			});
		}
		
		private BitSet compileBitSet(Level level){
			final List<ReservoirIsland> islandCache = new ArrayList<>();
			final BitSet set = new BitSet(SCAN_SIZE * SCAN_SIZE);
			final int r = SCAN_RADIUS;
			for(int j = -r,a = 0;j <= r;j++,a++){
				for(int i = -r,b = 0;i <= r;i++,b++){
					int x = this.x - i;
					int z = this.z - j;
					
					double current = ReservoirHandler.getValueOf(level, x, z);
					if(current != -1){
						Optional<ReservoirIsland> optional = islandCache.stream().filter(res -> {
							return res.contains(x, z);
						}).findFirst();
						
						ReservoirIsland nearbyIsland = optional.orElse(null);
						if(nearbyIsland == null){
							nearbyIsland = ReservoirHandler.getIslandNoCache(level, new ColumnPos(x, z));
							
							if(nearbyIsland != null){
								islandCache.add(nearbyIsland);
							}
						}
						
						if(nearbyIsland != null){
							set.set(a * SCAN_SIZE + b);
						}
					}
				}
			}
			return set;
		}
	}
	
	public static class ServerToClient implements INetMessage{
		private BitSet replyBitSet;
		private UUID scanId;
		public ServerToClient(UUID scanId, BitSet replyBitSet){
			this.scanId = scanId;
			this.replyBitSet = replyBitSet;
		}
		
		public ServerToClient(FriendlyByteBuf buf){
			this.scanId = buf.readUUID();
			this.replyBitSet = buf.readBitSet();
		}
		
		@Override
		public void toBytes(FriendlyByteBuf buf){
			buf.writeUUID(this.scanId);
			buf.writeBitSet(this.replyBitSet);
		}
		
		@Override
		public void process(Supplier<Context> context){
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
				if(MCUtil.getScreen() instanceof SeismicSurveyScreen surveyScreen && this.scanId.equals(surveyScreen.scan.getUuid())){
					surveyScreen.setBitSet(this.replyBitSet);
				}
			});
		}
	}
}
