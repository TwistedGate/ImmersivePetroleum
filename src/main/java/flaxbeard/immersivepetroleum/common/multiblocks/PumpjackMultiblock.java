package flaxbeard.immersivepetroleum.common.multiblocks;

@Deprecated(forRemoval = true)
public class PumpjackMultiblock //extends IPTemplateMultiblock
{
	/*public static final PumpjackMultiblock INSTANCE = new PumpjackMultiblock();
	
	private PumpjackMultiblock(){
		super(ResourceUtils.ip("multiblocks/pumpjack"), new BlockPos(1, 0, 0), new BlockPos(1, 1, 4), new BlockPos(3, 4, 6), IPContent.Multiblock.PUMPJACK);
	}
	
	@Override
	public float getManualScale(){
		return 12;
	}
	
	@Override
	public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer){
		consumer.accept(new PumpjackClientData());
	}
	
	public class PumpjackClientData extends IPClientMultiblockProperties{
		private BlockEntity te;
		private List<BakedQuad> list;
		public PumpjackClientData(){
			super(PumpjackMultiblock.INSTANCE, 0, 0, 0);
		}
		
		@Override
		protected boolean usingCustomRendering(){
			return true;
		}
		
		@Override
		public boolean canRenderFormedStructure(){
			return true;
		}
		
		@Override
		public void renderCustomFormedStructure(PoseStack matrix, MultiBufferSource buffer){
			if(this.te == null){
				this.te = IPContent.Multiblock.PUMPJACK.masterBE().get().create(BlockPos.ZERO, IPContent.Multiblock.PUMPJACK.block().get().defaultBlockState());
			}
			
			if(this.list == null){
				BlockState state = this.te.getBlockState();
				BakedModel model = MCUtil.getBlockRenderer().getBlockModel(state);
				this.list = model.getQuads(state, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
			}
			
			if(this.list != null && this.list.size() > 0){
				Level world = MCUtil.getLevel();
				if(world != null){
					matrix.pushPose();
					{
						matrix.translate(1, 0, 0);
						
						RenderUtils.renderModelTESRFast(this.list, buffer.getBuffer(RenderType.solid()), matrix, 0xF000F0, OverlayTexture.NO_OVERLAY);
						
						matrix.pushPose();
						{
							matrix.mulPose(this.rot);
							matrix.translate(-2, -1, -1);
							ImmersivePetroleum.proxy.renderTile(this.te, buffer.getBuffer(RenderType.solid()), matrix, buffer);
						}
						matrix.popPose();
					}
					matrix.popPose();
				}
			}
		}
		
		final Quaternionf rot = Axis.YP.rotationDegrees(90);
	}*/
}
