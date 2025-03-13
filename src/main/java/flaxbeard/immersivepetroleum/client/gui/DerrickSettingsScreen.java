package flaxbeard.immersivepetroleum.client.gui;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import flaxbeard.immersivepetroleum.client.gui.elements.PipeConfig;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.DerrickLogic;
import flaxbeard.immersivepetroleum.common.gui.DerrickContainer;
import flaxbeard.immersivepetroleum.common.network.MessageDerrick;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import flaxbeard.immersivepetroleum.common.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DerrickSettingsScreen extends Screen{
	static final ResourceLocation GUI_TEXTURE = ResourceUtils.ip("textures/gui/derrick_settings.png");
	
	private final int xSize = 158;
	private final int ySize = 176;
	private int guiLeft;
	private int guiTop;
	private PipeConfig pipeConfig;
	
	final DerrickScreen derrickScreen;
	public DerrickSettingsScreen(DerrickScreen derrickScreen){
		super(Component.literal("DerrickSettings"));
		this.derrickScreen = derrickScreen;
	}
	
	@Override
	protected void init(){
		this.width = this.minecraft.getWindow().getGuiScaledWidth();
		this.height = this.minecraft.getWindow().getGuiScaledHeight();
		
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;

		BlockEntity tile = this.derrickScreen.getMenu().level.getBlockEntity(DerrickContainer.getPos(this.derrickScreen.getMenu().pos.get()));
		if (tile instanceof IMultiblockBE<?> be && be.getHelper().getState() instanceof DerrickLogic.State)
		{
			this.pipeConfig = new PipeConfig(be.getHelper().asType(IPContent.Multiblock.DERRICK), this.guiLeft + 10, this.guiTop + 10, 138, 138, 69, 69, 2);
			addRenderableWidget(this.pipeConfig);

			// IDEA Users: these lambdas are like this for readability: Don't Change Them!
			final Component set = Component.translatable("gui.immersivepetroleum.derrick.settings.button.set");
			addRenderableWidget(Button.builder(set, b ->
			{
				MessageDerrick.sendToServer(be.getHelper().getContext().getLevel().getAbsoluteOrigin(), this.pipeConfig.getGrid());
			}).		bounds(this.guiLeft + (this.xSize / 2) - 65, this.guiTop + this.ySize - 25, 40, 20).
					tooltip(Tooltip.create(Component.translatable("gui.immersivepetroleum.derrick.settings.button.set.desc"))).
					build());

			final Component reset = Component.translatable("gui.immersivepetroleum.derrick.settings.button.reset");
			addRenderableWidget(Button.builder(reset, b -> {
				this.pipeConfig.reset(be.getHelper().asType(IPContent.Multiblock.DERRICK));
			}).
					bounds(this.guiLeft + (this.xSize / 2) - 20, this.guiTop + this.ySize - 25, 40, 20).
					tooltip(Tooltip.create(Component.translatable("gui.immersivepetroleum.derrick.settings.button.reset.desc"))).
					build());

			final Component close = Component.translatable("gui.immersivepetroleum.derrick.settings.button.close");
			addRenderableWidget(Button.builder(close, b -> {
				DerrickSettingsScreen.this.onClose();
			}).
					bounds(this.guiLeft + (this.xSize / 2) + 25, this.guiTop + this.ySize - 25, 40, 20).
					tooltip(Tooltip.create(Component.translatable("gui.immersivepetroleum.derrick.settings.button.close.desc"))).
					build());
		}
	}
	
	@Override
	public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick){
		background(guiGraphics, mouseX, mouseY, partialTick);
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		
		final List<Component> tooltip = new ArrayList<>();
		
		if((mouseX >= this.pipeConfig.getX() && mouseX < (this.pipeConfig.getX() + this.pipeConfig.getWidth())) && (mouseY >= this.pipeConfig.getY() && mouseY < (this.pipeConfig.getY() + this.pipeConfig.getHeight()))){
			int x = (mouseX - this.pipeConfig.getX()) / this.pipeConfig.getGridScale();
			int y = (mouseY - this.pipeConfig.getY()) / this.pipeConfig.getGridScale();
			
			int px = x - (this.pipeConfig.getGrid().getWidth() / 2);
			int py = y - (this.pipeConfig.getGrid().getHeight() / 2);
			
			if((px >= -2 && px <= 2) && (py >= -2 && py <= 2)){
				tooltip.add(Component.translatable("gui.immersivepetroleum.derrick.settings.derrickishere"));
			}else{
				MutableComponent d = Component.empty();
				if(py < 0){
					d.append(Component.translatable("gui.immersivepetroleum.dirs.north"));
				}else if(py > 0){
					d.append(Component.translatable("gui.immersivepetroleum.dirs.south"));
				}
				if(px != 0){
					if(py != 0){
						d.append(Component.literal("-"));
					}
					
					if(px < 0){
						d.append(Component.translatable("gui.immersivepetroleum.dirs.west"));
					}else if(px > 0){
						d.append(Component.translatable("gui.immersivepetroleum.dirs.east"));
					}
				}
				
				tooltip.add(d.withStyle(ChatFormatting.UNDERLINE));
			}

			BlockEntity tile = this.derrickScreen.getMenu().level.getBlockEntity(DerrickContainer.getPos(this.derrickScreen.getMenu().pos.get()));
			if (tile instanceof IMultiblockBE<?> be && be.getHelper().getState() instanceof DerrickLogic.State)
			{
				ColumnPos tilePos = Utils.toColumnPos(be.getHelper().getPositionInMB());
				tooltip.add(Component.literal(String.format(Locale.ENGLISH, "X: %d §7(%d)", (tilePos.x() + px), px)));
				tooltip.add(Component.literal(String.format(Locale.ENGLISH, "Z: %d §7(%d)", (tilePos.z() + py), py)));
			}

			int i = this.pipeConfig.getGrid().get(x, y);
			if (i > PipeConfig.EMPTY) {
				switch (i) {
					case PipeConfig.PIPE_NORMAL -> tooltip.add(Component.translatable("gui.immersivepetroleum.derrick.settings.pipe.normal"));
					case PipeConfig.PIPE_PERFORATED -> tooltip.add(Component.translatable("gui.immersivepetroleum.derrick.settings.pipe.perforated"));
					case PipeConfig.PIPE_PERFORATED_FIXED -> tooltip.add(Component.translatable("gui.immersivepetroleum.derrick.settings.pipe.perforated_fixed"));
				}
			}

			int xa = this.pipeConfig.getX() + (x * this.pipeConfig.getGridScale());
			int ya = this.pipeConfig.getY() + (y * this.pipeConfig.getGridScale());
			guiGraphics.fill(xa, ya, xa + this.pipeConfig.getGridScale(), ya + this.pipeConfig.getGridScale(), 0x7FFFFFFF);
		}
		
		if(!tooltip.isEmpty())
			guiGraphics.renderComponentTooltip(this.minecraft.font, tooltip, mouseX, mouseY);
	}
	
	@Override
	public boolean isPauseScreen(){
		return false;
	}
	
	@Override
	public void onClose(){
		this.minecraft.setScreen(this.derrickScreen);
		this.pipeConfig.dispose();
	}
	
	@Override
	public void resize(@Nonnull Minecraft minecraft, int width, int height){
		PipeConfig oldGrid = this.pipeConfig;
		super.resize(minecraft, width, height);
		this.pipeConfig.copyDataFrom(oldGrid);
		oldGrid.dispose();
	}
	
	private void background(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){
		guiGraphics.blit(GUI_TEXTURE, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}
}
