package me.duquee.createutilities.ponder;

import me.duquee.createutilities.CreateUtilities;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Create Utilities Ponder插件实现类
 * 
 * 这个类是Create Utilities模组与Ponder教程系统的桥梁接口实现。
 * 通过实现PonderPlugin接口，该类使得模组能够无缝集成到Create的
 * Ponder教程生态系统中，为玩家提供统一的学习体验。
 * 
 * Ponder插件系统原理：
 * Ponder使用插件架构来发现和加载不同模组的教程内容。
 * 每个模组通过实现PonderPlugin接口来注册自己的教程场景，
 * 系统会在初始化时自动发现并注册这些插件。
 * 
 * 核心职责：
 * - 模组标识：提供模组的唯一标识符用于插件识别
 * - 场景代理：将教程场景注册委托给专门的注册类处理
 * - 系统集成：确保模组教程能够正确显示在Ponder界面中
 * - 依赖管理：处理与Create模组Ponder系统的依赖关系
 * 
 * 插件生命周期：
 * 1. 系统扫描：Ponder系统在启动时扫描所有实现PonderPlugin的类
 * 2. 插件识别：通过getModId()方法识别插件所属的模组
 * 3. 场景注册：调用registerScenes()方法注册所有教程场景
 * 4. 界面集成：将注册的场景集成到Ponder的用户界面中
 * 
 * 设计模式：
 * - 代理模式：将实际的注册逻辑委托给CUPonders类处理
 * - 接口分离：保持插件接口的简洁性，将复杂逻辑分离
 * - 单一职责：仅负责与Ponder系统的集成，不处理具体业务
 * 
 * 技术特性：
 * - 类型安全：使用泛型确保场景注册的类型安全性
 * - 自动发现：通过服务提供者接口(SPI)机制自动被系统发现
 * - 版本兼容：与Create模组的Ponder版本保持兼容
 * - 错误隔离：插件的错误不会影响其他模组的教程系统
 * 
 * @author duquee
 * @since 1.0.0
 */
public class CUPonderPlugin implements PonderPlugin {

    /**
     * 获取模组标识符
     * 
     * 返回Create Utilities模组的唯一标识符，用于Ponder系统
     * 识别和管理这个插件。这个标识符必须与模组的实际ID完全一致，
     * 确保教程场景能够正确关联到相应的模组内容。
     * 
     * 标识符的重要性：
     * - 唯一性标识：在多模组环境中唯一标识此插件
     * - 资源隔离：确保教程资源不与其他模组冲突
     * - 版本管理：用于处理模组版本兼容性问题
     * - 调试支持：便于在日志中识别和定位问题
     * 
     * @return 模组ID字符串，对应CreateUtilities.ID常量
     */
    @Override
    public @NotNull String getModId() {
        return CreateUtilities.ID;
    }

    /**
     * 注册Ponder教程场景
     * 
     * 这是插件的核心方法，负责将模组的所有教程场景注册到
     * Ponder系统中。方法接收一个注册助手实例，该助手提供了
     * 类型安全的场景注册功能。
     * 
     * 实现策略：
     * 使用代理模式将实际的注册逻辑委托给CUPonders类处理。
     * 这种设计分离了接口实现和业务逻辑，使代码更加清晰
     * 和易于维护。
     * 
     * 注册流程：
     * 1. 接收Ponder系统提供的注册助手实例
     * 2. 将注册任务委托给专门的CUPonders.registerScenes()方法
     * 3. CUPonders负责具体的场景注册逻辑
     * 4. 注册完成后，所有场景将自动出现在Ponder界面中
     * 
     * 错误处理：
     * 如果注册过程中发生错误，Ponder系统会捕获异常并记录日志，
     * 不会影响其他模组的教程功能或游戏的正常运行。
     * 
     * @param helper Ponder场景注册助手，提供场景注册功能
     */
    @Override
    public void registerScenes(@NotNull PonderSceneRegistrationHelper<ResourceLocation> helper) {
        CUPonders.registerScenes(helper);
    }

}
