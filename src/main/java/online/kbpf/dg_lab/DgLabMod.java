package online.kbpf.dg_lab;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import online.kbpf.dg_lab.client.DgLabClient;
import online.kbpf.dg_lab.client.command.DgLabClientCommands;

@Mod(DgLabMod.MOD_ID)
public final class DgLabMod {

    public static final String MOD_ID = "dg_lab";

    public DgLabMod() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientSide::new);
    }

    private static final class ClientSide {

        ClientSide() {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);

            MinecraftForge.EVENT_BUS.register(this);
            MinecraftForge.EVENT_BUS.addListener(this::onRegisterClientCommands);
        }

        private void onClientSetup(final FMLClientSetupEvent event) {
            event.enqueueWork(DgLabClient::initialize);
        }

        private void onRegisterClientCommands(final RegisterClientCommandsEvent event) {
            DgLabClientCommands.register(event);
        }

        @SubscribeEvent
        public void onClientTick(final TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                DgLabClient.handleClientTick();
            }
        }

        @SubscribeEvent
        public void onRenderOverlay(final RenderGameOverlayEvent.Text event) {
            DgLabClient.renderHud(event.getMatrixStack());
        }
    }
}
