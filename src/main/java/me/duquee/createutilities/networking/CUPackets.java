package me.duquee.createutilities.networking;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import me.duquee.createutilities.CreateUtilities;
import me.duquee.createutilities.networking.packets.VoidBatteryUpdatePacket;
import me.duquee.createutilities.networking.packets.VoidTankUpdatePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Create Utilities 模组网络数据包管理类
 * 
 * 这个枚举类集中管理所有的网络数据包注册和配置。
 * 通过Forge的SimpleChannel系统，为模组提供高效、可靠的
 * 客户端-服务器间数据同步机制。
 * 
 * 核心功能：
 * - 数据包注册：自动注册所有模组专用数据包
 * - 版本管理：确保客户端与服务器版本兼容
 * - 方向控制：为不同数据包指定传输方向
 * - 编解码：提供一体化的数据序列化和反序列化
 * 
 * 数据包类型：
 * - VOID_TANK_UPDATE：虚空储罐状态更新数据包
 * - VOID_BATTERY_UPDATE：虚空电池状态更新数据包
 * 
 * 技术特性：
 * - 枚举设计：集中管理所有数据包类型
 * - 自动索引：自动分配唯一的数据包ID
 * - 方向安全：严格控制数据包传输方向
 * - 线程安全：在网络线程中安全处理数据包
 * 
 * 使用模式：
 * 1. 在模组初始化期间调用registerPackets()
 * 2. 通过channel.send()发送数据包
 * 3. 数据包自动路由到相应的处理器
 * 
 * @author duquee
 * @since 1.0.0
 */
public enum CUPackets {

	/**
	 * 虚空储罐更新数据包
	 * 
	 * 用于将服务器端的虚空储罐状态同步到所有客户端。
	 * 这确保了所有玩家能够看到一致的流体存储状态。
	 * 
	 * 数据包内容：
	 * - 网络键：标识特定的虚空储罐网络
	 * - 流体状态：当前存储的流体类型和数量
	 * - 容量信息：最大存储容量
	 * 
	 * 触发条件：当虚空储罐中的流体发生变化时
	 * 传输方向：服务器 → 客户端（广播所有玩家）
	 */
	VOID_TANK_UPDATE(VoidTankUpdatePacket.class, VoidTankUpdatePacket::new, NetworkDirection.PLAY_TO_CLIENT),
	
	/**
	 * 虚空电池更新数据包
	 * 
	 * 用于将服务器端的虚空电池状态同步到所有客户端。
	 * 这确保了所有玩家能够看到一致的电能存储状态。
	 * 
	 * 数据包内容：
	 * - 网络键：标识特定的虚空电池网络
	 * - 电能状态：当前存储的电能数量
	 * - 容量信息：最大存储容量
	 * 
	 * 触发条件：当虚空电池中的电能发生变化时
	 * 传输方向：服务器 → 客户端（广播所有玩家）
	 */
	VOID_BATTERY_UPDATE(VoidBatteryUpdatePacket.class, VoidBatteryUpdatePacket::new, NetworkDirection.PLAY_TO_CLIENT);

	/**
	 * 网络通道名称
	 * 
	 * 使用模组ID作为名称空间，确保与其他模组的网络通道不发生冲突。
	 * 这个标识符在客户端和服务器端都必须一致。
	 */
	public static final ResourceLocation CHANNEL_NAME = CreateUtilities.asResource("main");
	
	/**
	 * 网络协议版本号
	 * 
	 * 用于版本兼容性检查，确保客户端和服务器使用相同的数据包格式。
	 * 当数据包结构发生不兼容变化时，应该增加这个版本号。
	 */
	public static final int NETWORK_VERSION = 2;
	
	/**
	 * 网络协议版本号字符串形式
	 * 
	 * Forge网络系统需要字符串形式的版本号进行比较。
	 */
	public static final String NETWORK_VERSION_STR = String.valueOf(NETWORK_VERSION);
	
	/**
	 * 网络通道实例
	 * 
	 * Forge SimpleChannel的实例，用于实际的数据包发送和接收。
	 * 在registerPackets()方法中初始化，模组的其他部分通过这个实例发送数据包。
	 */
	public static SimpleChannel channel;

	/**
	 * 加载的数据包实例
	 * 
	 * 每个枚举值都包含一个LoadedPacket实例，
	 * 用于存储该数据包类型的所有相关信息。
	 */
	private final LoadedPacket<?> packet;

	/**
	 * 构造枚举值
	 * 
	 * 为每个数据包类型创建对应的LoadedPacket实例。
	 * 
	 * @param type 数据包类型，必须继承SimplePacketBase
	 * @param factory 数据包工厂方法，用于从FriendlyByteBuf反序列化数据包
	 * @param direction 数据包传输方向，指定客户端到服务器还是相反
	 */
	<T extends SimplePacketBase> CUPackets(Class<T> type, Function<FriendlyByteBuf, T> factory,
										   NetworkDirection direction) {
		packet = new LoadedPacket<>(type, factory, direction);
	}

	/**
	 * 注册所有数据包
	 * 
	 * 这个方法必须在模组初始化阶段调用，理想情况下是在
	 * FMLCommonSetupEvent事件中调用。它会：
	 * 
	 * 1. 创建网络通道：使用模组的ResourceLocation作为通道名
	 * 2. 设置版本检查：确保客户端和服务器版本一致
	 * 3. 配置协议版本：设置网络协议的版本号
	 * 4. 注册所有数据包：自动遍历枚举值并注册每个数据包
	 * 
	 * 版本检查机制：
	 * - 客户端和服务器版本必须完全一致
	 * - 版本不匹配时会阻止连接，防止数据包格式错误
	 * - 这确保了网络通信的稳定性和正确性
	 */
	public static void registerPackets() {
		channel = NetworkRegistry.ChannelBuilder.named(CHANNEL_NAME)
				.serverAcceptedVersions(NETWORK_VERSION_STR::equals)  // 服务器接受的版本
				.clientAcceptedVersions(NETWORK_VERSION_STR::equals)  // 客户端接受的版本
				.networkProtocolVersion(() -> NETWORK_VERSION_STR)   // 协议版本号
				.simpleChannel();                                    // 创建简单通道
		
		// 自动注册所有枚举定义的数据包
		for (CUPackets packet : values())
			packet.packet.register();
	}

	/**
	 * 加载的数据包内部类
	 * 
	 * 这个静态内部类封装了单个数据包类型的所有相关信息，包括：
	 * - 编码器：将数据包对象序列化为字节流
	 * - 解码器：从字节流反序列化数据包对象
	 * - 处理器：在接收到数据包时执行的逻辑
	 * - 方向控制：指定数据包的传输方向
	 * 
	 * 这种设计将数据包的配置和注册逻辑集中在一起，
	 * 便于维护和管理。
	 * 
	 * @param <T> 数据包类型，必须继承SimplePacketBase
	 */
	private static class LoadedPacket<T extends SimplePacketBase> {
		/**
		 * 数据包索引计数器
		 * 
		 * 用于为每个数据包类型分配唯一的ID。
		 * 这个ID在网络传输中用于标识数据包类型。
		 */
		private static int index = 0;

		/**
		 * 数据包编码器
		 * 
		 * 负责将数据包对象序列化为字节流，以便网络传输。
		 * 使用数据包类的write方法进行序列化。
		 */
		private BiConsumer<T, FriendlyByteBuf> encoder;
		
		/**
		 * 数据包解码器
		 * 
		 * 负责从字节流反序列化数据包对象。
		 * 使用数据包类的构造函数进行反序列化。
		 */
		private Function<FriendlyByteBuf, T> decoder;
		
		/**
		 * 数据包处理器
		 * 
		 * 在接收到数据包时执行的逻辑。负责调用数据包的handle方法，
		 * 并在处理成功后标记数据包为已处理状态。
		 */
		private BiConsumer<T, Supplier<NetworkEvent.Context>> handler;
		
		/**
		 * 数据包类型
		 * 
		 * 存储数据包的Class对象，用于类型校验和反射操作。
		 */
		private Class<T> type;
		
		/**
		 * 网络传输方向
		 * 
		 * 指定数据包的传输方向：
		 * - PLAY_TO_CLIENT：从服务器到客户端
		 * - PLAY_TO_SERVER：从客户端到服务器
		 */
		private NetworkDirection direction;

		/**
		 * 构造加载的数据包
		 * 
		 * 初始化数据包的所有相关组件，为注册做准备。
		 * 
		 * 编码器设置：使用数据包类的write方法引用
		 * 解码器设置：使用传入的工厂方法
		 * 处理器设置：封装数据包的handle方法调用
		 * 
		 * @param type 数据包类型
		 * @param factory 数据包工厂方法
		 * @param direction 传输方向
		 */
		private LoadedPacket(Class<T> type, Function<FriendlyByteBuf, T> factory, NetworkDirection direction) {
			encoder = T::write;  // 使用数据包类的write方法作为编码器
			decoder = factory;   // 使用传入的工厂方法作为解码器
			
			// 设置数据包处理器
			handler = (packet, contextSupplier) -> {
				NetworkEvent.Context context = contextSupplier.get();
				// 调用数据包的处理方法
				if (packet.handle(context)) {
					// 标记数据包为已处理
					context.setPacketHandled(true);
				}
			};
			
			this.type = type;
			this.direction = direction;
		}

		/**
		 * 注册数据包到网络通道
		 * 
		 * 将当前数据包类型注册到Forge的网络通道中。
		 * 这个过程包括：
		 * 
		 * 1. 分配唯一的数据包ID（自动递增）
		 * 2. 设置传输方向（客户端到服务器或相反）
		 * 3. 绑定编码器（序列化方法）
		 * 4. 绑定解码器（反序列化方法）
		 * 5. 绑定处理器（接收后的处理逻辑）
		 * 
		 * 注册后，这个数据包类型就可以在网络中传输了。
		 */
		private void register() {
			channel.messageBuilder(type, index++, direction)  // 创建消息构建器
					.encoder(encoder)                           // 设置编码器
					.decoder(decoder)                           // 设置解码器
					.consumerNetworkThread(handler)             // 设置处理器（在网络线程中执行）
					.add();                                     // 添加到通道
		}

	}

}