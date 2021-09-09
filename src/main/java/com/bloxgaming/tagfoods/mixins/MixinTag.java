package com.bloxgaming.tagfoods.mixins;

import com.google.common.collect.ImmutableList;
import net.minecraft.tags.SetTag;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;

@Mixin(SetTag.class)
public class MixinTag<T extends Item> {
    @Shadow
    private ImmutableList<T> valuesList;

    @Shadow
    public Set<T> values;

    @Inject(at = @At("HEAD"), method = "getValues()Ljava/util/List;")
    private void getAllElements(CallbackInfoReturnable<List<T>> callback) {
        if (values.size() != valuesList.size()) {
            valuesList = ImmutableList.copyOf(values);
        }
    }
}
