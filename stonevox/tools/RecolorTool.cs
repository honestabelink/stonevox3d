using OpenTK.Input;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public class RecolorTool : ITool
{
    private Colort ___hackcolor = new Colort(1, 0, 0);

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
            matrix.recolor(hit.x, hit.y, hit.z, ___hackcolor);
            return true;
        }
        return false;
    }
}
