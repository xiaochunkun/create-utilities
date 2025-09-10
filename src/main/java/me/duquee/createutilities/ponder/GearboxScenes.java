package me.duquee.createutilities.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import me.duquee.createutilities.blocks.CUBlocks;
import me.duquee.createutilities.blocks.lgearbox.LShapedGearboxBlock;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 齿轮箱场景类
 * 
 * 这个类为Create Utilities模组的齿轮系统提供了互动教程场景。
 * 通过Ponder教程系统，玩家可以学习如何使用齿轮立方体和L型齿轮箱
 * 来构建更加紧凑和高效的动力传输系统。
 * 
 * 核心功能：
 * - 齿轮立方体教程：展示6轴全向传动的优势和使用方法
 * - L型齿轮箱教程：演示紧凑型直角传动的解决方案
 * - 互动演示：通过动画和文字说明帮助玩家理解复杂概念
 * - 对比展示：显示传统方法与新型齿轮系统的区别
 * 
 * 技术实现：
 * - 使用CreateSceneBuilder构建丰富的视觉效果
 * - 集成Ponder API提供流畅的教程体验
 * - 采用渐进式演示，从简单到复杂
 * - 结合实际案例展示实用性
 * 
 * 教程设计理念：
 * - 逐步引导：从basic到advanced的学习路径
 * - 问题导向：先展示问题，再提供解决方案
 * - 对比学习：通过对比突出新技术的优势
 * - 互动体验：鼓励玩家亲手尝试和实验
 * 
 * @author duquee
 * @since 1.0.0
 */
public class GearboxScenes {

	/**
	 * 齿轮立方体教程场景
	 * 
	 * 这个场景向玩家展示了齿轮立方体在多向动力传输中的优势。
	 * 通过对比传统的多个齿轮箱组合，展示了单个齿轮立方体
	 * 如何简化复杂的传动网络设计。
	 * 
	 * 教程结构：
	 * 1. 问题展示：显示传统多齿轮箱的笨重和复杂性
	 * 2. 解决方案：引入齿轮立方体作为替代方案
	 * 3. 效果对比：同样功能下的空间占用对比
	 * 4. 实际应用：展示6个方向的动力输出能力
	 * 
	 * 教学目标：
	 * - 让玩家理解齿轮立方体的空间优势
	 * - 展示如何用一个方块替代多个齿轮箱
	 * - 强调在紧凑空间中的实用性
	 * - 演示整洁、简洁的机械设计思路
	 * 
	 * @param builder 场景构建器，用于创建教程场景
	 * @param util 场景构建工具，提供各种便捷方法
	 */
	public static void gearCube(SceneBuilder builder, SceneBuildingUtil util) {

		// 创建增强Create教程场景构建器
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);

		// 设置教程标题和场景参数
		scene.title("gearcube", "Relaying Rotational Force using 6-Axis Gearboxes");
		scene.configureBasePlate(0, 0, 5);  // 配置基础平台
		scene.setSceneOffsetY(-1);           // 设置场景高度偏移

		// 选择初始的传送带系统（作为动力源）
		Selection belt = util.select().fromTo(2, 0, 5, 2, 3, 5)
				.add(util.select().fromTo(2, 3, 4, 3, 2, 3));

		// 显示基础平台和动力源
		scene.showBasePlate();
		scene.world().showSection(belt, Direction.UP);  // 显示传送带
		scene.idle(10);  // 等待10刻

		// 显示传统的齿轮箱组合（问题展示）
		Selection gearboxes = util.select().fromTo(2, 3, 2, 2, 4, 2);
		scene.world().showSection(gearboxes, Direction.SOUTH);
		scene.idle(10);

		// 逐个显示各个方向的传动轴，展示多向传动的复杂性
		scene.world().showSection(util.select().position(2, 2, 2), Direction.UP);    // 下方轴
		scene.world().showSection(util.select().position(2, 3, 1), Direction.SOUTH); // 前方轴
		scene.world().showSection(util.select().position(1, 4, 2), Direction.EAST);  // 左方轴
		scene.world().showSection(util.select().position(3, 4, 2), Direction.WEST);  // 右方轴
		scene.world().showSection(util.select().position(2, 5, 2), Direction.DOWN);  // 上方轴
		scene.idle(20);

		// 指出传统方法的问题：空间占用大
		BlockPos bottomGBPos = util.grid().at(2, 3, 2);
		Vec3 bottomGBVec = util.vector().centerOf(bottomGBPos);
		scene.overlay().showText(50)
				.pointAt(bottomGBVec)
				.placeNearTarget()
				.text("Relaying rotation in all directions can get bulky quickly");
		scene.idle(60);

		// 隐藏周围的传动轴，为展示新方案做准备
		scene.world().hideSection(util.select().position(2, 2, 2), Direction.DOWN);
		scene.world().hideSection(util.select().position(2, 3, 1), Direction.NORTH);
		scene.world().hideSection(util.select().position(1, 4, 2), Direction.WEST);
		scene.world().hideSection(util.select().position(3, 4, 2), Direction.EAST);
		scene.world().hideSection(util.select().position(2, 5, 2), Direction.UP);
		scene.idle(10);

		// 隐藏传统齿轮箱组合
		scene.world().hideSection(gearboxes, Direction.NORTH);
		scene.idle(20);

		// 引入齿轮立方体作为解决方案
		BlockPos gearcubePos = util.grid().at(2, 3, 2);
		Selection gearcube = util.select().position(gearcubePos);
		scene.world().setBlock(gearcubePos, CUBlocks.GEARCUBE.getDefaultState(), false);
		scene.world().showSection(gearcube, Direction.SOUTH);  // 显示齿轮立方体
		scene.world().setKineticSpeed(gearcube, 16);           // 设置旋转速度
		scene.idle(10);

		// 设置各个方向的传动轴位置和方向
		BlockPos shaft1Pos = util.grid().at(1, 3, 2);  // 左侧轴位置
		BlockPos shaft2Pos = util.grid().at(3, 3, 2);  // 右侧轴位置
		BlockPos shaft3Pos = util.grid().at(2, 4, 2);  // 上方轴位置
		BlockState shaftState = AllBlocks.SHAFT.getDefaultState()
				.setValue(ShaftBlock.AXIS, Direction.Axis.X);  // 水平轴
		scene.world().setBlock(shaft1Pos, shaftState, false);
		scene.world().setBlock(shaft2Pos, shaftState, false);
		scene.world().setBlock(shaft3Pos, shaftState.setValue(ShaftBlock.AXIS, Direction.Axis.Y), false);  // 垂直轴

		// 为各个轴设置旋转速度，模拟齿轮立方体的传动效果
		Selection shaft1 = util.select().position(shaft1Pos);
		Selection shaft2 = util.select().position(shaft2Pos);
		Selection shaft3 = util.select().position(shaft3Pos);
		scene.world().setKineticSpeed(shaft1, 16);   // 左侧正转
		scene.world().setKineticSpeed(shaft2, -16);  // 右侧反转（齿轮传动效果）
		scene.world().setKineticSpeed(shaft3, 16);   // 上方正转

		// 重新显示所有连接的传动轴，展示齿轮立方体的全向传动能力
		scene.world().showSection(util.select().position(2, 2, 2), Direction.UP);      // 下方轴
		scene.world().showSection(util.select().position(2, 3, 1), Direction.SOUTH);   // 前方轴
		scene.world().showSection(util.select().position(shaft1Pos), Direction.EAST);  // 左侧轴
		scene.world().showSection(util.select().position(shaft2Pos), Direction.WEST);  // 右侧轴
		scene.world().showSection(util.select().position(shaft3Pos), Direction.DOWN);  // 上方轴
		scene.idle(20);

		// 展示结论：齿轮立方体是更紧凑的解决方案
		scene.overlay().showText(60)
				.colored(PonderPalette.GREEN)  // 使用绿色突出优点
				.pointAt(util.vector().topOf(3, 2, 3))
				.placeNearTarget()
				.attachKeyFrame()  // 附加关键帧
				.text("A 6-Axis Gearcube is the more compact equivalent of this setup");
		scene.idle(70);

	}

	/**
	 * L型齿轮箱教程场景
	 * 
	 * 这个场景向玩家介绍了L型齿轮箱在直角传动中的优势。
	 * 通过对比传统的齿轮组合和标准齿轮箱，展示了L型设计
	 * 在紧凑空间和成本效益方面的优势。
	 * 
	 * 教程结构：
	 * 1. 问题识别：展示传统直角传动的笨重和复杂
	 * 2. 标准方案：介绍标准齿轮箱的缺点（浪费轴）
	 * 3. 优化方案：引入L型齿轮箱作为最优解
	 * 4. 交互演示：展示扫手调整功能
	 * 
	 * 教学目标：
	 * - 理解L型齿轮箱的设计意图和优势
	 * - 学会使用扫手调整第二轴方向
	 * - 掌握在紧凑设计中的实用技巧
	 * - 体验成本效益和空间优化的平衡
	 * 
	 * @param builder 场景构建器，用于创建教程场景
	 * @param util 场景构建工具，提供各种便捷方法
	 */
	public static void lShapedGearbox(SceneBuilder builder, SceneBuildingUtil util) {

		// 创建增强Create教程场景构建器
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);

		// 设置教程标题和场景参数
		scene.title("lshaped_gearbox", "Relaying Rotational Force using L-Shaped Gearboxes");
		scene.configureBasePlate(0, 0, 5);  // 配置基础平台

		// 选择初始的传送带系统（作为动力源）
		Selection belt = util.select().fromTo(2, 0, 5, 2, 2, 5)
				.add(util.select().position(2, 2, 4));

		// 显示基础平台和动力源
		scene.showBasePlate();
		scene.world().showSection(belt, Direction.UP);  // 显示传送带
		scene.idle(10);

		// 定义传统齿轮传动系统的位置
		BlockPos cog1Pos = util.grid().at(2, 2, 3);  // 第一个齿轮位置
		BlockPos cog2Pos = util.grid().at(1, 2, 2);  // 第二个齿轮位置

		// 创建选择区域
		Selection cog1 = util.select().position(cog1Pos);
		Selection cog2 = util.select().position(cog2Pos);
		BlockPos shaftPos = util.grid().at(0, 2, 2);  // 输出轴位置
		Selection shaft = util.select().position(shaftPos);

		// 显示传统的齿轮传动系统
		scene.world().showSection(cog1, Direction.SOUTH);      // 显示第一个齿轮
		scene.idle(5);
		scene.world().showSection(cog2.add(shaft), Direction.EAST);  // 显示第二个齿轮和输出轴
		scene.idle(10);

		// 指出传统方法的问题：方向改变会让结构变得笨重
		scene.overlay().showText(50)
				.pointAt(util.vector().blockSurface(shaftPos, Direction.WEST))
				.placeNearTarget()
				.text("Changing directions can get bulky quickly");

		scene.idle(50);
		// 隐藏传统齿轮组合，为介绍标准齿轮箱做准备
		scene.world().hideSection(cog1.add(cog2), Direction.UP);
		scene.idle(20);

		// 定义标准齿轮箱的位置和设置
		BlockPos gearboxPos = util.grid().at(2, 2, 2);
		Selection gearbox = util.select().position(gearboxPos);
		scene.world().setKineticSpeed(gearbox, 16);  // 设置旋转速度

		// 设置两个方向的传动轴，模拟标准齿轮箱的连接
		BlockState shaftState = AllBlocks.SHAFT.getDefaultState();
		scene.world().setBlock(cog1Pos, shaftState.setValue(ShaftBlock.AXIS, Direction.Axis.Z), false);  // Z轴方向
		scene.world().setBlock(cog2Pos, shaftState.setValue(ShaftBlock.AXIS, Direction.Axis.X), false);  // X轴方向
		scene.world().showSection(util.select().fromTo(cog1Pos, cog2Pos), Direction.DOWN);
		scene.idle(10);

		// 指出标准齿轮箱的问题：有两个轴未被使用
		scene.overlay().showText(80)
				.colored(PonderPalette.GREEN)
				.pointAt(util.vector().blockSurface(gearboxPos, Direction.NORTH))
				.placeNearTarget()
				.text("A more compact alternative to this setup is using a Gearbox, but two shafts go unused");
		scene.idle(80);

		// 隐藏标准齿轮箱，为展示L型齿轮箱做准备
		scene.world().hideSection(gearbox, Direction.UP);
		scene.idle(20);

		// 设置L型齿轮箱的状态和显示
		BlockState gearboxState = CUBlocks.LSHAPED_GEARBOX.getDefaultState()
				.setValue(LShapedGearboxBlock.FACING_1, Direction.WEST)   // 第一轴朝向
				.setValue(LShapedGearboxBlock.FACING_2, Direction.WEST);  // 第二轴朝向
		scene.world().setBlock(gearboxPos, gearboxState, false);
		scene.world().setKineticSpeed(gearbox, -16);  // 设置反向旋转
		ElementLink<WorldSectionElement> lGearbox = scene.world().showIndependentSection(gearbox, Direction.DOWN);
		scene.idle(10);

		// 展示L型齿轮箱的优势：更清洁、更经济
		scene.overlay().showText(80)
				.colored(PonderPalette.GREEN)
				.pointAt(util.vector().blockSurface(gearboxPos, Direction.NORTH))
				.placeNearTarget()
				.text("With an L-Shaped Gearbox you can make this in a cleaner and cheaper way");
		scene.idle(80);
		scene.addKeyframe();  // 添加关键帧

		// 隐藏其他元素，专注于L型齿轮箱的演示
		belt = util.select().fromTo(2, 0, 5, 2, 2, 5)
				.add(util.select().position(2, 2, 4));
		scene.world().hideSection(belt.add(cog1), Direction.SOUTH);  // 隐藏传送带和轴
		scene.world().hideSection(cog2.add(shaft), Direction.WEST);   // 隐藏齿轮和输出轴
		scene.idle(20);

		// 准备交互演示：停止旋转、调整视角、移动位置
		scene.world().setKineticSpeed(gearbox, 0);               // 停止旋转
		scene.rotateCameraY(-90);                               // 旋转相机视角
		scene.world().moveSection(lGearbox, new Vec3(0, -1, 0), 15);  // 向下移动
		scene.idle(30);

		// 显示扫手交互提示
		BlockPos lGearboxPos = util.grid().at(2, 1, 2);
		Vec3 lGearboxVec = util.vector().blockSurface(lGearboxPos, Direction.DOWN);
		scene.overlay().showControls(lGearboxVec, Pointing.UP, 40).rightClick().withItem(AllItems.WRENCH.asStack());

		// 演示扫手调整功能：循环切换第二轴的方向
		for (int i = 0; i < 8; i++) {
			scene.idle(10);
			// 循环切换第二轴方向
			scene.world().modifyBlock(gearboxPos, s -> s.cycle(LShapedGearboxBlock.FACING_2), false);
			if (i == 1) {
				// 在第二次循环时显示说明文本
				scene.overlay().showText(50)
						.text("By Right-clicking it with a Wrench, you can change the orientation of the secondary axis")
						.pointAt(lGearboxVec);
			}
		}
		scene.idle(20);

	}

}
