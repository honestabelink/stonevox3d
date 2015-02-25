using OpenTK.Input;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public class RemoveTool : ITool
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
        if (input.mousedown(MouseButton.Left))
        {
            //Console.WriteLine(string.Format("add : {0}", hit.ToString()));
            Raycaster.testdirt = true;
            matrix.remove(hit.x, hit.y, hit.z);
            return true;
        }
        return false;
    }
}
