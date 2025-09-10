package me.duquee.createutilities;

import com.simibubi.create.foundation.data.CreateRegistrate;
import me.duquee.createutilities.blocks.CUBlocks;
import me.duquee.createutilities.blocks.CUTileEntities;
import me.duquee.createutilities.blocks.voidtypes.CUContainerTypes;
import me.duquee.createutilities.blocks.voidtypes.battery.VoidBatteryData;
import me.duquee.createutilities.blocks.voidtypes.chest.VoidChestInventoriesData;
import me.duquee.createutilities.blocks.voidtypes.motor.VoidMotorNetworkHandler;
import me.duquee.createutilities.blocks.voidtypes.tank.VoidTanksData;
import me.duquee.createutilities.items.CUItems;
import me.duquee.createutilities.mountedstorage.CUMountedStorages;
import me.duquee.createutilities.networking.CUPackets;
import me.duquee.createutilities.tabs.CUCreativeTabs;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create Utilities 主模组类
 * 
 * 这是Create Utilities模组的核心入口点，负责整个模组的初始化和生命周期管理。
 * 该模组为Create模组添加了革命性的"虚空存储"功能，允许玩家在不同维度、
 * 不同位置之间共享物品、流体、能量和旋转力。
 * 
 * 主要功能模块：
 * - 虚空箱子：跨维度物品存储共享
 * - 虚空储罐：跨维度流体存储共享
 * - 虚空电池：跨维度能量存储共享
 * - 虚空马达：跨维度旋转力传输
 * - L型齿轮箱和齿轮立方体：增强的动力传输组件
 * 
 * 技术特性：
 * - 基于网络键的存储映射系统
 * - 客户端-服务器数据同步机制
 * - 与Create模组的深度集成
 * - Forge能力系统的完整支持
 */
@Mod(CreateUtilities.ID)
public class CreateUtilities {

	/**
	 * 模组ID - 用于唯一标识此模组
	 * 用于资源定位、注册表命名和网络通信标识
	 */
	public static final String ID = "createutilities";
	
	/**
	 * 模组显示名称 - 用于日志输出和用户界面显示
	 */
	public static final String NAME = "Create Utilities";
	
	/**
	 * 日志记录器 - 用于模组运行时的日志输出和调试
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	/**
	 * Create Registrate实例 - Create模组的注册管理器
	 * 
	 * Registrate是Create模组提供的强大注册系统，提供了：
	 * - 链式API简化注册流程
	 * - 自动生成模型和材质
	 * - 集成的配方和战利品表生成
	 * - 统一的本地化支持
	 */
	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID);

	/**
	 * 虚空马达网络处理器 - 管理虚空马达之间的旋转力传输网络
	 * 
	 * 负责维护虚空马达网络的连接状态，处理跨维度的旋转力传输。
	 * 每个世界维度都有独立的网络映射，确保维度间的网络隔离。
	 */
	public static final VoidMotorNetworkHandler VOID_MOTOR_LINK_NETWORK_HANDLER = new VoidMotorNetworkHandler();
	
	/**
	 * 虚空箱子库存数据管理器
	 * 在服务器启动时初始化，管理所有虚空箱子的物品存储数据
	 */
	public static VoidChestInventoriesData VOID_CHEST_INVENTORIES_DATA;

	/**
	 * 虚空储罐数据管理器  
	 * 在服务器启动时初始化，管理所有虚空储罐的流体存储数据
	 */
	public static VoidTanksData VOID_TANKS_DATA;
	
	/**
	 * 虚空电池数据管理器
	 * 在服务器启动时初始化，管理所有虚空电池的能量存储数据
	 */
	public static VoidBatteryData VOID_BATTERIES_DATA;

	/**
	 * 主构造函数
	 * 
	 * Forge模组加载器会自动调用此构造函数来创建模组实例。
	 * 构造函数只是简单地委托给onCtor()方法进行实际的初始化工作。
	 */
	public CreateUtilities() {
		onCtor();
	}

	/**
	 * 模组构造时初始化方法
	 * 
	 * 这是模组初始化的核心方法，负责：
	 * - 设置事件总线监听器
	 * - 注册所有模组内容（方块、物品、方块实体等）
	 * - 配置客户端专用功能
	 * - 准备网络通信系统
	 * 
	 * 执行顺序：
	 * 1. 获取模组和Forge事件总线
	 * 2. 注册Registrate事件监听器
	 * 3. 注册所有游戏内容
	 * 4. 设置通用初始化事件监听器
	 * 5. 配置客户端专用功能（仅在客户端执行）
	 */
	public static void onCtor() {

		// 获取模组事件总线（用于模组加载阶段的事件）
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		// 获取Forge事件总线（用于游戏运行时的事件）
		IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

		// 注册Registrate的事件监听器，用于自动化注册流程
		REGISTRATE.registerEventListeners(modEventBus);

		// 注册所有游戏内容 - 顺序很重要，依赖关系需要先注册
		CUBlocks.register();           // 注册方块
		CUItems.register();            // 注册物品
		CUTileEntities.register();     // 注册方块实体类型
		CUContainerTypes.register();   // 注册容器类型（用于GUI）
		CUCreativeTabs.register(modEventBus); // 注册创造模式选项卡
		CUMountedStorages.register();  // 注册挂载存储类型

		// 注册通用设置事件监听器，用于网络数据包注册等后期初始化
		modEventBus.addListener(CreateUtilities::init);
		
		// 仅在客户端环境下执行客户端专用初始化
		// DistExecutor.unsafeRunWhenOn确保代码只在指定环境下运行
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
				CreateUtilitiesClient.onCtorClient(modEventBus, forgeEventBus)
		);

	}

	/**
	 * 通用设置阶段初始化方法
	 * 
	 * 在FMLCommonSetupEvent阶段执行，此时所有模组的基础注册都已完成，
	 * 可以安全地进行跨模组交互和网络设置。
	 * 
	 * 主要任务：
	 * - 注册网络数据包处理器
	 * - 设置跨模组兼容性
	 * - 初始化复杂的系统组件
	 * 
	 * @param event FML通用设置事件
	 */
	public static void init(final FMLCommonSetupEvent event) {
		// 注册所有网络数据包类型和处理器
		// 必须在CommonSetup阶段注册，确保客户端和服务器都能正确处理数据包
		CUPackets.registerPackets();
	}

	/**
	 * 创建资源定位器
	 * 
	 * 这是一个便利方法，用于创建带有模组命名空间的ResourceLocation。
	 * ResourceLocation用于唯一标识游戏中的资源（纹理、模型、配方等）。
	 * 
	 * 使用示例：
	 * - asResource("void_chest") -> "createutilities:void_chest"
	 * - asResource("textures/block/void_steel.png") -> "createutilities:textures/block/void_steel.png"
	 * 
	 * @param path 资源路径，不包含命名空间前缀
	 * @return 完整的资源定位器，格式为 "createutilities:path"
	 */
	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(ID, path);
	}
}
