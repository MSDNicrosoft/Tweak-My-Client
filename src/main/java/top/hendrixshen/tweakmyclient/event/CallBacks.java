package top.hendrixshen.tweakmyclient.event;

import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import top.hendrixshen.magiclib.config.Option;
import top.hendrixshen.tweakmyclient.TweakMyClient;
import top.hendrixshen.tweakmyclient.TweakMyClientConfigGui;
import top.hendrixshen.tweakmyclient.TweakMyClientReference;
import top.hendrixshen.tweakmyclient.config.Configs;
import top.hendrixshen.tweakmyclient.helper.ListCache;
import top.hendrixshen.tweakmyclient.helper.TargetBlockPositionPrintMode;
import top.hendrixshen.tweakmyclient.util.CustomWindowUtil;
import top.hendrixshen.tweakmyclient.util.InventoryUtil;
import top.hendrixshen.tweakmyclient.util.StringUtil;

public class CallBacks {
    public static boolean getTargetBlockPositionCallback(KeyAction keyAction, IKeybind keybind) {
        Minecraft minecraft = TweakMyClient.getMinecraftClient();
        Entity cameraEntity = minecraft.cameraEntity;
        MultiPlayerGameMode multiPlayerGameMode = minecraft.gameMode;
        if (cameraEntity != null && multiPlayerGameMode != null) {
            HitResult hitResult = cameraEntity.pick(Configs.targetBlockMaxTraceDistance, minecraft.getFrameTime(), false);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
                String str = Configs.targetBlockPositionFormat;
                str = str.replace("{X}", String.format("%d", blockPos.getX()));
                str = str.replace("{Y}", String.format("%d", blockPos.getY()));
                str = str.replace("{Z}", String.format("%d", blockPos.getZ()));
                if (minecraft.player != null) {
                    switch (Configs.targetBlockPositionPrintMode) {
                        case PUBLIC:
                            minecraft.player.chat(str);
                            break;
                        case PRIVATE:
                            //#if MC >= 11600
                            minecraft.player.connection.handleChat(new ClientboundChatPacket(new TextComponent(str), ChatType.CHAT, minecraft.player.getUUID()));
                            //#else
                            //$$ minecraft.player.connection.handleChat(new ClientboundChatPacket(new TextComponent(str), ChatType.CHAT));
                            //#endif
                            break;
                    }
                }
            }
        }
        return true;
    }

    public static boolean memoryCleanerCallback(KeyAction keyAction, IKeybind keybind) {
        class CleanerThread implements Runnable {
            public CleanerThread() {
            }

            @Override
            public void run() {
                TweakMyClient.getLogger().info(String.format("[%s]: Memory cleaner thread started!", TweakMyClientReference.getModName()));
                System.gc();
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException interruptedException) {
                    // ignored
                }
                System.gc();
                TweakMyClient.getLogger().info(String.format("[%s]: Memory cleaner thread finished!", TweakMyClientReference.getModName()));
            }
        }
        Runnable runnable = new CleanerThread();
        Thread gcThread = new Thread(runnable, "MemoryCleaner GC Thread");
        gcThread.setDaemon(true);
        gcThread.start();
        return true;
    }

    public static void debugExperimentalModeCallBack(Option option) {
        if (option != null) {
            TweakMyClientConfigGui.getInstance().reDraw();
        }
    }

    public static void debugModeCallBack(Option option) {
        Configurator.setLevel(TweakMyClientReference.getModId(), Level.toLevel((Configs.debugMode ? "DEBUG" : "INFO")));
        CallBacks.debugExperimentalModeCallBack(option);
    }

    public static boolean syncInventoryCallback(KeyAction keyAction, IKeybind keybind) {
        InventoryUtil.refreshInventory();
        return true;
    }

    public static boolean syncBlocksCallback(KeyAction keyAction, IKeybind keybind) {
        Minecraft minecraftClient = TweakMyClient.getMinecraftClient();
        ClientPacketListener clientPlayNetworkHandler = minecraftClient.getConnection();
        assert minecraftClient.player != null;
        assert clientPlayNetworkHandler != null;
        BlockPos blockPos = minecraftClient.player.blockPosition();
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();
        for (int i = -3; i <= 3; i++) {
            for (int j = -3; j <= 3; j++) {
                for (int k = -3; k <= 3; k++) {
                    clientPlayNetworkHandler.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, new BlockPos(x + i, y + j, z + k), Direction.UP));
                }
            }
        }
        return true;
    }

    public static void disableRenderToastCallback(Option option) {
        if (Configs.disableRenderToast) {
            TweakMyClient.getMinecraftClient().getToasts().clear();
        }
    }

    public static void featureCustomWindowTitleCallback(Option option) {
        if (Configs.featureCustomWindowTitle) {
            CustomWindowUtil.rebuildCache(CustomWindowUtil.TitleType.TITLE);
            CustomWindowUtil.rebuildCache(CustomWindowUtil.TitleType.TITLE_WITH_ACTIVITY);
            CustomWindowUtil.updateTitle();
        } else {
            CustomWindowUtil.reSetTitle();
        }
    }

    public static boolean openConfigGuiCallback(KeyAction keyAction, IKeybind keybind) {
        TweakMyClientConfigGui tweakMyClientConfigGui = TweakMyClientConfigGui.getInstance();
        tweakMyClientConfigGui.setParentGui(TweakMyClient.getMinecraftClient().screen);
        TweakMyClient.getMinecraftClient().setScreen(tweakMyClientConfigGui);
        return true;
    }
}
