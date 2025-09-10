package me.duquee.createutilities;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import me.duquee.createutilities.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * Create Utilities 编解码器集合
 * 
 * 这个类定义了模组中用于数据序列化和反序列化的各种Codec编解码器。
 * Codec是Minecraft/Forge用于数据持久化和网络传输的现代序列化系统，
 * 比传统的NBT手工序列化更加类型安全和易维护。
 * 
 * 编解码器的作用：
 * - 数据持久化：将复杂对象保存到磁盘
 * - 网络传输：在客户端和服务器之间安全传输数据
 * - 配置文件：支持JSON格式的配置文件读写
 * - 命令系统：支持复杂数据类型的命令参数
 * 
 * 设计原则：
 * - 类型安全：编译时检查数据结构
 * - 向后兼容：支持数据格式的版本演进
 * - 性能优化：比反射和NBT手工序列化更高效
 * - 易于维护：声明式的数据结构定义
 */
public class CUCodecs {

    /**
     * UUID编解码器
     * 
     * 将Java UUID对象编码为包含mostSigBits和leastSigBits的结构。
     * UUID在虚空存储系统中用于唯一标识玩家，是权限控制的基础。
     * 
     * 数据结构：
     * - mostSigBits: 64位高位数据（Long类型）
     * - leastSigBits: 64位低位数据（Long类型）
     * 
     * 使用场景：
     * - 玩家身份识别
     * - 虚空存储网络的所有权管理
     * - 权限验证和访问控制
     */
    public static final Codec<UUID> UUID_CODEC = RecordCodecBuilder.create((instance) ->
            instance.group( Codec.LONG.fieldOf("mostSigBits").forGetter(UUID::getMostSignificantBits),
                            Codec.LONG.fieldOf("leastSigBits").forGetter(UUID::getLeastSignificantBits))
                    .apply(instance, UUID::new));

    /**
     * 游戏档案编解码器
     * 
     * 将Mojang GameProfile对象编码为包含UUID和用户名的结构。
     * GameProfile是Minecraft用于表示玩家身份的标准对象，包含了
     * 玩家的唯一标识符和显示名称，是整个权限系统的核心。
     * 
     * 数据结构：
     * - uuid: 玩家的唯一标识符（使用UUID_CODEC编码）
     * - name: 玩家的显示名称（String类型）
     * 
     * 使用场景：
     * - 虚空存储的所有权记录
     * - 权限检查和访问控制
     * - 用户界面中显示所有者信息
     * - 审计日志和操作记录
     */
    public static final Codec<GameProfile> GAME_PROFILE_CODEC = RecordCodecBuilder.create((instance) ->
            instance.group( UUID_CODEC.fieldOf("uuid").forGetter(GameProfile::getId),
                            Codec.STRING.fieldOf("name").forGetter(GameProfile::getName))
                    .apply(instance, GameProfile::new));

    /**
     * 网络键编解码器
     * 
     * 这是虚空存储系统的核心编解码器，用于序列化NetworkKey对象。
     * NetworkKey是虚空网络的唯一标识符，由所有者信息和两个频率组成，
     * 决定了哪些虚空方块可以共享存储内容。
     * 
     * 数据结构：
     * - owner: 网络所有者（使用GAME_PROFILE_CODEC编码）
     * - frequency1: 第一个频率标识符（ItemStack，表示频率物品）
     * - frequency2: 第二个频率标识符（ItemStack，表示频率物品）
     * 
     * 编解码逻辑：
     * 1. 序列化时：提取NetworkKey的所有者和两个频率的ItemStack
     * 2. 反序列化时：使用所有者和两个ItemStack重建NetworkKey
     * 3. 自动处理Frequency对象的创建和转换
     * 
     * 使用场景：
     * - 虚空存储数据的持久化
     * - 网络传输中的键值同步
     * - 配置文件中的网络设置
     * - 调试和日志记录
     * 
     * 安全考虑：
     * - 所有者信息确保访问控制
     * - 频率组合提供网络隔离
     * - 类型安全的序列化防止数据损坏
     */
    public static final Codec<NetworkKey> NETWORK_KEY_CODEC = RecordCodecBuilder.create((instance) ->
            instance.group( GAME_PROFILE_CODEC.fieldOf("owner").forGetter((key) -> key.owner),
                            ItemStack.CODEC.fieldOf("frequency1").forGetter((key) -> key.frequencies.get(true).getStack()),
                            ItemStack.CODEC.fieldOf("frequency2").forGetter((key) -> key.frequencies.get(false).getStack()))
                    .apply(instance, (owner, f1, f2) -> new NetworkKey(owner, Frequency.of(f1), Frequency.of(f2))));

}
