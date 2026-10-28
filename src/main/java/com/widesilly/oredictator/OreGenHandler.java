package com.widesilly.oredictator;

import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class OreGenHandler {

    @SubscribeEvent
    public static void onGenerateMinable(OreGenEvent.GenerateMinable event) {
        // Deny all standard/default minable generation passes globally
        event.setResult(Event.Result.DENY);
    }
}