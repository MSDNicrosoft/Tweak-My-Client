package top.hendrixshen.TweakMyClient.mixin.disable.disableRenderOverlayPumpkin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.hendrixshen.TweakMyClient.config.Configs;

@Mixin(Gui.class)
public abstract class MixinGui {
    @Inject(
            method = "render",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void onRenderPumpkinOverlay(PoseStack poseStack, float f, CallbackInfo ci) {
        if (Configs.Disable.DISABLE_RENDER_OVERLAY_PUMPKIN.getBooleanValue()) {
            ci.cancel();
        }
    }
}
