package top.hendrixshen.tweakmyclient.mixin.feature.featureCustomWindowIcon;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.hendrixshen.tweakmyclient.TweakMyClient;
import top.hendrixshen.tweakmyclient.fakeInterface.IMinecraft;

@Mixin(Main.class)
public class MixinMain {
    @Inject(
            method = "main",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;finishInitialization()V"
            ),
            remap = false
    )
    private static void finishInitializationRenderSystem(String[] strings, CallbackInfo ci) {
        ((IMinecraft) TweakMyClient.getMinecraftClient()).refreshIcon();
    }
}
