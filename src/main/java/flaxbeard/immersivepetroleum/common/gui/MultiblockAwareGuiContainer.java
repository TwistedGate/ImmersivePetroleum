package flaxbeard.immersivepetroleum.common.gui;

import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import blusunrize.immersiveengineering.common.gui.IEContainerMenu;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * @author TwistedGate © 2021
 */
@SuppressWarnings("deprecation")
// TODO Replace IEBaseContainerOld as soon as possible
public class MultiblockAwareGuiContainer extends IEContainerMenu {
	//static final Vec3i ONE = new Vec3i(1, 1, 1);
	
	//protected BlockPos templateSize;
	public MultiblockAwareGuiContainer(MenuContext ctx, IETemplateMultiblock template){
		super(ctx);
		
		//this.templateSize = new BlockPos(template.getSize(null));
	}
	
	// TODO This is only Temporary until i've replaced IEBaseContainerOld
	/** Only exists to keep the Deprecation warning at bay and will then be removed/replace */
	/*public Container getInv(){
		return this.inv;
	}*/
	
	// TODO This is only Temporary until i've replaced IEBaseContainerOld
	/** Only exists to keep the Deprecation warning at bay and will then be removed/replace */
	/*public T getTile(){
		return this.tile;
	}*/
	
	/**
	 * Returns the maximum distance in blocks to the multiblock befor the GUI get's closed automaticly
	 */
	/*public int getMaxDistance(){
		return 5;
	}*/
	
	/*@Override
	public boolean stillValid(@Nonnull Player player){
		if(getInv() != null){
			BlockPos min = getTile().getBlockPosForPos(BlockPos.ZERO);
			BlockPos max = getTile().getBlockPosForPos(this.templateSize);
			
			AABB box = new AABB(min, max).inflate(getMaxDistance());
			
			return box.intersects(player.getBoundingBox());
		}
		
		return false;
	}*/
	
	protected final void addPlayerInventorySlots(Inventory playerInventory, int x, int y){
		for(int i = 0;i < 3;i++){
			for(int j = 0;j < 9;j++){
				addSlot(new Slot(playerInventory, j + i * 9 + 9, x + j * 18, y + i * 18));
			}
		}
	}
	
	protected final void addPlayerHotbarSlots(Inventory playerInventory, int x, int y){
		for(int i = 0;i < 9;i++){
			addSlot(new Slot(playerInventory, i, x + i * 18, y));
		}
	}
}
