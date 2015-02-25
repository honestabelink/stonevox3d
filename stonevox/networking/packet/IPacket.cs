using Lidgren.Network;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public interface IPacket
{
    PacketID ID { get; }
    void onclientrecieve(NetIncomingMessage message);
    void onserverrecieve(NetIncomingMessage message);
}