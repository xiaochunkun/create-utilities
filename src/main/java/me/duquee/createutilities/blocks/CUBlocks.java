package me.duquee.createutilities.blocks;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.content.decoration.MetalLadderBlock;
import com.simibubi.create.content.decoration.MetalScaffoldingBlock;
import com.simibubi.create.content.decoration.encasing.CasingBlock;
import com.simibubi.create.content.decoration.encasing.EncasedCTBehaviour;
import com.simibubi.create.foundation.data.BuilderTransformers;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import me.duquee.createutilities.blocks.gearcube.GearcubeBlock;
import me.duquee.createutilities.blocks.lgearbox.LShapedGearboxBlock;
import me.duquee.createutilities.blocks.voidtypes.battery.VoidBatteryBlock;
import me.duquee.createutilities.blocks.voidtypes.chest.VoidChestBlock;
import me.duquee.createutilities.blocks.voidtypes.motor.VoidMotorBlock;
import me.duquee.createutilities.blocks.voidtypes.tank.VoidTankBlock;
import me.duquee.createutilities.items.CUItems;
import me.duquee.createutilities.mountedstorage.CUMountedStorages;
import me.duquee.createutilities.tabs.CUCreativeTabs;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static me.duquee.createutilities.CreateUtilities.REGISTRATE;

/**
 * Create Utilities 方块注册类
 * 
 * 这个类负责注册所有的方块类型，使用Create模组的Registrate系统实现链式配置。
 * 包含了模组的所有方块内容，从基础建筑材料到复杂的功能性方块。
 * 
 * 方块分类：
 * 1. 虚空钢材料方块：建筑和装饰用的高级材料
 * 2. 虚空存储方块：核心功能方块，提供跨维度存储
 * 3. 动力传输方块：增强的齿轮系统
 * 4. 装饰方块：美观的建筑材料
 * 
 * 设计原则：
 * - 使用Registrate的链式API简化注册流程
 * - 统一的材质和音效风格
 * - 合理的挖掘工具和强度配置
 * - 与Create模组的完美集成
 */
public class CUBlocks {

	/**
	 * 静态初始化块
	 * 设置所有方块的默认创造模式选项卡为CU基础选项卡
	 */
	static {
		REGISTRATE.setCreativeTab(CUCreativeTabs.BASE);
	}

	/**
	 * 虚空钢方块 - 高级建筑材料
	 * 
	 * 这是由虚空钢锭制作的装饰性方块，具有极高的强度和抗爆性。
	 * 特性：
	 * - 硬度55.0F，抗爆值1200.0F（超过黑曜石）
	 * - 绿色地图颜色，独特的视觉标识
	 * - 下界合金方块的音效，彰显其珍贵程度
	 * - 只能用镐子挖掘，需要正确工具
	 */
	public static final BlockEntry<Block> VOID_STEEL_BLOCK = REGISTRATE.block("void_steel_block", Block::new)
			.initialProperties(() -> Blocks.NETHERITE_BLOCK)
			.properties(p -> p.mapColor(MapColor.COLOR_GREEN))
			.properties(p -> p.strength(55.0F, 1200.0F))
			.properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
			.transform(pickaxeOnly())
			.simpleItem()
			.register();

	/**
	 * 虚空钢脚手架 - 高级建筑支撑结构
	 * 
	 * 使用Create模组的BuilderTransformers.scaffold变换器自动生成：
	 * - 完整的脚手架功能（攀爬、连接、放置逻辑）
	 * - 使用虚空钢锭作为合成材料
	 * - 绿色地图颜色与虚空钢系列统一
	 * - 自动生成对应的材质和模型
	 */
	public static final BlockEntry<MetalScaffoldingBlock> VOID_STEEL_SCAFFOLD = REGISTRATE.block("void_steel_scaffolding", MetalScaffoldingBlock::new)
			.transform(BuilderTransformers.scaffold("void_steel",
					() -> DataIngredient.items(CUItems.VOID_STEEL_INGOT.get()), MapColor.COLOR_GREEN,
					CUSpriteShifts.VOID_STEEL_SCAFFOLD, CUSpriteShifts.VOID_STEEL_SCAFFOLD_INSIDE, CUSpriteShifts.VOID_CASING))
			.register();

	/**
	 * 虚空钢梯子 - 高级攀爬设施
	 * 
	 * 使用BuilderTransformers.ladder变换器自动配置：
	 * - 标准的梯子攀爬功能
	 * - 使用虚空钢锭作为合成材料
	 * - 绿色地图颜色保持材料一致性
	 * - 自动生成配方、模型和标签
	 */
	public static final BlockEntry<MetalLadderBlock> VOID_STEEL_LADDER = REGISTRATE.block("void_steel_ladder", MetalLadderBlock::new)
			.transform(BuilderTransformers.ladder("void_steel",
					() -> DataIngredient.items(CUItems.VOID_STEEL_INGOT.get()), MapColor.COLOR_GREEN))
			.register();

	/**
	 * 虚空钢栏杆 - 装饰性防护结构
	 * 
	 * 基于原版铁栏杆的功能，具有以下特性：
	 * - cutoutMipped渲染层，支持透明效果
	 * - 铜质音效，独特的听觉体验
	 * - 绿色地图颜色，与虚空钢系列保持一致
	 * - 支持扳手拾取和风扇透明标签
	 * - 自动连接相邻栏杆形成连续结构
	 */
	public static final BlockEntry<IronBarsBlock> VOID_STEEL_BARS = REGISTRATE.block("void_steel_bars", IronBarsBlock::new)
			.addLayer(() -> RenderType::cutoutMipped)
			.initialProperties(() -> Blocks.IRON_BARS)
			.properties(p -> p.sound(SoundType.COPPER).mapColor(MapColor.COLOR_GREEN))
			.tag(AllTags.AllBlockTags.WRENCH_PICKUP.tag)
			.tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
			.transform(pickaxeOnly())
			.item()
			.build()
			.register();

	/**
	 * 虚空外壳 - 机械装置外壳材料
	 * 
	 * 使用Create的BuilderTransformers.casing变换器，提供：
	 * - 标准的外壳连接行为
	 * - 自定义的虚空外壳材质变换
	 * - 黑色地图颜色，神秘的外观
	 * - 极高的强度和抗爆性能
	 * - 下界合金方块的音效
	 */
	public static final BlockEntry<CasingBlock> VOID_CASING = REGISTRATE.block("void_casing", CasingBlock::new)
			.transform(BuilderTransformers.casing(() -> CUSpriteShifts.VOID_CASING))
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(55.0F, 1200.0F))
			.properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
			.transform(pickaxeOnly())
			.register();

	/**
	 * 虚空马达 - 跨维度旋转力传输核心
	 * 
	 * 虚空存储系统的动力传输组件，实现跨维度的旋转力共享。
	 * 核心特性：
	 * - 与其他相同网络键的虚空马达连接
	 * - 不产生应力影响（注释掉的setNoImpact）
	 * - 黑色地图颜色，神秘而强大的外观
	 * - 高强度（30F硬度，600.0F抗爆）
	 * - EPIC稀有度，彰显其重要性
	 * - 自定义物品模型，独特的视觉效果
	 */
	public static final BlockEntry<VoidMotorBlock> VOID_MOTOR = REGISTRATE.block("void_motor", VoidMotorBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(30F, 600.0F))
			.transform(pickaxeOnly())
			//.transform(CStress.setNoImpact()) // 注释：不产生应力影响
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	/**
	 * 虚空箱子 - 跨维度物品存储核心
	 * 
	 * 虚空存储系统的物品存储组件，提供无限容量的跨维度物品共享。
	 * 核心特性：
	 * - noOcclusion：不阻挡视线，允许复杂渲染
	 * - 黑色地图颜色，与虚空系列保持一致
	 * - 高强度防护，保护珍贵内容
	 * - 支持挂载存储（可安装到Create的运动装置上）
	 * - EPIC稀有度，突出其价值
	 * - 自定义物品模型，精美的UI表现
	 */
	public static final BlockEntry<VoidChestBlock> VOID_CHEST = REGISTRATE.block("void_chest", VoidChestBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(30F, 600.0F))
			.transform(pickaxeOnly())
			.transform(MountedItemStorageType.mountedItemStorage(CUMountedStorages.VOID_CHEST))
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	/**
	 * 虚空储罐 - 跨维度流体存储核心
	 * 
	 * 虚空存储系统的流体存储组件，支持大容量流体的跨维度共享。
	 * 核心特性：
	 * - noOcclusion：支持透明窗口和流体渲染
	 * - 黑色地图颜色，保持系列一致性
	 * - 高强度保护，防止意外破坏
	 * - isRedstoneConductor：支持红石信号传导
	 * - EPIC稀有度，体现其重要价值
	 * - 自定义物品模型，精致的外观设计
	 */
	public static final BlockEntry<VoidTankBlock> VOID_TANK = REGISTRATE.block("void_tank", VoidTankBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(30F, 600.0F))
			.properties(p -> p.isRedstoneConductor((p1, p2, p3) -> true))
			.transform(pickaxeOnly())
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	/**
	 * 虚空电池 - 跨维度能量存储核心
	 * 
	 * 虚空存储系统的能量存储组件，提供大容量能量的跨维度共享。
	 * 核心特性：
	 * - noOcclusion：支持能量指示器和动画渲染
	 * - 黑色地图颜色，与虚空系列统一
	 * - 高强度保护，确保能量安全
	 * - EPIC稀有度，突出其价值地位
	 * - 自定义物品模型，科技感十足的设计
	 * - 支持工程师护目镜显示能量信息
	 */
	public static final BlockEntry<VoidBatteryBlock> VOID_BATTERY = REGISTRATE.block("void_battery", VoidBatteryBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.mapColor(MapColor.COLOR_BLACK))
			.properties(p -> p.strength(30F, 600.0F))
			.transform(pickaxeOnly())
			.item()
			.properties(p -> p.rarity(Rarity.EPIC))
			.transform(customItemModel())
			.register();

	/**
	 * 齿轮立方体 - 多向旋转力传输方块
	 * 
	 * 创新的动力传输组件，可以在六个面都进行旋转力传输。
	 * 核心特性：
	 * - noOcclusion：显示内部齿轮结构
	 * - PODZOL地图颜色，棕色调与木质材料呼应
	 * - 不产生应力影响（注释的setNoImpact）
	 * - 支持斧头或镐子挖掘，灵活的工具选择
	 * - 简单物品模型，朴素实用的设计
	 */
	public static final BlockEntry<GearcubeBlock> GEARCUBE = REGISTRATE.block("gearcube", GearcubeBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.mapColor(MapColor.PODZOL))
			//.transform(CStress.setNoImpact()) // 注释：不产生应力影响
			.transform(axeOrPickaxe())
			.simpleItem()
			.register();

	/**
	 * L型齿轮箱 - 直角动力传输组件
	 * 
	 * 特殊设计的齿轮箱，专门用于90度角的旋转力传输。
	 * 核心特性：
	 * - noOcclusion：展示内部机械结构
	 * - PODZOL地图颜色，与齿轮立方体保持一致
	 * - 不产生应力影响（注释的setNoImpact）
	 * - 安山岩外壳连接纹理系统
	 * - 智能连接逻辑，避免在传动轴面连接外壳
	 * - 支持斧头或镐子挖掘
	 * - 简单物品模型，实用至上的设计
	 */
	public static final BlockEntry<LShapedGearboxBlock> LSHAPED_GEARBOX = REGISTRATE.block("lshaped_gearbox", LShapedGearboxBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(BlockBehaviour.Properties::noOcclusion)
			.properties(p -> p.mapColor(MapColor.PODZOL))
			//.transform(CStress.setNoImpact()) // 注释：不产生应力影响
			.transform(axeOrPickaxe())
			// 注册连接纹理行为，使用安山岩外壳样式
			.onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(AllSpriteShifts.ANDESITE_CASING)))
			// 注册外壳连接性，智能判断哪些面可以连接外壳
			.onRegister(CreateRegistrate.casingConnectivity((block, cc) -> cc.make(block, AllSpriteShifts.ANDESITE_CASING,
					(state, face) -> !LShapedGearboxBlock.hasShaftTowards(state, face))))
			.simpleItem()
			.register();

	/**
	 * 紫水晶瓦片 - 装饰性建筑方块
	 * 
	 * 使用紫水晶制作的高级装饰方块，带有独特的声音效果。
	 * 特性：
	 * - 深板岩基础属性，坚固耐用
	 * - 紫色陶瓦地图颜色，优雅的视觉效果
	 * - 需要正确工具挖掘，保持游戏平衡
	 * - 紫水晶簇音效，清脆悦耳
	 * - 只能用镐子挖掘，符合石材特性
	 * - 简单物品模型，经典实用
	 */
	public static final BlockEntry<Block> AMETHYST_TILES = REGISTRATE.block("amethyst_tiles", Block::new)
			.initialProperties(() -> Blocks.DEEPSLATE)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_PURPLE).requiresCorrectToolForDrops())
			.properties(p -> p.sound(SoundType.AMETHYST_CLUSTER))
			.transform(pickaxeOnly())
			.simpleItem()
			.register();

	/**
	 * 小紫水晶瓦片 - 精致装饰方块
	 * 
	 * 紫水晶瓦片的小型变种，提供更精细的装饰选择。
	 * 特性：
	 * - 与大紫水晶瓦片相同的基础属性
	 * - 深板岩强度和耐久性
	 * - 紫色陶瓦地图颜色，保持色彩一致性
	 * - 需要正确工具挖掘
	 * - 紫水晶簇的独特音效
	 * - 只能用镐子挖掘
	 * - 简单物品模型
	 */
	public static final BlockEntry<Block> SMALL_AMETHYST_TILES = REGISTRATE.block("small_amethyst_tiles", Block::new)
			.initialProperties(() -> Blocks.DEEPSLATE)
			.properties(p -> p.mapColor(MapColor.TERRACOTTA_PURPLE).requiresCorrectToolForDrops())
			.properties(p -> p.sound(SoundType.AMETHYST_CLUSTER))
			.transform(pickaxeOnly())
			.simpleItem()
			.register();

	/**
	 * 注册方法 - 触发所有方块的注册
	 * 
	 * 这是一个空方法，但其调用会触发静态字段的初始化，
	 * 从而完成所有方块的注册过程。这是Registrate模式的标准做法。
	 */
	public static void register() {}

}
