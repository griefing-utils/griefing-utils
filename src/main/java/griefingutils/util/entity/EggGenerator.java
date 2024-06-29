package griefingutils.util.entity;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.math.Vec3d;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public enum EggGenerator {
    FIREBALL("fireball", nbt -> {
        NbtList power = new NbtList();
        power.add(NbtDouble.of(0.0));
        power.add(NbtDouble.of(-10000.0));
        power.add(NbtDouble.of(0.0));
        nbt.put("power", power);
    }, (nbt, args) -> {
        NbtByte explosionPower = (NbtByte) args[0];
        nbt.put("ExplosionPower", explosionPower);
    }),

    TNT("tnt", nbt -> nbt.putInt("fuse", 0), (nbt, args) -> {}),

    CREEPER("creeper", nbt -> {
        nbt.putBoolean("ignited", true);
        nbt.putBoolean("Invulnerable", true);
        nbt.putInt("Fuse", 0);
    }, (nbt, args) -> {
        NbtByte explosionRadius = (NbtByte) args[0];
        nbt.put("ExplosionRadius", explosionRadius);
    }),

    WITHER("wither", nbt -> nbt.putBoolean("Invulnerable", true), (nbt, args) -> {
        NbtString customName = (NbtString) args[0];
        nbt.put("CustomName", customName);
    }),

    ARMOR_STAND("armor_stand", nbt -> {
        nbt.putString("id", "minecraft:armor_stand");
        nbt.putBoolean("CustomNameVisible", true);
        nbt.putBoolean("Marker", true);
        nbt.putBoolean("Invisible", true);
    }, (nbt, args) -> {
        NbtString customName = (NbtString) args[0];
        nbt.put("CustomName", customName);
    });

    private final NbtCompound baseNbt;
    private final BiConsumer<NbtCompound, NbtElement[]> variableNbtApplier;

    EggGenerator(
        String id,
        Consumer<NbtCompound> baseNbtApplier,
        BiConsumer<NbtCompound, NbtElement[]> variableNbtApplier
    ) {
        NbtCompound baseNbt = new NbtCompound();
        baseNbt.putString("id", id);
        baseNbtApplier.accept(baseNbt);
        this.baseNbt = baseNbt;
        this.variableNbtApplier = variableNbtApplier;
    }

    public ItemStack get(Item item, Vec3d pos, NbtElement... args) {
        ItemStack stack = item.getDefaultStack();
        NbtCompound entityData = baseNbt.copy();

        NbtList pos2 = new NbtList();
        pos2.add(NbtDouble.of(pos.x));
        pos2.add(NbtDouble.of(pos.y));
        pos2.add(NbtDouble.of(pos.z));

        entityData.put("Pos", pos2);

        variableNbtApplier.accept(entityData, args);
        stack.set(DataComponentTypes.ENTITY_DATA, NbtComponent.of(entityData));
        return stack;
    }
}
