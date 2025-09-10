package me.duquee.createutilities.blocks.voidtypes.motor;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import me.duquee.createutilities.CreateUtilities;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.levelWrappers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;
import java.util.*;

/**
 * 虚空马达网络处理器
 * 
 * 管理虚空马达之间的网络连接，实现跨维度的动力同步功能。
 * 虚空马达网络允许在不同维度之间传输旋转动力，突破了物理距离的限制。
 * 
 * 核心功能：
 * - 管理虚空马达网络的连接关系
 * - 维护世界到网络的映射关系
 * - 处理网络的加入和离开操作
 * - 支持跨维度的动力传输
 * 
 * 网络架构：
 * - 每个世界维护独立的网络映射表
 * - 网络键由所有者和双频率组成，确保唯一性和权限控制
 * - 相同网络键的虚空马达自动组网，实现动力共享
 * 
 * 设计理念：
 * - 使用IdentityHashMap避免世界对象比较问题
 * - 自动清理空网络，节省内存
 * - 提供完整的生命周期管理
 */
public class VoidMotorNetworkHandler {

	/**
	 * 全局网络连接映射表
	 * 
	 * 这是虚空马达网络的核心数据结构，维护所有世界中的网络连接关系。
	 * 
	 * 结构层次：
	 * 1. 外层Map：世界 -> 该世界中的所有网络
	 * 2. 中层Map：网络键 -> 该网络中的所有方块位置
	 * 3. 内层Set：存储网络中所有虚空马达的位置
	 * 
	 * 使用IdentityHashMap确保世界对象的唯一性比较，
	 * 避免因为世界对象重新创建导致的映射错误。
	 */
	static final Map<LevelAccessor, Map<NetworkKey, Set<BlockPos>>> connections =
			new IdentityHashMap<>();

	/**
	 * 获取虚空马达所属的网络
	 * 
	 * 根据虚空马达的网络键查找其所属网络。如果网络不存在，
	 * 则自动创建一个新的空网络。这确保了每个虚空马达都有
	 * 对应的网络空间，即使是刚放置的马达也能正常工作。
	 * 
	 * @param world 虚空马达所在的世界
	 * @param actor 虚空马达的连接行为组件
	 * @return 该虚空马达所属网络中的所有方块位置集合
	 */
	public Set<BlockPos> getNetworkOf(LevelAccessor world, VoidMotorLinkBehaviour actor) {
		Map<NetworkKey, Set<BlockPos>> networksInWorld = networksIn(world);
		NetworkKey key = actor.getNetworkKey();
		if (!networksInWorld.containsKey(key))
			networksInWorld.put(key, new LinkedHashSet<>());
		return networksInWorld.get(key);
	}

	/**
	 * 获取指定世界中的所有虚空马达网络
	 * 
	 * 返回该世界中所有网络的映射表。如果世界尚未初始化网络空间，
	 * 会记录警告并返回空的映射表以避免程序崩溃。
	 * 
	 * @param world 目标世界
	 * @return 该世界中的网络映射表（网络键 -> 方块位置集合）
	 */
	public Map<NetworkKey, Set<BlockPos>> networksIn(LevelAccessor world) {
		if (!connections.containsKey(world)) {
			Create.LOGGER.warn("Tried to Access unprepared network space of " + WorldHelper.getDimensionID(world));
			return new HashMap<>();
		}
		return connections.get(world);
	}

	/**
	 * 世界加载时的网络初始化
	 * 
	 * 当世界被加载时调用，为该世界创建独立的网络空间。
	 * 这确保了每个世界都有自己的虚空马达网络管理系统。
	 * 
	 * @param world 被加载的世界
	 */
	public void onLoadWorld(LevelAccessor world) {
		connections.put(world, new HashMap<>());
		Create.LOGGER.debug("Prepared Void Motor Network Space for " + WorldHelper.getDimensionID(world));
	}

	/**
	 * 世界卸载时的网络清理
	 * 
	 * 当世界被卸载时调用，清理该世界的所有网络数据。
	 * 这避免了内存泄漏，确保服务器的长期稳定运行。
	 * 
	 * @param world 被卸载的世界
	 */
	public void onUnloadWorld(LevelAccessor world) {
		connections.remove(world);
		Create.LOGGER.debug("Removed Void Motor Network Space for " + WorldHelper.getDimensionID(world));
	}

	/**
	 * 将虚空马达添加到网络
	 * 
	 * 当虚空马达建立连接时调用，将其位置添加到对应的网络中。
	 * 同时触发马达的网络连接回调，让马达知道它已成功加入网络。
	 * 
	 * @param world 虚空马达所在的世界
	 * @param actor 要加入网络的虚空马达连接行为
	 */
	public void addToNetwork(LevelAccessor world, VoidMotorLinkBehaviour actor) {
		getNetworkOf(world, actor).add(actor.getPos());
		if (actor.blockEntity instanceof VoidMotorTileEntity voidMotor) voidMotor.onConnectToVoidNetwork();
	}

	/**
	 * 从网络中移除虚空马达
	 * 
	 * 当虚空马达断开连接或被破坏时调用，将其从网络中移除。
	 * 先触发马达的断开回调，然后从网络中删除位置信息。
	 * 如果网络变为空，则自动清理整个网络以节省内存。
	 * 
	 * @param world 虚空马达所在的世界
	 * @param actor 要从网络中移除的虚空马达连接行为
	 */
	public void removeFromNetwork(LevelAccessor world, VoidMotorLinkBehaviour actor) {
		if (actor.blockEntity instanceof VoidMotorTileEntity voidMotor) voidMotor.onDisconnectFromVoidNetwork();
		Set<BlockPos> network = getNetworkOf(world, actor);
		network.remove(actor.getPos());
		if (network.isEmpty()) networksIn(world).remove(actor.getNetworkKey());
	}

	/**
	 * 虚空网络键类
	 * 
	 * 这是虚空网络系统的核心标识符，由所有者信息和双频率组成。
	 * 网络键的作用类似于网络地址，只有拥有相同网络键的虚空马达
	 * 才能组成同一个网络，实现动力共享。
	 * 
	 * 组成部分：
	 * - owner: 网络所有者，用于权限控制（可为null表示公共网络）
	 * - frequencies: 双频率对，类似于无线电的双频道设置
	 * 
	 * 安全特性：
	 * - 所有者控制：只有所有者可以修改网络配置
	 * - 频率隔离：不同频率组合形成独立的网络
	 * - 序列化支持：可保存和网络传输
	 */
	public static class NetworkKey {

		/**
		 * 网络所有者的游戏档案
		 * 
		 * 用于权限控制，只有所有者可以修改网络配置。
		 * 如果为null，则表示这是一个公共网络，任何玩家都可以使用。
		 */
		@Nullable
		public final GameProfile owner;
		
		/**
		 * 双频率对
		 * 
		 * 两个频率标识符的组合，类似于无线电的双频道设置。
		 * 使用Couple类型确保频率的有序存储和比较。
		 */
		public final Couple<Frequency> frequencies;

		/**
		 * 构造网络键
		 * 
		 * @param owner 网络所有者（可为null表示公共网络）
		 * @param frequencyFirst 第一个频率标识符
		 * @param frequencySecond 第二个频率标识符
		 */
		public NetworkKey(@Nullable GameProfile owner, Frequency frequencyFirst, Frequency frequencySecond) {
			this.owner = owner;
			this.frequencies = Couple.create(frequencyFirst, frequencySecond);
		}

		/**
		 * 将网络键写入网络缓冲区
		 * 
		 * 用于客户端-服务器之间的网络通信，按顺序写入：
		 * 1. 第一个频率的物品堆栈
		 * 2. 第二个频率的物品堆栈
		 * 3. 是否有所有者的布尔标记
		 * 4. 所有者信息（如果存在）
		 * 
		 * @param buffer 目标网络缓冲区
		 */
		public void writeToBuffer(FriendlyByteBuf buffer) {
			buffer.writeItem(frequencies.get(true).getStack());
			buffer.writeItem(frequencies.get(false).getStack());
			buffer.writeBoolean(owner != null);
			if (owner != null) buffer.writeGameProfile(owner);
		}

		/**
		 * 从网络缓冲区读取网络键
		 * 
		 * 与writeToBuffer()对应的反序列化方法，按顺序读取：
		 * 1. 第一个频率的物品堆栈
		 * 2. 第二个频率的物品堆栈
		 * 3. 是否有所有者的布尔标记
		 * 4. 所有者信息（如果标记为true）
		 * 
		 * @param buffer 源网络缓冲区
		 * @return 重构的网络键对象
		 */
		public static NetworkKey fromBuffer(FriendlyByteBuf buffer) {
			ItemStack frequencyFirst = buffer.readItem();
			ItemStack frequencyLast = buffer.readItem();
			GameProfile owner = null;
			if (buffer.readBoolean()) owner = buffer.readGameProfile();
			return new NetworkKey(owner, Frequency.of(frequencyFirst), Frequency.of(frequencyLast));
		}

		/**
		 * 计算网络键的哈希码
		 * 
		 * 基于所有者和频率组合计算哈希值，用于在HashMap中快速查找。
		 * 
		 * @return 网络键的哈希码
		 */
		@Override
		public int hashCode() {
			return Objects.hash(owner, frequencies);
		}

		/**
		 * 比较网络键是否相等
		 * 
		 * 两个网络键相等的条件：
		 * 1. 所有者相同（都为null或GameProfile相等）
		 * 2. 双频率完全相同
		 * 
		 * @param obj 要比较的对象
		 * @return 是否相等
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null || getClass() != obj.getClass())
				return false;
			NetworkKey other = (NetworkKey) obj;
			return Objects.equals(owner, other.owner) && frequencies.equals(other.frequencies);
		}

		/**
		 * 将网络键序列化为NBT标签
		 * 
		 * 用于数据持久化，将网络键保存到磁盘。包含：
		 * - Owner: 所有者信息（如果存在）
		 * - FrequencyFirst: 第一个频率的物品数据
		 * - FrequencyLast: 第二个频率的物品数据
		 * 
		 * @return 包含网络键数据的NBT标签
		 */
		public CompoundTag serialize() {
			CompoundTag tag = new CompoundTag();
			if (owner != null) {
				CompoundTag tag_ = new CompoundTag();
				NbtUtils.writeGameProfile(tag_, owner);
				tag.put("Owner", tag_);
			}
			tag.put("FrequencyFirst", frequencies.get(true).getStack().save(new CompoundTag()));
			tag.put("FrequencyLast", frequencies.get(false).getStack().save(new CompoundTag()));
			return tag;
		}

		/**
		 * 从NBT标签反序列化网络键
		 * 
		 * 与serialize()对应的反序列化方法，从磁盘加载网络键数据。
		 * 按预定义格式读取所有者和双频率信息。
		 * 
		 * @param tag 包含网络键数据的NBT标签
		 * @return 重构的网络键对象
		 */
		public static NetworkKey deserialize(CompoundTag tag) {
			Frequency frequencyFirst = Frequency.of(ItemStack.of(tag.getCompound("FrequencyFirst")));
			Frequency frequencyLast = Frequency.of(ItemStack.of(tag.getCompound("FrequencyLast")));
			GameProfile owner = tag.contains("Owner", 10) ? NbtUtils.readGameProfile(tag.getCompound("Owner")) : null;
			return new NetworkKey(owner, frequencyFirst, frequencyLast);
		}

		/**
		 * 将网络键转换为字符串表示
		 * 
		 * 通过序列化为NBT然后转字符串的方式，
		 * 生成包含完整网络键信息的字符串表示。
		 * 
		 * @return 网络键的字符串表示
		 */
		@Override
		public String toString() {
			return serialize().toString();
		}

		/**
		 * 从字符串解析网络键
		 * 
		 * 与toString()对应的解析方法，从字符串重构网络键对象。
		 * 包含错误处理，如果解析失败会记录错误并返回null。
		 * 
		 * @param json 网络键的字符串表示
		 * @return 解析得到的网络键对象，失败时返回null
		 */
		public static NetworkKey fromString(String json) {

			CompoundTag tag;
			try {
				tag = TagParser.parseTag(json);
			} catch (CommandSyntaxException e) {
				CreateUtilities.LOGGER.error("Tried to load invalid NetworkKey '" + json + "'");
				return null;
			}

			return deserialize(tag);
		}

	}

}
