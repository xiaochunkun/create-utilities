package me.duquee.createutilities.items;

import com.tterrag.registrate.util.entry.ItemEntry;
import me.duquee.createutilities.tabs.CUCreativeTabs;
import net.minecraft.world.item.Item;

import static me.duquee.createutilities.CreateUtilities.REGISTRATE;

/**
 * Create Utilities 物品注册类
 * 
 * 这个类负责注册所有的物品类型，主要是原材料和制作成分。
 * 物品设计遵循简洁实用的原则，专注于功能性而非复杂的特殊效果。
 * 
 * 物品分类：
 * - 虚空钢系列：高级金属材料，用于制作虚空存储方块
 * - 宝石加工产品：用于装饰性方块的制作
 * - 特殊组件：用于高级机械设备的核心部件
 * 
 * 设计理念：
 * - 统一使用ingredient()辅助方法简化注册
 * - 所有物品归类到统一的创造模式选项卡
 * - 保持与Create模组的材料体系一致性
 * - 简洁的注册流程，专注于游戏内容
 */
public class CUItems {

	/**
	 * 静态初始化块
	 * 设置所有物品的默认创造模式选项卡为CU基础选项卡
	 */
	static {
		REGISTRATE.setCreativeTab(CUCreativeTabs.BASE);
	}

	/**
	 * 虚空钢锭 - 高级金属材料
	 * 
	 * 虚空存储系列方块的核心制作材料，通过复杂的合金工艺制成。
	 * 用途：
	 * - 制作虚空钢方块用于建筑
	 * - 制作虚空钢装饰材料（梯子、脚手架、栏杆等）
	 * - 虚空存储系统核心组件的基础材料
	 * - 高级机械设备的结构材料
	 */
	public static final ItemEntry<Item> VOID_STEEL_INGOT = ingredient("void_steel_ingot");
	
	/**
	 * 虚空钢板 - 精加工金属制品
	 * 
	 * 由虚空钢锭通过压片机加工而成的薄板材料。
	 * 用途：
	 * - 制作精密机械组件
	 * - 虚空存储设备的外壳材料
	 * - 高级装饰元素的制作
	 * - 复杂合成配方的中间产物
	 */
	public static final ItemEntry<Item> VOID_STEEL_SHEET = ingredient("void_steel_sheet");
	
	/**
	 * 抛光紫水晶 - 精美宝石制品
	 * 
	 * 通过砂纸打磨普通紫水晶制成的高品质宝石材料。
	 * 用途：
	 * - 制作紫水晶瓦片装饰方块
	 * - 高级建筑材料的装饰元素
	 * - 奢华建筑风格的核心材料
	 * - 体现工艺水平的精品材料
	 */
	public static final ItemEntry<Item> POLISHED_AMETHYST = ingredient("polished_amethyst");
	
	/**
	 * 引力子管 - 神秘科技组件
	 * 
	 * 虚空存储系统的核心科技组件，包含了空间传输的奥秘。
	 * 用途：
	 * - 虚空存储方块的关键制作材料
	 * - 跨维度传输技术的载体
	 * - 高科技设备的核心元件
	 * - 体现模组科幻主题的标志性物品
	 */
	public static final ItemEntry<Item> GRAVITON_TUBE = ingredient("graviton_tube");

	/**
	 * 物品注册辅助方法
	 * 
	 * 这是一个私有辅助方法，用于简化物品注册流程。
	 * 所有通过此方法注册的物品都是基础的Item类型，没有特殊功能。
	 * 
	 * 统一处理：
	 * - 使用标准的Item构造器
	 * - 自动设置创造模式选项卡
	 * - 简化的注册流程
	 * - 一致的命名规范
	 * 
	 * @param name 物品的注册名称，也是资源定位器的路径部分
	 * @return 注册完成的物品条目，可用于配方和引用
	 */
	private static ItemEntry<Item> ingredient(String name) {
		return REGISTRATE.item(name, Item::new)
				.register();
	}

	/**
	 * 注册方法 - 触发所有物品的注册
	 * 
	 * 这是一个空方法，但其调用会触发静态字段的初始化，
	 * 从而完成所有物品的注册过程。这是Registrate模式的标准做法。
	 */
	public static void register() {}

}
