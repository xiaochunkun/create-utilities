package me.duquee.createutilities.blocks.voidtypes;

import me.duquee.createutilities.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 虚空存储数据基类
 * 
 * 这是所有虚空存储类型（箱子、储罐、电池等）的抽象基类。
 * 负责管理虚空网络中的存储实例，提供持久化和网络键映射功能。
 * 
 * 核心功能：
 * - 通过网络键映射存储实例
 * - 提供数据持久化和反序列化
 * - 管理存储实例的生命周期
 * 
 * @param <T> 存储类型的泛型参数（如FluidTank、ItemStackHandler等）
 */
public abstract class VoidStorageData<T> extends SavedData {

    /**
     * 存储实例映射表
     * 键为网络标识键，值为对应的存储实例
     */
    protected final Map<NetworkKey, T> storages = new HashMap<>();

    /**
     * 计算并获取存储实例，如果不存在则创建
     * 
     * 这是虚空存储系统的核心方法，确保每个网络键都有对应的存储实例。
     * 当多个虚空方块使用相同的网络键时，它们会共享同一个存储实例。
     * 
     * @param key 网络标识键
     * @param function 创建新存储实例的函数
     * @return 对应网络键的存储实例
     */
    public T computeStorageIfAbsent(NetworkKey key, Function<NetworkKey, T> function) {
        return storages.computeIfAbsent(key, function);
    }

    /**
     * 保存存储数据到NBT标签
     * 
     * 遍历所有存储实例，将非空的实例序列化到NBT中。
     * 这确保了服务器重启后虚空存储的内容不会丢失。
     * 
     * @param tag 要写入数据的NBT标签
     * @param isEmpty 判断存储是否为空的函数
     * @param serializeNBT 将存储序列化为NBT的函数
     * @return 包含所有存储数据的NBT标签
     */
    public @NotNull CompoundTag save(@NotNull CompoundTag tag,
                                     Function<T, Boolean> isEmpty,
                                     Function<T, CompoundTag> serializeNBT) {
        storages.forEach( (key, inventory) -> {
            if (!isEmpty.apply(inventory))
                tag.put(key.toString(), serializeNBT.apply(inventory));
        } );
        return tag;
    }

    /**
     * 从NBT标签加载存储数据
     * 
     * 这是一个静态工厂方法，用于从保存的NBT数据中重建虚空存储数据。
     * 遍历NBT中的所有条目，为每个网络键创建对应的存储实例并反序列化数据。
     * 
     * @param <T> 存储类型参数
     * @param <S> 存储数据类型参数
     * @param tag 包含存储数据的NBT标签
     * @param storageDataSupplier 创建存储数据实例的供应商
     * @param storageSupplier 创建存储实例的供应商
     * @param deserializeNBT 从NBT反序列化存储的消费者
     * @return 加载了数据的存储数据实例
     */
    public static <T, S extends VoidStorageData<T>> S load(CompoundTag tag,
                                                           Supplier<S> storageDataSupplier,
                                                           Function<NetworkKey, T> storageSupplier,
                                                           BiConsumer<T, CompoundTag> deserializeNBT) {
        S data = storageDataSupplier.get();
        tag.getAllKeys().forEach(k -> {
            NetworkKey key = NetworkKey.fromString(k);
            T inventory = storageSupplier.apply(key);
            deserializeNBT.accept(inventory, tag.getCompound(k));
            data.storages.put(key, inventory);
        });
        return data;
    }

}