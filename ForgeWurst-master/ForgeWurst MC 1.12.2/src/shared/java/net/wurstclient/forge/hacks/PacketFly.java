package net.wurstclient.forge.hacks;

import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WPacketInputEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.utils.MathUtils;

public final class PacketFly extends Hack {

	public PacketFly() {
		super("PacketFly", "Fly around with packets.");
		setCategory(Category.MOVEMENT);
	}

	protected void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	protected void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onUpdate(WUpdateEvent event) {
		//This is how we will be moving by teleporting
		double reqY = mc.player.posY + mc.player.motionY;
		double reqX = mc.player.posX + mc.player.motionX;
		double reqZ = mc.player.posZ + mc.player.motionZ;

		//Moving logic (forward and ect)
		if (mc.player.moveForward != 0 || mc.player.moveStrafing != 0) {
			double[] dir = MathUtils.directionSpeed(0.1);
			mc.player.motionX = mc.player.motionX + dir[0];
			mc.player.motionZ = mc.player.motionZ + dir[1];
		}

		//Up logic
		if (mc.gameSettings.keyBindJump.isKeyDown()) {
			mc.player.motionY += 0.1;
		}

		//Down logic
		if (mc.gameSettings.keyBindSneak.isKeyDown()) {
			mc.player.motionY -= 0.1;
		}

		//If were not jumping nor sneaking we will hold the motion y
		if (!mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) {
			mc.player.motionY = 0;
		}

		//Client side = Server side
		mc.player.onGround = true;
		mc.player.fallDistance = 0;
		mc.player.noClip = true;
		mc.player.isAirBorne = false;

		mc.player.setPosition(reqX, reqY, reqZ);
	}

	@SubscribeEvent
	public void onPacket(WPacketInputEvent event) {
		//How we will be moving by teleporting
		double reqY = mc.player.posY + mc.player.motionY;
		double reqX = mc.player.posX + mc.player.motionX;
		double reqZ = mc.player.posZ + mc.player.motionZ;

		//Overriding packet to player position
		if (event.getPacket() instanceof CPacketPlayer.Rotation) {
			mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, mc.player.rotationPitch, true));
			event.setCanceled(true);
		}
		if (event.getPacket() instanceof CPacketPlayer.Position) {
			mc.player.connection.sendPacket(new CPacketPlayer.Position(reqX, reqY, reqZ, true));
			event.setCanceled(true);
		}
		if (event.getPacket() instanceof CPacketPlayer.PositionRotation) {
			mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(reqX, reqY, reqZ, mc.player.rotationYaw, mc.player.rotationPitch,true));
			event.setCanceled(true);
		}
		if (event.getPacket() instanceof CPacketPlayer) {
			mc.player.connection.sendPacket(new CPacketPlayer(true));
			event.setCanceled(true);
		}
	}
}