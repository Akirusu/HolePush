package net.spartanb312.cursa.module.modules.combat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.spartanb312.cursa.common.annotations.ModuleInfo;
import net.spartanb312.cursa.core.setting.Setting;
import net.spartanb312.cursa.module.Category;
import net.spartanb312.cursa.module.Module;
import net.spartanb312.cursa.utils.*;

@ModuleInfo(name = "AutoPistion", category = Category.COMBAT)
public class AutoPistion extends Module {
    Setting<Boolean> rotate = setting("Rotate", false);
    Setting<Boolean> toggle = setting("Toggle", false);
    Setting<Boolean> aiMode = setting("AI", true);
    EntityPlayer target;
    int oldSlot;

    @Override
    public void onDisable() {
        target = null;
        oldSlot = -1;
    }

    @Override
    public void onRenderTick() {
        target = EntityUtil.getTarget(6);
        int pistionSlot = ItemUtils.findBlockInHotBar(Blocks.PISTON);
        int redStoneSlot = ItemUtils.findBlockInHotBar(Blocks.REDSTONE_BLOCK);
        int obsidianSlot = ItemUtils.findBlockInHotBar(Blocks.OBSIDIAN);
        if (pistionSlot == -1 || redStoneSlot == -1) return;
        if (target == null) return;
        BlockPos playerFloorPos = EntityUtil.getPlayerPosFloored(target);
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            BlockPos pistionPos = playerFloorPos.offset(facing).up();
            if (!BlockInteractionHelper.isAirBlock(pistionPos)) continue;
            float[] rotate = getRotationsBlock(EntityUtil.getLocalPlayerPosFloored().offset(facing).up(),EnumFacing.UP,false);
            setRotate(rotate[0], rotate[1]);
            placeBlock(pistionSlot,pistionPos);
            if(BlockInteractionHelper.isAirBlock(pistionPos.offset(facing))){
                placeBlock(redStoneSlot,pistionPos.offset(facing));
            }else {
                placeBlock(redStoneSlot,pistionPos.up());
            }
            mc.player.inventory.currentItem = oldSlot;
            mc.playerController.updateController();
            break;
        }
        if(toggle.getValue()) toggle();
    }

    private void placeBlock(int slot, BlockPos pos) {
        if (mc.player.inventory.currentItem != slot) {
            if (slot != oldSlot) oldSlot = mc.player.inventory.currentItem;
            mc.player.inventory.currentItem = slot;
            mc.playerController.updateController();
        }
        BlockInteractionHelper.place(pos, 6f, false, false);
        mc.player.swingArm(EnumHand.MAIN_HAND);
        mc.player.inventory.currentItem = oldSlot;
        mc.playerController.updateController();
    }

    private void setRotate(float yaw1, float pitch1) {
        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw1, pitch1, true));
    }
    
    public float[] getRotationsBlock(BlockPos block, EnumFacing face, boolean Legit) {
        double x = block.getX() + 0.5 - mc.player.posX +  (double) face.getXOffset()/2;
        double z = block.getZ() + 0.5 - mc.player.posZ +  (double) face.getZOffset()/2;
        double y = (block.getY() + 0.5);

        if (Legit)
            y += 0.5;

        double d1 = mc.player.posY + mc.player.getEyeHeight() - y;
        double d3 = MathHelper.sqrt(x * x + z * z);
        float yaw = (float) (Math.atan2(z, x) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (Math.atan2(d1, d3) * 180.0D / Math.PI);

        if (yaw < 0.0F) {
            yaw += 360f;
        }
        return new float[]{yaw, pitch};
    }
}
