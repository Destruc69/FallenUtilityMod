package net.wurstclient.forge.hacks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.utils.*;

public final class Scaffold extends Hack {

	public final EnumSetting<TowerMode> towermode =
			new EnumSetting<>("TowerMode", TowerMode.values(), TowerMode.NORMAL);

	public final EnumSetting<SneakMode> sneakmode =
			new EnumSetting<>("SneakMode", SneakMode.values(), SneakMode.LEGIT);

	private final SliderSetting speed =
			new SliderSetting("Speed", "1 = normal speed", 0.1, 0.00005, 0.1, 0.001, SliderSetting.ValueDisplay.DECIMAL);

	private enum TowerMode {
		NORMAL("Normal", true, false),
		NCP("NCP", false, true);

		private final String name;
		private final boolean normal;
		private final boolean ncp;

		private TowerMode(String name, boolean normal, boolean ncp) {
			this.name = name;
			this.normal = normal;
			this.ncp = ncp;
		}

		public String toString() {
			return name;
		}
	}

	private enum SneakMode {
		LEGIT("Legit", true, false),
		PACKET("Packet", false, true);

		private final String name;
		private final boolean legit;
		private final boolean packet;

		private SneakMode(String name, boolean legit, boolean packet) {
			this.name = name;
			this.legit = legit;
			this.packet = packet;
		}

		public String toString() {
			return name;
		}
	}

	public Scaffold() {
		super("Scaffold", "Places blocks under you.");
		setCategory(Category.WORLD);
		addSetting(towermode);
		addSetting(sneakmode);
		addSetting(speed);
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
	public void onUpdate(WUpdateEvent event) {
		BlockPos playerBlock;

		towerLogic();
		speedLogic();
		sneakLogic();

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

	public void towerLogic() {
		if (towermode.getSelected().normal) {
			if (mc.gameSettings.keyBindJump.isKeyDown()) {
				if (!mc.player.onGround && mc.player.posY - Math.floor(mc.player.posY) <= 0.1) {
					mc.player.motionY = 0.41999998688697815;
				}
			}
		} else if (towermode.getSelected().ncp) {
			if (mc.gameSettings.keyBindJump.isKeyDown()) {
				if (mc.player.ticksExisted % 2 == 0) {
					mc.player.motionY = 0.41999998688697815;
				}
			}
		}
	}

	public void sneakLogic() {
		if (sneakmode.getSelected().packet) {
			if (mc.player.isSwingInProgress) {
				mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
			} else {
				mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
			}
		} else if (sneakmode.getSelected().legit) {
			if (mc.player.isSwingInProgress) {
				KeyBindingUtils.setPressed(mc.gameSettings.keyBindSneak, true);
			} else {
				KeyBindingUtils.setPressed(mc.gameSettings.keyBindSneak, false);
			}
		}
	}

	public void speedLogic() {
		double[] dir = MathUtils.directionSpeed(speed.getValueF());
		if (mc.player.moveForward != 0 || mc.player.moveStrafing != 0) {
			mc.player.motionX = dir[0];
			mc.player.motionZ = dir[1];
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

		float[] angle = MathUtils.calcAngle(Scaffold.mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d((double) ((float) pos.getX() + 0.5f), (double) ((float) pos.getY() - 0.5f), (double) ((float) pos.getZ() + 0.5f)));
		mc.player.connection.sendPacket((Packet) new CPacketPlayer.Rotation(angle[0], (float) MathHelper.normalizeAngle((int) ((int) angle[1]), (int) 360), Scaffold.mc.player.onGround));

		Scaffold.mc.playerController.processRightClickBlock(Scaffold.mc.player, Scaffold.mc.world, pos, face, new Vec3d(0.5, 0.5, 0.5), EnumHand.MAIN_HAND);
		Scaffold.mc.player.swingArm(EnumHand.MAIN_HAND);
	}
}