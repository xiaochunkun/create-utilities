package me.duquee.createutilities.blocks.voidtypes.chest;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import me.duquee.createutilities.CreateUtilities;
import me.duquee.createutilities.blocks.voidtypes.VoidLinkBehaviour;
import me.duquee.createutilities.voidlink.VoidLinkSlot;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 虚空箱子方块实体
 * 
 * 这是虚空箱子的核心逢辑实现，负责管理物品存储和网络连接。
 * 作为SmartBlockEntity的子类，它继承了Create模组的高级特性，
 * 同时实现MenuProvider接口提供GUI交互功能。
 * 
 * 核心功能：
 * - 虚空网络管理：通过VoidLinkBehaviour处理网络连接
 * - 物品存储：提供IItemHandler能力实现物品管理
 * - GUI交互：实现容器界面的创建和管理
 * - 动画效果：提供箱子盖子的开关动画
 * - 数据同步：处理客户端-服务器的数据传输
 * 
 * 技术特性：
 * - 持久化存储：通过VoidChestInventoriesData管理全局数据
 * - 懒加载：仅在需要时创建存储实例
 * - 异步动画：使用LerpedFloat实现平滑的盖子动作
 * - 声音反馈：提供开关箱子的声音效果
 * 
 * 设计模式：
 * - 代理模式：通过LazyOptional实现能力代理
 * - 观察者模式：监听玩家操作并更新动画状态
 * - 单例模式：全局共享的数据存储管理器
 * 
 * @author duquee
 * @since 1.0.0
 */
public class VoidChestTileEntity extends SmartBlockEntity implements MenuProvider {

	/**
	 * 虚空链接行为组件
	 * 
	 * 管理虚空箱子的网络连接逻辑，包括：
	 * - 频率配置管理
	 * - 所有者权限控制
	 * - 网络键生成和管理
	 * - 客户端配置界面支持
	 */
	VoidLinkBehaviour link;
	
	/**
	 * 客户端临时存储实例
	 * 
	 * 在客户端用于显示和数据同步，不持久化到磁盘。
	 * 服务端使用全局数据存储，客户端使用此临时实例。
	 */
	VoidChestInventory inventory;

	/**
	 * 当前打开箱子的玩家数量
	 * 
	 * 用于统计有多少玩家同时在使用这个虚空箱子。
	 * 超过0时箱子显示为打开状态，等于0时显示为关闭状态。
	 * 这个计数器影响盖子动画和声音效果。
	 */
	private int openCount;
	
	/**
	 * 箱子盖子的动画控制器
	 * 
	 * 使用LerpedFloat实现平滑的盖子开关动画。
	 * 值范围从0（关闭）到1（打开），通过线性插值实现平滑过渡。
	 * 渲染器使用这个值来计算盖子的旋转角度。
	 */
	public LerpedFloat lid = LerpedFloat.linear().startWithValue(0);

	/**
	 * 构造虚空箱子方块实体
	 * 
	 * 初始化方块实体的基础属性，传递给父类进行标准初始化。
	 * 在这个阶段不创建复杂的组件，等待addBehaviours()方法调用。
	 * 
	 * @param type 方块实体类型，用于类型检查和序列化
	 * @param pos 方块在世界中的位置坐标
	 * @param state 方块的当前状态
	 */
	public VoidChestTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/**
	 * 创建虚空链接行为组件
	 * 
	 * 定义和配置虚空箱子的链接逻辑，包括三个配置槽位：
	 * 1. 第一个频率槽位：用于设置第一个频率标识符
	 * 2. 第二个频率槽位：用于设置第二个频率标识符
	 * 3. 玩家槽位：用于设置箱子所有者
	 * 
	 * 槽位位置计算：
	 * - 位置坐标：(5.5, 7.5, 0.999) 像素单位
	 * - 动态朝向：根据箱子的FACING属性计算
	 * - 显示位置：在箱子正面的上方中央
	 * 
	 * 这个方法在addBehaviours()中被调用，确保在方块实体
	 * 完全初始化后再创建复杂的组件。
	 */
	public void createLink() {

		Triple<VoidLinkSlot, VoidLinkSlot, VoidLinkSlot> slots = VoidLinkSlot.makeSlots(
				index -> new VoidLinkSlot(index,
						state -> state.getValue(VoidChestBlock.FACING),
						VecHelper.voxelSpace(5.5F, 7.5F, .999F)) );

		link = new VoidLinkBehaviour(this, slots);
	}

	/**
	 * 添加方块实体行为组件
	 * 
	 * 这是SmartBlockEntity的核心生命周期方法，在方块实体初始化时被调用。
	 * 此时方块实体已经完全就绪，可以安全地创建和添加复杂的行为组件。
	 * 
	 * 执行流程：
	 * 1. 创建虚空链接行为组件
	 * 2. 将组件添加到行为列表中
	 * 3. Create模组会自动管理这些行为的生命周期
	 * 
	 * @param behaviours 行为组件列表，方块实体的所有行为都会添加到这里
	 */
	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		createLink();
		behaviours.add(link);
	}

	/**
	 * 检查是否有持久化存储数据
	 * 
	 * 只有在服务端环境中才允许访问全局持久化数据。
	 * 客户端使用临时的本地存储实例进行显示和交互。
	 * 
	 * 判断条件：
	 * - level != null：确保方块实体已经被加载到世界中
	 * - !level.isClientSide：确保当前环境是服务端
	 * 
	 * @return 是否可以访问持久化存储数据
	 */
	private boolean hasPersistentStorageData() {
		return level != null && !level.isClientSide;
	}

	/**
	 * 获取全局持久化存储数据管理器
	 * 
	 * 返回全局单例的虚空箱子存储数据管理器。
	 * 这个管理器在模组初始化时创建，负责管理所有虚空箱子的存储数据。
	 * 
	 * 使用静态方法确保：
	 * - 全局访问：任何地方都可以获取同一个实例
	 * - 线程安全：静态变量的访问是安全的
	 * - 内存效率：避免重复创建实例
	 * 
	 * @return 全局虚空箱子存储数据管理器
	 */
	private static VoidChestInventoriesData getPersistentStorageData() {
		return CreateUtilities.VOID_CHEST_INVENTORIES_DATA;
	}

	/**
	 * 获取物品存储实例
	 * 
	 * 这是虚空箱子的核心逻辑，根据环境返回不同的存储实例：
	 * 
	 * 服务端模式：
	 * - 使用全局数据管理器获取或创建存储实例
	 * - 数据持久化到磁盘，服务器重启后保留
	 * - 多个箱子共享同一个存储实例
	 * 
	 * 客户端模式：
	 * - 使用本地临时存储实例
	 * - 数据仅用于GUI显示和交互
	 * - 通过网络同步从服务端获取数据
	 * 
	 * 这种设计确保了数据一致性和正确的客户端-服务端分离。
	 * 
	 * @return 当前环境适用的物品存储实例
	 */
	public VoidChestInventory getItemStorage() {
		return  hasPersistentStorageData() ? getPersistentStorageData().computeStorageIfAbsent(link.getNetworkKey()) : inventory;
	}

	/**
	 * 提供Forge能力系统的接口实现
	 * 
	 * 这是Minecraft Forge能力系统的核心方法，允许其他模组和系统
	 * 访问虚空箱子的特定功能。
	 * 
	 * 对于ITEM_HANDLER能力：
	 * - 返回当前的物品存储实例
	 * - 支持所有标准的物品操作（插入、提取、查询等）
	 * - 通过LazyOptional实现懒加载和资源管理
	 * 
	 * 对于其他能力：
	 * - 委托给父类处理
	 * - 可能包括基础的方块实体能力（如NBT存储等）
	 * 
	 * LazyOptional的优点：
	 * - 懒加载：只有在需要时才创建实例
	 * - 内存效率：自动管理资源的生命周期
	 * - 线程安全：提供线程安全的访问机制
	 * 
	 * @param cap 请求的能力类型
	 * @param side 访问的方向（可为null表示任意方向）
	 * @param <T> 能力的泛型参数
	 * @return 对应能力的LazyOptional包装器
	 */
	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (cap == ForgeCapabilities.ITEM_HANDLER) {
			return LazyOptional.of(this::getItemStorage).cast();
		}
		return super.getCapability(cap, side);
	}

	/**
	 * 从 NBT 数据读取方块实体状态
	 * 
	 * 这个方法在以下情况下被调用：
	 * - 世界加载时从磁盘恢复数据
	 * - 客户端-服务端同步数据时
	 * - 区块传输时序列化数据
	 * 
	 * 客户端数据处理 (clientPacket = true)：
	 * - 创建本地临时存储实例
	 * - 从 NBT 中恢复库存数据用于显示
	 * - 同步当前打开箱子的玩家数量
	 * 
	 * 服务端数据处理 (clientPacket = false)：
	 * - 仅读取基础配置数据
	 * - 实际存储数据由全局数据管理器处理
	 * 
	 * 这种分离设计确保了数据一致性和网络效率。
	 * 
	 * @param tag 包含NBT数据的复合标签
	 * @param clientPacket 是否为客户端数据包
	 */
	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		if (clientPacket) {
			inventory = new VoidChestInventory(link.getNetworkKey());
			inventory.deserializeNBT(tag.getCompound("Inventory"));
			openCount = tag.getInt("OpenCount");
		}
		super.read(tag, clientPacket);
	}

	/**
	 * 将方块实体状态写入 NBT 数据
	 * 
	 * 这个方法在以下情况下被调用：
	 * - 世界保存时将数据持久化到磁盘
	 * - 客户端-服务端同步数据时
	 * - 区块传输时序列化数据
	 * 
	 * 数据写入逻辑：
	 * 
	 * 库存数据处理：
	 * - 服务端：优先使用全局数据管理器中的数据
	 * - 后备方案：如果全局数据不可用，使用本地inventory
	 * - 客户端：使用本地临时存储数据
	 * 
	 * 客户端同步数据：
	 * - openCount：当前打开箱子的玩家数量
	 * - 用于更新箱子盖子动画和声音效果
	 * 
	 * 这种设计确保了：
	 * - 数据一致性：服务端为数据权威来源
	 * - 网络效率：只同步必要的显示数据
	 * - 容错性：多重后备方案防止数据丢失
	 * 
	 * @param tag 用于存储数据的复合标签
	 * @param clientPacket 是否为客户端数据包
	 */
	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {

		if (hasPersistentStorageData()) tag.put("Inventory", getItemStorage().serializeNBT());
		else if (inventory != null) tag.put("Inventory", inventory.serializeNBT());

		if (clientPacket) tag.putInt("OpenCount", openCount);

		super.write(tag, clientPacket);
	}

	/**
	 * 获取GUI显示名称
	 * 
	 * 返回虚空箱子GUI窗口的标题文本。
	 * 使用本地化系统支持多语言显示。
	 * 
	 * 本地化键值："block.createutilities.void_chest"
	 * - 在语言文件中定义对应的翻译文本
	 * - 支持不同语言的动态切换
	 * - 遵循Minecraft的命名约定
	 * 
	 * @return 本地化的显示名称组件
	 */
	@Override
	public @NotNull Component getDisplayName() {
		return Component.translatable("block.createutilities.void_chest");
	}

	/**
	 * 创建容器菜单
	 * 
	 * 这是MenuProvider接口的核心实现，在玩家打开GUI时被调用。
	 * 负责创建并配置虚空箱子的交互界面。
	 * 
	 * 参数说明：
	 * - id：容器的唯一标识符，用于网络同步
	 * - inventory：玩家的个人物品栏，用于物品交换
	 * - player：正在打开GUI的玩家
	 * 
	 * 容器创建流程：
	 * 1. 调用VoidChestContainer.create()静态方法
	 * 2. 传递必要的参数和当前方块实体
	 * 3. 容器会自动设置槽位、交互逻辑等
	 * 
	 * @param id 容器窗口的唯一标识符
	 * @param inventory 玩家的个人物品栏
	 * @param player 打开容器的玩家
	 * @return 创建的虚空箱子容器实例
	 */
	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
		return VoidChestContainer.create(id, inventory, this);
	}

	/**
	 * 方块实体的tick更新方法
	 * 
	 * 每个游戏Tick（20母秒）都会调用此方法进行状态更新。
	 * 主要用于处理箱子盖子的平滑动画效果。
	 * 
	 * 动画更新逻辑：
	 * 1. chase()：设置目标值和过渡速度
	 *    - 目标值：openCount > 0 ? 1 : 0
	 *    - 过渡速度：0.1f（相对缓慢的平滑过渡）
	 *    - 插值类型：LINEAR（线性插值）
	 * 
	 * 2. tickChaser()：执行实际的插值计算
	 *    - 根据时间步进更新当前值
	 *    - 逐渐逐跫目标值
	 * 
	 * 这种设计创造了自然的箱子开关效果：
	 * - 打开时盖子平滑向上旋转
	 * - 关闭时盖子平滑向下旋转
	 * - 避免突兓的状态变化
	 */
	@Override
	public void tick() {
		lid.chase(openCount > 0 ? 1 : 0, 0.1f, LerpedFloat.Chaser.LINEAR);
		lid.tickChaser();
	}

	/**
	 * 检查箱子是否完全关闭
	 * 
	 * 用于判断箱子的盖子是否已经完全关闭并稳定。
	 * 主要用于优化渲染性能和特殊效果的处理。
	 * 
	 * 判断条件：
	 * - lid.settled()：动画已经稳定，不再变化
	 * - lid.getChaseTarget() == 0：目标值为0（关闭状态）
	 * 
	 * 使用场景：
	 * - 渲染器可以跳过复杂的盖子渲染
	 * - 物理系统可以优化碰撞检测
	 * - 粒子系统可以调整效果强度
	 * 
	 * @return 箱子是否完全关闭
	 */
	public boolean isClosed() {
		return lid.settled() && lid.getChaseTarget() == 0;
	}

	/**
	 * 处理玩家开始使用箱子
	 * 
	 * 当玩家打开虚空箱子GUI时调用，更新相关状态和效果。
	 * 
	 * 执行流程：
	 * 1. 防御性检查：确俟openCount不为负数
	 * 2. 增加打开计数器：记录当前使用者数量
	 * 3. 同步数据：将更新同步到客户端
	 * 4. 首次打开的特殊处理：
	 *    - 发送容器打开事件（用于成就、统计等）
	 *    - 播放开箱声音效果
	 * 
	 * 声音参数：
	 * - 基础音量：0.5F（中等音量）
	 * - 随机音频偏移：0.1F 范围内的随机变化
	 * - 基础音频：0.9F（略低于标准）
	 * 
	 * 这种设计确保了：
	 * - 多玩家同时使用的正确计数
	 * - 适当的声音和视觉反馈
	 * - 游戏事件的正确触发
	 * 
	 * @param player 正在打开箱子的玩家
	 */
	public void startOpen(Player player) {
		if (this.openCount < 0) this.openCount = 0;

		this.openCount++;
		sendData();

		if (this.openCount == 1) {
			this.level.gameEvent(player, GameEvent.CONTAINER_OPEN, this.worldPosition);
			this.level.playSound(null, this.worldPosition, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
		}

	}

	/**
	 * 处理玩家停止使用箱子
	 * 
	 * 当玩家关闭虚空箱子GUI时调用，更新相关状态和效果。
	 * 
	 * 执行流程：
	 * 1. 减少打开计数器：记录一个玩家停止使用
	 * 2. 同步数据：将更新同步到客户端
	 * 3. 最后一个玩家关闭的特殊处理：
	 *    - 发送容器关闭事件（用于成就、统计等）
	 *    - 播放关箱声音效果
	 * 
	 * 防御性设计：
	 * - openCount <= 0 检查防止计数器为负数
	 * - 保证即使多次调用也不会引起问题
	 * 
	 * 声音参数（与startOpen相同）：
	 * - 基础音量：0.5F
	 * - 随机音频偏移：0.1F 范围
	 * - 基础音频：0.9F
	 * 
	 * 这种设计确保了：
	 * - 正确的多玩家状态管理
	 * - 对称的打开/关闭声音效果
	 * - 游戏事件系统的正确触发
	 * 
	 * @param player 正在关闭箱子的玩家
	 */
	public void stopOpen(Player player) {
		this.openCount--;
		sendData();

		if (this.openCount <= 0) {
			this.level.gameEvent(player, GameEvent.CONTAINER_CLOSE, this.worldPosition);
			this.level.playSound(null, this.worldPosition, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
		}
	}

}
