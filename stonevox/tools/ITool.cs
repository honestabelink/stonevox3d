using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public interface ITool
{
    void use();
    void _use();

    VoxelModifier getModifier(int x, int y, int z, VoxelSide side);

    bool onraycasthitchanged(ClientInput input, QbMatrix matrix, RaycastHit hit);
}