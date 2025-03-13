package flaxbeard.immersivepetroleum.common.items;

import blusunrize.immersiveengineering.api.tool.IUpgrade;
import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public class IPUpgradeItem extends IPItemBase implements IUpgrade{
	private Set<String> set;
	public IPUpgradeItem(String type){
		super(new Item.Properties().stacksTo(1));
		this.set = ImmutableSet.of(type);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(@Nonnull ItemStack stack, Level worldIn, List<Component> tooltip, @Nonnull TooltipFlag flagIn){
		tooltip.add(Component.translatable("desc.immersivepetroleum.flavour." + ForgeRegistries.ITEMS.getKey(this).getPath()));
	}
	
	@Override
	public Set<String> getUpgradeTypes(ItemStack upgrade){
		return this.set;
	}
	
	@Override
	public boolean canApplyUpgrades(ItemStack target, ItemStack upgrade){
		return true;
	}
	
	@Override
	public void applyUpgrades(ItemStack target, ItemStack upgrade, CompoundTag modifications){
	}
}
