package me.duquee.createutilities;

import me.duquee.createutilities.blocks.CUPartialsModels;
import me.duquee.createutilities.blocks.voidtypes.VoidStorageClient;
import me.duquee.createutilities.blocks.voidtypes.battery.VoidBattery;
import me.duquee.createutilities.blocks.voidtypes.tank.VoidTank;
import me.duquee.createutilities.ponder.CUPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Create Utilities 客户端初始化类
 * 
 * 这个类负责所有客户端专用的初始化逻辑，包括：
 * - 客户端虚空存储缓存管理
 * - 渲染相关组件注册
 * - Ponder教程系统集成
 * - 客户端特有的事件处理
 * 
 * 客户端缓存系统：
 * 为了提供流畅的用户体验，客户端维护本地的虚空存储缓存。
 * 当服务器端虚空存储发生变化时，通过网络数据包同步更新客户端缓存，
 * 确保UI显示的内容与服务器状态保持一致。
 */
public class CreateUtilitiesClient {

	/**
	 * 虚空储罐客户端缓存管理器
	 * 
	 * 管理客户端的虚空储罐流体数据缓存。每个网络键对应一个FluidTank实例，
	 * 容量与服务器端保持一致。当收到VoidTankUpdatePacket时会更新对应的缓存。
	 * 
	 * 缓存策略：
	 * - 按需创建：只为实际使用的网络键创建缓存
	 * - 容量匹配：客户端缓存容量与服务器端VoidTank.CAPACITY一致
	 * - 实时同步：通过网络数据包保持与服务器同步
	 */
	public static final VoidStorageClient<FluidTank> VOID_TANKS = new VoidStorageClient<>(
			k -> new FluidTank(VoidTank.CAPACITY));

	/**
	 * 虚空电池客户端缓存管理器
	 * 
	 * 管理客户端的虚空电池能量数据缓存。每个网络键对应一个VoidBattery实例，
	 * 用于在客户端显示正确的能量存储状态和工程师护目镜信息。
	 * 
	 * 缓存特性：
	 * - 延迟创建：通过lambda表达式实现按需创建
	 * - 状态同步：接收VoidBatteryUpdatePacket更新能量状态
	 * - UI集成：为护目镜和GUI提供实时数据
	 */
	public static final VoidStorageClient<VoidBattery> VOID_BATTERIES = new VoidStorageClient<>(
			VoidBattery::new);

	/**
	 * 客户端构造时初始化方法
	 * 
	 * 在客户端环境下被CreateUtilities.onCtor()调用，负责设置所有客户端专用功能：
	 * - 注册客户端初始化事件监听器
	 * - 初始化部分模型（Partial Models）系统
	 * - 准备渲染相关组件
	 * 
	 * @param modEventBus 模组事件总线，用于监听模组加载阶段的事件
	 * @param forgeEventBus Forge事件总线，用于监听游戏运行时的事件（当前未使用但保留扩展性）
	 */
	public static void onCtorClient(IEventBus modEventBus, IEventBus forgeEventBus) {
		// 注册客户端设置事件监听器，在FMLClientSetupEvent阶段执行clientInit
		modEventBus.addListener(CreateUtilitiesClient::clientInit);
		
		// 初始化部分模型系统
		// 部分模型用于复杂方块的动态渲染，如旋转的齿轮、移动的组件等
		CUPartialsModels.init();
	}

	/**
	 * 客户端设置阶段初始化方法
	 * 
	 * 在FMLClientSetupEvent阶段执行，此时客户端的基础系统已经准备就绪，
	 * 可以安全地注册复杂的客户端功能。
	 * 
	 * 主要任务：
	 * - 注册Ponder教程插件
	 * - 配置渲染器和特殊效果
	 * - 设置客户端专用的事件处理器
	 * 
	 * @param event FML客户端设置事件
	 */
	public static void clientInit(final FMLClientSetupEvent event) {
		// 注册Ponder教程插件
		// Ponder是Create模组的交互式教程系统，允许在游戏内展示3D动画教程
		// CUPonderPlugin包含了所有虚空存储方块的使用教程和演示动画
		PonderIndex.addPlugin(new CUPonderPlugin());
	}

}
