package com.plk4.mixin.client;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import net.minecraft.client.MinecraftClient;
import java.util.*;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	ArrayList<String> spectators = new ArrayList<String>();
	@Inject(at = @At("HEAD"), method = "onPlayerList")
	private void onPlayerList(PlayerListS2CPacket packet, CallbackInfo info) {
		for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
			for (PlayerListS2CPacket.Action action : packet.getActions()) {
				if (action == PlayerListS2CPacket.Action.UPDATE_GAME_MODE) {
					System.out.println(packet.toString());
					if (entry.gameMode() == GameMode.SPECTATOR) {
						if (MinecraftClient.getInstance().player == null) {
							System.out.println("Player is null");
							return;
						}
						UUID uuid = entry.profileId();
						ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();

						if (networkHandler == null) {
							System.out.println("Network handler is null");
							return;
						}
						
						Optional<PlayerListEntry> playerListEntryOptional = Optional.ofNullable(networkHandler.getPlayerListEntry(uuid));
						playerListEntryOptional.ifPresent(playerListEntry -> {
							String username = playerListEntry.getProfile().getName();
							if (spectators.contains(username)) {
								return;
							}
							System.out.println("Player " + username + " is now in spectator mode");
							MinecraftClient.getInstance().player.sendMessage(Text.of("§5[§9Detective§5] §fPlayer " + username + " is now in spectator mode"), false);
							spectators.add(username);
						});
					} else if (entry.gameMode() == GameMode.SURVIVAL || entry.gameMode() == GameMode.ADVENTURE || entry.gameMode() == GameMode.CREATIVE) {
						UUID uuid = entry.profileId();
						ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
						if (networkHandler == null) {
							System.out.println("Network handler is null");
							return;
						}
						Optional<PlayerListEntry> playerListEntryOptional = Optional.ofNullable(networkHandler.getPlayerListEntry(uuid));
						playerListEntryOptional.ifPresent(playerListEntry -> {
							String username = playerListEntry.getProfile().getName();
							if (!spectators.contains(username)) {
								return;
							}
							System.out.println("Player " + username + " is no longer in spectator mode");
							MinecraftClient.getInstance().player.sendMessage(Text.of("§5[§9Detective§5] §fPlayer " + username + " is no longer in spectator mode"), false);
							spectators.remove(username);
						});
					}
				}
			}
		}

	}
}