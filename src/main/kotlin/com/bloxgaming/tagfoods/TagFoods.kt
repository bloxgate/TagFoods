package com.bloxgaming.tagfoods

import com.google.common.collect.ImmutableList
import net.minecraft.core.Holder
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraftforge.common.MinecraftForge
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
        private lateinit var foodTag: TagKey<Item>
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
        foodTag = ItemTags.create(ResourceLocation("forge", "foods"))
    }

    private object EventBusEvents {
        @SuppressWarnings("unchecked") //Our cast is safe, since we're applying a mixin to the base Holder.Reference class
        fun updateFoodsTag(items: Collection<Item>) {
            logger.info("Updating foods tag")
            val origContents = ForgeRegistries.ITEMS.tags()?.getTag(foodTag)?.stream()?.toList() ?: throw Exception("Unable to get food tag")
            //The list returned by toList is an unmodifiable collection, so we have to copy it
            val newContents = mutableSetOf<Item>()

            origContents.forEach {
                newContents += it
            }
            items.filter { !origContents.contains(it) }.forEach {
                if (it.isEdible) {
                    newContents += it
                }
            }

            //We must make sure that we add this tag to each item, so that holder.is(...) checks are true
            newContents.forEach {
                (it.builtInRegistryHolder() as IHolderWithModifiableTag<Item>).addTag(foodTag)
            }

            //We can't access-transformer the ForgeRegistryTag class, so we need to use reflection here
            val tag = ForgeRegistries.ITEMS.tags()?.getTag(foodTag)!!
            val forgeRegistryTag = Class.forName("net.minecraftforge.registries.ForgeRegistryTag")
            val contents = forgeRegistryTag.getDeclaredField("contents")
            if (!contents.canAccess(tag)) {
                val accessible = contents.trySetAccessible()
                if (!accessible) {
                    logger.warn("Unable to change access modifier on ForgeRegistryTag contents")
                    return
                }
            }
            if (!forgeRegistryTag.isInstance(tag)) {
                logger.warn("Food tag is not a ForgeRegistryTag, update aborted.")
                return
            }
            contents.set(tag, newContents.toList())

            val holderSet = forgeRegistryTag.getDeclaredField("holderSet")
            if (!holderSet.canAccess(tag)) {
                val accessible = holderSet.trySetAccessible()
                if (!accessible) {
                    logger.warn("Unable to change access modifier on ForgeRegistryTag holderSet")
                    return
                }
            }
            val actualHolderSet = holderSet.get(tag) as? IHolderSetNamedWithModifiableContents<Item>
            if (actualHolderSet == null) {
                logger.warn("Could not get holderSet")
                return
            }
            newContents.forEach {
                actualHolderSet.add(it.builtInRegistryHolder())
            }

            val replacedContents = tag.stream().toList()
            logger.info("Success! Tagged ${replacedContents.size} foods.")
            logger.debug("Foods entries: $replacedContents")
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