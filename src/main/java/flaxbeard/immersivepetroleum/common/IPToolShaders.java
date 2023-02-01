package flaxbeard.immersivepetroleum.common;

import java.util.ArrayList;
import java.util.List;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.shader.ShaderLayer;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.IShaderRegistryMethod;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import flaxbeard.immersivepetroleum.common.shaderscases.ShaderCaseProjector;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.Ingredient;

public class IPToolShaders{
	
	public static void preInit(){
		addShader("test",	0, Rarity.UNCOMMON,	0xFFFF0000, 0xFF00FF00, 0xFF0000FF, null, false, 0xFFFF00FF).setInfo(null, "Test", "Experimental");
	}
	
	public static ShaderRegistryEntry addShader(String name, int overlayType, Rarity rarity, int colorPrimary, int colorSecondary, int colorBackground, String additionalTexture, boolean loot, int colourOverlay){
		return addShader(ResourceUtils.ip(name), Integer.toString(overlayType), rarity, colorPrimary, colorSecondary, colorBackground, additionalTexture, colourOverlay, loot, true);
	}
	
	public static ShaderRegistryEntry addShader(ResourceLocation name, String overlayType, Rarity rarity, int colorPrimary, int colorSecondary, int colorBackground, String additionalTexture, int colourAdditional, boolean loot, boolean bags){
		ShaderRegistry.registerShader_Item(name, rarity, colorBackground, colorPrimary, colorSecondary);
		registerShader_Projector(name, overlayType, rarity, colorBackground, colorPrimary, colorSecondary, additionalTexture, colourAdditional);
		
		for(IShaderRegistryMethod<?> method:ShaderRegistry.shaderRegistrationMethods){
			method.apply(name, overlayType, rarity, colorBackground, colorPrimary, colorSecondary, 0xFFFFFF, additionalTexture, colourAdditional);
		}
		
		return ShaderRegistry.shaderRegistry.get(name)
			.setCrateLoot(loot)
			.setBagLoot(bags)
			.setReplicationCost(() -> new IngredientWithSize(Ingredient.of(ShaderRegistry.defaultReplicationCost), 10 - ShaderRegistry.rarityWeightMap.get(rarity)));
	}
	
	public static ShaderCaseProjector registerShader_Projector(ResourceLocation name, String overlayType, Rarity rarity, int color0, int color1, int color2, String additionalTexture, int colourAddtional){
		List<ShaderLayer> list = new ArrayList<>();
		list.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_0"), color0));
		list.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_0"), color1));
		list.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_1_" + overlayType), color2)); // Glowy bits?
		if(additionalTexture != null){
			// Do not understand yet
		}
		list.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_uncolored"), 0xFFFFFFFF));
		
		ShaderCaseProjector shader = new ShaderCaseProjector(list);
		return ShaderRegistry.registerShaderCase(name, shader, rarity);
	}
}
