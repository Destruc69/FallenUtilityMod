/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.forge.clickgui.ClickGui;
import net.wurstclient.forge.clickgui.ClickGuiScreen;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.utils.TextUtil;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Comparator;

public final class IngameHUD {
	private final Minecraft mc = Minecraft.getMinecraft();
	private final HackList hackList;
	private final ClickGui clickGui;

	float textColor;

	String color;

	public IngameHUD(HackList hackList, ClickGui clickGui) {
		this.hackList = hackList;
		this.clickGui = clickGui;
	}

	@SubscribeEvent
	public void onRenderGUI(RenderGameOverlayEvent.Post event) {
		if (event.getType() != ElementType.ALL || mc.gameSettings.showDebugInfo)
			return;

		boolean blend = GL11.glGetBoolean(GL11.GL_BLEND);

		// color
		clickGui.updateColors();

		ClickGui gui = ForgeWurst.getForgeWurst().getGui();
		if (gui.getAcColor()[2] > gui.getAcColor()[1]) {
			textColor = 0x000AFF;

			clickGui.updateColors();
		} else if (gui.getAcColor()[1] > gui.getAcColor()[2]) {
			textColor = 0x11FF00;

			clickGui.updateColors();
		} else if (gui.getAcColor()[0] > gui.getAcColor()[1] && gui.getAcColor()[0] > gui.getAcColor()[2]) {
			textColor = 0xFF0000;

			clickGui.updateColors();
		}


		if (!ForgeWurst.getForgeWurst().getHax().clickGuiHack.nogui().isChecked()) {
			ArrayList<Hack> hacks = new ArrayList<>();
			// title
			GL11.glPushMatrix();
			GL11.glScaled(1.55555555, 1.55555555, 0.88888888);
			WMinecraft.getFontRenderer().drawStringWithShadow("  allen", 3, 3, (int) textColor);
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glScaled(2, 2, 1);
			WMinecraft.getFontRenderer().drawStringWithShadow("F", 3, 3, (int) textColor);
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glScaled(1.55555555, 1.55555555, 0.88888888);
			WMinecraft.getFontRenderer().drawStringWithShadow(" _____", 3, 4, (int) textColor);
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glScaled(1.55555555, 1.55555555, 0.88888888);
			WMinecraft.getFontRenderer().drawStringWithShadow(" _____", 4, 4, (int) textColor);
			GL11.glPopMatrix();

			GL11.glShadeModel(GL11.GL_SMOOTH);

			// hack list
			int y = 23;
			hacks.addAll(hackList.getValues());
			hacks.sort(Comparator.comparing(Hack::getName));

			gui.updateColors();

			for (Hack hack : hacks) {
				if (!hack.isEnabled())
					continue;

				if (hack.getCategory().getName().contains("Combat")) {
					color = TextUtil.coloredString(hack.getName(), TextUtil.Color.RED);
				} else if (hack.getCategory().getName().contains("Movement")) {
					color = TextUtil.coloredString(hack.getName(), TextUtil.Color.BLUE);
				} else if (hack.getCategory().getName().contains("World")) {
					color = TextUtil.coloredString(hack.getName(), TextUtil.Color.GREEN);
				} else if (hack.getCategory().getName().contains("Player")) {
					color = TextUtil.coloredString(hack.getName(), TextUtil.Color.LIGHT_PURPLE);
				} else if (hack.getCategory().getName().contains("Render")) {
					color = TextUtil.coloredString(hack.getName(), TextUtil.Color.AQUA);
				} else if (hack.getCategory().getName().contains("Pathing")) {
					color = TextUtil.coloredString(hack.getName(), TextUtil.Color.YELLOW);
				} else if (hack.getCategory().getName().contains("Games")) {
					color = TextUtil.coloredString(hack.getName(), TextUtil.Color.GOLD);
				}

				WMinecraft.getFontRenderer().drawString(color, 4, y, (int) textColor, true);

				y += 9;
				// pinned windows
				if (!(mc.currentScreen instanceof ClickGuiScreen))
					clickGui.renderPinnedWindows(event.getPartialTicks());

				if (blend)
					GL11.glEnable(GL11.GL_BLEND);
				else
					GL11.glDisable(GL11.GL_BLEND);
			}
		}
	}
}