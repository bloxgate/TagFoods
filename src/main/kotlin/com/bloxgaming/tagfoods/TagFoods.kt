package com.bloxgaming.tagfoods

import net.minecraft.item.Item
import net.minecraft.tags.ItemTags
import net.minecraft.tags.Tag
import net.minecraft.tags.TagRegistry
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.Tags
import net.minecraftforge.event.TagsUpdatedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ExtensionPoint
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.server.FMLServerStartedEvent
import net.minecraftforge.fml.network.FMLNetworkConstants
import net.minecraftforge.registries.ForgeRegistries
import org.apache.commons.lang3.tuple.Pair
import org.apache.logging.log4j.LogManager
import thedarkcolour.kotlinforforge.forge.MOD_CONTEXT
import java.util.function.BiPredicate
import java.util.function.Supplier


const val MODID = "tagfoods"

@Mod(MODID)
class TagFoods {

    init {
        MOD_CONTEXT.getKEventBus().register(this)
        MinecraftForge.EVENT_BUS.register(this)

        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST) { Pair.of(Supplier { FMLNetworkConstants.IGNORESERVERONLY }, BiPredicate { _: String?, _: Boolean? -> true }) }
    }

    lateinit var foodTag: Tags.IOptionalNamedTag<Item>
    val logger = LogManager.getLogger()

    @SubscribeEvent
    fun onCommonSetup(event: FMLCommonSetupEvent) {
        foodTag = ItemTags.createOptional(ResourceLocation("forge", "foods"))
    }

    fun updateFoodsTag(items: Collection<Item>) {
        logger.info("Updating foods tag")
        val origContents = ((foodTag as TagRegistry.NamedTag<Item>).tag as Tag<Item>).contents
        //While not required to be immutable, it appears the Set chosen for contents is an immutable one
        //So we need to make a new copy
        val newContents = mutableSetOf<Item>()

        origContents.forEach {
            newContents += it
        }
        items.forEach {
            if (it.isFood) {
                newContents += it
            }
        }

        ((foodTag as TagRegistry.NamedTag<Item>).tag as Tag<Item>).contents = newContents
        logger.info("Success! Tagged ${newContents.size} foods.")
        logger.debug("Foods entries: ${foodTag.allElements}")
    }

    @SubscribeEvent
    fun onServerStartedEvent(event: FMLServerStartedEvent) {
        updateFoodsTag(ForgeRegistries.ITEMS.values)
    }

    @SubscribeEvent
    fun onTagsUpdatedEvent(event: TagsUpdatedEvent) {
        updateFoodsTag(ForgeRegistries.ITEMS.values)
    }
}