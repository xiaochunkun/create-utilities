package me.duquee.createutilities.ponder;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import me.duquee.createutilities.blocks.CUBlocks;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

/**
 * Create Utilities Ponder场景注册类
 * 
 * 这个类是Create Utilities模组的Ponder教程系统的中央注册中心。
 * 它负责将所有模组功能的互动教程场景与对应的方块绑定，
 * 让玩家能够在游戏中学习如何使用各种新功能。
 * 
 * 核心功能：
 * - 教程场景注册：为每个功能方块注册对应的Ponder教程
 * - 场景路由：建立方块与教程内容之间的映射关系
 * - 集中管理：统一管理所有模组相关的教程资源
 * - 自动发现：通过Ponder系统自动显示在合适的GUI中
 * 
 * Ponder教程系统简介：
 * Ponder是Create模组开发的沉浸式教程系统，通过3D场景演示
 * 和分步指导帮助玩家学习复杂的机械系统。它支持：
 * - 动态3D场景渲染和演示
 * - 分步骤的文字说明和指导
 * - 交互式控件展示（如右键、放置物品等）
 * - 时间轴控制和关键帧系统
 * 
 * 注册的教程分类：
 * 1. 虚空存储系统教程：
 *    - 虚空马达：展示无限动力传输
 *    - 虚空箱子：演示跨维度物品存储
 *    - 虚空储罐：说明流体远程共享
 *    - 虚空电池：介绍能量网络传输
 * 
 * 2. 齿轮传动系统教程：
 *    - 齿轮立方体：6方向全向传动
 *    - L型齿轮箱：紧凑直角传动
 * 
 * 设计理念：
 * - 渐进学习：从基础功能到高级应用
 * - 实例驱动：通过具体案例展示最佳实践
 * - 互动体验：鼓励玩家主动探索和实验
 * - 系统整合：展示不同组件之间的协作关系
 * 
 * @author duquee
 * @since 1.0.0
 */
public class CUPonders {

	/**
	 * 注册所有Ponder教程场景
	 * 
	 * 这是模组教程系统的核心注册方法，在模组初始化期间被调用。
	 * 它建立了方块与教程场景之间的映射关系，使得玩家在查看
	 * 相关方块时能够自动看到相应的教程内容。
	 * 
	 * 注册流程：
	 * 1. 创建类型安全的注册助手，使用方块注册表的ID作为键
	 * 2. 为每个功能方块注册对应的故事板场景
	 * 3. 将场景名称与实际的场景方法进行绑定
	 * 4. Ponder系统会自动处理场景的加载和显示
	 * 
	 * 场景命名约定：
	 * - 使用下划线分隔的小写命名（如"void_motor"）
	 * - 场景名称应简洁明了，反映功能特性
	 * - 保持与方块名称的一致性，便于理解
	 * 
	 * @param helper Ponder场景注册助手，提供场景绑定功能
	 */
	public static void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {

		// 创建类型安全的注册助手，使用注册表ID作为键函数
		PonderSceneRegistrationHelper<ItemProviderEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

		// 注册虚空存储系统教程场景
		HELPER.addStoryBoard(CUBlocks.VOID_MOTOR, "void_motor", VoidScenes::voidMotor);         // 虚空马达教程
		HELPER.addStoryBoard(CUBlocks.VOID_CHEST, "void_chest", VoidScenes::voidChest);         // 虚空箱子教程
		HELPER.addStoryBoard(CUBlocks.VOID_TANK, "void_tank", VoidScenes::voidTank);            // 虚空储罐教程
		HELPER.addStoryBoard(CUBlocks.VOID_BATTERY, "void_battery", VoidScenes::voidBattery);   // 虚空电池教程

		// 注册齿轮传动系统教程场景
		HELPER.addStoryBoard(CUBlocks.GEARCUBE, "gearcube", GearboxScenes::gearCube);                              // 齿轮立方体教程
		HELPER.addStoryBoard(CUBlocks.LSHAPED_GEARBOX, "lshaped_gearbox", GearboxScenes::lShapedGearbox);         // L型齿轮箱教程

	}

}
