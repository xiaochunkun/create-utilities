# Create Utilities 功能代码详解

## 📋 项目概述

Create Utilities是一个强大的Minecraft Forge模组，为Create模组添加了革命性的"虚空存储"功能。该系统允许玩家在不同维度、不同位置之间共享物品、流体和能量存储，极大简化了远距离资源传输。

## 🏗️ 核心架构设计

### 虚空存储系统架构

虚空存储系统基于以下核心概念：

1. **网络键（NetworkKey）**：唯一标识一个虚空存储网络
2. **频率配置**：通过两个物品频率组合定义网络地址
3. **所有者权限**：基于玩家档案的权限控制系统
4. **跨维度同步**：服务器端全局存储 + 客户端本地缓存

---

## 🔧 核心功能模块详解

### 1. 虚空网络管理系统

#### 🎯 VoidLinkBehaviour - 虚空连接行为
**位置**: `src/main/java/me/duquee/createutilities/blocks/voidtypes/VoidLinkBehaviour.java`

这是虚空存储系统的核心组件，负责管理每个虚空方块的网络连接配置。

**核心功能**:
- 管理两个频率标识符（类似无线电频道）
- 维护方块所有者信息（权限控制）
- 提供可视化频率配置槽位
- 生成唯一网络键用于存储映射

**关键代码**:
```java
/**
 * 获取网络键 - 虚空存储系统的核心方法
 * 网络键由所有者和两个频率组成，只有相同网络键的方块才能共享存储
 */
public NetworkKey getNetworkKey() {
    return new NetworkKey(owner, frequencyFirst, frequencyLast);
}

/**
 * 设置频率配置 - 用户配置虚空网络的核心方法
 * 频率变更会触发网络重连，确保方块连接到正确的虚空存储网络
 */
public void setFrequency(boolean first, ItemStack stack) {
    // 频率变化检测
    boolean changed = !ItemStack.isSameItemSameTags(stack, toCompare);
    
    // 先离开当前网络，再加入新网络
    if (changed) onLeaveNetwork();
    
    // 更新频率并同步数据
    if (first) frequencyFirst = Frequency.of(stack);
    else frequencyLast = Frequency.of(stack);
    
    blockEntity.sendData();
    onJoinNetwork();
}
```

#### 🎯 VoidStorageData - 虚空存储数据基类
**位置**: `src/main/java/me/duquee/createutilities/blocks/voidtypes/VoidStorageData.java`

所有虚空存储类型的抽象基类，提供持久化和网络键映射功能。

**核心功能**:
- 通过网络键映射存储实例
- 提供数据持久化和反序列化
- 管理存储实例的生命周期

**关键代码**:
```java
/**
 * 计算并获取存储实例，如果不存在则创建
 * 这是虚空存储系统的核心方法，确保每个网络键都有对应的存储实例
 */
public T computeStorageIfAbsent(NetworkKey key, Function<NetworkKey, T> function) {
    return storages.computeIfAbsent(key, function);
}

/**
 * 保存存储数据到NBT - 确保服务器重启后虚空存储内容不丢失
 */
public @NotNull CompoundTag save(@NotNull CompoundTag tag,
                                Function<T, Boolean> isEmpty,
                                Function<T, CompoundTag> serializeNBT) {
    storages.forEach((key, inventory) -> {
        if (!isEmpty.apply(inventory))
            tag.put(key.toString(), serializeNBT.apply(inventory));
    });
    return tag;
}
```

---

### 2. 虚空储罐系统

#### 🎯 VoidTankTileEntity - 虚空储罐方块实体
**位置**: `src/main/java/me/duquee/createutilities/blocks/voidtypes/tank/VoidTankTileEntity.java`

虚空储罐的核心实现，支持跨维度流体共享。

**核心功能**:
- 流体存储能力提供
- 工程师护目镜信息显示
- 客户端-服务器数据同步

**关键代码**:
```java
/**
 * 获取流体存储实例 - 根据客户端/服务器环境选择不同的存储源
 */
public FluidTank getFluidStorage() {
    return level != null && !level.isClientSide ?
            CreateUtilities.VOID_TANKS_DATA.computeStorageIfAbsent(link.getNetworkKey()) :
            CreateUtilitiesClient.VOID_TANKS.computeStorageIfAbsent(link.getNetworkKey());
}

/**
 * Forge能力系统集成 - 提供流体处理能力
 */
@Override
public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
    if (cap == ForgeCapabilities.FLUID_HANDLER) {
        return LazyOptional.of(this::getFluidStorage).cast();
    }
    return super.getCapability(cap, side);
}
```

#### 🎯 VoidTank - 虚空储罐实现
**位置**: `src/main/java/me/duquee/createutilities/blocks/voidtypes/tank/VoidTank.java`

继承自Forge的FluidTank，添加了网络同步功能。

**关键代码**:
```java
/**
 * 内容变化处理 - 自动同步到所有客户端
 */
@Override
protected void onContentsChanged() {
    // 标记服务器数据为脏（需要保存）
    if (CreateUtilities.VOID_TANKS_DATA != null) 
        CreateUtilities.VOID_TANKS_DATA.setDirty();
    
    // 发送更新数据包到所有客户端
    CUPackets.channel.send(PacketDistributor.ALL.noArg(), 
                          new VoidTankUpdatePacket(key, this));
}
```

---

### 3. 网络同步系统

#### 🎯 VoidTankUpdatePacket - 虚空储罐更新数据包
**位置**: `src/main/java/me/duquee/createutilities/networking/packets/VoidTankUpdatePacket.java`

负责在客户端和服务器之间同步虚空储罐的流体数据。

**核心功能**:
- 序列化/反序列化网络数据
- 客户端缓存更新
- 确保所有客户端显示一致的流体内容

**关键代码**:
```java
/**
 * 处理接收到的数据包 - 更新客户端的虚空储罐缓存
 */
@Override
public boolean handle(NetworkEvent.Context context) {
    context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
            CreateUtilitiesClient.VOID_TANKS.storages.put(key, tank)));
    return true;
}

/**
 * 将数据包内容写入网络缓冲区 - 序列化网络键和储罐数据
 */
@Override
public void write(FriendlyByteBuf buffer) {
    key.writeToBuffer(buffer);
    buffer.writeNbt(tank.writeToNBT(new CompoundTag()));
}
```

---

### 4. 虚空箱子系统

#### 🎯 VoidChestTileEntity - 虚空箱子方块实体
**位置**: `src/main/java/me/duquee/createutilities/blocks/voidtypes/chest/VoidChestTileEntity.java`

实现跨维度物品存储共享，包含开启/关闭动画效果。

**核心功能**:
- 物品存储能力提供
- 开启/关闭动画和音效
- GUI菜单提供

**关键代码**:
```java
/**
 * 获取物品存储实例 - 服务器端使用持久化数据，客户端使用本地缓存
 */
public VoidChestInventory getItemStorage() {
    return hasPersistentStorageData() ? 
           getPersistentStorageData().computeStorageIfAbsent(link.getNetworkKey()) : 
           inventory;
}

/**
 * 开始打开动画和音效
 */
public void startOpen(Player player) {
    this.openCount++;
    sendData();
    
    if (this.openCount == 1) {
        this.level.gameEvent(player, GameEvent.CONTAINER_OPEN, this.worldPosition);
        this.level.playSound(null, this.worldPosition, SoundEvents.CHEST_OPEN, 
                            SoundSource.BLOCKS, 0.5F, 
                            this.level.random.nextFloat() * 0.1F + 0.9F);
    }
}
```

---

### 5. 虚空电池系统

#### 🎯 VoidBatteryTileEntity - 虚空电池方块实体
**位置**: `src/main/java/me/duquee/createutilities/blocks/voidtypes/battery/VoidBatteryTileEntity.java`

实现跨维度能量存储共享，支持Forge Energy系统。

**核心功能**:
- 能量存储能力提供
- 工程师护目镜能量信息显示
- 客户端-服务器能量数据同步

**关键代码**:
```java
/**
 * 获取电池实例 - 根据环境选择持久化或客户端缓存
 */
public VoidBattery getBattery() {
    return hasPersistentData() ?
           getPersistentStorageData().computeStorageIfAbsent(link.getNetworkKey()) :
           CreateUtilitiesClient.VOID_BATTERIES.computeStorageIfAbsent(link.getNetworkKey());
}

/**
 * 工程师护目镜信息显示 - 显示能量存储状态
 */
@Override
public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
    VoidBattery battery = getBattery();
    
    // 显示能量信息：当前存储/最大容量
    new LangBuilder(CreateUtilities.ID)
            .text(battery.getEnergyStored() + "fe / " + battery.getMaxEnergyStored() + "fe")
            .forGoggles(tooltip, 1);
    
    return true;
}
```

---

### 6. 虚空马达系统

#### 🎯 VoidMotorTileEntity - 虚空马达方块实体
**位置**: `src/main/java/me/duquee/createutilities/blocks/voidtypes/motor/VoidMotorTileEntity.java`

实现跨维度旋转力传输，是Create模组动力系统的扩展。

**核心功能**:
- 旋转力网络连接
- 虚空网络传播逻辑
- 动力连接管理

**关键代码**:
```java
/**
 * 添加传播位置 - 将虚空网络中的其他马达添加到传播列表
 */
@Override
public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
    neighbours.addAll(link.getNetwork());
    return neighbours;
}

/**
 * 旋转力传播计算 - 只在相同虚空网络内传播旋转力
 */
@Override
public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, 
                                BlockState stateTo, BlockPos diff, 
                                boolean connectedViaAxes, boolean connectedViaCogs) {
    VoidMotorLinkBehaviour targetLink = (VoidMotorLinkBehaviour) 
                                       BlockEntityBehaviour.get(target, VoidMotorLinkBehaviour.TYPE);
    
    if (targetLink != null) 
        return targetLink.getNetworkKey().equals(link.getNetworkKey()) ? 1 : 0;
    
    return 0;
}
```

#### 🎯 VoidMotorNetworkHandler - 虚空马达网络处理器
**位置**: `src/main/java/me/duquee/createutilities/blocks/voidtypes/motor/VoidMotorNetworkHandler.java`

管理虚空马达网络的连接和断开，维护网络拓扑。

**核心功能**:
- 网络连接管理
- 维度间网络同步
- 网络键序列化/反序列化

**关键代码**:
```java
/**
 * 添加到网络 - 将马达加入虚空网络并触发连接事件
 */
public void addToNetwork(LevelAccessor world, VoidMotorLinkBehaviour actor) {
    getNetworkOf(world, actor).add(actor.getPos());
    if (actor.blockEntity instanceof VoidMotorTileEntity voidMotor) 
        voidMotor.onConnectToVoidNetwork();
}

/**
 * 从网络移除 - 断开马达连接并清理空网络
 */
public void removeFromNetwork(LevelAccessor world, VoidMotorLinkBehaviour actor) {
    if (actor.blockEntity instanceof VoidMotorTileEntity voidMotor) 
        voidMotor.onDisconnectFromVoidNetwork();
    
    Set<BlockPos> network = getNetworkOf(world, actor);
    network.remove(actor.getPos());
    
    // 如果网络为空，清理网络记录
    if (network.isEmpty()) 
        networksIn(world).remove(actor.getNetworkKey());
}
```

---

## 🔗 系统集成要点

### 客户端-服务器架构

1. **服务器端**: 使用SavedData持久化存储，确保重启后数据不丢失
2. **客户端**: 使用本地缓存快速响应UI操作
3. **同步机制**: 通过自定义数据包保持客户端与服务器数据一致

### Create模组集成

1. **Registrate系统**: 使用Create的注册框架简化方块和物品注册
2. **Ponder教程**: 集成交互式教程系统
3. **动力系统**: 虚空马达完全兼容Create的旋转动力网络
4. **护目镜支持**: 所有虚空方块都支持工程师护目镜信息显示

### Forge能力系统

1. **流体处理**: VoidTank实现IFluidHandler
2. **物品处理**: VoidChest实现IItemHandler  
3. **能量处理**: VoidBattery实现IEnergyStorage
4. **旋转力处理**: VoidMotor集成Create的动力系统

---

## 📊 性能优化特性

1. **延迟加载**: 存储实例只在需要时创建
2. **增量同步**: 只同步发生变化的数据
3. **客户端缓存**: 减少网络请求，提高响应速度
4. **智能清理**: 自动清理空的网络记录

---

## 🎯 关键设计模式

1. **策略模式**: 不同存储类型使用统一的VoidStorageData接口
2. **观察者模式**: 存储变化时自动通知客户端
3. **工厂模式**: 通过computeStorageIfAbsent动态创建存储实例
4. **命令模式**: 网络数据包封装具体的同步操作

---

这个虚空存储系统展现了现代Minecraft模组开发的最佳实践，通过精心设计的架构实现了跨维度资源共享的复杂功能，同时保持了良好的性能和用户体验。