package flaxbeard.immersivepetroleum.common.util.compat.jei;

import java.util.ArrayList;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.crafting.DistillationRecipe;
import flaxbeard.immersivepetroleum.client.gui.DistillationTowerScreen;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.compat.jei.distillation.DistillationRecipeCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class JEIStuff implements IModPlugin{
	private static final ResourceLocation ID = new ResourceLocation(ImmersivePetroleum.MODID, "main");
	
	@Override
	public ResourceLocation getPluginUid(){
		return ID;
	}
	
	@Override
	public void registerCategories(IRecipeCategoryRegistration registration){
		IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
		
		registration.addRecipeCategories(new DistillationRecipeCategory(guiHelper));
	}
	
	@Override
	public void registerRecipes(IRecipeRegistration registration){
		registration.addRecipes(new ArrayList<>(DistillationRecipe.recipes.values()), DistillationRecipeCategory.ID);
	}
	
	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration){
		registration.addRecipeCatalyst(new ItemStack(IPContent.Multiblock.distillationtower), DistillationRecipeCategory.ID);
	}
	
	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration){
		registration.addRecipeClickArea(DistillationTowerScreen.class, 85, 19, 18, 51, DistillationRecipeCategory.ID);
	}
}
