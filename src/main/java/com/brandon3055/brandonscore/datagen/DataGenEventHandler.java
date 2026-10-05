package com.brandon3055.brandonscore.datagen;

import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Created by brandon3055 on 26/2/20.
 */
@EventBusSubscriber
public class DataGenEventHandler {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        DataGenerator gen = event.getGenerator();

        gen.addProvider(true, new LangGenerator(gen));
    }
}
