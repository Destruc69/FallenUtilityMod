/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WEntityPlayerJumpEvent;
import net.wurstclient.fmlevents.WPacketInputEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.compatibility.WPlayerController;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.utils.*;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public final class AutoCrystal extends Hack {

	ArrayList<AxisAlignedBB> bbsBreak = new ArrayList<>();

	private final EnumSetting<Mode> mode =
			new EnumSetting<>("Packets", Mode.values(), Mode.NCP);

	private final EnumSetting<Rot> rot =
			new EnumSetting<>("Rotations", Rot.values(), Rot.NCP);

	private final EnumSetting<If> onIf =
			new EnumSetting<>("OnlyActIf", If.values(), If.NORMAL);

	private final SliderSetting rangep =
			new SliderSetting("Range [PLACE]", "Range for witch we will place the crystals", 4, 1.0, 5, 1.0, SliderSetting.ValueDisplay.DECIMAL);

	private final SliderSetting rangeb =
			new SliderSetting("Range [BREAK]", "Range for witch we will break the crystals", 4, 1.0, 5, 1.0, SliderSetting.ValueDisplay.DECIMAL);


	private final CheckboxSetting autoPlace =
			new CheckboxSetting("AutoPlace", "Shall we place crystals automatically?",
					true);

	private final CheckboxSetting autoBreak =
			new CheckboxSetting("AutoBreak", "Shall we break crystals automatically?",
					true);

	private final SliderSetting radiuse =
			new SliderSetting("Radius", "Radius around the enemy we are allowed to place in", 4, 1.0, 5, 1.0, SliderSetting.ValueDisplay.DECIMAL);

	private final CheckboxSetting render =
			new CheckboxSetting("RenderCrystal", "Show the crystals we are going to attempt to break",
					true);

	private final SliderSetting minDEnemy =
			new SliderSetting("MinDamageEnemy", "The minimum damage we can do to an enemy", 4, 1.0, 5, 1.0, SliderSetting.ValueDisplay.DECIMAL);

	private final SliderSetting maxDPlayer=
			new SliderSetting("MaxDamagePlayer", "The max damage we can do you the player (yourself)", 4, 1.0, 5, 1.0, SliderSetting.ValueDisplay.DECIMAL);

	private final CheckboxSetting debug =
			new CheckboxSetting("Debug", "Tells you what the module is doing/thinking",
					true);

	public AutoCrystal() {
		super("AutoCrystal", "Auto Crystal but for Killaura.");
		setCategory(Category.COMBAT);
		addSetting(mode);
		addSetting(rot);
		addSetting(onIf);
		addSetting(rangep);
		addSetting(rangeb);
		addSetting(autoPlace);
		addSetting(autoBreak);
		addSetting(radiuse);
		addSetting(render);
		addSetting(minDEnemy);
		addSetting(maxDPlayer);
		addSetting(debug);
	}

	@Override
	protected void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
		TimerUtils.reset();
	}

	@Override
	protected void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void update(WUpdateEvent event) {
		if (autoPlace.isChecked()) {
			if (TimerUtils.hasReached(70)) {
				placing();
			}
		}
		if (autoBreak.isChecked()) {
			if (TimerUtils.hasReached(140)) {
				breaking();
				TimerUtils.reset();
			}
		}
	}

	private void placing() {
		if (mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL)) {
			int radius = radiuse.getValueI();
			for (Entity entity : mc.world.getLoadedEntityList()) {
				if (entity instanceof EntityPlayer) {
					if (entity != mc.player) {
						if (mc.player.getDistance(entity.posX, entity.posY, entity.posZ) < rangep.getValueF()) {
							for (int x = (int) -radius; x <= radius; x++) {
								for (int z = (int) -radius; z <= radius; z++) {
									if (mc.world.getBlockState(entity.getPosition().add(x, 0, z)).getBlock().equals(Blocks.AIR) && (mc.world.getBlockState(entity.getPosition().add(x, -1, z)).getBlock().equals(Blocks.OBSIDIAN) || mc.world.getBlockState(entity.getPosition().add(x, -1, z)).getBlock().equals(Blocks.BEDROCK))) {

										if (CrystalUtil.calculateDamage(entity.getPosition().add(x, -1, z), mc.player) > maxDPlayer.getValueF()) {
											if (debug.isChecked()) {
												ChatUtils.message("[AC] Damage to player is to high and placing was aborted...");
											}
										}

										if (CrystalUtil.calculateDamage(entity.getPosition().add(x, -1, z), (EntityLivingBase) entity) < minDEnemy.getValueF()) {
											if (debug.isChecked()) {
												ChatUtils.message("[AC] Damage to enemy was to low and placing was aborted...");
											}
										}

										if (CrystalUtil.calculateDamage(entity.getPosition().add(x, -1, z), mc.player) > maxDPlayer.getValueF())
											return;

										if (CrystalUtil.calculateDamage(entity.getPosition().add(x, -1, z), (EntityLivingBase) entity) < minDEnemy.getValueF())
											return;

										if (!PlayerUtils.CanSeeBlock(entity.getPosition().add(x, -1, z)) && onIf.getSelected().block)
											return;

										mc.playerController.processRightClickBlock(mc.player, mc.world,
												entity.getPosition().add(x, -1, z), EnumFacing.UP, mc.objectMouseOver.hitVec,
												EnumHand.MAIN_HAND);

										if (debug.isChecked()) {
											ChatUtils.message("[AC] Placing crystal at:" + " " + Math.round(x) + " " + Math.round(z));
										}

										if (mode.getSelected().ncp || mode.getSelected().normal) {
											if (mode.getSelected().ncp) {
												lookAtPacket(entity.getPosition().add(x, -1, z).getX(), entity.getPosition().add(x, -1, z).getY(), entity.getPosition().add(x, -1, z).getZ(), mc.player);
												lookAtPacket(entity.getPosition().add(x, -1, z).getX(), entity.getPosition().add(x, -1, z).getY(), entity.getPosition().add(x, -1, z).getZ(), mc.player);
												lookAtPacket(entity.getPosition().add(x, -1, z).getX(), entity.getPosition().add(x, -1, z).getY(), entity.getPosition().add(x, -1, z).getZ(), mc.player);
											} else {
												lookAtPacket2(entity.getPosition().add(x, -1, z).getX(), entity.getPosition().add(x, -1, z).getY(), entity.getPosition().add(x, -1, z).getZ(), mc.player);
												lookAtPacket2(entity.getPosition().add(x, -1, z).getX(), entity.getPosition().add(x, -1, z).getY(), entity.getPosition().add(x, -1, z).getZ(), mc.player);
												lookAtPacket2(entity.getPosition().add(x, -1, z).getX(), entity.getPosition().add(x, -1, z).getY(), entity.getPosition().add(x, -1, z).getZ(), mc.player);
											}
										}
										mc.player.swingArm(EnumHand.MAIN_HAND);
										if (mode.getSelected().ncp) {
											mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(entity.getPosition().add(x, -1, z), EnumFacing.UP, EnumHand.MAIN_HAND, (float) entity.posX, (float) entity.posY, (float) entity.posZ));
											mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
											mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void breaking() {
		for (Entity entity : mc.world.getLoadedEntityList()) {
			if (entity instanceof EntityEnderCrystal) {
				if (mc.player.getDistance(entity) < rangeb.getValueF()) {

					if (!PlayerUtils.CanSeeBlock(entity.getPosition()) && onIf.getSelected().block)
						return;

					mc.playerController.attackEntity(mc.player, entity);
					bbsBreak.add(BlockUtils.getBoundingBox(entity.getPosition()));

					if (mode.getSelected().normal || mode.getSelected().ncp) {
						if (rot.getSelected().ncp) {
							lookAtPacket(entity.posX, entity.posY, entity.posZ, mc.player);
							lookAtPacket(entity.posX, entity.posY, entity.posZ, mc.player);
							lookAtPacket(entity.posX, entity.posY, entity.posZ, mc.player);
						} else {
							lookAtPacket2(entity.posX, entity.posY, entity.posZ, mc.player);
							lookAtPacket2(entity.posX, entity.posY, entity.posZ, mc.player);
							lookAtPacket2(entity.posX, entity.posY, entity.posZ, mc.player);
						}
					}

					if (mode.getSelected().ncp) {
						mc.player.connection.sendPacket(new CPacketUseEntity(entity));
						mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
					}
					mc.player.swingArm(EnumHand.MAIN_HAND);

					if (debug.isChecked()) {
						ChatUtils.message("[AC] Breaking crystal at:" + " " + Math.round(entity.lastTickPosX) + " " + Math.round(entity.lastTickPosZ));
					}
				}
			} else {
				bbsBreak.clear();
			}
		}
	}

	private enum Mode {
		NCP("NCP",false, true),
		NORMAL("Normal", true, false);

		private final String name;
		private final boolean normal;
		private final boolean ncp;

		private Mode(String name, boolean normal, boolean ncp) {
			this.name = name;
			this.normal = normal;
			this.ncp = ncp;
		}

		public String toString() {
			return name;
		}
	}

	private enum Rot {
		NCP("NCP", false, true),
		NORMAL("Instant", true, false);

		private final String name;
		private final boolean normal;
		private final boolean ncp;

		private Rot(String name, boolean normal, boolean ncp) {
			this.name = name;
			this.normal = normal;
			this.ncp = ncp;
		}

		public String toString() {
			return name;
		}
	}

	private enum If {
		BLOCK("CanSee", false, true),
		NORMAL("Normal", true, false);

		private final String name;
		private final boolean block;
		private final boolean normal;

		private If(String name, boolean normal, boolean block) {
			this.name = name;
			this.normal = normal;
			this.block = block;
		}

		public String toString() {
			return name;
		}
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		if (render.isChecked()) {
			// GL settings
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GL11.glLineWidth(2);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_DEPTH_TEST);

			GL11.glPushMatrix();
			GL11.glTranslated(-TileEntityRendererDispatcher.staticPlayerX,
					-TileEntityRendererDispatcher.staticPlayerY,
					-TileEntityRendererDispatcher.staticPlayerZ);

			Vec3d start = RotationUtils.getClientLookVec()
					.addVector(0, 0, 0)
					.addVector(TileEntityRendererDispatcher.staticPlayerX,
							TileEntityRendererDispatcher.staticPlayerY,
							TileEntityRendererDispatcher.staticPlayerZ);

			for (AxisAlignedBB alignedBB1 : bbsBreak) {
				GL11.glColor4f(1, 0, 0, 0.5F);
				GL11.glBegin(GL11.GL_QUADS);
				RenderUtils.drawSolidBox(alignedBB1);
				GL11.glEnd();
			}

			GL11.glPopMatrix();

			// GL resets
			GL11.glColor4f(1, 1, 1, 1);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_LINE_SMOOTH);

			GL11.glPushMatrix();
			GL11.glTranslated(start.x, start.y, start.z);
			GL11.glTranslated(0.5, 0.5, 0.5);

			GL11.glColor4f(0, 1, 1, 0.5F);
			GL11.glBegin(GL11.GL_LINES);
			RenderUtils
					.drawNode(new AxisAlignedBB(-0.25, -0.25, -0.25, 0.25, 0.25, 0.25));
			GL11.glEnd();

			GL11.glPopMatrix();
			GL11.glEndList();
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

	private static void setYawAndPitch(float yaw1, float pitch1) {
		RotationUtils.faceVectorPacket(new Vec3d(yaw1, pitch1, 0));
		mc.player.rotationYawHead = yaw1;
		mc.player.renderYawOffset = yaw1;
	}

	private void lookAtPacket(double px, double py, double pz, EntityPlayer me) {
		double[] v = calculateLookAt(px, py, pz, me);
		setYawAndPitch((float) v[0], (float) v[1]);
	}

	private static void setYawAndPitch2(float yaw1, float pitch1) {
		RotationUtils.faceVectorPacketInstant(new Vec3d(yaw1, pitch1, 0));
		mc.player.rotationYawHead = yaw1;
	}

	private void lookAtPacket2(double px, double py, double pz, EntityPlayer me) {
		double[] v = calculateLookAt(px, py, pz, me);
		setYawAndPitch2((float) v[0], (float) v[1]);
	}
}