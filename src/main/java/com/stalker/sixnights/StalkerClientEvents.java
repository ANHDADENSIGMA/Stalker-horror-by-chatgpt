package com.stalker.sixnights;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = StalkerSixNights.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class StalkerClientEvents {
    private static final Random RANDOM = new Random();
    private static final ResourceLocation[] JUMPSCARES = {
            new ResourceLocation(StalkerSixNights.MOD_ID, "textures/gui/jumpscare_1.png"),
            new ResourceLocation(StalkerSixNights.MOD_ID, "textures/gui/jumpscare_2.png"),
            new ResourceLocation(StalkerSixNights.MOD_ID, "textures/gui/jumpscare_3.png")
    };
    private static int scareTicks = 0;
    private static int scareCooldown = 0;
    private static ResourceLocation activeScare = JUMPSCARES[0];

    private StalkerClientEvents() {}

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(StalkerModel.LAYER, StalkerModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(StalkerRegistry.STALKER.get(), StalkerRenderer::new);
    }

    @Mod.EventBusSubscriber(modid = StalkerSixNights.MOD_ID, value = Dist.CLIENT)
    public static final class ClientGameplay {
        private ClientGameplay() {}

        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) {
                scareTicks = 0;
                return;
            }
            if (scareCooldown > 0) scareCooldown--;
            if (scareTicks > 0) scareTicks--;

            List<StalkerEntity> nearby = mc.level.getEntitiesOfClass(
                    StalkerEntity.class, mc.player.getBoundingBox().inflate(8.0));
            double nearest = Double.MAX_VALUE;
            StalkerEntity closest = null;
            for (StalkerEntity entity : nearby) {
                double distance = mc.player.distanceToSqr(entity);
                if (distance < nearest) {
                    nearest = distance;
                    closest = entity;
                }
            }
            // A rare, short visual scare only when a Stalker gets dangerously close.
            if (closest != null && nearest < 3.2 * 3.2 && scareCooldown == 0
                    && RANDOM.nextFloat() < 0.018F) {
                activeScare = JUMPSCARES[RANDOM.nextInt(JUMPSCARES.length)];
                scareTicks = 5 + RANDOM.nextInt(3);
                scareCooldown = 20 * 32;
                mc.player.playSound(SoundEvents.ENDERMAN_SCREAM, 0.9F, 0.55F + RANDOM.nextFloat() * 0.25F);
            }
        }

        @SubscribeEvent
        public static void fogColor(ViewportEvent.ComputeFogColor event) {
            Minecraft mc = Minecraft.getInstance();
            Entity camera = event.getCamera().getEntity();
            if (mc.level == null || camera == null) return;
            double distance = nearestStalkerDistance(mc, camera);
            if (distance > 22.0) return;
            float intensity = (float) (1.0 - Math.max(0.0, distance - 3.0) / 19.0);
            intensity = Math.max(0.0F, Math.min(0.88F, intensity * 0.82F));
            event.setRed(event.getRed() * (1.0F - intensity) + 0.012F * intensity);
            event.setGreen(event.getGreen() * (1.0F - intensity) + 0.008F * intensity);
            event.setBlue(event.getBlue() * (1.0F - intensity) + 0.018F * intensity);
        }

        @SubscribeEvent
        public static void fogDistance(ViewportEvent.ComputeFogDistance event) {
            Minecraft mc = Minecraft.getInstance();
            Entity camera = event.getCamera().getEntity();
            if (mc.level == null || camera == null) return;
            double distance = nearestStalkerDistance(mc, camera);
            if (distance > 18.0) return;
            float strength = (float) Math.max(0.0, Math.min(1.0, (18.0 - distance) / 15.0));
            event.setNearPlaneDistance(Math.max(0.0F, event.getNearPlaneDistance() - strength * 1.8F));
            event.setFarPlaneDistance(Math.max(5.0F, event.getFarPlaneDistance() * (1.0F - strength * 0.78F)));
        }

        @SubscribeEvent
        public static void renderJumpscare(RenderGuiOverlayEvent.Post event) {
            if (scareTicks <= 0) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            int width = mc.getWindow().getGuiScaledWidth();
            int height = mc.getWindow().getGuiScaledHeight();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            event.getGuiGraphics().blit(activeScare, 0, 0, 0.0F, 0.0F, width, height, width, height);
            RenderSystem.disableBlend();
        }

        private static double nearestStalkerDistance(Minecraft mc, Entity camera) {
            double nearest = Double.MAX_VALUE;
            for (StalkerEntity stalker : mc.level.getEntitiesOfClass(
                    StalkerEntity.class, camera.getBoundingBox().inflate(24.0))) {
                nearest = Math.min(nearest, camera.distanceToSqr(stalker));
            }
            return Math.sqrt(nearest);
        }
    }
}
