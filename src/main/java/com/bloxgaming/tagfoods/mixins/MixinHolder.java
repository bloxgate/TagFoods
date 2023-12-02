package com.bloxgaming.tagfoods.mixins;

import com.bloxgaming.tagfoods.IHolderWithModifiableTag;
import com.google.common.collect.Sets;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashSet;
import java.util.Set;

@Mixin(Holder.Reference.class)
public class MixinHolder<T extends Item> implements IHolderWithModifiableTag<T> {

    @Shadow
    public Set<TagKey<T>> tags;

    public void addTag(@NotNull TagKey<T> tag) {
        //Tags will be immutable at runtime
        HashSet<TagKey<T>> newTags = Sets.newHashSet(tags);
        newTags.add(tag);
        tags = newTags; //So we replace it instead
    }
}
