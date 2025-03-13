package flaxbeard.immersivepetroleum.common.multiblocks;

@Deprecated(forRemoval = true)
public class IPClientMultiblockProperties //implements ClientMultiblocks.MultiblockManualData
{
	/*private final flaxbeard.immersivepetroleum.common.blocks.multiblocks.IPTemplateMultiblock multiblock;
	@Nullable
	private NonNullList<ItemStack> materials;
	private final ItemStack renderStack;
	@Nullable
	private final Vec3 renderOffset;
	
	private IPClientMultiblockProperties(flaxbeard.immersivepetroleum.common.blocks.multiblocks.IPTemplateMultiblock multiblock, @Nullable Vec3 renderOffset){
		this.multiblock = multiblock;
		this.renderStack = new ItemStack(multiblock.getBlock());
		this.renderOffset = renderOffset;
	}
	
	public IPClientMultiblockProperties(flaxbeard.immersivepetroleum.common.blocks.multiblocks.IPTemplateMultiblock multiblock, double offX, double offY, double offZ){
		this(multiblock, new Vec3(offX, offY, offZ));
	}
	
	public IPClientMultiblockProperties(IPTemplateMultiblock multiblock){
		this(multiblock, null);
	}
	
	/** Skipping normal rendering behaviour */
	/*protected boolean usingCustomRendering(){
		return false;
	}
	
	@Override
	public NonNullList<ItemStack> getTotalMaterials(){
		// TODO (malte): Add helper for this to IE API
		if(this.materials == null){
			List<StructureTemplate.StructureBlockInfo> structure = this.multiblock.getStructure(null);
			this.materials = NonNullList.create();
			for(StructureTemplate.StructureBlockInfo info:structure){
				ItemStack picked = Utils.getPickBlock(info.state());
				boolean added = false;
				for(ItemStack existing:this.materials)
					if(ItemStack.isSameItem(existing, picked)){
						existing.grow(1);
						added = true;
						break;
					}
				if(!added)
					this.materials.add(picked.copy());
			}
		}
		return this.materials;
	}
	
	@Override
	public boolean canRenderFormedStructure(){
		return this.renderOffset != null;
	}
	
	/** Allowing custom accessories to be rendered. Unused if {@link #usingCustomRendering()} returns true */
	/*public void renderExtras(PoseStack matrix, MultiBufferSource buffer){
	}
	
	/** Only used when {@link #usingCustomRendering()} returns true */
	/*public void renderCustomFormedStructure(PoseStack matrix, MultiBufferSource buffer){
	}
	
	@Override
	public final void renderFormedStructure(PoseStack matrix, MultiBufferSource buffer){
		Objects.requireNonNull(this.renderOffset);
		
		if(usingCustomRendering()){
			renderCustomFormedStructure(matrix, buffer);
			return;
		}
		
		matrix.translate(this.renderOffset.x, this.renderOffset.y, this.renderOffset.z);
		MCUtil.getItemRenderer().renderStatic(this.renderStack, ItemDisplayContext.NONE, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, matrix, buffer, MCUtil.getLevel(), 0);
		matrix.pushPose();
		{
			renderExtras(matrix, buffer);
		}
		matrix.popPose();
	}*/
}
