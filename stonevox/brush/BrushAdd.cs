using OpenTK;
using OpenTK.Input;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace stonevox
{
    public class BrushAdd : IVoxelBrush
    {
        ToolState state = ToolState.Start;
        VoxelLocation startPosition;
        VoxelLocation endPosition;
        VoxelVolume volume;
        VoxelVolume lastvolume;
        QbMatrix lastmatrix;
        Dictionary<double, bool> modifiedVoxels = new Dictionary<double, bool>();
        public MouseCursor Cursor { get; set; }

        public VoxelBrushTypes BrushType { get { return VoxelBrushTypes.Add; } }

        public bool Active { get; set; }

        public BrushAdd()
        {
            using (Bitmap bitmap = new Bitmap("./data/images/cursor_add.png"))
            {
                bitmap.RotateFlip(RotateFlipType.RotateNoneFlipY);
                var data = bitmap.LockBits(
                    new Rectangle(0, 0, bitmap.Width, bitmap.Height),
                    System.Drawing.Imaging.ImageLockMode.ReadOnly,
                    System.Drawing.Imaging.PixelFormat.Format32bppPArgb);

                Cursor = new OpenTK.MouseCursor(
                    0, 0, data.Width, data.Height, data.Scan0);
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
                        Raycaster.testdirt = false;
                        startPosition = new VoxelLocation(hit);
                        endPosition = new VoxelLocation(hit);
                        volume = new VoxelVolume(startPosition, endPosition);
                        modifiedVoxels.Clear();
                        EnumerateVolume(lastvolume, volume, matrix, ref color);
                        lastvolume = volume;
                        return true;
                    }
                    break;
                case ToolState.Base:
                    if (input.mousedown(MouseButton.Left))
                    {
                        endPosition = new VoxelLocation(hit);
                        volume = new VoxelVolume(startPosition, endPosition);

                        EnumerateVolume(lastvolume, volume, matrix, ref color);
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

        void EnumerateVolume(VoxelVolume volume, VoxelVolume currentVolume, QbMatrix matrix, ref Colort color)
        {
            for (int z = currentVolume.minz; z <= currentVolume.maxz; z++)
                for (int y = currentVolume.miny; y <= currentVolume.maxy; y++)
                    for (int x = currentVolume.minx; x <= currentVolume.maxx; x++)
                    {
                        if (!volume.ContainsPoint(x,y,z) && !modifiedVoxels.ContainsKey(matrix.GetHash(x,y,z)))
                            modifiedVoxels.Add(matrix.GetHash(x, y, z), matrix.Add(x, y, z, color));
                    }
        }
        void CleanLastVolume(VoxelVolume volume, VoxelVolume currentVolume, QbMatrix matrix)
        {

            for (int z = volume.minz; z <= volume.maxz; z++)
                for (int y = volume.miny; y <= volume.maxy; y++)
                    for (int x = volume.minx; x <= volume.maxx; x++)
                    {
                        if (!currentVolume.ContainsPoint(x, y, z))
                        {
                            if (modifiedVoxels[matrix.GetHash(x, y, z)])
                            {
                                matrix.Remove(x, y, z, false, false);
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
            for (int z = volume.minz; z <= volume.maxz; z++)
                for (int y = volume.miny; y <= volume.maxy; y++)
                    for (int x = volume.minx; x <= volume.maxx; x++)
                    {
                        bool _temp;
                        if (modifiedVoxels.TryGetValue(lastmatrix.GetHash(x, y, z), out _temp) && _temp)
                        {
                            lastmatrix.Remove(x, y, z, false, false);
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

        public void Dispose()
        {
        }
    }
}