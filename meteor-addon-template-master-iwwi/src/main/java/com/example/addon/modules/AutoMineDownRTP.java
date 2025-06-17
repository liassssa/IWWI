package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;

public class AutoMineDownRTP extends Module {
    private final Setting<RTPRegion> rtpRegion = settings.getDefaultGroup().add(new EnumSetting.Builder<RTPRegion>()
        .name("rtp-region")
        .description("The region to RTP to.")
        .defaultValue(RTPRegion.EU_CENTRAL)
        .build()
    );

    private final Setting<Integer> postRtpDelaySeconds = settings.getDefaultGroup().add(new IntSetting.Builder()
        .name("post-rtp-delay-seconds")
        .description("Time to wait after RTP before resuming.")
        .defaultValue(5)
        .min(1)
        .sliderMin(1)
        .sliderMax(60)
        .build()
    );

    private int rtpStage = 0;
    private long rtpStageStart = 0;

    private BlockPos lastXZPos = null;
    private long lastXZCheck = 0;

    public AutoMineDownRTP() {
        super(AddonTemplate.CATEGORY, "IWWI RTP base finder", "Mines to Y -5 using #goto, RTPs, and repeats.");
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) mc.player.networkHandler.sendChatMessage("#stop");
    }

    @Override
    public void onActivate() {
        rtpStage = 0;
        rtpStageStart = System.currentTimeMillis();
        if (mc.player != null) {
            lastXZPos = mc.player.getBlockPos();
            lastXZCheck = System.currentTimeMillis();
        }
    }

    private void startGoto() {
        BlockPos playerPos = mc.player.getBlockPos();
        BlockPos target = new BlockPos(playerPos.getX(), -5, playerPos.getZ());
        ChatUtils.sendPlayerMsg("#goto " + target.getX() + " -5 " + target.getZ());
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();
        BlockPos currentXZ = new BlockPos(mc.player.getBlockPos().getX(), 0, mc.player.getBlockPos().getZ());


        if (lastXZPos != null && currentXZ.equals(new BlockPos(lastXZPos.getX(), 0, lastXZPos.getZ()))) {
            if (now - lastXZCheck >= 5000) {
                ChatUtils.info("[IWWI] Stuck for 5s, re-RTPing...");
                ChatUtils.sendPlayerMsg("/rtp " + rtpRegion.get().getCommandPart());
                rtpStage = 3;
                rtpStageStart = now;
                lastXZCheck = now;
            }
        } else {
            lastXZPos = currentXZ;
            lastXZCheck = now;
        }

        switch (rtpStage) {
            case 0:
                startGoto();
                rtpStage = 1;
                return;

            case 1:
                if (mc.player.getY() <= -5) {
                    ChatUtils.sendPlayerMsg("#stop");
                    rtpStage = 2;
                    rtpStageStart = now;
                }
                break;

            case 2:
                if (now - rtpStageStart >= 1000) {
                    ChatUtils.sendPlayerMsg("/rtp " + rtpRegion.get().getCommandPart());
                    rtpStage = 3;
                    rtpStageStart = now;
                }
                break;

            case 3:
                if (now - rtpStageStart >= postRtpDelaySeconds.get() * 1000L) {
                    rtpStage = 4;
                    rtpStageStart = now;
                }
                break;

            case 4:
                if (now - rtpStageStart >= 1000) {
                    rtpStage = 0;
                }
                break;
        }
    }

    public enum RTPRegion {
        ASIA("asia"),
        EAST("east"),
        EU_CENTRAL("eu central"),
        EU_WEST("eu west"),
        OCEANIA("oceania"),
        WEST("west");

        private final String commandPart;

        RTPRegion(String commandPart) {
            this.commandPart = commandPart;
        }

        public String getCommandPart() {
            return commandPart;
        }
    }
}
