/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package flaxbeard.immersivepetroleum.client.gui.elements;

import blusunrize.immersiveengineering.api.Lib;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Function;

@SuppressWarnings("unused")
public class GuiReactiveList extends Button{
	private final Screen gui;
	private String[] entries;
	private int[] padding = {0, 0, 0, 0};
	private boolean needsSlider = false;
	private int perPage;
	private Function<String, String> translationFunction;
	private int scrollMode = 0;
	private float textScale = 1;
	private int textColor = 0xE0E0E0;
	private int textHoverColor = Lib.COLOUR_I_ImmersiveOrange;
	
	private int offset;
	private int maxOffset;
	
	private long prevWheelNano = 0;
	private int targetEntry = -1;
	private float hoverTimer = 0;
	
	public GuiReactiveList(Screen gui, int x, int y, int w, int h, OnPress handler, String... entries){
		super(x, y, w, h, Component.empty(), handler, DEFAULT_NARRATION);
		this.gui = gui;
		this.entries = entries;
		recalculateEntries();
	}
	
	private void recalculateEntries(){
		perPage = (int) ((this.height - padding[0] - padding[1]) / (MCUtil.getFont().lineHeight * textScale));
		if(perPage < entries.length){
			needsSlider = true;
			maxOffset = entries.length - perPage;
		}else
			needsSlider = false;
	}
	
	/**
	 * Changes the default text color for entries
	 * 
	 * @param color RGB value
	 */
	public GuiReactiveList setTextColor(int color){
		this.textColor = color;
		return this;
	}
	
	/**
	 * Changes the text color for entries when being hovered over
	 * 
	 * @param color RGB value
	 * @return {@link GuiReactiveList} self
	 */
	public GuiReactiveList setTextHoverColor(int color){
		this.textHoverColor = color;
		return this;
	}
	
	public GuiReactiveList setPadding(int up, int down, int left, int right){
		this.padding[0] = up;
		this.padding[1] = down;
		this.padding[2] = left;
		this.padding[3] = right;
		recalculateEntries();
		return this;
	}
	
	public GuiReactiveList setTranslationFunc(Function<String, String> func){
		this.translationFunction = func;
		return this;
	}
	
	/**
	 * @param mode 0: No scrolling<br>
	 *             1: Scroll when hovered<br>
	 *             2: Scroll all
	 */
	public GuiReactiveList setScrollMode(int mode){
		this.scrollMode = mode;
		return this;
	}
	
	public GuiReactiveList setFormatting(float textScale){
		this.textScale = textScale;
		this.recalculateEntries();
		return this;
	}
	
	public int getOffset(){
		return this.offset;
	}
	
	public void setOffset(int offset){
		this.offset = offset;
	}
	
	public int getMaxOffset(){
		return this.maxOffset;
	}
	
	static final ResourceLocation TEXTURE = ResourceUtils.ie("textures/gui/hud_elements.png");
	
	@Override
	public void render(@Nonnull GuiGraphics gui, int mx, int my, float partialTicks){
		Font fr = MCUtil.getFont();
		
		int mmY = my - this.getY();
		int strWidth = width - padding[2] - padding[3] - (needsSlider ? 6 : 0);
		if(needsSlider){
			//ClientUtils.bindTexture(TEXTURE);
			gui.blit(TEXTURE, getX() + width - 6, getY(), 16, 136, 6, 4);
			gui.blit(TEXTURE, getX() + width - 6, getY() + height - 4, 16, 144, 6, 4);
			for(int i = 0;i < height - 8;i += 2)
				gui.blit(TEXTURE, getX() + width - 6, getY() + 4 + i, 16, 141, 6, 2);
			
			int sliderSize = Math.max(6, height - maxOffset * fr.lineHeight);
			float silderShift = (height - sliderSize) / (float) maxOffset * offset;
			
			gui.blit(TEXTURE, getX() + width - 5, (int) (getY() + silderShift + 1), 20, 129, 4, 2);
			gui.blit(TEXTURE, getX() + width - 5, (int) (getY() + silderShift + sliderSize - 4), 20, 132, 4, 3);
			for(int i = 0;i < sliderSize - 7;i++)
				gui.blit(TEXTURE, getX() + width - 5, (int) (getY() + silderShift + 3 + i), 20, 131, 4, 1);
		}
		
		gui.pose().scale(textScale, textScale, 1);
		this.isHovered = mx >= getX() && mx < getX() + width && my >= getY() && my < getY() + height;
		boolean hasTarget = false;
		for(int i = 0;i < Math.min(perPage, entries.length);i++){
			int j = offset + i;
			int col = textColor;
			boolean selectionHover = isHovered && mmY >= i * fr.lineHeight && mmY < (i + 1) * fr.lineHeight;
			if(selectionHover){
				hasTarget = true;
				if(targetEntry != j){
					targetEntry = j;
					hoverTimer = 0;
				}else
					hoverTimer += 2.5 * partialTicks;
				col = textHoverColor;
			}
			if(j > entries.length - 1)
				j = entries.length - 1;
			String s = translationFunction != null ? translationFunction.apply(entries[j]) : entries[j];
			int overLength = s.length() - fr.plainSubstrByWidth(s, strWidth).length();
			if(overLength > 0)// String is too long
			{
				if(selectionHover && hoverTimer > 20){
					int textOffset = ((int) hoverTimer / 10) % (s.length());
					s = s.substring(textOffset) + " " + s.substring(0, textOffset);
				}
				s = fr.plainSubstrByWidth(s, strWidth);
			}
			float tx = ((getX() + padding[2]) / textScale);
			float ty = ((getY() + padding[0] + (fr.lineHeight * i)) / textScale);
			gui.pose().translate(tx, ty, 0);
			gui.drawString(fr, s, 0, 0, col, false);
			gui.pose().translate(-tx, -ty, 0);
		}
		gui.pose().scale(1 / textScale, 1 / textScale, 1);
		if(!hasTarget){
			targetEntry = -1;
			hoverTimer = 0;
		}
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta){
		if(delta != 0 && maxOffset > 0){
			if(delta < 0 && offset < maxOffset)
				offset++;
			if(delta > 0 && offset > 0)
				offset--;
			return true;
		}else
			return false;
	}
	
	public int selectedOption = -1;
	
	@Override
	public boolean mouseClicked(double mx, double my, int key){
		selectedOption = -1;
		if(this.active && this.visible)
			if(this.isValidClickButton(key) && this.clicked(mx, my)){
				
				Font fr = MCUtil.getFont();
				double mmY = my - this.getY();
				for(int i = 0;i < Math.min(perPage, entries.length);i++)
					if(mmY >= i * fr.lineHeight && mmY < (i + 1) * fr.lineHeight)
						selectedOption = offset + i;
			}
		super.mouseClicked(mx, my, key);
		return selectedOption != -1;
	}
}
