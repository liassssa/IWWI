package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.util.math.BlockPos;

public class MineToYMinus50 extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> targetY = sgGeneral.add(new IntSetting.Builder()
        .name("target-y")
        .description("Y level to mine down to. Max: -5")
        .defaultValue(-50)
        .min(-64)
        .sliderMin(-64)
        .sliderMax(-5)
        .build()
    );

    private final Setting<Boolean> rtpEnabled = sgGeneral.add(new BoolSetting.Builder()
        .name("enable-rtp")
        .description("Enable RTP after timeout or if stuck.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> rtpDelayMin = sgGeneral.add(new IntSetting.Builder()
        .name("rtp-delay-minutes")
        .description("Time in minutes before triggering RTP.")
        .defaultValue(5)
        .min(1)
        .sliderMin(1)
        .sliderMax(120)
        .visible(rtpEnabled::get)
        .build()
    );

    private final Setting<RTPRegion> rtpRegion = sgGeneral.add(new EnumSetting.Builder<RTPRegion>()
        .name("rtp-region")
        .description("Region to RTP to.")
        .defaultValue(RTPRegion.EU_CENTRAL)
        .visible(rtpEnabled::get)
        .build()
    );

    private final Setting<Integer> postRtpDelaySeconds = sgGeneral.add(new IntSetting.Builder()
        .name("post-rtp-delay-seconds")
        .description("Time to wait after RTP before continuing.")
        .defaultValue(15)
        .min(1)
        .sliderMin(1)
        .sliderMax(60)
        .visible(rtpEnabled::get)
        .build()
    );

    private boolean miningStarted = false;
    private boolean tunnelStarted = false;
    private long startTime = 0;

    private boolean triggeredRtp = false;
    private long rtpStageStart = 0;
    private int rtpStage = 0;

    private boolean autoSendRtp = false;
    private String rtpCommand = "";

    private BlockPos lastXZPos = null;
    private long lastMoveTime = 0;

    public MineToYMinus50() {
        super(AddonTemplate.CATEGORY, "IWWI-Tunnel", "Mines down to the depth you chose and starts tunneling, with optional RTP + resume.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) return;

        miningStarted = false;
        tunnelStarted = false;
        triggeredRtp = false;
        rtpStage = 0;
        rtpStageStart = 0;
        startTime = System.currentTimeMillis();
        autoSendRtp = false;

        lastXZPos = mc.player.getBlockPos();
        lastMoveTime = System.currentTimeMillis();

        int playerY = (int) mc.player.getY();
        if (playerY <= targetY.get()) {
            mc.player.networkHandler.sendChatMessage("#tunnel");
            tunnelStarted = true;
        }
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) mc.player.networkHandler.sendChatMessage("#stop");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        long now = System.currentTimeMillis();
        int playerY = (int) mc.player.getY();

        BlockPos currentXZ = new BlockPos(mc.player.getBlockPos().getX(), 0, mc.player.getBlockPos().getZ());
        if (!currentXZ.equals(lastXZPos)) {
            lastMoveTime = now;
            lastXZPos = currentXZ;
        }

        boolean stuck = rtpEnabled.get() && (now - lastMoveTime >= 5000);

        if (autoSendRtp && !rtpCommand.isEmpty()) {
            ChatUtils.sendPlayerMsg(rtpCommand);
            autoSendRtp = false;
            rtpCommand = "";
            return;
        }

        if (rtpEnabled.get() && !triggeredRtp) {
            long elapsedSec = (now - startTime) / 1000;
            if (elapsedSec >= rtpDelayMin.get() * 60L || stuck) {
                triggeredRtp = true;
                rtpStage = 1;
                rtpStageStart = now;
                mc.player.networkHandler.sendChatMessage("#stop");
                return;
            }
        }

        if (triggeredRtp) {
            long stageElapsed = now - rtpStageStart;

            if (rtpStage == 1 && stageElapsed >= 1000) {
                rtpCommand = "/rtp " + rtpRegion.get().getCommandPart();
                autoSendRtp = true;
                rtpStage++;
                rtpStageStart = now;
            } else if (rtpStage == 2 && stageElapsed >= postRtpDelaySeconds.get() * 1000L) {
                mc.player.networkHandler.sendChatMessage("#goto ~ " + targetY.get() + " ~");
                miningStarted = true;
                tunnelStarted = false;
                triggeredRtp = false;
                startTime = now;
                lastMoveTime = now;
                lastXZPos = mc.player.getBlockPos();
            }
            return;
        }

        if (!miningStarted && !tunnelStarted && playerY > targetY.get()) {
            mc.player.networkHandler.sendChatMessage("#goto ~ " + targetY.get() + " ~");
            miningStarted = true;
        }

        if (miningStarted && !tunnelStarted && playerY <= targetY.get()) {
            mc.player.networkHandler.sendChatMessage("#tunnel");
            tunnelStarted = true;
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
