package flaxbeard.immersivepetroleum.common.util.survey;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirIsland;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SurveyScan implements ISurveyInfo{
	public static final String TAG_KEY = "surveyscan";
	
	public static final int SCAN_RADIUS = 32;
	public static final int SCAN_SIZE = SCAN_RADIUS * 2 + 1;
	private static final double sqrt2048 = Math.sqrt((SCAN_RADIUS * SCAN_RADIUS) * 2);
	
	@Nullable
	private UUID uuid;
	private int x, z;
	private byte[] data;
	
	public SurveyScan(CompoundTag tag){
		this.uuid = tag.hasUUID("uuid") ? tag.getUUID("uuid") : null;
		this.x = tag.getInt("x");
		this.z = tag.getInt("z");
		this.data = tag.getByteArray("map");
	}
	
	public SurveyScan(Level world, BlockPos pos){
		this.uuid = UUID.randomUUID();
		this.x = pos.getX();
		this.z = pos.getZ();
		
		this.data = scanArea(world, pos);
	}
	
	@Nullable
	public UUID getUuid(){
		return this.uuid;
	}
	
	public int getX(){
		return this.x;
	}
	
	public int getZ(){
		return this.z;
	}
	
	public byte[] getData(){
		return this.data;
	}
	
	@Override
	public CompoundTag writeToStack(ItemStack stack){
		return writeToTag(stack.getOrCreateTagElement(TAG_KEY));
	}
	
	@Override
	public CompoundTag writeToTag(CompoundTag tag){
		tag.putUUID("uuid", UUID.randomUUID());
		tag.putInt("x", this.x);
		tag.putInt("z", this.z);
		tag.putByteArray("map", this.data);
		
		return tag;
	}
	
	private byte[] scanArea(Level world, BlockPos pos){
		final List<ReservoirIsland> islandCache = new ArrayList<>();
		byte[] scanData = new byte[SCAN_SIZE * SCAN_SIZE];
		
		for(int j = -SCAN_RADIUS,
				a = 0;j <= SCAN_RADIUS;j++,a++){
			for(int i = -SCAN_RADIUS,
					b = 0;i <= SCAN_RADIUS;i++,b++){
				int x = pos.getX() - i;
				int z = pos.getZ() - j;
				
				int data = 0;
				double current = ReservoirHandler.getValueOf(world, x, z);
				if(current != -1){
					Optional<ReservoirIsland> optional = islandCache.stream().filter(res -> {
						return res.contains(x, z);
					}).findFirst();
					
					ReservoirIsland nearbyIsland = optional.isPresent() ? optional.get() : null;
					if(nearbyIsland == null){
						nearbyIsland = ReservoirHandler.getIslandNoCache(world, new ColumnPos(x, z));
						
						if(nearbyIsland != null){
							islandCache.add(nearbyIsland);
						}
					}
					
					if(nearbyIsland != null){
						data = (int) Mth.clamp(255 * current, 0, 255);
					}
				}
				
				int noise = 31 + (int) (127 * Math.random());
				
				double blend = Math.sqrt(i * i + j * j) / sqrt2048;
				int lerped = (int) (Mth.clampedLerp(data, noise, blend));
				scanData[(a * SCAN_SIZE) + b] = (byte) (lerped & 0xFF);
			}
		}
		
		return normalizeScanData(scanData);
	}
	
	private byte[] normalizeScanData(byte[] scanData){
		int max = Integer.MIN_VALUE;
		for(int i = 0;i < scanData.length;i++){
			int data = ((int) scanData[i]) & 0xFF;
			if(data > max) max = data;
		}
		for(int i = 0;i < scanData.length;i++){
			int data = ((int) scanData[i]) & 0xFF;
			scanData[i] = (byte) (255 * (data / (float) max));
		}
		return scanData;
	}
}
