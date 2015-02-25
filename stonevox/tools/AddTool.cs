using OpenTK;
using OpenTK.Input;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public class AddTool : ITool
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
            Raycaster.testdirt = false;
            matrix.add(SideUtil.modify_x(hit.x, hit.side), SideUtil.modify_y(hit.y, hit.side),  SideUtil.modify_z(hit.z, hit.side), new Colort(0,1,1));
            return true;
        }
        return false;
    }
}
