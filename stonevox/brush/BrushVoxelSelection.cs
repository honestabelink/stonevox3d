using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using OpenTK;
using OpenTK.Input;

namespace stonevox
{
    public class BrushVoxelSelection : IVoxelBrush
    {
        private bool active;

        public bool Active
        {
            get
            {
                return active;
            }

            set
            {
                active = value;
            }
        }

        public VoxelBrushType BrushType
        {
            get
            {
                return VoxelBrushType.Select;
            }
        }

        public MouseCursor Cursor
        {
            get;
            set;
        }

        public string CursorPath
        {
            get
            {
                return "";
            }
        }

        public void AddVolume(VoxelVolume volume, QbMatrix matrix, ref Colort color, Dictionary<double, VoxelUndoData> modifiedVoxels)
        {
        }

        public void CleanLastVolume(VoxelVolume volume, VoxelVolume currentVolume, QbMatrix matrix, Dictionary<double, VoxelUndoData> modifiedVoxels)
        {
        }

        public void Disable()
        {
            active = false;
            Singleton<Raycaster>.INSTANCE.Enabled = true;
            Singleton<GUI>.INSTANCE.Get<EmptyWidget>(GUIID.HACKYSELECTIONTOOL).Enable = false;
        }

        public void Enable()
        {
            active = true;
            Singleton<Raycaster>.INSTANCE.Enabled = false;
            Singleton<GUI>.INSTANCE.Get<EmptyWidget>(GUIID.HACKYSELECTIONTOOL).Enable = true;
        }

        public void EnumerateVolume(VoxelVolume volume, VoxelVolume currentVolume, QbMatrix matrix, ref Colort color, Dictionary<double, VoxelUndoData> modifiedVoxels)
        {
        }

        public bool OnRaycastHitchanged(Input input, QbMatrix matrix, RaycastHit hit, ref Colort color, MouseButtonEventArgs e)
        {
            return true;
        }

        public void RemoveVolume(VoxelVolume volume, QbMatrix matrix, Dictionary<double, VoxelUndoData> modifiedVoxels)
        {
        }
    }
}
