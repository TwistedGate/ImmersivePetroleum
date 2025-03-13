package flaxbeard.immersivepetroleum.client.gui;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks.MultiblockManualData;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.utils.TemplateWorldCreator;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Axis;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.client.gui.elements.GuiReactiveList;
import flaxbeard.immersivepetroleum.client.render.IPRenderTypes;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import flaxbeard.immersivepetroleum.common.util.projector.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ProjectorScreen extends Screen{
	static final ResourceLocation GUI_TEXTURE = ResourceUtils.ip("textures/gui/projector.png");
	
	static final Component GUI_CONFIRM = translation("gui.immersivepetroleum.projector.button.confirm");
	static final Component GUI_CANCEL = translation("gui.immersivepetroleum.projector.button.cancel");
	static final Component GUI_MIRROR = translation("gui.immersivepetroleum.projector.button.mirror");
	static final Component GUI_ROTATE_CW = translation("gui.immersivepetroleum.projector.button.rcw");
	static final Component GUI_ROTATE_CCW = translation("gui.immersivepetroleum.projector.button.rccw");
	static final Component GUI_UP = translation("gui.immersivepetroleum.projector.button.up");
	static final Component GUI_DOWN = translation("gui.immersivepetroleum.projector.button.down");
	static final Component GUI_SEARCH = translation("gui.immersivepetroleum.projector.search");
	
	private final int xSize = 256;
	private final int ySize = 166;
	private int guiLeft;
	private int guiTop;
	
	private Lazy<List<IMultiblock>> multiblocks;
	private Level templateWorld;
	private IMultiblock multiblock;
	private GuiReactiveList list;
	private String[] listEntries;
	
	private SearchField searchField;
	
	Settings settings;
	InteractionHand hand;
	
	float rotation = 0.0F, move = 0.0F;
	public ProjectorScreen(InteractionHand hand, ItemStack projector){
		super(Component.literal("projector"));
		this.settings = new Settings(projector);
		this.hand = hand;
		this.multiblocks = Lazy.of(MultiblockHandler::getMultiblocks);
		
		if(this.settings.getMultiblock() != null){
			this.move = 20F;
		}
	}
	
	@Override
	protected void init(){
		this.width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		this.height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
		
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
		
		this.searchField = addRenderableWidget(new SearchField(this.font, this.guiLeft + 25, this.guiTop + 13));
		
		addRenderableWidget(new ConfirmButton(this.guiLeft + 115, this.guiTop + 10, but -> {
			this.settings.setMode(Settings.Mode.PROJECTION);
			
			ItemStack held = MCUtil.getPlayer().getItemInHand(this.hand);
			this.settings.applyTo(held);
			this.settings.sendPacketToServer(this.hand);
			MCUtil.getScreen().onClose();
			
			MCUtil.getPlayer().displayClientMessage(this.settings.getMode().getTranslated(), true);
		}));
		addRenderableWidget(new CancelButton(this.guiLeft + 115, this.guiTop + 34, but -> {
			MCUtil.getScreen().onClose();
		}));
		addRenderableWidget(new MirrorButton(this.guiLeft + 115, this.guiTop + 58, this.settings, but -> {
			this.settings.flip();
		}));
		addRenderableWidget(new RotateLeftButton(this.guiLeft + 115, this.guiTop + 106, but -> {
			this.settings.rotateCCW();
		}));
		addRenderableWidget(new RotateRightButton(this.guiLeft + 115, this.guiTop + 130, but -> {
			this.settings.rotateCW();
		}));
		
		updatelist();
	}
	
	private void listaction(Button button){
		GuiReactiveList l = (GuiReactiveList) button;
		if(l.selectedOption >= 0 && l.selectedOption < listEntries.length){
			String str = this.listEntries[l.selectedOption];
			IMultiblock mb = this.multiblocks.get().get(Integer.parseInt(str));
			this.settings.setMultiblock(mb);
		}
	}
	
	private void updatelist(){
		boolean exists = this.renderables.contains(this.list);
		
		List<String> list = new ArrayList<>();
		for(int i = 0;i < this.multiblocks.get().size();i++){
			IMultiblock mb = this.multiblocks.get().get(i);
			if(!mb.getUniqueName().toString().equals("immersiveengineering:feedthrough")){
				list.add(Integer.toString(i));
			}
		}
		
		// Sorting in alphabetical order
		list.sort((a, b) -> {
			String nameA = getMBName(a);
			String nameB = getMBName(b);
			
			return nameA.compareToIgnoreCase(nameB);
		});
		
		// Lazy search based on content
		list.removeIf(str -> {
			String name = getMBName(str);
			return !name.toLowerCase().contains(this.searchField.getValue().toLowerCase());
		});
		
		this.listEntries = list.toArray(new String[0]);
		GuiReactiveList guilist = new GuiReactiveList(this, this.guiLeft + 15, this.guiTop + 29, 89, 127, this::listaction, this.listEntries);
		guilist.setPadding(1, 1, 1, 1);
		guilist.setTextColor(0);
		guilist.setTextHoverColor(0x7F7FFF);
		guilist.setTranslationFunc(this::getMBName);
		
		if(!exists){
			this.list = addRenderableWidget(guilist);
			return;
		}
		
		removeWidget(this.list);
		this.list = guilist;
		addRenderableWidget(this.list);
	}
	
	private String getMBName(String str){
		return getMBName(Integer.parseInt(str));
	}
	
	private String getMBName(int index){
		IMultiblock mb = this.multiblocks.get().get(index);
		return mb.getDisplayName().getString();
	}
	
	@Override
	public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){
		// Over-GUI Text
		if(this.settings.getMultiblock() != null){
			IMultiblock mb = this.settings.getMultiblock();
			int x = this.guiLeft + 28;
			int y = this.guiTop - (int) (15 * (this.move / 20F));
			
			if(this.move < 20){
				this.move += 1.5 * partialTicks;
			}
			
			//ClientUtils.bindTexture(GUI_TEXTURE);
			guiGraphics.blit(GUI_TEXTURE, x, y, 0, 166, 200, 13);
			
			x += 100;
			y += 3;
			
			Component text = mb.getDisplayName();
			FormattedCharSequence re = text.getVisualOrderText();
			guiGraphics.drawString(this.font, re, (x - this.font.width(re) / 2), y, 0x3F3F3F, false);
		}
		background(guiGraphics, mouseX, mouseY, partialTicks);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.searchField.render(guiGraphics, mouseX, mouseY, partialTicks);
		
		/*for(Renderable rawWidget:this.renderables){
			if(rawWidget instanceof AbstractWidget widget && widget.isHoveredOrFocused()){
				guiGraphics.renderTooltip(this.font, widget.getTooltip()., mouseX, mouseY);
				break;
			}
		}*/
		
		renderDirectionDisplay(guiGraphics, mouseX, mouseY);
		
		if(this.settings.getMultiblock() != null){
			IMultiblock mb = this.settings.getMultiblock();

			MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
			try{
				
				this.rotation += 1.5F * partialTicks;
				
				Vec3i size = mb.getSize(null);

				guiGraphics.pose().pushPose();
				{
					guiGraphics.pose().translate(this.guiLeft + 190, this.guiTop + 80, 64);
					guiGraphics.pose().scale(mb.getManualScale(), -mb.getManualScale(), 1);
					guiGraphics.pose().mulPose(Axis.XP.rotationDegrees(25));
					guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(45 - this.rotation));
					guiGraphics.pose().translate(size.getX() / -2F, size.getY() / -2F, size.getZ() / -2F);
					
					MultiblockManualData mbClientData = ClientMultiblocks.get(mb);
					boolean tempDisable = true;
					if(tempDisable && mbClientData.canRenderFormedStructure()){
						guiGraphics.pose().pushPose();
						{
							mbClientData.renderFormedStructure(guiGraphics.pose(), IPRenderTypes.disableLighting(buffer));
						}
						guiGraphics.pose().popPose();
					}else{
						if(this.templateWorld == null || (!this.multiblock.getUniqueName().equals(mb.getUniqueName()))){
							this.templateWorld = TemplateWorldCreator.CREATOR.getValue().makeWorld(mb.getStructure(this.getMinecraft().level), pos -> true, this.getMinecraft().level.registryAccess());
							this.multiblock = mb;
						}
						
						final BlockRenderDispatcher blockRender = Minecraft.getInstance().getBlockRenderer();
						List<StructureTemplate.StructureBlockInfo> infos = mb.getStructure(this.getMinecraft().level);
						for(StructureTemplate.StructureBlockInfo info:infos){
							if(!info.state().is(Blocks.AIR)){
								guiGraphics.pose().pushPose();
								{
									guiGraphics.pose().translate(info.pos().getX(), info.pos().getY(), info.pos().getZ());
									ModelData modelData = ModelData.EMPTY;
									BlockEntity te = this.templateWorld.getBlockEntity(info.pos());
									if(te != null){
										modelData = te.getModelData();
									}
									blockRender.renderSingleBlock(info.state(), guiGraphics.pose(), IPRenderTypes.disableLighting(buffer), 0xF000F0, OverlayTexture.NO_OVERLAY, modelData, null);
								}
								guiGraphics.pose().popPose();
							}
						}
					}
				}
				guiGraphics.pose().popPose();
			}catch(Exception e){
				e.printStackTrace();
			}
			buffer.endBatch();
		}
	}
	
	private void renderDirectionDisplay(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY){
		int x = this.guiLeft + 115;
		int y = this.guiTop + 82;
		
		// Idealy it'd be: N-S-E-W
		Direction dir = Direction.from2DDataValue(this.settings.getRotation().ordinal());
		Component dirText = Component.literal(dir.toString().toUpperCase().substring(0, 1));
		guiGraphics.drawCenteredString(this.font, dirText, x + 5, y + 1, -1);
		
		if(mouseX > x && mouseX < x + 10 && mouseY > y && mouseY < y + 10){
			Component rotText = Component.translatable("desc.immersivepetroleum.info.projector.rotated." + dir);
			guiGraphics.renderTooltip(this.font, rotText, mouseX, mouseY);
		}
	}
	
	private void background(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){
		//ClientUtils.bindTexture(GUI_TEXTURE);
		guiGraphics.blit(GUI_TEXTURE, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers){
		return super.keyPressed(keyCode, scanCode, modifiers) || this.searchField.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean charTyped(char codePoint, int modifiers){
		return super.charTyped(codePoint, modifiers) || this.searchField.charTyped(codePoint, modifiers);
	}
	
	@Override
	public boolean isPauseScreen(){
		return false;
	}
	
	// CLASSES
	
	class ConfirmButton extends ProjectorScreen.ControlButton{
		public ConfirmButton(int x, int y, Consumer<PButton> action){
			super(x, y, 10, 10, 0, 179, action, GUI_CONFIRM);
		}
	}
	
	class CancelButton extends ProjectorScreen.ControlButton{
		public CancelButton(int x, int y, Consumer<PButton> action){
			super(x, y, 10, 10, 10, 179, action, GUI_CANCEL);
		}
	}
	
	class MirrorButton extends ProjectorScreen.ControlButton{
		Settings settings;
		public MirrorButton(int x, int y, Settings settings, Consumer<PButton> action){
			super(x, y, 10, 10, 20, 179, action, GUI_MIRROR);
			this.settings = settings;
		}
		
		@Override
		public void renderWidget(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){
			//ClientUtils.bindTexture(GUI_TEXTURE);
			if(isHovered){
				guiGraphics.fill(this.getX(), this.getY() + 1, this.getX() + this.iconSize, this.getY() + this.iconSize - 1, 0xAF7F7FFF);
			}
			
			if(this.settings.isMirrored()){
				guiGraphics.blit(GUI_TEXTURE, this.getX(), this.getY(), this.xOverlay, this.yOverlay + this.iconSize, this.iconSize, this.iconSize);
			}else{
				guiGraphics.blit(GUI_TEXTURE, this.getX(), this.getY(), this.xOverlay, this.yOverlay, this.iconSize, this.iconSize);
			}
		}
	}
	
	class RotateLeftButton extends ProjectorScreen.ControlButton{
		public RotateLeftButton(int x, int y, Consumer<PButton> action){
			super(x, y, 10, 10, 30, 179, action, GUI_ROTATE_CCW);
		}
	}
	
	class RotateRightButton extends ProjectorScreen.ControlButton{
		public RotateRightButton(int x, int y, Consumer<PButton> action){
			super(x, y, 10, 10, 40, 179, action, GUI_ROTATE_CW);
		}
	}
	
	class ControlButton extends ProjectorScreen.PButton{
		Component hoverText;
		public ControlButton(int x, int y, int width, int height, int overlayX, int overlayY, Consumer<PButton> action, Component hoverText){
			super(x, y, width, height, overlayX, overlayY, action);
			this.hoverText = hoverText;
		}
		
		/*@Override
		public void renderToolTip(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY){
			if(this.hoverText != null){
				ProjectorScreen.this.renderTooltip(matrixStack, this.hoverText, mouseX, mouseY);
			}
		}*/
	}
	
	class SearchField extends EditBox{
		public SearchField(Font font, int x, int y){
			super(font, x, y, 60, 14, GUI_SEARCH); // Font, x, y, width, height, tooltip
			setMaxLength(50);
			setBordered(false);
			setVisible(true);
			setTextColor(0xFFFFFF);
		}
		
		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers){
			String s = getValue();
			if(super.keyPressed(keyCode, scanCode, modifiers)){
				if(!Objects.equals(s, getValue())){
					ProjectorScreen.this.updatelist();
				}
				
				return true;
			}else{
				return isFocused() && isVisible() && keyCode != 256 || super.keyPressed(keyCode, scanCode, modifiers);
			}
		}
		
		@Override
		public boolean charTyped(char codePoint, int modifiers){
			if(!isFocused()){
				//changeFocus(this);
				setFocused(true);
			}
			
			String s = getValue();
			if(super.charTyped(codePoint, modifiers)){
				if(!Objects.equals(s, getValue())){
					ProjectorScreen.this.updatelist();
				}
				
				return true;
			}else{
				return false;
			}
		}
	}
	
	// STATIC METHODS
	
	static Component translation(String key){
		return Component.translatable(key);
	}
	
	// STATIC CLASSES
	
	static class PButton extends AbstractButton{
		protected boolean selected;
		protected final int xOverlay, yOverlay;
		protected int iconSize = 10;
		protected int bgStartX = 0, bgStartY = 166;
		protected Consumer<PButton> action;
		public PButton(int x, int y, int width, int height, int overlayX, int overlayY, Consumer<PButton> action){
			super(x, y, width, height, Component.empty());
			this.action = action;
			this.xOverlay = overlayX;
			this.yOverlay = overlayY;
		}
		
		@Override
		public void renderWidget(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){
			//ClientUtils.bindTexture(GUI_TEXTURE);
			if(isHovered){
				guiGraphics.fill(this.getX(), this.getY() + 1, this.getX() + this.iconSize, this.getY() + this.iconSize - 1, 0xAF7F7FFF);
			}
			guiGraphics.blit(GUI_TEXTURE, this.getX(), this.getY(), this.xOverlay, this.yOverlay, this.iconSize, this.iconSize);
		}

		@Override
		public void onPress(){
			this.action.accept(this);
		}
		
		public boolean isSelected(){
			return this.selected;
		}
		
		public void setSelected(boolean isSelected){
			this.selected = isSelected;
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
		}
	}
}
