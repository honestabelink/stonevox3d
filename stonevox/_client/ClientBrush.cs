using OpenTK.Input;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public class ClientBrush : Singleton<ClientBrush>
    {
        public Colort brushColor = new Colort(.3f, .2f, .8f);
        public IVoxelBrush currentBrush;
        public IVoxelBrush previousBrush;

        public Dictionary<VoxelBrushType, IVoxelBrush> brushes;

        private GLWindow window;

        public ClientBrush(GLWindow window, ClientInput input)
            : base()
        {
            this.window = window;

            brushes = new Dictionary<VoxelBrushType, IVoxelBrush>();
            brushes.Add(VoxelBrushType.Add, new BrushAdd());
            brushes.Add(VoxelBrushType.Remove, new BrushRemove());
            brushes.Add(VoxelBrushType.Recolor, new BrushRecolor());

            input.AddHandler(new InputHandler()
            {
                Keydownhandler = (e) =>
                {
                    if (e.Key == Key.Tab)
                    {
                        NextBrush();
                    }
                }
            });

            window.SVReizeEvent += (e, o) =>
            {
                var values = Enum.GetValues(typeof(VoxelBrushType));
                var enumer = values.GetEnumerator();
                while(enumer.MoveNext())
                {
                    string path = brushes[(VoxelBrushType)enumer.Current].CursorPath;

                    Bitmap bitmap = new Bitmap(path);

                    if (window.Width <= 1280)
                        bitmap = bitmap.ResizeImage(new Size((int)(bitmap.Width * .75f), (int)(bitmap.Height * .75f)));
                    else if (window.Width <= 1400)
                        bitmap = bitmap.ResizeImage(new Size((int)(bitmap.Width * .8f), (int)(bitmap.Height * .8f)));

                    bitmap.RotateFlip(RotateFlipType.RotateNoneFlipY);
                    var data = bitmap.LockBits(
                        new Rectangle(0, 0, bitmap.Width, bitmap.Height),
                        System.Drawing.Imaging.ImageLockMode.ReadOnly,
                        System.Drawing.Imaging.PixelFormat.Format32bppPArgb);

                    brushes[(VoxelBrushType)enumer.Current].Cursor = new OpenTK.MouseCursor(
                        0, 0, data.Width, data.Height, data.Scan0);
                }
            };

            NextBrush();
        }

        public void SetCurrentBrush(VoxelBrushType type)
        {
            if (previousBrush != null)
            {
                currentBrush.Disable();
                previousBrush = currentBrush;
            }
            currentBrush = brushes[type];
            currentBrush.Enable();

            if (Singleton<ClientGUI>.INSTANCE?.OverWidget == false)
                window.Cursor = currentBrush.Cursor;
        }

        public void NextBrush()
        {
            var values = Enum.GetValues(typeof(VoxelBrushType));
            var enumer = values.GetEnumerator();
            enumer.MoveNext();
            VoxelBrushType first = (VoxelBrushType)enumer.Current;

            if (currentBrush != null)
            {
                do
                {
                    if ((VoxelBrushType)enumer.Current == currentBrush.BrushType)
                    {
                        if (enumer.MoveNext())
                        {
                            SetCurrentBrush((VoxelBrushType)enumer.Current);
                            return;
                        }
                    }
                } while (enumer.MoveNext());
            }

            SetCurrentBrush(first);
        }

        public bool onselectionchanged(ClientInput input, QbMatrix matrix, RaycastHit hit, MouseButtonEventArgs e = null)
        {
            return currentBrush.OnRaycastHitchanged(input, matrix, hit, ref brushColor, e);
        }
    }
}
