package flaxbeard.immersivepetroleum.common.shaderscases;

import java.util.Collection;

import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderLayer;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.resources.ResourceLocation;

public class ShaderCaseProjector extends ShaderCase{
	public static final ResourceLocation TYPE = ResourceUtils.ip("projector");
	
	public ShaderCaseProjector(ShaderLayer... layers){
		super(layers);
	}
	
	public ShaderCaseProjector(Collection<ShaderLayer> layers){
		super(layers);
	}
	
	@Override
	public int getLayerInsertionIndex(){
		return this.layers.length - 1;
	}
	
	@Override
	public boolean shouldRenderGroupForPass(String modelPart, int pass){
		return true;
	}
	
	@Override
	public ResourceLocation getShaderType(){
		return TYPE;
	}
}