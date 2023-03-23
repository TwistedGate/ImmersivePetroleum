package flaxbeard.immersivepetroleum.common.util.compat.jei;

import java.util.Locale;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;

import flaxbeard.immersivepetroleum.api.crafting.HighPressureRefineryRecipe;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import flaxbeard.immersivepetroleum.common.util.Utils;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class HighPressureRefineryRecipeCategory extends IPRecipeCategory<HighPressureRefineryRecipe>{
	public static final ResourceLocation ID = ResourceUtils.ip("hydrotreater");
	
	private final IDrawableStatic tankOverlay;
	public HighPressureRefineryRecipeCategory(IGuiHelper guiHelper){
		super(HighPressureRefineryRecipe.class, guiHelper, ID, "block.immersivepetroleum.hydrotreater");
		ResourceLocation background = ResourceUtils.ip("textures/gui/jei/hydrotreater.png");
		setBackground(guiHelper.createDrawable(background, 0, 0, 113, 75));
		setIcon(new ItemStack(IPContent.Multiblock.HYDROTREATER.get()));
		
		this.tankOverlay = guiHelper.createDrawable(background, 113, 0, 20, 51);
	}
	
	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, HighPressureRefineryRecipe recipe, @Nonnull IFocusGroup focuses){
		builder.addSlot(RecipeIngredientRole.INPUT, 25, 3)
			.setFluidRenderer(1, false, 20, 51)
			.setOverlay(this.tankOverlay, 0, 0)
			.addIngredients(ForgeTypes.FLUID_STACK, recipe.inputFluid.getMatchingFluidStacks());
		
		IRecipeSlotBuilder secondary = builder.addSlot(RecipeIngredientRole.INPUT, 3, 3)
			.setFluidRenderer(1, false, 20, 51)
			.setOverlay(this.tankOverlay, 0, 0);
		if(recipe.inputFluidSecondary != null)
			secondary.addIngredients(ForgeTypes.FLUID_STACK, recipe.inputFluidSecondary.getMatchingFluidStacks());
		
		builder.addSlot(RecipeIngredientRole.OUTPUT, 71, 3)
			.setFluidRenderer(1, false, 20, 51)
			.setOverlay(this.tankOverlay, 0, 0)
			.addIngredient(ForgeTypes.FLUID_STACK, recipe.output);
		
		builder.addSlot(RecipeIngredientRole.OUTPUT, 94, 21)
			.addIngredient(VanillaTypes.ITEM_STACK, recipe.outputItem);
	}
	
	@Override
	public void draw(HighPressureRefineryRecipe recipe, @Nonnull IRecipeSlotsView recipeSlotsView, PoseStack matrix, double mouseX, double mouseY){
		IDrawable background = getBackground();
		int bWidth = background.getWidth();
		int bHeight = background.getHeight();
		Font font = MCUtil.getFont();
		
		int time = recipe.getTotalProcessTime();
		int energy = recipe.getTotalProcessEnergy();
		int chance = (int) (100 * recipe.chance);
		
		matrix.pushPose();
		String text0 = I18n.get("desc.immersiveengineering.info.ift", Utils.fDecimal(energy));
		font.draw(matrix, text0, bWidth / 2 - font.width(text0) / 2, bHeight - (font.lineHeight * 2), 0);
		
		String text1 = I18n.get("desc.immersiveengineering.info.seconds", Utils.fDecimal(time / 20D));
		font.draw(matrix, text1, bWidth / 2 - font.width(text1) / 2, bHeight - font.lineHeight, 0);
		
		if(recipe.hasSecondaryItem()){
			String text2 = String.format(Locale.US, "%d%%", chance);
			font.draw(matrix, text2, bWidth + 3 - font.width(text2), bHeight / 2 + 4, 0);
		}
		matrix.popPose();
	}
	
	@Override
	@Deprecated
	public ResourceLocation getUid(){
		return null;
	}
	
	@Override
	@Deprecated
	public Class<? extends HighPressureRefineryRecipe> getRecipeClass(){
		return null;
	}
}
