package com.disconnectcmd;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.KickCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.Collection;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(DisconnectCMD.MODID)
public class DisconnectCMD {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "disconnectcmd";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public DisconnectCMD() {
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public void onRegisterLocalizeCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(Config.DISCONNECT_CMD_NAME.get())
                .requires(source -> source.hasPermission(3))
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(context ->
                            kickPlayers(context.getSource(), EntityArgument.getPlayers(context, "targets"), Component.translatable("multiplayer.disconnect.kicked"))
                        )
                        .then(Commands.argument("message", MessageArgument.message())
                                .executes(context ->
                                    kickPlayers(context.getSource(), EntityArgument.getPlayers(context, "targets"), MessageArgument.getMessage(context, "message"))
                                )
                        )
                )
        );
    }

    public static int kickPlayers(CommandSourceStack source, Collection<ServerPlayer> players, Component component) {
        for(ServerPlayer player : players) {
            player.connection.disconnect(component);
            source.sendSuccess(() -> Component.translatable("commands.kick.success", new Object[]{player.getDisplayName(), component}), true);
        }

        return players.size();
    }
}
