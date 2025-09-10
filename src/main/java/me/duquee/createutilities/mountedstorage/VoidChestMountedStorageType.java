package me.duquee.createutilities.mountedstorage;

import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import me.duquee.createutilities.blocks.voidtypes.chest.VoidChestTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 虚空胸子挂载存储类型
 * 
 * 这个类是虚空胸子的挂载存储类型实现，负责处理虚空胸子在Create装置
 * 系统中的挂载和操作。它将静态的虚空胸子转换为可移动的挂载存储。
 * 
 * 主要职责：
 * - 类型匹配：验证方块实体是否为虚空胸子
 * - 实例创建：为匹配的胸子创建挂载存储实例
 * - 编解码器管理：提供序列化和反序列化支持
 * - 生命周期管理：管理挂载存储的创建和销毁
 * 
 * 挂载过程：
 * 1. 装置系统调用mount方法
 * 2. 检查方块实体类型
 * 3. 验证是否为虚空胸子实体
 * 4. 创建并返回挂载存储实例
 * 
 * 技术实现：
 * - 继承MountedItemStorageType，获得基础挂载功能
 * - 使用VoidChestMountedStorage.CODEC进行数据序列化
 * - 通过类型检查确保只处理虚空胸子
 * - 使用@Nullable注释确保空安全
 * 
 * 虚空胸子特性保持：
 * - 网络访问：保持与虚空存储网络的连接
 * - 数据同步：与原始胸子共享相同的存储数据
 * - 配置保持：挂载后保持频率和所有者配置
 * - 功能一致：挂载状态下的行为与静态状态相同
 * 
 * 错误处理：
 * - 类型不匹配时返回null，不抛出异常
 * - 空实体检查，避免空指针异常
 * - 优雅降级，不影响其他存储类型的挂载
 * 
 * 性能考虑：
 * - 快速类型检查：使用instanceof进行高效类型判断
 * - 轻量实例：挂载存储实例不保留不必要的引用
 * - 内存优化：及时释放不需要的资源
 * 
 * @author duquee
 * @since 1.0.0
 */
public class VoidChestMountedStorageType extends MountedItemStorageType<VoidChestMountedStorage> {

    /**
     * 构造函数
     * 
     * 初始化虚空胸子挂载存储类型，设置必要的编解码器。
     * 
     * 编解码器配置：
     * - 使用VoidChestMountedStorage.CODEC进行数据序列化
     * - 支持装置保存和加载时的数据持久化
     * - 保证挂载存储在不同状态间的数据一致性
     * - 兼容网络同步和本地存储
     */
    public VoidChestMountedStorageType() {
        super(VoidChestMountedStorage.CODEC);
    }

    /**
     * 挂载方法
     * 
     * 将静态的虚空胸子转换为可移动的挂载存储实例。这个方法是Create
     * 装置系统的核心入口点，在装置组装过程中被调用。
     * 
     * 挂载流程：
     * 1. 接收装置系统传入的方块信息
     * 2. 验证方块实体是否为虚空胸子类型
     * 3. 如果匹配，创建新的挂载存储实例
     * 4. 如果不匹配，返回null表示无法处理
     * 
     * 类型安全：
     * - 使用instanceof进行类型检查
     * - 使用pattern matching简化类型转换
     * - @Nullable注释明确表示可能返回null
     * - 避免强制类型转换的风险
     * 
     * 数据保持：
     * - 挂载过程中保持原始胸子的所有特性
     * - 网络配置（所有者、频率）完整传递
     * - 存储内容通过网络访问保持同步
     * - 胸子状态（开关等）信息保持
     * 
     * 错误处理：
     * - 类型不匹配时返回null，不抛出异常
     * - 方块实体为null时返回null
     * - 无副作用，不修改输入参数
     * 
     * @param level 世界实例，提供世界环境上下文
     * @param state 方块状态，包含方块的属性信息
     * @param pos 方块位置，用于定位方块实体
     * @param be 方块实体，可能为null
     * @return 挂载存储实例（如果匹配）或null（如果不匹配）
     */
    public @Nullable VoidChestMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        // 使用pattern matching检查并转换类型
        if (be instanceof VoidChestTileEntity voidChest) {
            // 从虚空胸子创建挂载存储实例
            return VoidChestMountedStorage.fromVoidChest(voidChest);
        } else {
            // 类型不匹配，返回null表示无法处理
            return null;
        }
    }

}