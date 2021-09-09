package com.bloxgaming.tagfoods

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.*
import net.minecraft.world.item.Item
import net.minecraftforge.common.ForgeTagHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.Tags
import net.minecraftforge.event.TagsUpdatedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ExtensionPoint
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.network.FMLNetworkConstants
import net.minecraftforge.fmlserverevents.FMLServerStartedEvent
import net.minecraftforge.registries.ForgeRegistries
import org.apache.commons.lang3.tuple.Pair
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.MOD_CONTEXT
import java.util.function.BiPredicate
import java.util.function.Supplier


const val MODID = "tagfoods"

@Mod(MODID)
class TagFoods {

    init {
        MOD_CONTEXT.getKEventBus().register(this)
        MinecraftForge.EVENT_BUS.register(this)

        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST) {
            Pair.of(Supplier { FMLNetworkConstants.IGNORESERVERONLY },
                BiPredicate { _: String?, _: Boolean? -> true })
        }
    }

    private lateinit var foodTag: Tags.IOptionalNamedTag<Item>
    private val logger: Logger = LogManager.getLogger()

    @SubscribeEvent
    fun onCommonSetup(event: FMLCommonSetupEvent) {
        foodTag = ItemTags.createOptional(ResourceLocation("forge", "foods"))
    }

    fun updateFoodsTag(items: Collection<Item>) {
        logger.info("Updating foods tag")
        //TODO: Can we make this tag reference static and just re-use it?
        val origContents = (ForgeTagHandler.makeWrapperTag(
            ForgeRegistries.ITEMS,
            ResourceLocation("forge", "foods")
        ) as SetTag<Item>).values
        //While not required to be immutable, it appears the Set chosen for contents is an immutable one
        //So we need to make a new copy
        val newContents = mutableSetOf<Item>()

        origContents.forEach {
            newContents += it
        }
        items.forEach {
            if (it.isEdible) {
                newContents += it
            }
        }

        (ForgeTagHandler.makeWrapperTag(
            ForgeRegistries.ITEMS,
            ResourceLocation("forge", "foods")
        ) as SetTag<Item>).values = newContents
        logger.info("Success! Tagged ${newContents.size} foods.")
        logger.debug("Foods entries: ${foodTag.values}")
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