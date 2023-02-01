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
	
	public static ShaderRegistryEntry addProjectorShader(String name, Rarity rarity, int colorPrimary, int colorSecondary, int colorBackground, boolean loot, boolean bags, LayerAdder<Integer, Integer, Integer, List<ShaderLayer>> extraLayers){
		ResourceLocation rlName = ResourceUtils.ip(name);
		
		ShaderRegistry.registerShader_Item(rlName, rarity, colorBackground, colorPrimary, colorSecondary);
		
		List<ShaderLayer> list = new ArrayList<>();
		extraLayers.accept(colorPrimary, colorSecondary, colorBackground, list);
		list.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_uncolored"), -1));
		
		ShaderCaseProjector shader = new ShaderCaseProjector(list);
		ShaderRegistry.registerShaderCase(rlName, shader, rarity);
		
		for(IShaderRegistryMethod<?> method:ShaderRegistry.shaderRegistrationMethods){
			method.apply(rlName, "0", rarity, colorBackground, colorPrimary, colorSecondary, 0xFFFFFFFF, null, 0xFFFFFFFF);
		}
		
		return ShaderRegistry.shaderRegistry.get(rlName)
				.setCrateLoot(loot)
				.setBagLoot(bags)
				.setReplicationCost(() -> new IngredientWithSize(Ingredient.of(ShaderRegistry.defaultReplicationCost), 10 - ShaderRegistry.rarityWeightMap.get(rarity)));
	}
	
	@FunctionalInterface
	private interface LayerAdder<P, S, B, L>{
		void accept(P colorPrimary, S colorSecondary, B colorBackground, L list);
	}
}
