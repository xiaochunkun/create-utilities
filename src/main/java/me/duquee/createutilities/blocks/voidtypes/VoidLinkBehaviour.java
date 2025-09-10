package me.duquee.createutilities.blocks.voidtypes;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.content.equipment.clipboard.ClipboardCloneable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import me.duquee.createutilities.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import me.duquee.createutilities.voidlink.VoidLinkSlot;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 虚空连接行为类
 * 
 * 这是虚空存储系统中最重要的组件之一，负责管理虚空网络的连接配置。
 * 每个虚空方块都有一个VoidLinkBehaviour实例，用于：
 * 
 * 核心功能：
 * - 管理两个频率标识符（类似于无线电频道）
 * - 维护方块所有者信息（用于权限控制）
 * - 提供可视化的频率配置槽位
 * - 支持剪贴板复制粘贴配置
 * - 生成唯一的网络键用于存储映射
 * 
 * 网络键组成：所有者 + 频率1 + 频率2
 * 只有拥有相同网络键的虚空方块才能共享存储内容。
 */
public class VoidLinkBehaviour extends BlockEntityBehaviour implements ClipboardCloneable {

	/**
	 * 行为类型标识符，用于系统识别此行为类型
	 */
	public static final BehaviourType<VoidLinkBehaviour> TYPE = new BehaviourType<>();

	/**
	 * 第一个频率标识符
	 * 与第二个频率组合形成唯一的网络地址
	 */
	Frequency frequencyFirst = Frequency.EMPTY;
	
	/**
	 * 第二个频率标识符
	 * 与第一个频率组合形成唯一的网络地址
	 */
	Frequency frequencyLast = Frequency.EMPTY;
	
	/**
	 * 方块所有者的游戏档案
	 * 用于权限控制，只有所有者可以修改频率配置
	 * 如果为null，则任何玩家都可以配置
	 */
	@Nullable
	GameProfile owner;

	/**
	 * 第一个频率配置槽位（用于UI显示和交互）
	 */
	VoidLinkSlot firstSlot;
	
	/**
	 * 第二个频率配置槽位（用于UI显示和交互）
	 */
	VoidLinkSlot secondSlot;
	
	/**
	 * 玩家槽位（用于设置所有者）
	 */
	VoidLinkSlot playerSlot;

	/**
	 * 构造函数：创建虚空连接行为
	 * 
	 * @param te 关联的智能方块实体
	 * @param slots 三个配置槽位的三元组（频率1、频率2、玩家槽）
	 */
	public VoidLinkBehaviour(SmartBlockEntity te,
							 Triple<VoidLinkSlot, VoidLinkSlot, VoidLinkSlot> slots) {
		super(te);
		firstSlot = slots.getLeft();
		secondSlot = slots.getMiddle();
		this.playerSlot = slots.getRight();
	}

	/**
	 * 将连接配置写入NBT标签
	 * 
	 * 保存频率配置和所有者信息，用于数据持久化。
	 * 
	 * @param nbt NBT标签
	 * @param clientPacket 是否为客户端数据包
	 */
	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		super.write(nbt, clientPacket);

		nbt.put("FrequencyFirst", frequencyFirst.getStack().save(new CompoundTag()));
		nbt.put("FrequencyLast", frequencyLast.getStack().save(new CompoundTag()));

		if (this.owner != null) {
			CompoundTag compoundTag = new CompoundTag();
			NbtUtils.writeGameProfile(compoundTag, this.owner);
			nbt.put("Owner", compoundTag);
		}

	}

	/**
	 * 从NBT标签读取连接配置
	 * 
	 * 加载保存的频率配置和所有者信息。
	 * 
	 * @param nbt NBT标签
	 * @param clientPacket 是否为客户端数据包
	 */
	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		super.read(nbt, clientPacket);

		frequencyFirst = Frequency.of(ItemStack.of(nbt.getCompound("FrequencyFirst")));
		frequencyLast = Frequency.of(ItemStack.of(nbt.getCompound("FrequencyLast")));

		owner = nbt.contains("Owner", 10) ? NbtUtils.readGameProfile(nbt.getCompound("Owner")) : null;
	}

	/**
	 * 获取网络键
	 * 
	 * 这是虚空存储系统的核心方法，生成唯一的网络标识。
	 * 网络键由所有者和两个频率组成，只有相同网络键的方块才能共享存储。
	 * 
	 * @return 当前配置对应的网络键
	 */
	public NetworkKey getNetworkKey() {
		return new NetworkKey(owner, frequencyFirst, frequencyLast);
	}

	/**
	 * 设置频率配置
	 * 
	 * 这是用户配置虚空网络的核心方法。当玩家在频率槽位中放入物品时调用。
	 * 频率变更会触发网络重连，确保方块连接到正确的虚空存储网络。
	 * 
	 * @param first 是否设置第一个频率（false表示设置第二个频率）
	 * @param stack 用作频率标识的物品堆栈
	 */
	public void setFrequency(boolean first, ItemStack stack) {

		stack = stack.copy();
		stack.setCount(1);
		ItemStack toCompare = getFrequencyStack(first);
		boolean changed = !ItemStack.isSameItemSameTags(stack, toCompare);

		// 如果频率发生变化，先离开当前网络
		if (changed) onLeaveNetwork();

		if (first) frequencyFirst = Frequency.of(stack);
		else frequencyLast = Frequency.of(stack);

		if (!changed) return;

		// 同步数据并加入新网络
		blockEntity.sendData();
		onJoinNetwork();

		updateBlock();

	}

	private void updateBlock() {
		blockEntity.getLevel().blockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState().getBlock());
	}

	public boolean testHit(int index, Vec3 hit) {
		BlockState state = blockEntity.getBlockState();
		Vec3 localHit = hit.subtract(Vec3.atLowerCornerOf(blockEntity.getBlockPos()));
		return getSlot(index).testHit(blockEntity.getLevel(), blockEntity.getBlockPos(), state, localHit);
	}

	public ValueBoxTransform getSlot(int index) {
		return index < 2 ? getFrequencySlot(index == 0) : playerSlot;
	}

	public ValueBoxTransform getFrequencySlot(boolean first) {
		return first ? firstSlot : secondSlot;
	}
	public ItemStack getFrequencyStack(boolean first) {
		return first ? frequencyFirst.getStack() : frequencyLast.getStack();
	}

	public boolean canInteract(Player player) {
		return !isAdventure(player) && isOwner(player);
	}

	private boolean isAdventure(Player player) {
		return player != null && !player.mayBuild() && !player.isSpectator();
	}

	@Nullable
	public GameProfile getOwner() {
		return owner;
	}

	public void setOwner(@Nullable GameProfile owner) {
		if (!Objects.equals(this.owner, owner)) {
			onLeaveNetwork();
			this.owner = owner;
			blockEntity.sendData();
			onJoinNetwork();
			updateBlock();
		}
	}

	protected void onLeaveNetwork() {}
	protected void onJoinNetwork() {}

	public boolean isOwner(Player player) {
		return owner == null || player.getGameProfile().equals(owner);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	@Override
	public String getClipboardKey() {
		return "Frequencies";
	}

	@Override
	public boolean writeToClipboard(CompoundTag nbt, Direction side) {
		nbt.put("First", frequencyFirst.getStack().save(new CompoundTag()));
		nbt.put("Last", frequencyLast.getStack().save(new CompoundTag()));
		if (owner != null) NBTHelper.putMarker(nbt, "Owned");
		return true;
	}

	@Override
	public boolean readFromClipboard(CompoundTag nbt, Player player, Direction side, boolean simulate) {

		if (!nbt.contains("First") || !nbt.contains("Last") || !isOwner(player)) return false;
		if (simulate) return true;

		setFrequency(true, ItemStack.of(nbt.getCompound("First")));
		setFrequency(false, ItemStack.of(nbt.getCompound("Last")));
		setOwner(nbt.contains("Owned") ? player.getGameProfile() : null);

		return true;
	}
}
