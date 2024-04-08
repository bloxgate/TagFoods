package com.bloxgaming.tagfoods.mixins;

import com.bloxgaming.tagfoods.IHolderSetNamedWithModifiableContents;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(HolderSet.Named.class)
public class MixinHolderSet<T extends Item> implements IHolderSetNamedWithModifiableContents<T> {

    @Shadow
    private List<Holder<T>> contents = List.of();

    @Override
    public void add(@NotNull Holder.Reference<T> holder) {
        if (!contents.contains(holder)) {
            ArrayList<Holder<T>> newContents = new ArrayList<>(contents);
            newContents.add(holder);
            contents = newContents;
        }
    }
}
