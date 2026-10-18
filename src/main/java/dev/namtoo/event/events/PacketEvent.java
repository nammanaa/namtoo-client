package dev.namtoo.event.events;
import net.minecraft.network.packet.Packet;
public class PacketEvent {
    public final Packet<?> packet;
    public boolean cancelled = false;
    public PacketEvent(Packet<?> p) { packet = p; }
}
