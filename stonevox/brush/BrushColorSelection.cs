using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using OpenTK;
using OpenTK.Input;
using OpenTK.Graphics;

namespace stonevox
{
    public class BrushColorSelection : IVoxelBrush
    {
        public bool Active { get; set; }

        public VoxelBrushType BrushType
        {
            get
            {
                return VoxelBrushType.ColorSelect;
            }
        }

        public MouseCursor Cursor
        {
            get; set;
        }

        public string CursorPath
        {
            get
            {
                return "./data/images/cursor_eyedrop.png";
            }
        }

        public BrushColorSelection()
        {
            Singleton<ClientInput>.INSTANCE.AddHandler(new InputHandler()
            {
                Keydownhandler = (e) =>
                {
                    if (Singleton<ClientGUI>.INSTANCE.OverWidget) return;
                    if (!Active && e.Shift)
                    {
                        Singleton<ClientBrush>.INSTANCE.SetCurrentBrush(BrushType);
                    }
                },
                Keyuphandler = (e) =>
                {
                    if (Singleton<ClientGUI>.INSTANCE.OverWidget) return;

                    if (Active && (e.Key == Key.ShiftLeft || e.Key == Key.ShiftRight))
                    {
                        var clientbrush = Singleton<ClientBrush>.INSTANCE;
                        clientbrush.SetCurrentBrush(clientbrush.previousBrush.BrushType);
                    }
                }
            });
        }

        public bool OnRaycastHitchanged(ClientInput input, QbMatrix matrix, RaycastHit hit, ref Colort color, MouseButtonEventArgs e)
        {
            if ((e != null && e.IsPressed && e.Button == MouseButton.Left) || (e == null && input.mousedown(MouseButton.Left)))
            {
                QbMatrix mat = Singleton<QbManager>.INSTANCE.ActiveModel.matrices[hit.matrixIndex];
                if (mat != null)
                {
                    Voxel voxel;
                    if (mat.voxels.TryGetValue(mat.GetHash(hit.x, hit.y, hit.z), out voxel))
                    {
                        for (int i = 0; i < 10; i++)
                        {
                            var colorpal = Singleton<ClientGUI>.INSTANCE.Get<EmptyWidget>(GUIID.START_COLOR_SELECTORS + i);

                            if ((bool)colorpal.customData["active"])
                            {
                                colorpal.appearence.Get<PlainBackground>("background").color = mat.colors[voxel.colorindex];
                                Singleton<ClientGUI>.INSTANCE.Dirty = true;

                                Singleton<ClientBrush>.INSTANCE.brushColor = mat.colors[voxel.colorindex];

                                Color4 colorr = mat.colors[voxel.colorindex];
                                Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.ColorSelectionChanged, colorpal, colorr);
                            }
                        }
                    }
                }
                return true;
            }
            return false;
        }

        public void Disable()
        {
            Active = false;
            Singleton<Raycaster>.INSTANCE.Mode = RaycastMode.ActiveMatrix;
        }

        public void Enable()
        {
            Active = true;
            Singleton<Raycaster>.INSTANCE.Mode = RaycastMode.MatrixColorSelection;
        }

        public void AddVolume(VoxelVolume volume, QbMatrix matrix, ref Colort color, Dictionary<double, VoxelUndoData> modifiedVoxels)
        {
            throw new NotImplementedException();
        }

        public void CleanLastVolume(VoxelVolume volume, VoxelVolume currentVolume, QbMatrix matrix, Dictionary<double, VoxelUndoData> modifiedVoxels)
        {
            throw new NotImplementedException();
        }


        public void EnumerateVolume(VoxelVolume volume, VoxelVolume currentVolume, QbMatrix matrix, ref Colort color, Dictionary<double, VoxelUndoData> modifiedVoxels)
        {
            throw new NotImplementedException();
        }

        public void RemoveVolume(VoxelVolume volume, QbMatrix matrix, Dictionary<double, VoxelUndoData> modifiedVoxels)
        {
            throw new NotImplementedException();
        }
    }
}
