package flaxbeard.immersivepetroleum.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import flaxbeard.immersivepetroleum.client.render.IPRenderTypes;
import flaxbeard.immersivepetroleum.client.render.dyn.DynamicTextureWrapper;
import flaxbeard.immersivepetroleum.common.network.MessageSurveyResultDetails;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import flaxbeard.immersivepetroleum.common.util.survey.SurveyScan;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

public class SeismicSurveyScreen extends Screen{
	private static final ResourceLocation GUI_TEXTURE = ResourceUtils.ip("textures/gui/seismicsurvey_gui.png");
	private static final ResourceLocation OVERLAY_TEXTURE = ResourceUtils.ip("textures/gui/seismicsurvey_overlay.png");
	
	private static final int X_SIZE = 154;
	private static final int Y_SIZE = 154;
	
	private int guiLeft;
	private int guiTop;
	
	private int surveyLeft, surveyTop;
	private int surveyRight, surveyBottom;
	
	private final int gridScale = 2;
	private float hoverSquareScale;
	
	boolean requestSent = false;
	private BitSet bitSet;
	
	@Nonnull
	public final SurveyScan scan;
	public SeismicSurveyScreen(Level level, @Nonnull SurveyScan scan){
		super(Component.literal("seismicsurveyscreen"));
		this.scan = Objects.requireNonNull(scan);
	}
	
	@Override
	protected void init(){
		this.width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		this.height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
		
		this.hoverSquareScale = 0.5F - (float) (this.minecraft.getWindow().getGuiScale() / 10D);
		
		this.guiLeft = (this.width - X_SIZE) / this.gridScale;
		this.guiTop = (this.height - Y_SIZE) / this.gridScale;
		
		this.surveyLeft = this.guiLeft + 12;
		this.surveyTop = this.guiTop + 12;
		this.surveyRight = this.surveyLeft + (SurveyScan.SCAN_SIZE * this.gridScale);
		this.surveyBottom = this.surveyTop + (SurveyScan.SCAN_SIZE * this.gridScale);
		
		this.requestSent = true;
		MessageSurveyResultDetails.sendRequestToServer(this.scan);
	}
	
	public void setBitSet(BitSet bitSet){
		this.bitSet = bitSet;
	}
	
	private int getScanData(int x, int y){
		if(x < 0 || x >= SurveyScan.SCAN_SIZE || y < 0 || y >= SurveyScan.SCAN_SIZE)
			return -1;
		
		int index = y * SurveyScan.SCAN_SIZE + x;
		return ((int) this.scan.getData()[index]) & 0xFF;
	}
	
	private boolean hasReservoirAt(int x, int y){
		if(this.bitSet == null)
			return false;
		
		if(x < 0 || x >= SurveyScan.SCAN_SIZE || y < 0 || y >= SurveyScan.SCAN_SIZE)
			return false;
		
		// Clamping just to be sure
		int index = Mth.clamp(y * SurveyScan.SCAN_SIZE + x, 0, SurveyScan.SCAN_SIZE * SurveyScan.SCAN_SIZE);
		return this.bitSet.get(index);
	}
	
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick){
		background(guiGraphics, mouseX, mouseY, partialTick);
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		
		DynamicTextureWrapper wrapper = DynamicTextureWrapper.getOrCreate(SurveyScan.SCAN_SIZE, SurveyScan.SCAN_SIZE, this.scan);
		if(wrapper == null)
			return; // I hope this never happens..
		
		final List<Component> tooltip = new ArrayList<>();
		
		int scanX = (SurveyScan.SCAN_SIZE - 1) - ((mouseX - this.surveyLeft) / this.gridScale);
		int scanY = (SurveyScan.SCAN_SIZE - 1) - ((mouseY - this.surveyTop) / this.gridScale);
		
		int scanXCentered = scanX - SurveyScan.SCAN_SIZE / 2;
		int scanYCentered = scanY - SurveyScan.SCAN_SIZE / 2;
		
		if(mouseX >= (this.guiLeft + 70) && mouseX <= (this.guiLeft + 83) && mouseY >= (this.guiTop + 4) && mouseY <= (this.guiTop + 10)){
			tooltip.add(Component.translatable("gui.immersivepetroleum.dirs.north").withStyle(ChatFormatting.AQUA));
		}
		
		guiGraphics.pose().pushPose();
		{
			guiGraphics.pose().translate(this.surveyLeft, this.surveyTop, 0);
			
			renderScanTexture(guiGraphics, wrapper);
			
			if(mouseX >= this.surveyLeft && mouseX < this.surveyRight && mouseY >= this.surveyTop && mouseY < this.surveyBottom){
				int data;
				if((data = getScanData(scanX, scanY)) != -1){
					renderCursorBox(guiGraphics, mouseX, mouseY, 0xFF000000 | (data < 0x7F ? 0xFFFFFF : 0));
				}
				
				int worldX = this.scan.getX() - scanXCentered;
				int worldZ = this.scan.getZ() - scanYCentered;
				tooltip.add(Component.translatable("gui.immersivepetroleum.seismicsurvey.worldcoords", worldX, worldZ));
				
				if(scanXCentered == 0 && scanYCentered == 0){
					tooltip.add(Component.translatable("gui.immersivepetroleum.seismicsurvey.takenhere").withStyle(ChatFormatting.GRAY));
				}
				
				if(this.bitSet != null){
					if(hasReservoirAt(scanX, scanY)){
						tooltip.add(Component.translatable("gui.immersivepetroleum.seismicsurvey.possibility").withStyle(ChatFormatting.DARK_GRAY));
					}
				}else{
					if(this.requestSent){
						tooltip.add(Component.translatable("gui.immersivepetroleum.seismicsurvey.awaitingresponse").withStyle(ChatFormatting.GRAY));
					}
				}
			}
		}
		guiGraphics.pose().popPose();
		
		if(!tooltip.isEmpty())
			guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
	}
	
	private void renderCursorBox(GuiGraphics guiGraphics, int mouseX, int mouseY, int color){
		MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		
		guiGraphics.pose().pushPose();
		{
			guiGraphics.pose().scale(this.gridScale, this.gridScale, 1.0F);
			guiGraphics.pose().translate( (mouseX - this.surveyLeft) / (this.gridScale * 1f),  (mouseY - this.surveyTop) / (this.gridScale * 1f), 0);
			
			VertexConsumer builder = buffer.getBuffer(IPRenderTypes.TRANSLUCENT_POSITION_COLOR);
			Matrix4f mat = guiGraphics.pose().last().pose();
			
			float s = this.hoverSquareScale;
			builder.vertex(mat, 0, 0, 0).color(color).endVertex();
			builder.vertex(mat, 0, s, 0).color(color).endVertex();
			builder.vertex(mat, 1, s, 0).color(color).endVertex();
			builder.vertex(mat, 1, 0, 0).color(color).endVertex();
			
			builder.vertex(mat, 0, 1 - s, 0).color(color).endVertex();
			builder.vertex(mat, 0, 1, 0).color(color).endVertex();
			builder.vertex(mat, 1, 1, 0).color(color).endVertex();
			builder.vertex(mat, 1, 1 - s, 0).color(color).endVertex();
			
			builder.vertex(mat, 0, 0, 0).color(color).endVertex();
			builder.vertex(mat, 0, 1, 0).color(color).endVertex();
			builder.vertex(mat, s, 1, 0).color(color).endVertex();
			builder.vertex(mat, s, 0, 0).color(color).endVertex();
			
			builder.vertex(mat, 1 - s, 0, 0).color(color).endVertex();
			builder.vertex(mat, 1 - s, 1, 0).color(color).endVertex();
			builder.vertex(mat, 1, 1, 0).color(color).endVertex();
			builder.vertex(mat, 1, 0, 0).color(color).endVertex();
		}
		guiGraphics.pose().popPose();
		
		buffer.endBatch();
	}
	
	private void renderScanTexture(GuiGraphics guiGraphics, DynamicTextureWrapper wrapper){
		MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		guiGraphics.pose().pushPose();
		{
			guiGraphics.pose().scale(this.gridScale, this.gridScale, 1.0F);
			VertexConsumer builder = buffer.getBuffer(wrapper.renderType);
			Matrix4f mat = guiGraphics.pose().last().pose();
			
			int a = wrapper.width;
			int b = wrapper.height;
			
			builder.vertex(mat, 0, 0, 0).color(-1).uv(1.0F, 1.0F).uv2(0xF000F0).endVertex();
			builder.vertex(mat, 0, b, 0).color(-1).uv(1.0F, 0.0F).uv2(0xF000F0).endVertex();
			builder.vertex(mat, a, b, 0).color(-1).uv(0.0F, 0.0F).uv2(0xF000F0).endVertex();
			builder.vertex(mat, a, 0, 0).color(-1).uv(0.0F, 1.0F).uv2(0xF000F0).endVertex();
			
			builder = buffer.getBuffer(RenderType.text(OVERLAY_TEXTURE));
			builder.vertex(mat, 0, 0, 0).color(-1).uv(0, 0).uv2(0xF000F0).endVertex();
			builder.vertex(mat, 0, b, 0).color(-1).uv(0, 1).uv2(0xF000F0).endVertex();
			builder.vertex(mat, a, b, 0).color(-1).uv(1, 1).uv2(0xF000F0).endVertex();
			builder.vertex(mat, a, 0, 0).color(-1).uv(1, 0).uv2(0xF000F0).endVertex();
		}
		guiGraphics.pose().popPose();
		
		buffer.endBatch();
	}
	
	private void background(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){
		//MCUtil.bindTexture(GUI_TEXTURE);
		guiGraphics.blit(GUI_TEXTURE, this.guiLeft, this.guiTop, 0, 0, X_SIZE, Y_SIZE);
	}
	
	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers){
		InputConstants.Key key = InputConstants.getKey(pKeyCode, pScanCode);
		if(this.minecraft.options.keyInventory.isActiveAndMatches(key)){
			this.onClose();
			return true;
		}
		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}
	
	@Override
	public boolean isPauseScreen(){
		return false;
	}
}
