package me.duquee.createutilities.networking.packets;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import me.duquee.createutilities.CreateUtilitiesClient;
import me.duquee.createutilities.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import me.duquee.createutilities.blocks.voidtypes.tank.VoidTank;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 虚空储罐更新数据包类
 * 
 * 负责在客户端和服务器之间同步虚空储罐的流体数据。
 * 当虚空储罐的内容发生变化时，服务器会发送此数据包通知所有客户端更新本地缓存。
 * 这确保了所有连接到同一虚空网络的储罐在不同维度和位置都显示相同的流体内容。
 */
public class VoidTankUpdatePacket extends SimplePacketBase {

	/**
	 * 网络键，用于标识特定的虚空存储网络
	 * 包含所有者信息和两个频率标识符
	 */
	private final NetworkKey key;
	
	/**
	 * 流体储罐实例，包含实际的流体数据
	 */
	private final FluidTank tank;

	/**
	 * 构造函数：从服务器端创建更新数据包
	 * 
	 * @param key 虚空网络的唯一标识键
	 * @param tank 虚空储罐实例，包含当前流体状态
	 */
	public VoidTankUpdatePacket(NetworkKey key, VoidTank tank) {
		this.key = key;
		this.tank = tank;
	}

	/**
	 * 构造函数：从网络缓冲区反序列化数据包
	 * 
	 * 当客户端接收到数据包时，使用此构造函数从字节流中重建数据包内容。
	 * 
	 * @param buffer 包含序列化数据的网络缓冲区
	 */
	public VoidTankUpdatePacket(FriendlyByteBuf buffer) {
		key = NetworkKey.fromBuffer(buffer);
		tank = new FluidTank(VoidTank.CAPACITY).readFromNBT(buffer.readNbt());
	}

	/**
	 * 将数据包内容写入网络缓冲区
	 * 
	 * 在发送数据包之前调用，将网络键和储罐数据序列化到字节流中。
	 * 
	 * @param buffer 用于存储序列化数据的网络缓冲区
	 */
	@Override
	public void write(FriendlyByteBuf buffer) {
		key.writeToBuffer(buffer);
		buffer.writeNbt(tank.writeToNBT(new CompoundTag()));
	}

	/**
	 * 处理接收到的数据包
	 * 
	 * 当客户端接收到此数据包时，更新客户端的虚空储罐缓存。
	 * 此方法只在客户端执行，确保客户端显示的流体内容与服务器同步。
	 * 
	 * @param context 网络事件上下文，提供数据包处理环境
	 * @return 总是返回true，表示数据包处理成功
	 */
	@Override
	public boolean handle(NetworkEvent.Context context) {
		context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
				CreateUtilitiesClient.VOID_TANKS.storages.put(key, tank)));
		return true;
	}

}
