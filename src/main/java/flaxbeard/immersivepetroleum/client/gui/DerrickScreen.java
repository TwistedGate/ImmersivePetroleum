package flaxbeard.immersivepetroleum.client.gui;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.ExternalModContent;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.DerrickLogic;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.WellTileEntity;
import flaxbeard.immersivepetroleum.common.gui.DerrickContainer;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import flaxbeard.immersivepetroleum.common.util.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class DerrickScreen extends AbstractContainerScreen<DerrickContainer>{
	static final ResourceLocation GUI_TEXTURE = ResourceUtils.ip("textures/gui/derrick.png");
	static final int colour_nixieTubeText = 0xff9900;
	Button cfgButton;
	private List<InfoArea> areas;
	
	public DerrickScreen(DerrickContainer inventorySlotsIn, Inventory inv, Component title){
		super(inventorySlotsIn, inv, title);
		
		// TODO GUI may either get bigger or smaller as i figure this out
		this.imageWidth = 200;
		this.imageHeight = 164;
	}
	
	@Override
	protected void init(){
		this.leftPos = (this.width - this.imageWidth) / 2;
		this.topPos = (this.height - this.imageHeight) / 2;

		this.cfgButton = new Button.Builder(Component.translatable("gui.immersivepetroleum.derrick.msg.config"), button -> this.minecraft.setScreen(new DerrickSettingsScreen(this))).
				bounds(this.leftPos + 125, this.topPos + 52, 50, 20).
				tooltip(Tooltip.create(Component.translatable("gui.immersivepetroleum.derrick.msg.set_in_stone"))).build();

		addRenderableWidget(this.cfgButton);
		this.areas = List.of(
			new FluidInfoArea(getMenu().tank, new Rect2i(leftPos + 11, topPos + 16, 16, 47), 200, 0, 20, 51, GUI_TEXTURE),
			new EnergyInfoArea(leftPos + 185, topPos + 19, getMenu().energy)
		);
	}
	
	@Override
	public void render(@Nonnull GuiGraphics guiGraphics, int mx, int my, float partialTicks){
		this.inventoryLabelY = this.imageHeight - 40;
		this.renderBackground(guiGraphics);
		super.render(guiGraphics, mx, my, partialTicks);
		this.renderTooltip(guiGraphics, mx, my);
		
		List<Component> tooltip = new ArrayList<>();
		
		for(InfoArea area:areas){
			area.fillTooltip(mx, my, tooltip);
		}
		
		if(!tooltip.isEmpty()){
			guiGraphics.renderTooltip(minecraft.font, tooltip, Optional.empty(), mx, my);
		}
	}
	
	@Override
	protected void renderLabels(@Nonnull GuiGraphics guiGraphics, int x, int y){
		if(DerrickContainer.getPos(this.getMenu().pos.get()).getY() <= 62){
			drawInfoTextCenteredMultiLine(guiGraphics, I18n.get("gui.immersivepetroleum.derrick.msg.water_table"), 0xEF0000);
			return;
		}

		BlockEntity tile = this.getMenu().level.getBlockEntity(DerrickContainer.getPos(this.getMenu().pos.get()));

		if(tile instanceof IMultiblockBE<?> multiblockBE && multiblockBE.getHelper().getContext().getState() instanceof DerrickLogic.State state)
		{
			IMultiblockLevel level = multiblockBE.getHelper().getContext().getLevel();
			IMultiblockContext<DerrickLogic.State> ctx = multiblockBE.getHelper().asType(IPContent.Multiblock.DERRICK).getContext();

			WellTileEntity well = state.getWell(level, level.toAbsolute(IPContent.Multiblock.DERRICK.masterPosInMB()));
			if(well != null){
				if(this.cfgButton.active && well.wellPipeLength > 0){
					this.cfgButton.active = false;
				}

				// Possible display prototypes
				if(well.wellPipeLength < well.getMaxPipeLength()){
					if(!state.rsState.isEnabled(ctx)){
						drawInfoTextCentered(guiGraphics, Component.translatable("gui.immersivepetroleum.derrick.msg.disabled"), 0, 0xEF0000);
						return;
					}

					if(state.drilling){
						String str = String.format(Locale.ROOT, "(%d%%)", (int) (100 * well.wellPipeLength / (float) well.getMaxPipeLength()));
						drawInfoText(guiGraphics, Component.translatable("gui.immersivepetroleum.derrick.msg.drilling", str), 0);
						return;
					}else if(well.pipes <= 0 && !this.menu.getSlot(0).hasItem()){
						drawInfoTextCentered(guiGraphics, Component.translatable("gui.immersivepetroleum.derrick.msg.out_of_pipes"), 3, 0xEF0000);
						return;
					}

					if(getMenu().tank.isEmpty()){
						int realPipeLength = (level.getAbsoluteOrigin().getY() - 1) - well.getBlockPos().getY();
						int concreteNeeded = (DerrickLogic.REQUIRED_CONCRETE_AMOUNT * (realPipeLength - well.wellPipeLength));
						if(concreteNeeded > 0){
							drawInfoText(guiGraphics, Component.translatable("gui.immersivepetroleum.derrick.msg.missing", Utils.fDecimal(concreteNeeded) + "mB"), 0, 0xEF0000);
							drawInfoText(guiGraphics, ExternalModContent.getIEFluid_Concrete(1).getDisplayName(), 1, 0xEF0000);
							return;
						}

						int waterNeeded = DerrickLogic.REQUIRED_WATER_AMOUNT * (well.getMaxPipeLength() - well.wellPipeLength);
						if(waterNeeded > 0){
							drawInfoText(guiGraphics, Component.translatable("gui.immersivepetroleum.derrick.msg.missing", Utils.fDecimal(waterNeeded) + "mB"), 0, 0xEF0000);
							drawInfoText(guiGraphics, new FluidStack(Fluids.WATER, 1).getDisplayName(), 1, 0xEF0000);
							return;
						}
					}
				}else{
					if(state.spilling){
						drawInfoTextCenteredMultiLine(guiGraphics, I18n.get("gui.immersivepetroleum.derrick.msg.safety_valve"), 0xEF0000);
					}else{
						drawInfoTextCenteredMultiLine(guiGraphics, I18n.get("gui.immersivepetroleum.derrick.msg.completed"), colour_nixieTubeText);
					}
				}
			}
		}
	}
	
	private void drawInfoText(GuiGraphics guiGraphics, Component text, int line){
		drawInfoText(guiGraphics, text, line, colour_nixieTubeText);
	}
	
	private void drawInfoText(GuiGraphics guiGraphics, Component text, int line, int color){
		guiGraphics.drawString(this.font, text, 60, 10 + (9 * line), color);
	}
	
	@SuppressWarnings("unused")
	private void drawInfoTextCentered(GuiGraphics guiGraphics, Component text, int line){
		drawInfoTextCentered(guiGraphics, text, line, colour_nixieTubeText);
	}
	
	private void drawInfoTextCentered(GuiGraphics guiGraphics, Component text, int line, int color){
		int strWidth = this.font.width(text.getString());
		guiGraphics.drawString(this.font, text, (int)(118.5F - (strWidth / 2F)), 10 + (9 * line), color);
	}
	
	private void drawInfoTextCenteredMultiLine(GuiGraphics guiGraphics, String text, int color){
		String[] lines = text.split("<br>");
		for(int i = 0;i < Math.min(lines.length, 4);i++){
			drawInfoTextCentered(guiGraphics, Component.literal(lines[i].length() > 25 ? lines[i].substring(0, 25) : lines[i]), i, color);
		}
	}
	
	@Override
	protected void renderBg(@Nonnull GuiGraphics guiGraphics, float partialTicks, int mx, int my){
		guiGraphics.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		for(InfoArea area:areas){
			area.draw(guiGraphics);
		}
	}
}
