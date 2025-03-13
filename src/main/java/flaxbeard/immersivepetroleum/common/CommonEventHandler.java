package flaxbeard.immersivepetroleum.common;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.ILubricationHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler.LubricatedTileInfo;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.common.cfg.IPServerConfig;
import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import flaxbeard.immersivepetroleum.common.fluids.NapalmFluid;
import flaxbeard.immersivepetroleum.common.util.IPEffects;
import flaxbeard.immersivepetroleum.common.util.Utils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.*;

public class CommonEventHandler{
	@SubscribeEvent
	public void onSave(LevelEvent.Save event){
		if(!event.getLevel().isClientSide()){
			IPSaveData.markDirty();
		}
	}
	
	@SubscribeEvent
	public void onUnload(LevelEvent.Unload event){
		if(!event.getLevel().isClientSide()){
			IPSaveData.markDirty();
			ReservoirRegionDataStorage.get().markAllDirty();
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onServerStopped(ServerStoppedEvent event){
		ImmersivePetroleum.log.debug("[ReservoirIslands]: Clearing Cache...");
		ReservoirHandler.clearCache();
	}
	
	@SubscribeEvent
	public void handleBoatImmunity(LivingAttackEvent event){
		Level level = event.getEntity().level();
		DamageSources dmg = level.damageSources();
		if(event.getSource() == dmg.lava() || event.getSource() == dmg.onFire() || event.getSource() == dmg.inFire()){
			LivingEntity entity = event.getEntity();
			if(entity.getVehicle() instanceof MotorboatEntity boat){
				if(boat.isFireproof){
					event.setCanceled(true);
					return;
				}
			}
			
			if(entity.getRemainingFireTicks() > 0 && entity.getEffect(IPEffects.ANTI_DISMOUNT_FIRE.get()) != null){
				entity.clearFire();
				entity.removeEffect(IPEffects.ANTI_DISMOUNT_FIRE.get());
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public void handleBoatImmunity(PlayerTickEvent event){
		Player entity = event.player;
		if(entity.isOnFire() && entity.getVehicle() instanceof MotorboatEntity boat){
			if(boat.isFireproof){
				entity.clearFire();
				boat.setSharedFlag(0, false);
			}
		}
	}
	
	/**
	 * Handles dismounting the Speedboat while in lava to trying avoid getting burned
	 */
	@SubscribeEvent
	public void handleDismountingBoat(EntityMountEvent event){
		if(event.getEntityMounting() == null){
			return;
		}
		
		if(event.getEntityMounting() instanceof LivingEntity living && event.getEntityBeingMounted() instanceof MotorboatEntity boat){
			if(event.isDismounting()){
				if(boat.isFireproof){
					FluidState fluidstate = event.getLevel().getBlockState(BlockPos.containing(boat.position().add(0.5, 0, 0.5))).getFluidState();
					if(fluidstate != Fluids.EMPTY.defaultFluidState() && fluidstate.is(FluidTags.LAVA)){
						living.addEffect(new MobEffectInstance(IPEffects.ANTI_DISMOUNT_FIRE.get(), 1, 0, false, false));
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void handleLubricatingMachinesServer(TickEvent.LevelTickEvent event){
		if(event.phase == Phase.END){
			handleLubricatingMachines(event.level);
		}
	}
	
	static final Random random = new Random();
	@SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
	public static void handleLubricatingMachines(Level world){
		Set<LubricatedTileInfo> toRemove = new HashSet<>();
		for(LubricatedTileInfo info:LubricatedHandler.lubricatedTiles){
			if(info.world == world.dimension() && world.isAreaLoaded(info.pos, 0)){
				BlockEntity te = world.getBlockEntity(info.pos);
				if (te instanceof IMultiblockBEHelper<?> helper)
				{
					ILubricationHandler lubeHandler = LubricatedHandler.getHandlerForTile(helper);
					if(lubeHandler != null){
						if(lubeHandler.isMachineEnabled(world, helper)){
							if(world.isClientSide){
								lubeHandler.lubricateClient((ClientLevel) world, info.lubricant, info.ticks, helper);
							}else{
								lubeHandler.lubricateServer((ServerLevel) world, info.lubricant, info.ticks, helper);
							}
						}

						if(world.isClientSide){
							if(te instanceof IMultiblockBE<?> part){

								Vec3i size = lubeHandler.getStructureDimensions();
								int numBlocks = (int) (size.getX() * size.getY() * size.getZ() * 0.25F);
								for(int i = 0;i < numBlocks;i++){
									BlockPos pos = part.getHelper().getContext().getLevel().toAbsolute(BlockPos.containing(size.getX() * random.nextFloat(), size.getY() * random.nextFloat(), size.getZ() * random.nextFloat()));

									if(world.getBlockState(pos).getBlock() != Blocks.AIR && world.getBlockEntity(pos) instanceof IMultiblockBE<?> part2 && part2.getHelper().getContext().getState() == part.getHelper().getContext().getState()){
										for(Direction facing:Direction.Plane.HORIZONTAL){
											if(world.random.nextInt(30) == 0){
												Vec3i direction = facing.getNormal();

												float x = (pos.getX() + .5f) + (direction.getX() * .65f);
												float y = pos.getY() + 1;
												float z = (pos.getZ() + .5f) + (direction.getZ() * .65f);

												world.addParticle(ParticleTypes.FALLING_HONEY, x, y, z, 0, 0, 0);
											}
										}
									}
								}
							}
						}

						if(info.ticks-- <= 0)
							toRemove.add(info);
					}
				}
			}
		}
		
		for(LubricatedTileInfo info:toRemove){
			LubricatedHandler.lubricatedTiles.remove(info);
		}
	}
	
	@SubscribeEvent
	public void onEntityJoiningWorld(EntityJoinLevelEvent event){
		if(event.getEntity() instanceof Player player){
			if(event.getEntity() instanceof FakePlayer){
				return;
			}
			
			if(IPServerConfig.MISCELLANEOUS.autounlock_recipes.get()){
				List<Recipe<?>> l = new ArrayList<>();
				Collection<Recipe<?>> recipes = event.getLevel().getRecipeManager().getRecipes();
				recipes.forEach(recipe -> {
					ResourceLocation name = recipe.getId();
					if(name.getNamespace().equals(ImmersivePetroleum.MODID)){
						l.add(recipe);
					}
				});
				
				player.awardRecipes(l);
			}
		}
	}
	
	@SubscribeEvent
	public void livingDeath(LivingDeathEvent event){
		if(event.getEntity() instanceof Skeleton skelly && !skelly.level().isClientSide){
			DamageSource src = event.getSource();
			if(src.getEntity() instanceof Player player && !player.level().isClientSide){
				if(player.getVehicle() instanceof MotorboatEntity motorboat && !motorboat.level().isClientSide){
					if(src.isIndirect() && motorboat.isSpinningFastEnough() && motorboat.hasRudders){
						Utils.unlockIPAdvancement(player, "main/rudders");
					}
				}
			}
		}
	}
	
	public static final Map<ResourceLocation, List<BlockPos>> napalmPositions = new HashMap<>();
	public static final Map<ResourceLocation, List<BlockPos>> toRemove = new HashMap<>();
	
	@SubscribeEvent
	public void handleNapalm(TickEvent.LevelTickEvent event){
		if(event.side == LogicalSide.CLIENT)
			return;
		
		ResourceLocation d = event.level.dimension().location();

		switch (event.phase) {
			case START -> {
				if (napalmPositions.get(d) != null) {
					List<BlockPos> trList = toRemove.computeIfAbsent(d, f -> new ArrayList<>());

					new ArrayList<>(napalmPositions.get(d)).forEach(pos -> {
						BlockState state = event.level.getBlockState(pos);
						if (state.getBlock() instanceof LiquidBlock fluidBlock && fluidBlock == IPContent.Fluids.NAPALM.block().get()) {
							NapalmFluid.processFire(IPContent.Fluids.NAPALM, event.level, pos);
						}
						trList.add(pos);
					});
				}

			}
			case END -> {
				if (toRemove.get(d) != null && napalmPositions.get(d) != null) {
					List<BlockPos> list = new ArrayList<>(toRemove.get(d));
					napalmPositions.get(d).removeAll(list);
					toRemove.get(d).clear();
				}

			}
		}
	}
}
