package com.bloxgaming.tagfoods

import net.minecraft.core.Holder
import net.minecraft.world.item.Item

interface IHolderSetNamedWithModifiableContents<T : Item> {

    fun add(holder: Holder.Reference<T>)

}