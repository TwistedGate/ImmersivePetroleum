package flaxbeard.immersivepetroleum.common.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockPartBlock;
import flaxbeard.immersivepetroleum.common.blocks.interfaces.ICanSkipGUI;
import flaxbeard.immersivepetroleum.common.blocks.ticking.IPCommonTickableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

public class IPMetalMultiblock<T extends IMultiblockState & IPCommonTickableTile> extends MultiblockPartBlock<T> {
	//private final MultiblockBEType<T> multiblockBEType;

	public IPMetalMultiblock(MultiblockRegistration<T> te){
		super(Block.Properties.of()
				.mapColor(MapColor.METAL)
				.sound(SoundType.METAL)
				.strength(3, 15)
				.requiresCorrectToolForDrops()
				.isViewBlocking((state, blockReader, pos) -> false)
				.noOcclusion()
				.dynamicShape(),
				te
		);
		//this.multiblockBEType = te;
	}
	
	/*@Override
	public <E extends BlockEntity> BlockEntityTicker<E> getTicker(@Nonnull Level world, @Nonnull BlockState state, @Nonnull BlockEntityType<E> type){
		return IPBlockBase.createCommonTicker(world.isClientSide, type, multiblockBEType.master());
	}*/
	
	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit){
		if(hand == InteractionHand.MAIN_HAND){
			BlockEntity te = world.getBlockEntity(pos);
			
			if(te instanceof MenuProvider menuProvider){
				if(te instanceof ICanSkipGUI skippable && !player.getItemInHand(hand).isEmpty() && skippable.skipGui(hit.getDirection())){
					return InteractionResult.FAIL;
				}
				
				/*if(!player.isShiftKeyDown()){
					if(player instanceof ServerPlayer serverPlayer){
						if(menuProvider instanceof IHasGUIInteraction<?> interaction){
							interaction = interaction.getGuiMaster();
							if(interaction != null && interaction.canUseGui(player)){
								
								// Between these lines is basicly a direct copy-paste from IEEntityBlock
								// ---------------------------------------------------------------------------- //
								
								// This can be removed once IEBaseContainerOld is gone
								AbstractContainerMenu tempMenu = interaction.createMenu(0, player.getInventory(), player);
								if(tempMenu instanceof IEBaseContainerOld<?>)
									NetworkHooks.openScreen(serverPlayer, interaction, ((BlockEntity) interaction).getBlockPos());
								else
									NetworkHooks.openScreen(serverPlayer, interaction);
								// ---------------------------------------------------------------------------- //
							}
						}
					}
					return InteractionResult.SUCCESS;
				}*/
			}
		}
		return super.use(state, world, pos, player, hand, hit);
	}
}
