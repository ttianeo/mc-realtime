package com.tianeo.realtime.realtime;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalTime;

public class Realtime implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Realtime");
    public int SetTimeOfDayCount = 0;

    @Override
    public void onInitialize() {
        // Register GameRules Event
        ServerWorldEvents.LOAD.register(this::DisableDoDaylightCycle);
        ServerTickEvents.START_SERVER_TICK.register(this::SetTimeToRealtime);
        LOGGER.info("Successfully loaded Realtime");
    }

    // disable doDaylightCycle and init the value of timeOfDay
    public void DisableDoDaylightCycle(MinecraftServer server, ServerWorld _w) {
        Iterable<ServerWorld> worlds = server.getWorlds();
        worlds.forEach(world -> {
            GameRules rules = world.getGameRules();
            rules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
        });
        SetTimeOfDayCount = 59;
        SetTimeToRealtime(server);
    }

    // set game time to real time (every 60 ticks)
    public void SetTimeToRealtime(MinecraftServer server) {
        if (++SetTimeOfDayCount % 60 == 0) {
            try {
                ServerWorld world = server.getOverworld().toServerWorld();
                if (world != null) {
                    LocalTime systemTime = LocalTime.now(); // system time
                    // 6:00-0tick,18:00-12000tick
                    // seconds passed from 6:00
                    long secondsFromSixAM = systemTime.getSecond() + systemTime.getMinute() * 60 + (systemTime.getHour() - 6) * 3600;
                    // transfer to ticks
                    long ticks = (Math.round(secondsFromSixAM * 24000 / 86400.0) + 24000) % 24000;

                    world.setTimeOfDay(ticks); // setTimeOfDay
                } else {
                    throw new Error("Failed to get overworld pointer");
                }
            } catch (Exception e) {
                LOGGER.error("Failed to set time of day", e);
            }


            SetTimeOfDayCount = 0;
        }

    }
}
