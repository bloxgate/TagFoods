package com.bloxgaming.tagfoods

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.tags.SetTag
import net.minecraft.tags.StaticTagHelper
import net.minecraft.world.item.Item
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.Tags
import net.minecraftforge.event.TagsUpdatedEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.IExtensionPoint
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.network.NetworkConstants
import net.minecraftforge.registries.ForgeRegistries
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.MOD_CONTEXT


const val MODID = "tagfoods"

@Mod(MODID)
class TagFoods {

    companion object {
        private lateinit var logger: Logger
        private lateinit var foodTag: Tags.IOptionalNamedTag<Item>
    }

    init {
        logger = LogManager.getLogger()

        MOD_CONTEXT.getKEventBus().register(this)
        MinecraftForge.EVENT_BUS.register(EventBusEvents)

        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest::class.java) {
            IExtensionPoint.DisplayTest(
                { NetworkConstants.IGNORESERVERONLY },
                { _: String?, _: Boolean? -> true }
            )
        }
    }

    @SubscribeEvent
    fun onCommonSetup(event: FMLCommonSetupEvent) {
        foodTag = ItemTags.createOptional(ResourceLocation("forge", "foods"))
    }

    private object EventBusEvents {
        @SuppressWarnings("unchecked") //We know that casting foodTag to Wrapper<Item> is safe here
        //Ideally we'd cast right to OptionalNamedTag<Item>, but I couldn't get the Access Transformer to actually make
        // that public
        fun updateFoodsTag(items: Collection<Item>) {
            logger.info("Updating foods tag")
            val origContents = ((foodTag as StaticTagHelper.Wrapper<Item>).tag as SetTag<Item>).values
            //While not required to be immutable, it appears the Set chosen for contents is an immutable one
            //So we need to make a new copy
            val newContents = mutableSetOf<Item>()

            origContents.forEach {
                newContents += it
            }
            items.filter { !origContents.contains(it) }.forEach {
                if (it.isEdible) {
                    newContents += it
                }
            }

            ((foodTag as StaticTagHelper.Wrapper<Item>).tag as SetTag<Item>).values = newContents
            logger.info("Success! Tagged ${newContents.size} foods.")
            logger.debug("Foods entries: ${foodTag.values}")
        }

        @SubscribeEvent
        fun onServerStartedEvent(event: ServerStartedEvent) {
            updateFoodsTag(ForgeRegistries.ITEMS.values)
        }

        @SubscribeEvent
        fun onTagsUpdatedEvent(event: TagsUpdatedEvent) {
            updateFoodsTag(ForgeRegistries.ITEMS.values)
        }
    }
}