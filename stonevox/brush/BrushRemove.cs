using OpenTK;
using OpenTK.Input;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public class BrushRemove : IVoxelBrush
    {
        ToolState state = ToolState.Start;
        VoxelLocation startPosition;
        VoxelLocation endPosition;
        VoxelVolume volume;
        VoxelVolume lastvolume;
        QbMatrix lastmatrix;
        Dictionary<double, VoxelRemoved> modifiedVoxels = new Dictionary<double, VoxelRemoved>();
        public MouseCursor Cursor { get; set; }

        public VoxelBrushTypes BrushType { get { return VoxelBrushTypes.Remove; } }

        public bool Active { get; set; }

        public string CursorPath
        {
            get
            {
                return "./data/images/cursor_remove.png"; 
            }
        }

        public BrushRemove()
        {
            Singleton<ClientInput>.INSTANCE.AddHandler(new InputHandler()
            {
                mouseuphandler = new MouseHandler((MouseButtonEventArgs e) =>
                {
                    if (!Active) return;

                    if (e.Button == MouseButton.Right && Singleton<ClientInput>.INSTANCE.Keydown(Key.AltLeft) && state == ToolState.Base)
                    {
                        state = ToolState.Disabled;
                        if (Singleton<ClientInput>.INSTANCE.mouseup(MouseButton.Left))
                        {
                            CleanForToolReset();
                            state = ToolState.Start;
                        }
                    }

                    if (state == ToolState.Disabled && e.Button == MouseButton.Left)
                    {
                        state = ToolState.Start;
                        CleanForToolReset();
                    }
                })
            });
        }

        public bool OnRaycastHitchanged(ClientInput input, QbMatrix matrix, RaycastHit hit, ref Colort color)
        {
            lastmatrix = matrix;
            switch (state)
            {
                case ToolState.Start:
                    if (input.mousedown(MouseButton.Left))
                    {
                        state = ToolState.Base;
                        Raycaster.testdirt = true;
                        startPosition = new VoxelLocation(hit, false);
                        endPosition = new VoxelLocation(hit, false);
                        volume = new VoxelVolume(startPosition, endPosition);
                        modifiedVoxels.Clear();
                        EnumerateVolume(volume, matrix);
                        lastvolume = volume;
                        return true;
                    }
                    break;
                case ToolState.Base:
                    if (input.mousedown(MouseButton.Left))
                    {
                        endPosition = new VoxelLocation(hit, false);
                        volume = new VoxelVolume(startPosition, endPosition);

                        EnumerateVolume(volume, matrix);
                        CleanLastVolume(lastvolume, volume, matrix);
                        lastvolume = volume;

                        return true;
                    }
                    else if (input.mouseup(MouseButton.Left))
                    {
                        state = ToolState.Start;
                        lastvolume = VoxelVolume.NEGATIVE_ZERO;
                        return true;
                    }
                    break;
                case ToolState.Limit:
                    break;
            }
            return false;
        }

        void EnumerateVolume(VoxelVolume volume, QbMatrix matrix)
        {
            VoxelRemoved removed;
            for (int z = volume.minz; z <= volume.maxz; z++)
                for (int y = volume.miny; y <= volume.maxy; y++)
                    for (int x = volume.minx; x <= volume.maxx; x++)
                    {
                        if (!modifiedVoxels.ContainsKey(matrix.GetHash(x, y, z)))
                        {
                            if (matrix.GetColorIndex_Alphamask(x, y, z, out removed.colorindex, out removed.alphamask))
                            {
                                if (matrix.Remove(x, y, z, true, false))
                                    modifiedVoxels.Add(matrix.GetHash(x, y, z), removed);
                            }
                        }
                    }
        }

        void CleanLastVolume(VoxelVolume volume, VoxelVolume currentVolume, QbMatrix matrix)
        {
            VoxelRemoved removed;

            for (int z = volume.minz; z <= volume.maxz; z++)
                for (int y = volume.miny; y <= volume.maxy; y++)
                    for (int x = volume.minx; x <= volume.maxx; x++)
                    {
                        if (!currentVolume.ContainsPoint(x, y, z))
                        {
                            if (modifiedVoxels.TryGetValue(matrix.GetHash(x, y, z), out removed))
                            {
                                if (removed.alphamask > 1)
                                    matrix.Add(x, y, z, matrix.colors[removed.colorindex]);
                                modifiedVoxels.Remove(matrix.GetHash(x, y, z));
                            }
                        }
                    }
        }

        void CleanForToolReset()
        {
            RemoveVolume(volume);
            modifiedVoxels.Clear();
            lastvolume = VoxelVolume.NEGATIVE_ZERO;
        }

        void RemoveVolume(VoxelVolume volume)
        {
            VoxelRemoved removed;

            for (int z = volume.minz; z <= volume.maxz; z++)
                for (int y = volume.miny; y <= volume.maxy; y++)
                    for (int x = volume.minx; x <= volume.maxx; x++)
                    {
                        if (modifiedVoxels.TryGetValue(lastmatrix.GetHash(x, y, z), out removed))
                        {
                            if (removed.alphamask > 1)
                                lastmatrix.Add(x, y, z, lastmatrix.colors[removed.colorindex]);
                            modifiedVoxels.Remove(lastmatrix.GetHash(x, y, z));
                        }
                    }
        }

        public void Enable()
        {
            Active = true;
        }

        public void Disable()
        {
            Active = false;
        }

        enum ToolState
        {
            Disabled,
            Start,
            Base,
            Limit
        }

        struct VoxelRemoved
        {
            public int colorindex;
            public byte alphamask;

            public VoxelRemoved(int colorindex, byte alphamask)
            {
                this.colorindex = colorindex;
                this.alphamask = alphamask;
            }
        }
    }
}