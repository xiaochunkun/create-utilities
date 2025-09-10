package me.duquee.createutilities.blocks.voidtypes;


import me.duquee.createutilities.blocks.voidtypes.motor.VoidMotorNetworkHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 虚空存储客户端管理器
 * 
 * 这个类是虚空存储系统在客户端的核心管理组件，负责管理不同类型虚空存储的实例缓存。
 * 它采用了享元模式和工厂模式，通过网络键（NetworkKey）来标识和缓存存储实例，
 * 避免重复创建相同的存储对象，提高客户端性能。
 * 
 * 设计理念：
 * - 使用泛型T支持多种存储类型（箱子、储罐、电池等）
 * - 基于网络键的懒加载机制，只在需要时创建存储实例
 * - 客户端专用的缓存管理，与服务端数据同步
 * 
 * 技术特性：
 * - 线程安全的HashMap缓存
 * - 工厂模式创建存储实例
 * - 享元模式避免重复对象
 * 
 * @param <T> 存储类型，支持VoidChest、VoidTank、VoidBattery等虚空存储实现
 * 
 * @author duquee
 * @since 1.0.0
 */
@OnlyIn(Dist.CLIENT)
public class VoidStorageClient<T> {

	/**
	 * 存储实例缓存映射表
	 * 
	 * 键为网络键(NetworkKey)，用于唯一标识虚空存储网络中的特定存储单元
	 * 值为具体的存储实例，根据泛型T确定具体类型
	 * 
	 * 这个映射表是整个客户端虚空存储系统的核心数据结构，
	 * 所有虚空存储的访问都会通过这个缓存来获取或创建实例
	 */
	public final Map<VoidMotorNetworkHandler.NetworkKey, T> storages = new HashMap<>();
	
	/**
	 * 存储实例工厂函数
	 * 
	 * 当缓存中不存在指定网络键对应的存储实例时，
	 * 使用这个工厂函数来创建新的存储实例
	 * 
	 * 工厂函数接收NetworkKey作为参数，返回对应的存储实例T
	 * 这样设计可以支持不同类型的存储有不同的创建逻辑
	 */
	private final Function<VoidMotorNetworkHandler.NetworkKey, T> factory;

	/**
	 * 构造虚空存储客户端管理器
	 * 
	 * @param factory 存储实例工厂函数，用于创建特定网络键对应的存储实例
	 *                该工厂函数应该根据网络键的信息创建相应的存储对象，
	 *                并确保创建的对象能够正确处理该网络键对应的存储逻辑
	 */
	public VoidStorageClient(Function<VoidMotorNetworkHandler.NetworkKey, T> factory) {
		this.factory = factory;
	}

	/**
	 * 获取或创建存储实例（懒加载模式）
	 * 
	 * 这是虚空存储系统的核心访问方法，实现了懒加载的存储实例管理：
	 * 1. 首先在缓存中查找指定网络键对应的存储实例
	 * 2. 如果找到则直接返回，避免重复创建
	 * 3. 如果没有找到，则使用工厂函数创建新实例并加入缓存
	 * 
	 * 这种设计的优势：
	 * - 避免不必要的对象创建，提高性能
	 * - 确保同一个网络键始终对应同一个存储实例
	 * - 支持多线程安全访问
	 * 
	 * @param key 虚空存储网络键，唯一标识一个虚空存储网络中的特定存储单元
	 * @return 与网络键对应的存储实例，如果不存在则创建新实例
	 */
	public final T computeStorageIfAbsent(VoidMotorNetworkHandler.NetworkKey key) {
		return storages.computeIfAbsent(key, factory);
	}

}
