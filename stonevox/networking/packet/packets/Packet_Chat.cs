using Lidgren.Network;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public class Packet_Chat : Packet
{
    public override PacketID ID
    {
        get { return PacketID.CHAT; }
    }

    public override void onclientrecieve(NetIncomingMessage message)
    {
        string id = message.ReadString();
        string text = message.ReadString();
        Console.WriteLine(text);

        base.onclientrecieve(message);
    }

    public override void onserverrecieve(NetIncomingMessage message)
    {
        var chat = PacketWriter.write<Packet_Chat>(NetEndpoint.SERVER);
        chat.outgoingmessage.Write(message.ReadString());
        chat.outgoingmessage.Write(message.ReadString());
        chat.send();

        base.onserverrecieve(message);
    }
}