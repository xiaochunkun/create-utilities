package me.duquee.createutilities.blocks;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.simibubi.create.content.kinetics.gearbox.GearboxBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import me.duquee.createutilities.blocks.gearcube.GearcubeVisual;
import me.duquee.createutilities.blocks.gearcube.SimpleKineticRenderer;
import me.duquee.createutilities.blocks.lgearbox.LShapedGearboxVisual;
import me.duquee.createutilities.blocks.voidtypes.battery.VoidBatteryRenderer;
import me.duquee.createutilities.blocks.voidtypes.battery.VoidBatteryTileEntity;
import me.duquee.createutilities.blocks.voidtypes.chest.VoidChestRenderer;
import me.duquee.createutilities.blocks.voidtypes.chest.VoidChestTileEntity;
import me.duquee.createutilities.blocks.voidtypes.motor.VoidMotorRenderer;
import me.duquee.createutilities.blocks.voidtypes.motor.VoidMotorTileEntity;
import me.duquee.createutilities.blocks.voidtypes.tank.VoidTankRenderer;
import me.duquee.createutilities.blocks.voidtypes.tank.VoidTankTileEntity;

import static me.duquee.createutilities.CreateUtilities.REGISTRATE;

/**
 * Create Utilities 方块实体注册类
 * 
 * 这个类负责注册所有的方块实体类型（BlockEntity/TileEntity），
 * 方块实体是那些需要存储复杂数据、执行逻辑或拥有特殊渲染的方块的后端实现。
 * 
 * 注册内容：
 * - 虚空存储系列方块实体：处理跨维度存储逻辑
 * - 动力传输系列方块实体：处理旋转力和机械逻辑
 * - 视觉效果和渲染器：提供精美的3D渲染效果
 * 
 * 技术特性：
 * - 使用Registrate的链式API简化注册
 * - 集成Create模组的渲染系统
 * - 支持动态视觉效果和部分模型
 * - 统一的渲染器架构
 */
public class CUTileEntities {

	/**
	 * 虚空马达方块实体注册
	 * 
	 * 虚空马达的方块实体，负责跨维度旋转力传输的核心逻辑。
	 * 
	 * 配置特性：
	 * - 使用VoidMotorTileEntity类处理虚空网络逻辑
	 * - OrientedRotatingVisual：定向旋转视觉效果，使用半轴部分模型
	 * - 启用实例化渲染（true）：提高性能
	 * - VoidMotorRenderer：专用渲染器，处理复杂的3D效果
	 * - 绑定到VOID_MOTOR方块类型
	 */
	public static final BlockEntityEntry<VoidMotorTileEntity> VOID_MOTOR = REGISTRATE
			.blockEntity("void_motor", VoidMotorTileEntity::new)
			.visual(() -> OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF), true)
			.validBlocks(CUBlocks.VOID_MOTOR)
			.renderer(() -> VoidMotorRenderer::new)
			.register();

	/**
	 * 虚空箱子方块实体注册
	 * 
	 * 虚空箱子的方块实体，管理跨维度物品存储和GUI交互。
	 * 
	 * 配置特性：
	 * - 使用VoidChestTileEntity类处理存储逻辑和动画
	 * - VoidChestRenderer：专用渲染器，处理箱盖动画和物品渲染
	 * - 绑定到VOID_CHEST方块类型
	 * - 支持开启/关闭动画和音效
	 */
	public static final BlockEntityEntry<VoidChestTileEntity> VOID_CHEST = REGISTRATE
			.blockEntity("void_chest", VoidChestTileEntity::new)
			.validBlocks(CUBlocks.VOID_CHEST)
			.renderer(() -> VoidChestRenderer::new)
			.register();

	/**
	 * 虚空储罐方块实体注册
	 * 
	 * 虚空储罐的方块实体，处理跨维度流体存储和液位显示。
	 * 
	 * 配置特性：
	 * - 使用VoidTankTileEntity类处理流体逻辑
	 * - VoidTankRenderer：专用渲染器，处理流体渲染和液位显示
	 * - 绑定到VOID_TANK方块类型
	 * - 支持透明窗口和流体动画
	 */
	public static final BlockEntityEntry<VoidTankTileEntity> VOID_TANK = REGISTRATE
			.blockEntity("void_tank", VoidTankTileEntity::new)
			.validBlocks(CUBlocks.VOID_TANK)
			.renderer(() -> VoidTankRenderer::new)
			.register();

	/**
	 * 虚空电池方块实体注册
	 * 
	 * 虚空电池的方块实体，管理跨维度能量存储和状态显示。
	 * 
	 * 配置特性：
	 * - 使用VoidBatteryTileEntity类处理能量逻辑
	 * - VoidBatteryRenderer：专用渲染器，处理能量指示器和动画
	 * - 绑定到VOID_BATTERY方块类型
	 * - 支持能量液晶显示和充放电动画
	 */
	public static final BlockEntityEntry<VoidBatteryTileEntity> VOID_BATTERY = REGISTRATE
			.blockEntity("void_battery", VoidBatteryTileEntity::new)
			.validBlocks(CUBlocks.VOID_BATTERY)
			.renderer(() -> VoidBatteryRenderer::new)
			.register();

	/**
	 * 齿轮立方体方块实体注册
	 * 
	 * 齿轮立方体的方块实体，复用Create的齿轮箱实体但使用自定义视觉效果。
	 * 
	 * 配置特性：
	 * - 使用GearboxBlockEntity：复用Create的成熟齿轮箱逻辑
	 * - GearcubeVisual：自定义视觉效果，展示六面齿轮
	 * - 禁用实例化渲染（false）：保持视觉效果的精确性
	 * - SimpleKineticRenderer：简化的动力学渲染器
	 * - 绑定到GEARCUBE方块类型
	 */
	public static final BlockEntityEntry<GearboxBlockEntity> GEARCUBE = REGISTRATE
			.blockEntity("gearcube", GearboxBlockEntity::new)
			.visual(() -> GearcubeVisual::new, false)
			.validBlocks(CUBlocks.GEARCUBE)
			.renderer(() -> SimpleKineticRenderer::new)
			.register();

	/**
	 * L型齿轮箱方块实体注册
	 * 
	 * L型齿轮箱的方块实体，提供90度角的旋转力传输功能。
	 * 
	 * 配置特性：
	 * - 使用GearboxBlockEntity：复用Create的齿轮箱核心逻辑
	 * - LShapedGearboxVisual：L型齿轮箱专用视觉效果
	 * - 禁用实例化渲染（false）：确保复杂几何体的正确渲染
	 * - SimpleKineticRenderer：统一的动力学渲染器
	 * - 绑定到LSHAPED_GEARBOX方块类型
	 */
	public static final BlockEntityEntry<GearboxBlockEntity> LSHAPED_GEARBOX = REGISTRATE
			.blockEntity("lshaped_gearbox", GearboxBlockEntity::new)
			.visual(() -> LShapedGearboxVisual::new, false)
			.validBlocks(CUBlocks.LSHAPED_GEARBOX)
			.renderer(() -> SimpleKineticRenderer::new)
			.register();

	/**
	 * 注册方法 - 触发所有方块实体的注册
	 * 
	 * 这是一个空方法，但其调用会触发静态字段的初始化，
	 * 从而完成所有方块实体的注册过程。这是Registrate模式的标准做法。
	 */
	public static void register() {}

}
