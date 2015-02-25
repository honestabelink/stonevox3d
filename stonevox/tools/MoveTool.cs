using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public class MoveTool : ITool
{
    public void use()
    {
    }

    public void _use()
    {
    }

    public VoxelModifier getModifier(int x, int y, int z, VoxelSide side)
    {
        return null;
    }

    public bool onraycasthitchanged(ClientInput input, QbMatrix matrix, RaycastHit hit)
    {
        return true;
    }
}
