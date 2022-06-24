/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WPlayerController;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.utils.NotiUtils;
import net.wurstclient.forge.utils.RotationUtils;

public final class AimBot extends Hack {

	public final EnumSetting<Mode> mode =
			new EnumSetting<>("Mode", Mode.values(), Mode.ROTATIONSIMPLE);

	public AimBot() {
		super("AimBot", "Sends packets to look at all entitys");
		setCategory(Category.WORLD);
		addSetting(mode);
	}

	@Override
	protected void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	protected void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void update(WUpdateEvent event) {
		for (Entity entity : mc.world.loadedEntityList) {
			if (entity != mc.player) {
				if (mc.player.getDistance(entity) < 5) {
					if (mode.getSelected().rotationsimple) {
						lookAtPacket1(entity.posX, entity.posY, entity.posZ, mc.player);
					}
					if (mode.getSelected().rotationwalk) {
						lookAtPacket2(entity.posX, entity.posY, entity.posZ, mc.player);
					}
				}
			}
		}
	}

	public static double[] calculateLookAt(double px, double py, double pz, EntityPlayer me) {
		double dirx = me.posX - px;
		double diry = me.posY - py;
		double dirz = me.posZ - pz;

		double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);

		dirx /= len;
		diry /= len;
		dirz /= len;

		double pitch = Math.asin(diry);
		double yaw = Math.atan2(dirz, dirx);

		pitch = pitch * 180.0d / Math.PI;
		yaw = yaw * 180.0d / Math.PI;

		yaw += 90f;

		return new double[]{yaw, pitch};
	}

	private static void setYawAndPitch1(float yaw1, float pitch1) {
		RotationUtils.faceVectorPacket(new Vec3d(yaw1, pitch1, 0));
	}

	private void lookAtPacket1(double px, double py, double pz, EntityPlayer me) {
		double[] v = calculateLookAt(px, py, pz, me);
		setYawAndPitch1((float) v[0], (float) v[1]);
	}

	private static void setYawAndPitch2(float yaw1, float pitch1) {
		RotationUtils.faceVectorPacketInstant(new Vec3d(yaw1, pitch1, 0));
	}

	private void lookAtPacket2(double px, double py, double pz, EntityPlayer me) {
		double[] v = calculateLookAt(px, py, pz, me);
		setYawAndPitch2((float) v[0], (float) v[1]);
	}
	private enum Mode {
		ROTATIONWALK("RotationInstant", true, false),
		ROTATIONSIMPLE("RotationSimple", false, true);

		private final String name;
		private final boolean rotationwalk;
		private final boolean rotationsimple;

		private Mode(String name, boolean rotationwalk, boolean rotationsimple) {
			this.name = name;
			this.rotationsimple = rotationsimple;
			this.rotationwalk = rotationwalk;
		}

		public String toString() {
			return name;
		}
	}
}

