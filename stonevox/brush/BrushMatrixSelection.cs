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
    public class BrushMatrixSelection : IVoxelBrush
    {
        public bool Active { get; set; }

        public VoxelBrushType BrushType
        {
            get
            {
                return VoxelBrushType.MatrixSelect;
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
               return "./data/images/target_cursor.png";
            }
        }

        private QbMatrix lastmatrix;

        public BrushMatrixSelection()
        {
            Singleton<ClientInput>.INSTANCE.AddHandler(new InputHandler()
            {
                Keydownhandler = (e) =>
                {
                    if (Singleton<ClientGUI>.INSTANCE.OverWidget) return;
                    if (!Active && e.Alt)
                    {
                        Singleton<ClientBrush>.INSTANCE.SetCurrentBrush(BrushType);
                    }
                },
                Keyuphandler = (e) =>
                {
                    if (Singleton<ClientGUI>.INSTANCE.OverWidget) return;

                    if (Active && (e.Key == Key.AltLeft || e.Key == Key.AltRight))
                    {
                        var clientbrush = Singleton<ClientBrush>.INSTANCE;
                        clientbrush.SetCurrentBrush(clientbrush.previousBrush.BrushType);
                        Singleton<ClientGUI>.INSTANCE.Get<Label>(GUIID.STATUS_TEXT).text = "";
                        if (lastmatrix != null)
                        {
                            Client.window.model.activematrix = Client.window.model.matrices.IndexOf(lastmatrix);
                            Singleton<Camera>.INSTANCE.LookAtMatrix();
                            lastmatrix.highlight = Color4.White;
                            lastmatrix = null;
                        }
                    }
                }
            });
        }


        public bool OnRaycastHitchanged(ClientInput input, QbMatrix matrix, RaycastHit hit, ref Colort color, MouseButtonEventArgs e)
        {
            if (matrix == null)
            {
                if (lastmatrix != null)
                {
                    lastmatrix.highlight = Color4.White;
                    lastmatrix = null;
                    Singleton<ClientGUI>.INSTANCE.Get<Label>(GUIID.STATUS_TEXT).text = "";
                }
                return true;
            }

            if(matrix != lastmatrix)
            {
                if (lastmatrix != null)
                    lastmatrix.highlight = Color4.White;
                matrix.highlight = new Colort(1.5f, 1.5f, 1.5f);

                lastmatrix = matrix;
                Singleton<ClientGUI>.INSTANCE.Get<Label>(GUIID.STATUS_TEXT).text = $"Over Matrix : {matrix.name}";
            }
            return true;
        }

        public void Disable()
        {
            Active = false;
            Singleton<Raycaster>.INSTANCE.Mode = RaycastMode.ActiveMatrix;
        }

        public void Enable()
        {
            Active = true;
            Singleton<Raycaster>.INSTANCE.Mode = RaycastMode.MatrixSelection;
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
