package me.duquee.createutilities.blocks.voidtypes.chest;

import com.simibubi.create.foundation.gui.menu.MenuBase;
import me.duquee.createutilities.blocks.voidtypes.CUContainerTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

/**
 * 虚空箱子容器类
 * 
 * 这个类管理虚空箱子的GUI界面逻辑，负责处理客户端和服务端之间的数据同步、
 * 槽位管理、物品移动等核心功能。它继承自Create模组的MenuBase，
 * 提供了与虚空存储系统集成的完整容器实现。
 * 
 * 核心功能：
 * - GUI界面管理：控制虚空箱子界面的显示和交互
 * - 数据同步：确保客户端和服务端的库存数据一致性
 * - 槽位布局：管理27个存储槽位和36个玩家背包槽位
 * - 物品传输：实现快速移动、批量操作等便捷功能
 * - 生命周期管理：处理容器的打开、关闭和资源清理
 * 
 * 设计特点：
 * - 基于Create模组的MenuBase架构
 * - 支持网络环境下的实时数据同步
 * - 实现标准的Minecraft容器接口
 * - 提供流畅的用户交互体验
 * 
 * 槽位布局：
 * - 存储区域：3行x9列（0-26号槽位）
 * - 玩家背包：4行x9列（27-62号槽位）
 * - 支持Shift+点击快速移动物品
 * 
 * 网络同步：
 * - 服务端推送完整的TileEntity数据
 * - 客户端接收并更新本地状态
 * - 确保虚空存储的实时性和一致性
 * 
 * @author duquee
 * @since 1.0.0
 */
public class VoidChestContainer extends MenuBase<VoidChestTileEntity> {

	/**
	 * 客户端容器构造函数
	 * 
	 * 这个构造函数用于在客户端创建容器实例，通过网络数据包传递的信息
	 * 来初始化容器状态。主要用于处理玩家打开GUI时的初始化过程。
	 * 
	 * @param type 容器类型，用于识别具体的容器实现
	 * @param id 容器ID，用于网络同步和状态管理
	 * @param inv 玩家背包实例
	 * @param extraData 额外数据缓冲区，包含TileEntity位置和状态信息
	 */
	public VoidChestContainer(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	/**
	 * 服务端容器构造函数
	 * 
	 * 这个构造函数用于在服务端创建容器实例，直接接收TileEntity引用
	 * 来建立容器与方块实体之间的关联。同时调用startOpen通知TileEntity
	 * 有玩家开始访问，用于统计和权限管理。
	 * 
	 * @param type 容器类型
	 * @param id 容器ID
	 * @param inv 玩家背包实例
	 * @param te 虚空箱子的方块实体实例
	 */
	public VoidChestContainer(MenuType<?> type, int id, Inventory inv, VoidChestTileEntity te) {
		super(type, id, inv, te);
		// 通知TileEntity有玩家开始访问
		contentHolder.startOpen(player);
	}

	/**
	 * 创建虚空箱子容器的静态工厂方法
	 * 
	 * 提供一个便捷的方式来创建虚空箱子容器实例，自动使用正确的容器类型。
	 * 这个方法主要在服务端使用，当玩家右键点击虚空箱子时被调用。
	 * 
	 * @param id 容器ID
	 * @param inv 玩家背包
	 * @param te 虚空箱子方块实体
	 * @return 配置好的虚空箱子容器实例
	 */
	public static VoidChestContainer create(int id, Inventory inv, VoidChestTileEntity te) {
		return new VoidChestContainer(CUContainerTypes.VOID_CHEST.get(), id, inv, te);
	}

	/**
	 * 在客户端创建TileEntity实例
	 * 
	 * 这是网络同步机制的核心方法。当玩家在客户端打开虚空箱子GUI时，
	 * 服务端会通过网络发送TileEntity的数据，客户端接收后调用这个方法
	 * 来重建本地的TileEntity状态。
	 * 
	 * 同步过程：
	 * 1. 从网络数据中读取方块位置
	 * 2. 读取序列化的NBT数据
	 * 3. 获取客户端世界中对应位置的TileEntity
	 * 4. 将服务端数据应用到客户端TileEntity
	 * 
	 * 这确保了客户端看到的库存数据与服务端完全一致。
	 * 
	 * @param extraData 包含位置和NBT数据的网络缓冲区
	 * @return 更新后的客户端TileEntity实例，失败时返回null
	 */
	@Override
	protected VoidChestTileEntity createOnClient(FriendlyByteBuf extraData) {
		BlockPos readBlockPos = extraData.readBlockPos();
		CompoundTag readNbt = extraData.readNbt();

		ClientLevel world = Minecraft.getInstance().level;
		assert world != null;
		BlockEntity tileEntity = world.getBlockEntity(readBlockPos);
		if (tileEntity instanceof VoidChestTileEntity voidChest) {
			// 应用服务端数据到客户端TileEntity
			voidChest.read(readNbt, true);
			return voidChest;
		}

		return null;
	}

	/**
	 * 初始化并读取库存数据
	 * 
	 * 这个方法在MenuBase的生命周期中被调用，用于初始化容器的库存状态。
	 * 对于虚空箱子，由于库存数据已经通过createOnClient方法同步，
	 * 这里不需要额外的初始化逻辑。
	 * 
	 * @param contentHolder 内容持有者（VoidChestTileEntity）
	 */
	@Override
	protected void initAndReadInventory(VoidChestTileEntity contentHolder) {
		// 虚空箱子的库存数据通过createOnClient同步，这里无需额外处理
	}

	/**
	 * 添加GUI槽位
	 * 
	 * 定义虚空箱子GUI的完整槽位布局：
	 * 1. 首先添加虚空箱子的存储槽位（3x9网格）
	 * 2. 然后添加玩家的背包槽位（标准位置）
	 * 
	 * 槽位编号分配：
	 * - 0-26：虚空箱子存储槽位
	 * - 27-62：玩家背包和快捷栏槽位
	 */
	@Override
	protected void addSlots() {
		addChestSlots();       // 添加虚空箱子的存储槽位
		addPlayerSlots(8, 90); // 添加玩家背包槽位，位置偏移为(8, 90)
	}

	/**
	 * 添加虚空箱子存储槽位
	 * 
	 * 创建3行9列的存储槽位网格，总共27个槽位。
	 * 每个槽位的位置按照标准的GUI布局计算：
	 * - X坐标：8 + 列号 × 18像素
	 * - Y坐标：18 + 行号 × 18像素
	 * 
	 * 使用SlotItemHandler来集成Forge的物品处理系统，
	 * 确保与虚空存储机制的正确交互。
	 */
	private void addChestSlots() {
		VoidChestInventory inventory = contentHolder.getItemStorage();
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				int slotIndex = y * 9 + x;  // 计算槽位索引
				int xPos = 8 + x * 18;      // 计算X坐标
				int yPos = 18 + y * 18;     // 计算Y坐标
				addSlot(new SlotItemHandler(inventory, slotIndex, xPos, yPos));
			}
		}
	}

	/**
	 * 保存容器数据
	 * 
	 * 在容器关闭或需要保存状态时调用。对于虚空箱子，
	 * 数据的持久化由VoidChestInventory和相关的数据管理器负责，
	 * 容器层面不需要额外的保存操作。
	 * 
	 * @param contentHolder 内容持有者
	 */
	@Override
	protected void saveData(VoidChestTileEntity contentHolder) {
		// 虚空箱子的数据持久化由底层存储系统处理
	}

	/**
	 * 实现快速移动物品逻辑（Shift+点击）
	 * 
	 * 这个方法处理玩家使用Shift+点击时的物品快速移动：
	 * - 如果点击的是虚空箱子槽位，物品移动到玩家背包
	 * - 如果点击的是玩家背包槽位，物品移动到虚空箱子
	 * 
	 * 移动逻辑：
	 * 1. 检查被点击的槽位是否有物品
	 * 2. 根据槽位索引判断移动方向
	 * 3. 执行物品堆移动操作
	 * 4. 通知相关系统内容已更改
	 * 
	 * @param player 执行操作的玩家
	 * @param index 被点击的槽位索引
	 * @return 移动失败时返回原物品堆，成功时返回空堆
	 */
	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot clickedSlot = getSlot(index);
		if (!clickedSlot.hasItem()) return ItemStack.EMPTY;

		ItemStack stack = clickedSlot.getItem();
		int size = contentHolder.getItemStorage().getSlots(); // 虚空箱子槽位数量
		boolean success;
		
		if (index < size) {
			// 从虚空箱子移动到玩家背包
			success = !moveItemStackTo(stack, size, slots.size(), false);
			// 通知虚空存储系统内容已更改，触发网络同步等操作
			contentHolder.getItemStorage().onContentsChanged(index);
		} else {
			// 从玩家背包移动到虚空箱子
			success = !moveItemStackTo(stack, 0, size - 1, false);
		}

		return success ? ItemStack.EMPTY : stack;
	}

	/**
	 * 处理容器关闭事件
	 * 
	 * 当玩家关闭虚空箱子GUI时调用，执行必要的清理工作：
	 * 1. 调用父类的关闭逻辑
	 * 2. 在服务端通知TileEntity停止访问状态
	 * 
	 * 停止访问的通知很重要，因为它：
	 * - 更新访问者计数，用于性能优化
	 * - 触发必要的数据保存操作
	 * - 释放可能占用的资源
	 * 
	 * @param player 关闭容器的玩家
	 */
	@Override
	public void removed(Player player) {
		super.removed(player);
		// 只在服务端执行关闭通知
		if (!player.level().isClientSide) 
			contentHolder.stopOpen(player);
	}
}
