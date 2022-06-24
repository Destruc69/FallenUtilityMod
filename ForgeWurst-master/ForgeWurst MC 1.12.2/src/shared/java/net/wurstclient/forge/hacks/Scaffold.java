package net.wurstclient.forge.hacks;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.handshake.HandshakeInjector;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.utils.*;
import org.lwjgl.opengl.GL11;

import javax.net.ssl.HandshakeCompletedEvent;
import java.util.ArrayList;
import java.util.Objects;

public final class Scaffold extends Hack {

	ArrayList<BlockPos> bbs = new ArrayList<>();

	private final EnumSetting<Mode> mode =
			new EnumSetting<>("RotationType", Mode.values(), Mode.NCP);

	private final CheckboxSetting render =
			new CheckboxSetting("RenderPos", "Renders where it will place",
					true);

	public Scaffold() {
		super("Scaffold", "Places blocks under you.");
		setCategory(Category.WORLD);
		addSetting(mode);
		addSetting(render);
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
	public void onUpdateWalkingPlayerPost(WUpdateEvent event) {
		BlockPos playerBlock;

		if (BlockUtils.isScaffoldPos((playerBlock = BlockUtils.getPlayerPosWithEntity()).add(0, -1, 0))) {
			if (BlockUtil.isValidBlock(playerBlock.add(0, -2, 0))) {
				this.place(playerBlock.add(0, -1, 0), EnumFacing.UP);
			} else if (BlockUtil.isValidBlock(playerBlock.add(-1, -1, 0))) {
				this.place(playerBlock.add(0, -1, 0), EnumFacing.EAST);
			} else if (BlockUtil.isValidBlock(playerBlock.add(1, -1, 0))) {
				this.place(playerBlock.add(0, -1, 0), EnumFacing.WEST);
			} else if (BlockUtil.isValidBlock(playerBlock.add(0, -1, -1))) {
				this.place(playerBlock.add(0, -1, 0), EnumFacing.SOUTH);
			} else if (BlockUtil.isValidBlock(playerBlock.add(0, -1, 1))) {
				this.place(playerBlock.add(0, -1, 0), EnumFacing.NORTH);
			} else if (BlockUtil.isValidBlock(playerBlock.add(1, -1, 1))) {
				if (BlockUtil.isValidBlock(playerBlock.add(0, -1, 1))) {
					this.place(playerBlock.add(0, -1, 1), EnumFacing.NORTH);
				}
				this.place(playerBlock.add(1, -1, 1), EnumFacing.EAST);
			} else if (BlockUtil.isValidBlock(playerBlock.add(-1, -1, 1))) {
				if (BlockUtil.isValidBlock(playerBlock.add(-1, -1, 0))) {
					this.place(playerBlock.add(0, -1, 1), EnumFacing.WEST);
				}
				this.place(playerBlock.add(-1, -1, 1), EnumFacing.SOUTH);
			} else if (BlockUtil.isValidBlock(playerBlock.add(1, -1, 1))) {
				if (BlockUtil.isValidBlock(playerBlock.add(0, -1, 1))) {
					this.place(playerBlock.add(0, -1, 1), EnumFacing.SOUTH);
				}
				this.place(playerBlock.add(1, -1, 1), EnumFacing.WEST);
			} else if (BlockUtil.isValidBlock(playerBlock.add(1, -1, 1))) {
				if (BlockUtil.isValidBlock(playerBlock.add(0, -1, 1))) {
					this.place(playerBlock.add(0, -1, 1), EnumFacing.EAST);
				}
				this.place(playerBlock.add(1, -1, 1), EnumFacing.NORTH);
			}
		}
	}

	public void place(BlockPos posI, EnumFacing face) {
		Block block;
		BlockPos pos = posI;
		if (face == EnumFacing.UP) {
			pos = pos.add(0, -1, 0);
		} else if (face == EnumFacing.NORTH) {
			pos = pos.add(0, 0, 1);
		} else if (face == EnumFacing.SOUTH) {
			pos = pos.add(0, 0, -1);
		} else if (face == EnumFacing.EAST) {
			pos = pos.add(-1, 0, 0);
		} else if (face == EnumFacing.WEST) {
			pos = pos.add(1, 0, 0);
		}
		int oldSlot = Scaffold.mc.player.inventory.currentItem;
		int newSlot = -1;
		for (int i = 0; true; ++i) {
			newSlot = i;
			break;
		}
		if (newSlot == -1) {
			return;
		}
		boolean crouched = false;
		if (!(Scaffold.mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock)) {
			Scaffold.mc.player.connection.sendPacket((Packet) new CPacketHeldItemChange(newSlot));
			Scaffold.mc.player.inventory.currentItem = newSlot;
			Scaffold.mc.playerController.updateController();
		}

		if (mode.getSelected().ncp) {
			float[] angle = MathUtils.calcAngle(Scaffold.mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d((double) ((float) pos.getX() + 0.5f), (double) ((float) pos.getY() - 0.5f), (double) ((float) pos.getZ() + 0.5f)));
			Scaffold.mc.player.connection.sendPacket((Packet) new CPacketPlayer.Rotation(angle[0], (float) MathHelper.normalizeAngle((int) ((int) angle[1]), (int) 360), Scaffold.mc.player.onGround));
		}
		if (mode.getSelected().normal) {
			lookAtPacket(pos.getX(), pos.getY(), pos.getZ(), mc.player);
		}

		if (TimerUtils.hasReached(1)) {
			bbs.clear();
			TimerUtils.reset();
		}

		Scaffold.mc.playerController.processRightClickBlock(Scaffold.mc.player, Scaffold.mc.world, pos, face, new Vec3d(0.5, 0.5, 0.5), EnumHand.MAIN_HAND);
		bbs.add(pos);
		Scaffold.mc.player.swingArm(EnumHand.MAIN_HAND);
		Scaffold.mc.player.connection.sendPacket((Packet) new CPacketHeldItemChange(oldSlot));
		Scaffold.mc.player.inventory.currentItem = oldSlot;
		Scaffold.mc.playerController.updateController();
		if (crouched) {
			Scaffold.mc.player.connection.sendPacket((Packet) new CPacketEntityAction((Entity) Scaffold.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
		}
	}

	private enum Mode {
		NCP("NCP", false, true),
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
		RotationUtils.faceVectorForWalking(new Vec3d(yaw1, pitch1, 0));
	}

	private void lookAtPacket(double px, double py, double pz, EntityPlayer me) {
		double[] v = calculateLookAt(px, py, pz, me);
		setYawAndPitch((float) v[0], (float) v[1]);
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(1);
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

		for (BlockPos blockPos : bbs) {
			GL11.glColor4f(0, 1, 0, 0.5F);
			GL11.glBegin(GL11.GL_LINES);
			RenderUtils.drawOutlinedBox(Objects.requireNonNull(BlockUtils.getBoundingBox(blockPos)));
			GL11.glEnd();
		}

		GL11.glPopMatrix();

		// GL resets
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}
}
