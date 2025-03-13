package flaxbeard.immersivepetroleum.common.sound;

import blusunrize.immersiveengineering.common.items.EarmuffsItem;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.util.RegistryUtils;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class IPEntitySound implements TickableSoundInstance{
	protected Sound sound;
	private WeighedSoundEvents soundEvent;
	private SoundSource category;
	public Attenuation attenuation;
	public final ResourceLocation resource;
	public float volume;
	public float pitch;
	
	public Entity entity;
	public boolean canRepeat;
	public int repeatDelay;
	public float volumeAjustment = 1;
	
	public IPEntitySound(SoundEvent event, float volume, float pitch, boolean repeat, int repeatDelay, Entity e, Attenuation attenuation, SoundSource category){
		this(RegistryUtils.getRegistryNameOf(event), volume, pitch, repeat, repeatDelay, e, attenuation, category);
	}
	
	public IPEntitySound(ResourceLocation sound, float volume, float pitch, boolean repeat, int repeatDelay, Entity e, Attenuation attenuation, SoundSource category){
		this.attenuation = attenuation;
		this.resource = sound;
		this.volume = volume;
		this.pitch = pitch;
		this.entity = e;
		this.canRepeat = repeat;
		this.repeatDelay = repeatDelay;
		this.category = category;
	}
	
	@Override
	@Nonnull
	public Attenuation getAttenuation(){
		return attenuation;
	}
	
	@Override
	@Nonnull
	public ResourceLocation getLocation(){
		return resource;
	}
	
	@Nullable
	@Override
	public WeighedSoundEvents resolve(SoundManager handler){
		this.soundEvent = handler.getSoundEvent(this.resource);
		if(this.soundEvent == null)
			this.sound = SoundManager.EMPTY_SOUND;
		else
			this.sound = this.soundEvent.getSound(RandomSource.create());
		return this.soundEvent;
	}
	
	@Override
	@Nonnull
	public Sound getSound(){
		return sound;
	}
	
	@Override
	@Nonnull
	public SoundSource getSource(){
		return category;
	}
	
	@Override
	public float getVolume(){
		return volume * volumeAjustment;
	}
	
	@Override
	public float getPitch(){
		return pitch;
	}
	
	@Override
	public double getX(){
		return (float) entity.getX();
	}
	
	@Override
	public double getY(){
		return (float) entity.getY();
	}
	
	@Override
	public double getZ(){
		return (float) entity.getZ();
	}
	
	@Override
	public boolean isLooping(){
		return canRepeat;
	}
	
	@Override
	public int getDelay(){
		return repeatDelay;
	}
	
	public void evaluateVolume(){
		volumeAjustment = 1f;
		if(MCUtil.getPlayer() != null && !MCUtil.getPlayer().getItemBySlot(EquipmentSlot.HEAD).isEmpty()){
			ItemStack stack = MCUtil.getPlayer().getItemBySlot(EquipmentSlot.HEAD);
			if(ItemNBTHelper.hasKey(stack, "IE:Earmuffs"))
				stack = ItemNBTHelper.getItemStack(stack, "IE:Earmuffs");
			if(stack != null && IEItems.Misc.EARMUFFS.asItem().equals(stack.getItem()))
				volumeAjustment = EarmuffsItem.getVolumeMod(stack);
		}
		
		if(volumeAjustment > .1f)
			for(int dx = (int) Math.floor(entity.getX() - 8) >> 4;dx <= (int) Math.floor(entity.getX() + 8) >> 4;dx++){
				for(int dz = (int) Math.floor(entity.getZ() - 8) >> 4;dz <= (int) Math.floor(entity.getZ() + 8) >> 4;dz++){
					for(BlockEntity tile:MCUtil.getPlayer().level().getChunk(dx, dz).getBlockEntities().values()){
						if(tile != null && tile.getClass().getName().contains("SoundMuffler")){
							BlockPos tPos = tile.getBlockPos();
							double d = entity.position().distanceTo(new Vec3(tPos.getX() + .5, tPos.getY() + .5, tPos.getZ() + .5));
							if(d <= 64 && d > 0){
								volumeAjustment = .1f;
							}
						}
					}
				}
			}
		
		if(!entity.isAlive())
			donePlaying = true;
	}
	
	@Override
	public void tick(){
		if(MCUtil.getPlayer() != null && MCUtil.getPlayer().level().getDayTime() % 40 == 0)
			evaluateVolume();
	}
	
	public boolean donePlaying = false;
	
	@Override
	public boolean isStopped(){
		return donePlaying;
	}
	
	@Override
	public boolean isRelative(){
		return false;
	}
}
