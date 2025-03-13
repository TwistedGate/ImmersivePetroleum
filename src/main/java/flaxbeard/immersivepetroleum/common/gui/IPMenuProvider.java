package flaxbeard.immersivepetroleum.common.gui;

import blusunrize.immersiveengineering.common.gui.IEContainerMenu;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import flaxbeard.immersivepetroleum.common.blocks.interfaces.IHasGUIInteraction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.RegistryObject;

public interface IPMenuProvider<T extends BlockEntity & IPMenuProvider<T>> extends IHasGUIInteraction<T>{
	record BEContainerIP<T extends BlockEntity, C extends IEContainerMenu> (RegistryObject<MenuType<C>> type, IEMenuTypes.ArgContainerConstructor<T, C> factory){
		public C create(int windowId, Inventory playerInv, T tile){
			return factory.construct(getType(), windowId, playerInv, tile);
		}
		
		public MenuType<C> getType(){
			return type.get();
		}
	}
}
