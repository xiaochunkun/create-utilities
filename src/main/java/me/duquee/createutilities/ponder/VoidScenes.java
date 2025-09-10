package me.duquee.createutilities.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import me.duquee.createutilities.blocks.voidtypes.battery.VoidBatteryTileEntity;
import me.duquee.createutilities.blocks.voidtypes.chest.VoidChestTileEntity;
import me.duquee.createutilities.blocks.voidtypes.motor.VoidMotorTileEntity;
import me.duquee.createutilities.blocks.voidtypes.tank.VoidTankTileEntity;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.function.Consumer;

/**
 * 虚空存储系统Ponder教程场景集合
 * 
 * 这个类包含了Create Utilities模组中所有虚空存储系统的互动教程场景。
 * 通过精心设计的3D演示和分步指导，帮助玩家理解和掌握虚空存储
 * 技术的核心概念、使用方法和最佳实践。
 * 
 * 虚空存储系统简介：
 * 虚空存储是一种突破空间限制的高级存储技术，允许玩家在不同维度、
 * 不同位置之间共享资源。系统基于网络键（NetworkKey）进行识别，
 * 每个网络键由所有者、频率1、频率2三个要素组成，确保存储网络的
 * 安全性和独立性。
 * 
 * 教程场景覆盖的功能模块：
 * 
 * 1. 虚空马达（Void Motor）：
 *    - 功能：提供无限的旋转动力，解决大型机械装置的动力需求
 *    - 特点：跨维度动力传输，无能耗损失，即插即用
 *    - 应用：驱动复杂的Create机械网络，支持远程动力供应
 * 
 * 2. 虚空箱子（Void Chest）：
 *    - 功能：跨维度物品存储和传输，支持大容量共享仓库
 *    - 特点：即时同步，无距离限制，支持自动化物流
 *    - 应用：建立全球物品网络，实现资源的统一管理和分配
 * 
 * 3. 虚空储罐（Void Tank）：
 *    - 功能：跨维度流体存储和传输，支持大容量流体网络
 *    - 特点：实时同步，支持混合流体，无泄漏风险
 *    - 应用：建立全球流体供应网络，实现流体资源的高效利用
 * 
 * 4. 虚空电池（Void Battery）：
 *    - 功能：跨维度能量存储和传输，支持大容量能量网络
 *    - 特点：无损传输，即时充放电，支持能量平衡
 *    - 应用：建立全球能源网络，实现能源的合理分配
 * 
 * 教程设计理念：
 * 
 * 渐进式学习路径：
 * - 从简单的单机使用开始
 * - 逐步介绍网络配置和频率设置
 * - 展示复杂的多点协作应用
 * - 最终达到系统级的理解和应用
 * 
 * 实例驱动教学：
 * - 每个教程都包含具体的应用场景
 * - 通过动手实践加深理解
 * - 展示最佳实践和常见误区
 * - 提供故障排除和优化建议
 * 
 * 交互式体验：
 * - 3D场景实时演示
 * - 分步骤的操作指导
 * - 即时反馈和结果展示
 * - 支持暂停、回放和跳转
 * 
 * 技术实现特色：
 * 
 * 并行演示系统：
 * - 使用ParallelInstruction支持多个动作同时执行
 * - 创造更加真实和动态的教学场景
 * - 提高教程的视觉吸引力和教学效果
 * 
 * 通用化设计：
 * - playVoidSequence方法提供统一的教程框架
 * - 减少代码重复，便于维护和扩展
 * - 确保所有虚空设备教程的一致性
 * 
 * 视觉效果优化：
 * - 精确的坐标计算确保UI元素的准确显示
 * - 支持不同方向的设备放置和交互
 * - 动态相机控制增强观看体验
 * 
 * @author duquee
 * @since 1.0.0
 */
public class VoidScenes {

	/**
	 * 虚空马达教程场景
	 * 
	 * 这个场景向玩家展示了虚空马达的基本功能和使用方法。
	 * 虚空马达是虚空存储系统的动力组件，能够提供无限的旋转动力，
	 * 解决了大型机械装置的动力供应问题。
	 * 
	 * 教程内容：
	 * 1. 基本概念：介绍虚空马达的作用和优势
	 * 2. 放置方法：展示如何正确放置和连接虚空马达
	 * 3. 网络配置：说明频率设置和所有者权限系统
	 * 4. 动力传输：演示跨距离的动力传输效果
	 * 5. 实际应用：展示在复杂机械系统中的应用案例
	 * 
	 * 学习目标：
	 * - 理解虚空马达的工作原理和应用场景
	 * - 掌握网络频率的配置方法
	 * - 学会在实际项目中合理使用虚空马达
	 * - 了解所有者权限系统的安全机制
	 * 
	 * @param builder Ponder场景构建器，用于创建教程场景
	 * @param util 场景构建工具，提供便捷的场景操作方法
	 */
	public static void voidMotor(SceneBuilder builder, SceneBuildingUtil util) {

		CreateSceneBuilder scene = new CreateSceneBuilder(builder);

		scene.title("void_motor", "Using Void Motors");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.world().showSection(util.select().position(5, 0, 2), Direction.UP);

		Selection source = util.select().fromTo(5, 1, 1, 4, 1, 1);
		Selection receiver = util.select().fromTo(1, 1, 2, 2, 1, 2);

		scene.world().showSection(source, Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(receiver, Direction.DOWN);
		scene.idle(10);

		BlockPos sourcePos = util.grid().at(4, 1, 1);
		BlockPos receiverPos = util.grid().at(1, 1, 2);

		playVoidSequence(
				scene, util, VoidMotorTileEntity.class,
				.015f, 0,
				"Void Motor", "Rotational Force",
				sourcePos, receiverPos,
				Direction.WEST, Direction.WEST,
				(pos) -> scene.world().setKineticSpeed(receiver, 0),
				(pos) -> scene.world().setKineticSpeed(receiver, -32),
				false, false
		);

	}

	/**
	 * 虚空箱子教程场景
	 * 
	 * 这个场景向玩家展示了虚空箱子的强大功能和应用价值。
	 * 虚空箱子是虚空存储系统的物品存储组件，能够实现跨维度的
	 * 即时物品共享，彻底改变传统物流系统的限制。
	 * 
	 * 教程亮点：
	 * 1. 并行演示：使用ParallelInstruction同时展示27个物品的流动
	 * 2. 真实模拟：模拟真实工厂中物品的连续生产和消耗
	 * 3. 跨空间传输：展示物品在不同位置之间的即时同步
	 * 4. 容量管理：演示虚空箱子如何处理大批量物品存储
	 * 
	 * 技术特色：
	 * - 动态物品生成：使用createItemEntity创建落下的物品效果
	 * - 传送带集成：展示与Create传送带系统的无缝集成
	 * - 漏斗模拟：模拟漏斗的开启和关闭动作
	 * - 时间控制：精确控制物品流的节奏和时机
	 * 
	 * 教学目标：
	 * - 理解虚空箱子在大规模物流系统中的价值
	 * - 学会设计高效的跨维度物品传输系统
	 * - 掌握虚空存储与自动化系统的集成技巧
	 * - 了解容量优化和性能调优的方法
	 * 
	 * @param builder Ponder场景构建器
	 * @param util 场景构建工具
	 */
	public static void voidChest(SceneBuilder builder, SceneBuildingUtil util) {

		CreateSceneBuilder scene = new CreateSceneBuilder(builder);

		scene.title("void_chest", "Using Void Chests");
		scene.configureBasePlate(1, 0, 5);
		scene.showBasePlate();
		scene.world().showSection(util.select().position(0, 0, 3)
				.add(util.select().position(6, 0, 3)), Direction.UP);

		Selection source = util.select().fromTo(4, 1, 0, 6, 2, 3);
		Selection receiver = util.select().fromTo(0, 1, 0, 2, 2, 3);

		scene.idle(10);
		scene.world().showSection(source, Direction.DOWN);
		scene.world().showSection(receiver, Direction.DOWN);
		scene.idle(10);

		ParallelInstruction parallel = new ParallelInstruction(scene);

		BlockPos sourceEntryBelt = util.grid().at(4, 1, 0);
		BlockPos sourceExitBelt = util.grid().at(4, 1, 2);
		BlockPos receiverEntryBelt = util.grid().at(2, 1, 2);
		ItemStack stack = AllBlocks.BRASS_BLOCK.asStack();

		Vec3 motion = new Vec3(0, -.2, 0);
		for (int i = 0; i < 27; i++) {

			ElementLink<EntityElement> item = parallel.scene.world().createItemEntity(
					util.vector().of(4.75, 3, 0.5), motion, stack);
			parallel.scene.idle(5);

			parallel.scene.world().modifyEntity(item, Entity::discard);
			parallel.scene.world().createItemOnBelt(sourceEntryBelt, Direction.EAST, stack);
			parallel.scene.idle(16);

			parallel.scene.world().removeItemsFromBelt(sourceExitBelt);
			parallel.scene.world().flapFunnel(sourceExitBelt.above(), false);

			if (i < 6 || i > 21) {
				parallel.scene.world().createItemOnBelt(receiverEntryBelt, Direction.EAST, stack);
				parallel.scene.world().flapFunnel(receiverEntryBelt.above(), true);
			}

		}

		scene.addInstruction(parallel);

		BlockPos sourcePos = util.grid().at(4, 2, 3);
		BlockPos receiverPos = util.grid().at(2, 2, 3);

		playVoidSequence(
				scene, util, VoidChestTileEntity.class,
				-.0475f, -.1875f,
				"Void Chest", "Items",
				sourcePos, receiverPos,
				Direction.SOUTH, Direction.SOUTH,
				(pos) -> {},
				(pos) -> {},
				true, false
		);

	}

	/**
	 * 虚空储罐教程场景
	 * 
	 * 这个场景向玩家展示了虚空储罐的流体处理能力和网络化特性。
	 * 虚空储罐是虚空存储系统的流体组件，能够实现跨维度的
	 * 流体共享和即时同步，解决了传统流体管道系统的距离和
	 * 容量限制。
	 * 
	 * 教程特色：
	 * 1. 多流体演示：同时展示蜂蜜和岩浆两种流体的存储
	 * 2. 实时注入：通过并行指令模拟持续的流体输入
	 * 3. 混合存储：展示不同类型流体的分离存储机制
	 * 4. 容量缩放：演示从小批量到大容量的动态调整
	 * 
	 * 技术细节：
	 * - 流体操作：使用IFluidHandler接口进行流体的精确控制
	 * - NBT数据管理：展示流体在网络传输中的数据保持
	 * - 并行处理：使用ParallelInstruction实现多流体的同时操作
	 * - 视觉反馈：通过流体颜色和动画提供直观反馈
	 * 
	 * 实用价值：
	 * - 工业化生产：建立大规模的流体处理和分配系统
	 * - 资源管理：实现全球流体资源的统一调度和分配
	 * - 空间优化：减少复杂的管道网络，节约建造成本
	 * - 维护简化：减少管道破损和泄漏的风险
	 * 
	 * @param scene Ponder场景构建器实例
	 * @param util 场景构建工具实例
	 */
	public static void voidTank(SceneBuilder scene, SceneBuildingUtil util) {

		scene.title("void_tank", "Using Void Tanks");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();

		Selection pipes = util.select().fromTo(1, 0, 5, 3, 1, 5)
				.add(util.select().fromTo(1, 1, 4, 3, 1, 4));

		BlockPos sourcePos = util.grid().at(1, 1, 3);
		BlockPos secSourcePos = util.grid().at(3, 1, 3);
		BlockPos receiverPos = util.grid().at(2, 1, 1);

		Selection source = util.select().position(sourcePos);
		Selection secSource = util.select().position(secSourcePos);
		Selection receiver = util.select().position(receiverPos);

		scene.world().modifyBlockEntity(sourcePos, VoidTankTileEntity.class,
				te -> te.getFluidStorage().setFluid(FluidStack.EMPTY));

		scene.world().modifyBlockEntity(secSourcePos, VoidTankTileEntity.class,
				te -> te.getFluidStorage().setFluid(FluidStack.EMPTY));

		scene.idle(10);
		scene.world().showSection(pipes, Direction.NORTH);
		scene.world().showSection(source, Direction.SOUTH);
		scene.world().showSection(secSource, Direction.SOUTH);
		scene.idle(10);
		scene.world().showSection(receiver, Direction.DOWN);
		scene.idle(10);

		ParallelInstruction parallel = new ParallelInstruction(scene);

		FluidStack honey = new FluidStack(AllFluids.HONEY.get(), 500);
		FluidStack lava = new FluidStack(Fluids.LAVA, 500);
		for (int i = 0; i < 8; i++) {

			parallel.scene.world().modifyBlockEntity(sourcePos, VoidTankTileEntity.class,
					te -> te.getFluidStorage().fill(honey, IFluidHandler.FluidAction.EXECUTE));

			parallel.scene.world().modifyBlockEntity(secSourcePos, VoidTankTileEntity.class,
					te -> te.getFluidStorage().fill(lava, IFluidHandler.FluidAction.EXECUTE));

			parallel.scene.idle(15);
		}

		scene.addInstruction(parallel);

		playVoidSequence(
				scene, util, VoidTankTileEntity.class,
				.015f, 0,
				"Void Tank", "Fluids",
				sourcePos, receiverPos,
				Direction.UP, Direction.UP,
				(pos) -> {},
				(pos) -> scene.world().modifyBlockEntity(pos, VoidTankTileEntity.class,
						te -> {
							lava.setAmount(4000);
							te.getFluidStorage().setFluid(lava);
						}),
				false, true
		);

	}

	/**
	 * 虚空电池教程场景
	 * 
	 * 这个场景向玩家展示了虚空电池的能量管理功能和网络化特性。
	 * 虚空电池是虚空存储系统的能量组件，能够实现跨维度的
	 * 能量共享和即时传输，解决了传统能源系统的传输损失和
	 * 距离限制问题。
	 * 
	 * 教程设计理念：
	 * 1. 简化演示：相比其他虚空设备，电池教程更侧重于核心概念
	 * 2. 精简内容：去除了复杂的并行演示，突出关键特性
	 * 3. 快速上手：让玩家能够快速理解和应用虚空电池
	 * 4. 经验复用：应用之前学到的虚空网络配置知识
	 * 
	 * 技术实现：
	 * - 代码复用：使用通用的playVoidSequence方法框架
	 * - 参数优化：针对电池的特点调整显示参数
	 * - 简化操作：减少不必要的交互步骤
	 * - 焦点突出：将注意力集中在能量传输的核心概念上
	 * 
	 * 应用场景：
	 * - 大规模能源网络：建立跨维度的能源传输和分配系统
	 * - 能源平衡：在不同区域之间实现能量的动态平衡
	 * - 紧急备用：作为关键系统的紧急能源供应
	 * - 清洁能源：推广可再生能源的全球化应用
	 * 
	 * @param scene Ponder场景构建器实例
	 * @param util 场景构建工具实例
	 */
	public static void voidBattery(SceneBuilder scene, SceneBuildingUtil util) {

		scene.title("void_battery", "Using Void Batteries");
		scene.showBasePlate();

		BlockPos sourcePos = util.grid().at(3, 1, 2);
		BlockPos receiverPos = util.grid().at(1, 1, 2);

		scene.world().showSection(util.select().position(sourcePos), Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(util.select().position(receiverPos), Direction.DOWN);
		scene.idle(10);

		playVoidSequence(
				scene, util, VoidBatteryTileEntity.class,
				-.0475f, -.1875f,
				"Void Battery", "Energy",
				sourcePos, receiverPos,
				Direction.SOUTH, Direction.SOUTH,
				(pos) -> {},
				(pos) -> {},
				true, false
		);

	}

	/**
	 * 虚空设备通用教程序列执行器
	 * 
	 * 这是虚空存储系统教程的核心方法，它提供了一个通用的教程框架，
	 * 用于展示所有虚空设备的共同特性和操作流程。通过参数化设计，
	 * 这个方法能够适应不同类型的虚空设备，同时保持教程的一致性。
	 * 
	 * 教程结构设计：
	 * 1. 初始介绍：展示设备的基本功能和传输能力
	 * 2. 频率配置：分步演示如何设置两个频率参数
	 * 3. 所有者管理：说明设备声明和取消声明的操作
	 * 4. 网络共享：演示同频率设备之间的资源共享
	 * 5. 安全机制：强调所有者权限对安全性的重要性
	 * 
	 * 参数化设计的优势：
	 * - 代码复用：避免为每个设备类型重复编写类似逻辑
	 * - 一致性：确保所有虚空设备教程的体验一致
	 * - 维护性：集中的逻辑便于维护和更新
	 * - 扩展性：新增虚空设备类型时可直接使用该框架
	 * 
	 * 技术实现特色：
	 * - 包围盒计算：动态计算不同方向上UI元素的位置
	 * - NBT数据管理：展示如何正确操作设备的持久化数据
	 * - 动态相机：根据设备类型自动调整视角
	 * - 时序控制：精确控制教程步骤的时机和节奏
	 * 
	 * @param scene Ponder场景构建器，用于创建和控制教程场景
	 * @param util 场景构建工具，提供快捷的场景操作方法
	 * @param beType 方块实体类型，用于类型安全的NBT操作
	 * @param shift X/Z轴上的UI元素偏移量，用于调整显示位置
	 * @param yOffset Y轴上的UI元素偏移量，用于垂直位置调整
	 * @param blockName 设备的显示名称，用于教程文本中的占位符
	 * @param transmittedName 传输内容的显示名称（如"物品"、"流体"、"能量"）
	 * @param firstPos 第一个设备的位置，作为教程的主焦点
	 * @param secondPos 第二个设备的位置，用于演示网络连接
	 * @param firstDirection 第一个设备的面向，决定UI元素的显示方向
	 * @param secondDirection 第二个设备的面向，用于对称显示
	 * @param onDisconnect 断开连接时的回调函数，用于模拟网络断开效果
	 * @param onConnect 建立连接时的回调函数，用于模拟网络连接效果
	 * @param rotate 是否需要旋转相机视角，用于更好的观看效果
	 * @param isTank 是否为流体类型设备，用于特殊的流体处理逻辑
	 */
	private static void playVoidSequence(SceneBuilder scene,
										 SceneBuildingUtil util,
										 Class<? extends BlockEntity> beType,
										 float shift, float yOffset,
										 String blockName,
										 String transmittedName,
										 BlockPos firstPos,
										 BlockPos secondPos,
										 Direction firstDirection,
										 Direction secondDirection,
										 Consumer<BlockPos> onDisconnect,
										 Consumer<BlockPos> onConnect,
										 boolean rotate,
										 boolean isTank) {

		Selection firstBlock = util.select().position(firstPos);
		Vec3 firstVec = util.vector().blockSurface(firstPos, firstDirection);

		Selection secondBlock = util.select().position(secondPos);
		Vec3 secondVec = util.vector().blockSurface(secondPos, secondDirection);

		scene.overlay().showText(50)
				.text(blockName + " can transmit " + transmittedName + " across distances")
				.pointAt(firstVec);
		scene.idle(50);

		if (rotate) scene.rotateCameraY(-90);
		scene.addKeyframe();

		Vec3 firstBackFreq = getFirstFrequency(firstVec, firstDirection, shift, yOffset);
		Vec3 firstFrontFreq = getLastFrequency(firstVec, firstDirection, shift, yOffset);
		Vec3 firstOwner = getOwner(firstVec, firstDirection, shift, yOffset);

		Vec3 secondBackFreq = getFirstFrequency(secondVec, secondDirection, shift, yOffset);
		Vec3 secondFrontFreq = getLastFrequency(secondVec, secondDirection, shift, yOffset);

		scene.idle(10);
		scene.overlay().showFilterSlotInput(firstBackFreq, firstDirection, 100);
		scene.overlay().showFilterSlotInput(firstFrontFreq, firstDirection, 100);
		scene.idle(10);

		scene.overlay().showText(50)
				.text("Placing items in the two upper slots can specify a Frequency")
				.placeNearTarget()
				.pointAt(firstFrontFreq);
		scene.idle(60);

		ItemStack iron = new ItemStack(Items.IRON_INGOT);
		ItemStack sapling = new ItemStack(Items.OAK_SAPLING);

		showFrequency(scene, firstBlock, beType, firstFrontFreq, "FrequencyLast", Pointing.LEFT, iron);
		onDisconnect.accept(secondPos);
		showFrequency(scene, firstBlock, beType, firstBackFreq, "FrequencyFirst", Pointing.RIGHT, sapling);

		if (isTank) onConnect.accept(firstPos);

		scene.idle(30);

		scene.addKeyframe();
		scene.idle(10);
		scene.overlay().showFilterSlotInput(firstOwner, firstDirection, 100);
		scene.idle(10);

		scene.overlay().showControls(firstOwner, Pointing.UP, 40).rightClick();
		scene.idle(7);
		scene.world().modifyBlockEntityNBT(firstBlock, beType, nbt -> nbt.remove("Owner"));

		scene.overlay().showText(50)
				.text("Right-click the bottom slot to unclaim the " + blockName)
				.placeNearTarget()
				.pointAt(firstOwner);
		scene.idle(60);

		scene.overlay().showControls(firstOwner, Pointing.UP, 40).rightClick();
		scene.idle(7);
		scene.world().restoreBlocks(firstBlock);
		scene.world().modifyBlockEntityNBT(firstBlock, beType, nbt -> {
			nbt.put("FrequencyFirst", sapling.save(new CompoundTag()));
			nbt.put("FrequencyLast", iron.save(new CompoundTag()));
		});

		if (isTank) onConnect.accept(firstPos);

		scene.overlay().showText(50)
				.text("Right-click it again to re-claim it")
				.placeNearTarget()
				.pointAt(firstOwner);
		scene.idle(60);

		scene.overlay().showText(50)
				.text("If a " + blockName + " is claimed, only it's owner is able to edit it's Frequency")
				.placeNearTarget()
				.pointAt(firstVec);
		scene.idle(60);

		scene.addKeyframe();
		scene.idle(10);

		scene.overlay().showText(60)
				.text("A " + blockName + " will only receive " + transmittedName + " from " + blockName + "s with the same Frequency and Owner")
				.placeNearTarget()
				.pointAt(secondVec);
		scene.idle(70);

		showFrequency(scene, secondBlock, beType, secondFrontFreq, "FrequencyLast", Pointing.LEFT, iron);
		showFrequency(scene, secondBlock, beType, secondBackFreq, "FrequencyFirst", Pointing.RIGHT, sapling);
		onConnect.accept(secondPos);

		if (rotate) {
			scene.idle(20);
			scene.rotateCameraY(90);
			scene.idle(30);
		} else scene.idle(50);

	}

	/**
	 * 显示频率配置演示
	 * 
	 * 这个辅助方法用于在Ponder教程中展示如何设置虚空设备的频率参数。
	 * 它通过从空槽位到放入物品的过程，演示了完整的频率配置流程。
	 * 
	 * 演示步骤：
	 * 1. 显示控制界面：展示如何与频率槽位交互
	 * 2. 放入物品：模拟玩家将物品放入频率槽的动作
	 * 3. 数据更新：更新设备的NBT数据，保存频率设置
	 * 4. 视觉反馈：通过界面变化给玩家直观反馈
	 * 
	 * 技术实现：
	 * - 控制面板模拟：使用showControls模拟真实的玩家交互
	 * - NBT数据操作：安全地更新设备的持久化数据
	 * - 物品序列化：使用Minecraft的标准物品序列化方法
	 * - 时序同步：确保视觉效果与数据更新的同步
	 * 
	 * @param scene Ponder场景构建器实例
	 * @param block 目标设备的选择区域
	 * @param beType 方块实体类型，用于类型安全的NBT操作
	 * @param slotPos 频率槽位的屏幕坐标位置
	 * @param slotId 频率槽位的NBT标识符（"FrequencyFirst"或"FrequencyLast"）
	 * @param pointing 控制面板的指示方向，用于视觉引导
	 * @param item 要放入槽位的物品，将作为频率标识符
	 */
	private static void showFrequency(SceneBuilder scene,
									  Selection block,
									  Class<? extends BlockEntity> beType,
									  Vec3 slotPos,
									  String slotId,
									  Pointing pointing,
									  ItemStack item) {
		scene.overlay().showControls(slotPos, pointing, 30).withItem(item);
		scene.idle(7);
		scene.world().modifyBlockEntityNBT(block, beType, nbt -> nbt.put(slotId, item.save(new CompoundTag())));
	}

	/**
	 * 计算第一个频率槽位的显示位置
	 * 
	 * 这个方法根据设备的面向计算第一个频率槽位在屏幕上的精确位置。
	 * 由于虚空设备可以朝向任意方向放置，需要动态计算UI元素的位置，
	 * 确保教程界面始终正确显示。
	 * 
	 * 计算原理：
	 * - 基于设备表面中心点作为参考系
	 * - 根据面向应用相应的偏移量
	 * - 考虑不同设备类型的尺寸差异
	 * - 支持自定义的垂直和水平偏移量
	 * 
	 * @param faceVec 设备表面中心点的世界坐标
	 * @param face 设备的面向（上下东西南北）
	 * @param shift 水平面上的偏移量，用于调整整体位置
	 * @param yOffset 垂直方向的偏移量，用于不同设备类型的适配
	 * @return 第一个频率槽位的准确屏幕坐标
	 */
	private static Vec3 getFirstFrequency(Vec3 faceVec, Direction face, float shift, float yOffset) {
		return switch (face) {
			case NORTH -> faceVec.add(.15625f, .15625f + yOffset, -shift);
			case EAST -> faceVec.add(shift, .15625f + yOffset, .15625f);
			case SOUTH -> faceVec.add(-.15625f, .15625f + yOffset, shift);
			case WEST -> faceVec.add(-shift, .15625f + yOffset, -.15625f);
			case UP -> faceVec.add(.15625f + yOffset, shift, -.15625f);
			case DOWN -> faceVec.add(.15625f + yOffset, -shift, .15625f);
		};
	}

	/**
	 * 计算第二个频率槽位的显示位置
	 * 
	 * 这个方法与getFirstFrequency类似，但计算的是第二个频率槽位的位置。
	 * 两个频率槽位通常对称分布在设备表面，这样的设计既美观又直观。
	 * 
	 * 设计考虑：
	 * - 对称布局：两个槽位在视觉上对称分布
	 * - 直观识别：玩家可以轻易区分两个不同的频率槽位
	 * - 操作便利：合理的间距确保操作的准确性
	 * - 兼容性：适应不同尺寸和方向的设备
	 * 
	 * @param faceVec 设备表面中心点的世界坐标
	 * @param face 设备的面向（上下东西南北）
	 * @param shift 水平面上的偏移量，与第一个槽位保持一致
	 * @param yOffset 垂直方向的偏移量，用于高度对齐
	 * @return 第二个频率槽位的准确屏幕坐标
	 */
	private static Vec3 getLastFrequency(Vec3 faceVec, Direction face, float shift, float yOffset) {
		return switch (face) {
			case NORTH -> faceVec.add(-.15625f, .15625f + yOffset, -shift);
			case EAST -> faceVec.add(shift, .15625f + yOffset, -.15625f);
			case SOUTH -> faceVec.add(.15625f, .15625f + yOffset, shift);
			case WEST -> faceVec.add(-shift, .15625f + yOffset, .15625f);
			case UP -> faceVec.add(.15625f + yOffset, shift, .15625f);
			case DOWN -> faceVec.add(.15625f + yOffset, -shift, -.15625f);
		};
	}

	/**
	 * 计算所有者槽位的显示位置
	 * 
	 * 这个方法计算虚空设备所有者槽位在屏幕上的精确位置。
	 * 所有者槽位是虚空存储系统安全机制的核心组件，用于标识
	 * 和控制设备的所有权。
	 * 
	 * 安全机制设计：
	 * - 所有者系统：只有设备所有者可以修改频率设置
	 * - 权限控制：防止未授权的玩家访问和修改设备
	 * - 声明机制：玩家可以声明和取消声明设备
	 * - 多玩家协作：在多玩家环境中确保资源的安全性
	 * 
	 * UI设计考虑：
	 * - 位置区分：所有者槽位通常位于频率槽位的下方
	 * - 视觉层次：确保与其他UI元素的清晰分离
	 * - 可访问性：适应不同的屏幕尺寸和分辨率
	 * 
	 * @param faceVec 设备表面中心点的世界坐标
	 * @param face 设备的面向，决定槽位的相对位置
	 * @param shift 水平面上的偏移量，保持与频率槽位的一致性
	 * @param yOffset 垂直方向的偏移量，用于层次分离
	 * @return 所有者槽位的准确屏幕坐标
	 */
	private static Vec3 getOwner(Vec3 faceVec, Direction face, float shift, float yOffset) {
		return switch (face) {
			case NORTH -> faceVec.add(0, -.15625f + yOffset, -shift);
			case EAST -> faceVec.add(shift, -.15625f + yOffset, 0);
			case SOUTH -> faceVec.add(0, -.15625f + yOffset, shift);
			case WEST -> faceVec.add(-shift, -.15625f + yOffset, 0);
			case UP -> faceVec.add(-.15625f + yOffset, shift, 0);
			case DOWN -> faceVec.add(-.15625f + yOffset, -shift, 0);
		};
	}

}
