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
    public class BrushRecolor : IVoxelBrush
    {
        ToolState state = ToolState.Start;
        VoxelLocation startPosition;
        VoxelLocation endPosition;
        VoxelVolume volume;
        VoxelVolume lastvolume;
        QbMatrix lastmatrix;
        Dictionary<double, int> modifiedVoxels = new Dictionary<double, int>();
        public MouseCursor Cursor { get; set; }


        public VoxelBrushTypes BrushType { get { return VoxelBrushTypes.Recolor; } }

        public bool Active { get; set; }

        public BrushRecolor()
        {
            using (Bitmap bitmap = new Bitmap("./data/images/cursor_paint.png"))
            {
                bitmap.RotateFlip(RotateFlipType.RotateNoneFlipY);
                var data = bitmap.LockBits(
                    new Rectangle(0, 0, bitmap.Width, bitmap.Height),
                    System.Drawing.Imaging.ImageLockMode.ReadOnly,
                    System.Drawing.Imaging.PixelFormat.Format32bppPArgb);

                Cursor = new OpenTK.MouseCursor(
                   0,0, data.Width, data.Height, data.Scan0);
            }

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
                        EnumerateVolume(volume, matrix, ref color);
                        lastvolume = volume;
                        return true;
                    }
                    break;
                case ToolState.Base:
                    if (input.mousedown(MouseButton.Left))
                    {
                        endPosition = new VoxelLocation(hit, false);
                        volume = new VoxelVolume(startPosition, endPosition);

                        EnumerateVolume(volume, matrix, ref color);
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

        void EnumerateVolume(VoxelVolume volume, QbMatrix matrix, ref Colort color)
        {
            double hash;
            Voxel voxel = null;
            for (int z = volume.minz; z <= volume.maxz; z++)
                for (int y = volume.miny; y <= volume.maxy; y++)
                    for (int x = volume.minx; x <= volume.maxx; x++)
                    {
                        hash = matrix.GetHash(x, y, z);

                        if (!modifiedVoxels.ContainsKey(hash) && matrix.voxels.TryGetValue(hash, out voxel) && voxel.alphamask > 1)
                        {
                            modifiedVoxels.Add(hash, matrix.voxels[hash].colorindex);
                            matrix.Color(x, y, z, color);
                        }
                    }
        }

        void CleanLastVolume(VoxelVolume volume, VoxelVolume currentVolume, QbMatrix matrix)
        {
            double hash;
            int colorindex;
            for (int z = volume.minz; z <= volume.maxz; z++)
                for (int y = volume.miny; y <= volume.maxy; y++)
                    for (int x = volume.minx; x <= volume.maxx; x++)
                    {
                        if (!currentVolume.ContainsPoint(x, y, z))
                        {
                            hash = matrix.GetHash(x, y, z);
                            if (modifiedVoxels.TryGetValue(hash, out colorindex))
                            {
                                matrix.Color(x, y, z, colorindex, false, true);
                                modifiedVoxels.Remove(hash);
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
            double hash;
            int colorindex;
            for (int z = volume.minz; z <= volume.maxz; z++)
                for (int y = volume.miny; y <= volume.maxy; y++)
                    for (int x = volume.minx; x <= volume.maxx; x++)
                    {
                        hash = lastmatrix.GetHash(x, y, z);
                        if (modifiedVoxels.TryGetValue(hash, out colorindex))
                        {
                            lastmatrix.Color(x, y, z, colorindex, false, true);
                        }
                    }
        }

        enum ToolState
        {
            Disabled,
            Start,
            Base,
            Limit
        }

        public void Enable()
        {
            Active = true;
        }

        public void Disable()
        {
            Active = false;
        }

    }
}