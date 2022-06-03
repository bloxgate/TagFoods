package com.bloxgaming.tagfoods

import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

interface IHolderWithModifiableTag<T : Item> {

    fun addTag(tag: TagKey<T>)

}