using OpenTK;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public interface IVoxelBrush
    {
        VoxelBrushTypes BrushType { get; }
        bool Active { get; set; }
        MouseCursor Cursor { get; set; }

        void Enable();
        void Disable();

        bool OnRaycastHitchanged(ClientInput input, QbMatrix matrix, RaycastHit hit, ref Colort color);
    }
}