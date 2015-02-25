using Lidgren.Network;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.Serialization.Formatters.Binary;
using System.Text;
using System.Threading.Tasks;

public class Packet_QbImported : Packet
{
    public override PacketID ID
    {
        get { return PacketID.QB_IMPORTED; }
    }

    public override void onclientrecieve(NetIncomingMessage message)
    {
        Client.print("info", "Recieve qb packet");
        StopwatchUtil.startclient("qbpacket", "Building qb model");

        QbModel model = new QbModel();
        int junk = message.ReadInt32();
        int matrixcount = message.ReadInt32();
        model.setmatrixcount((uint)matrixcount);

        foreach (var m in model.matrices)
        {
            m.name = message.ReadString();

            m.position = new OpenTK.Vector3(message.ReadFloat(), message.ReadFloat(), message.ReadFloat());
            m.setsize((int)message.ReadFloat(), (int)message.ReadFloat(), (int)message.ReadFloat());

            int colorcount = message.ReadInt32();

            for (int i = 0; i < colorcount; i++)
            {
                m.getcolorindex(message.ReadFloat(), message.ReadFloat(), message.ReadFloat());
            }

            int voxelcount = message.ReadInt32();

            for (int i = 0; i < voxelcount; i++)
            {
                int colorindex = message.ReadInt32();
                byte alphamask = message.ReadByte();
                int x = message.ReadInt32();
                int y = message.ReadInt32();
                int z = message.ReadInt32();

                m.voxels.GetOrAdd(m.gethash(x, y, z), new Voxel(x, y, z, alphamask, colorindex));
            }
        }

        Client.stonevoxcalls.Add(() =>
        { 
            Client.window.model = model;
            model.generatevertexbuffers();
            model.fillvertexbuffers();

            QbManager.models.Add(model);
            StopwatchUtil.stopclient("qbpacket", "End building qb model");
        });

        base.onclientrecieve(message);
    }

    public override void onserverrecieve(NetIncomingMessage message)
    {
        Server.print("info", "Recieved qb packet");
        NetConnection senderconnection = message.SenderConnection;

        var packet = PacketWriter.write<Packet_QbImported>(NetEndpoint.SERVER);
        packet.outgoingmessage.Write(message.Data);
        packet.send();

        base.onserverrecieve(message);
    }

    public void write(QbModel model)
    {
        outgoingmessage.Write(model.numMatrices);
        foreach(var m in model.matrices)
        {
            outgoingmessage.Write(m.name);
            outgoingmessage.Write(m.position.X);
            outgoingmessage.Write(m.position.Y);
            outgoingmessage.Write(m.position.Z);
            outgoingmessage.Write(m.size.X);
            outgoingmessage.Write(m.size.Y);
            outgoingmessage.Write(m.size.Z);

            outgoingmessage.Write(m.colors.Length);
            foreach (var color in m.colors)
            {
                outgoingmessage.Write(color.R);
                outgoingmessage.Write(color.G);
                outgoingmessage.Write(color.B);
            }

            outgoingmessage.Write(m.voxels.Count);
            foreach (var voxel in m.voxels.Values)
            {
                outgoingmessage.Write(voxel.colorindex);
                outgoingmessage.Write(voxel.alphamask);
                outgoingmessage.Write(voxel.x);
                outgoingmessage.Write(voxel.y);
                outgoingmessage.Write(voxel.z);
            }
        }
    }
}
