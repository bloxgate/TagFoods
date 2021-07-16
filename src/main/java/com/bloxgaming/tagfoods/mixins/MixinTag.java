package com.bloxgaming.tagfoods.mixins;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;

@Mixin(Tag.class)
public class MixinTag<T extends Item> {
    @Shadow
    private ImmutableList<T> immutableContents;

    @Shadow
    public Set<T> contents;

    @Inject(at = @At("HEAD"), method = "getAllElements()Ljava/util/List;")
    private void getAllElements(CallbackInfoReturnable<List<T>> callback) {
        if (contents.size() != immutableContents.size()) {
            immutableContents = ImmutableList.copyOf(contents);
        }
    }
}
