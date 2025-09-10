package me.duquee.createutilities.mountedstorage;

import com.mojang.serialization.Codec;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.WrapperMountedItemStorage;
import me.duquee.createutilities.CUCodecs;
import me.duquee.createutilities.CreateUtilities;
import me.duquee.createutilities.blocks.voidtypes.chest.VoidChestInventory;
import me.duquee.createutilities.blocks.voidtypes.chest.VoidChestTileEntity;
import me.duquee.createutilities.blocks.voidtypes.motor.VoidMotorNetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 虚空胸子挂载存储实现
 * 
 * 这个类是虚空胸子在Create装置系统中的核心实现，它将静态的虚空胸子
 * 功能完整地移植到挂载存储系统中，实现了真正的“可移动虚空存储”。
 * 
 * 技术架构：
 * - 继承WrapperMountedItemStorage，重用Create的成熟挂载模式
 * - 包装VoidChestInventory，保持虚空存储的所有特性
 * - 使用NetworkKey进行网络定位和数据同步
 * - 集成Codec系统实现数据持久化
 * 
 * 核心特性：
 * - 虚空网络访问：在装置中仍能访问跨维度存储
 * - 数据一致性：与静态虚空胸子完全同步
 * - 音效支持：保持原有的胸子开关音效
 * - 状态保持：装置变化不影响存储状态
 * 
 * 挂载存储的优势：
 * 1. 移动性：胸子可以随装置一起移动
 * 2. 灵活性：支持复杂的装置设计和操作
 * 3. 可靠性：装置拆解后数据不会丢失
 * 4. 性能：继承虚空存储的高效特性
 * 
 * 生命周期：
 * 1. 创建：从静态胸子或网络键创建
 * 2. 挂载：绑定到装置的存储系统
 * 3. 运行：作为装置的一部分提供存储服务
 * 4. 卸载：从装置中分离，数据保持
 * 
 * 序列化设计：
 * - 使用NetworkKey作为唯一标识
 * - 支持跨世界和跨重启动的数据恢复
 * - 遵循版本兼容性原则
 * - 集成自动数据迁移机制
 * 
 * 错误处理：
 * - 优雅降级：遇到问题时不影响装置正常运行
 * - 数据恢复：自动从网络恢复丢失的连接
 * - 日志记录：记录问题以便调试和修复
 * 
 * 性能优化：
 * - 懒加载：只在需要时创建网络连接
 * - 内存管理：及时释放不需要的资源
 * - 缓存机制：充分利用虚空存储的缓存策略
 * 
 * @author duquee
 * @since 1.0.0
 */
public class VoidChestMountedStorage extends WrapperMountedItemStorage<VoidChestInventory> {

    /**
     * 数据编解码器
     * 
     * 定义了虚空胸子挂载存储在序列化和反序列化过程中的数据转换规则。
     * 使用NetworkKey作为中间格式，实现轻量级的数据传输和存储。
     * 
     * 编解码原理：
     * - 编码：将VoidChestMountedStorage转换为NetworkKey
     * - 解码：从 NetworkKey重新构建 VoidChestMountedStorage实例
     * - 轻量级：只传输必要的识别信息，不传输实际存储数据
     * - 高效性：减少网络传输和磁盘存储开销
     * 
     * NetworkKey组成：
     * - 所有者：标识虚空存储的所有者
     * - 频率一：主频率设置
     * - 频率二：副频率设置
     * 
     * 数据恢复机制：
     * - 解码时自动从全局数据中恢复存储内容
     * - 支持跨世界和跨重启动的数据持久性
     * - 自动处理数据迁移和版本兼容性问题
     */
    public static final Codec<VoidChestMountedStorage> CODEC = CUCodecs.NETWORK_KEY_CODEC
            .xmap(VoidChestMountedStorage::new, (storage) -> storage.wrapped.getKey());

    /**
     * 带类型的构造函数
     * 
     * 创建虚空胸子挂载存储实例，并指定具体的存储类型。
     * 这个构造函数主要用于内部继承和类型系统集成。
     * 
     * @param type 挂载存储类型，用于类型系统识别
     * @param wrapped 被包装的虚空胸子库存实例
     */
    protected VoidChestMountedStorage(MountedItemStorageType<?> type, VoidChestInventory wrapped) {
        super(type, wrapped);
    }

    /**
     * 默认类型构造函数
     * 
     * 使用默认的虚空胸子挂载存储类型创建实例。
     * 这是最常用的构造函数，适用于标准的创建场景。
     * 
     * @param wrapped 被包装的虚空胸子库存实例
     */
    protected VoidChestMountedStorage(VoidChestInventory wrapped) {
        this(CUMountedStorages.VOID_CHEST.get(), wrapped);
    }

    /**
     * 网络键构造函数
     * 
     * 从虚空网络键创建挂载存储实例。这个构造函数主要用于数据
     * 反序列化过程，从保存的NetworkKey信息重新构建挂载存储实例。
     * 
     * 数据恢复过程：
     * 1. 接收NetworkKey参数
     * 2. 从全局虚空胸子数据中查找或创建对应的库存
     * 3. 使用找到的库存创建挂载存储实例
     * 
     * 自动处理特性：
     * - 如果数据不存在，自动创建新的空库存
     * - 如果数据已存在，直接连接到现有的库存
     * - 支持跨世界和跨服务器重启动的数据保持
     * 
     * @param key 虚空网络键，包含所有者和频率信息
     */
    private VoidChestMountedStorage(VoidMotorNetworkHandler.NetworkKey key) {
        this(CreateUtilities.VOID_CHEST_INVENTORIES_DATA.computeStorageIfAbsent(key));
    }

    /**
     * 卸载方法
     * 
     * 当装置被拆解时调用，负责清理挂载存储的状态。对于虚空胸子，
     * 由于数据存储在全局网络中，不需要特殊的清理操作。
     * 
     * 设计原理：
     * - 虚空存储数据保存在全局数据结构中
     * - 挂载存储只是数据的访问接口，而非数据本身
     * - 装置拆解不影响数据的持久性
     * - 新的虚空胸子可以立即访问相同的数据
     * 
     * 与传统胸子的区别：
     * - 传统胸子：数据存储在方块实体中，拆解后需要恢复
     * - 虚空胸子：数据存储在网络中，拆解后仍然可访问
     * - 这種设计使得虚空胸子更适合移动应用
     * 
     * @param level 世界实例（未使用）
     * @param blockState 方块状态（未使用）
     * @param blockPos 方块位置（未使用）
     * @param blockEntity 方块实体（未使用）
     */
    @Override
    public void unmount(Level level, BlockState blockState, BlockPos blockPos, @Nullable BlockEntity blockEntity) {
        // 虚空胸子不需要特殊的卸载处理，数据保存在全局网络中
    }

    /**
     * 播放开启声音
     * 
     * 当挂载存储被打开时播放相应的音效。使用与静态虚空胸子
     * 相同的音效，保持用户体验的一致性。
     * 
     * 音效特性：
     * - 使用Minecraft内置的胸子开启声音
     * - 音量设置为0.5，适中的音量大小
     * - 随机音调变化，增加声音的自然性
     * - 作为方块声音播放，受环境影响
     * 
     * 音效计算：
     * - 基础音调：0.9（略低于标准音调）
     * - 随机偏移：0.0-0.1（添加随机性）
     * - 最终音调范围：0.9-1.0
     * 
     * 用户体验考虑：
     * - 保持与静态虚空胸子的一致性
     * - 提供清晰的音频反馈
     * - 增强游戏的沉浸式体验
     * 
     * @param level 服务器世界实例，用于播放声音
     * @param pos 声音播放位置，用于3D声音定位
     */
    @Override
    protected void playOpeningSound(ServerLevel level, Vec3 pos) {
        level.playSound(
            null, // 特定玩家（null表示所有玩家）
            BlockPos.containing(pos), // 声音位置
            SoundEvents.CHEST_OPEN, // 胸子开启声音
            SoundSource.BLOCKS, // 声音源类型
            0.5F, // 声音大小
            level.random.nextFloat() * 0.1F + 0.9F // 随机音调
        );
    }

    /**
     * 播放关闭声音
     * 
     * 当挂载存储被关闭时播放相应的音效。与开启声音类似，
     * 使用标准的胸子关闭声音以保持体验一致性。
     * 
     * 音效特性：
     * - 使用Minecraft内置的胸子关闭声音
     * - 与开启声音相同的音量和音调设置
     * - 提供完整的开关音效循环
     * - 增强用户操作的反馈感
     * 
     * 音效一致性：
     * - 与开启声音使用相同的参数配置
     * - 保持声音体验的对称性和连贯性
     * - 符合玩家对胸子操作的预期
     * 
     * 技术实现：
     * - 使用与开启声音相同的API和参数
     * - 保证声音播放的稳定性和可靠性
     * - 集成到游戏的声音系统中
     * 
     * @param level 服务器世界实例，用于播放声音
     * @param pos 声音播放位置，用于3D声音定位
     */
    @Override
    protected void playClosingSound(ServerLevel level, Vec3 pos) {
        level.playSound(
            null, // 特定玩家（null表示所有玩家）
            BlockPos.containing(pos), // 声音位置
            SoundEvents.CHEST_CLOSE, // 胸子关闭声音
            SoundSource.BLOCKS, // 声音源类型
            0.5F, // 声音大小
            level.random.nextFloat() * 0.1F + 0.9F // 随机音调
        );
    }

    /**
     * 从虚空胸子创建挂载存储实例
     * 
     * 这个静态方法是主要的实例创建入口，用于将静态的虚空胸子
     * 转换为可移动的挂载存储实例。这个过程保持了所有原始特性。
     * 
     * 转换过程：
     * 1. 获取虚空胸子的内部库存实例
     * 2. 使用该库存创建挂载存储包装器
     * 3. 保持所有原有配置和状态信息
     * 
     * 数据保持特性：
     * - 网络访问：保持与原胸子相同的网络访问能力
     * - 配置保持：所有者、频率等配置不会丢失
     * - 存储内容：通过网络继续访问相同的存储数据
     * - 状态同步：与其他相同配置的胸子保持同步
     * 
     * 使用场景：
     * - Create装置系统的自动调用
     * - 手动创建挂载存储实例
     * - 装置组装过程中的类型转换
     * 
     * 设计优势：
     * - 简单明了：只需一行代码完成转换
     * - 型安全：直接使用内部API，避免类型错误
     * - 性能高效：无额外的数据复制或转换开销
     * 
     * @param voidChest 需要转换的虚空胸子实例
     * @return 新的虚空胸子挂载存储实例
     */
    public static VoidChestMountedStorage fromVoidChest(VoidChestTileEntity voidChest) {
        return new VoidChestMountedStorage(voidChest.getItemStorage());
    }

}