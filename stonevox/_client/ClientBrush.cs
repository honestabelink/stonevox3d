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

        private Dictionary<VoxelBrushTypes, IVoxelBrush> brushes;

        private GLWindow window;

        public ClientBrush(GLWindow window, ClientInput input)
            : base()
        {
            this.window = window;

            brushes = new Dictionary<VoxelBrushTypes, IVoxelBrush>();
            brushes.Add(VoxelBrushTypes.Add, new BrushAdd());
            brushes.Add(VoxelBrushTypes.Remove, new BrushRemove());
            brushes.Add(VoxelBrushTypes.Recolor, new BrushRecolor());

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
                var values = Enum.GetValues(typeof(VoxelBrushTypes));
                var enumer = values.GetEnumerator();
                while(enumer.MoveNext())
                {
                    string path = brushes[(VoxelBrushTypes)enumer.Current].CursorPath;

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

                    brushes[(VoxelBrushTypes)enumer.Current].Cursor = new OpenTK.MouseCursor(
                        0, 0, data.Width, data.Height, data.Scan0);
                }
            };

            NextBrush();
        }

        public void SetCurrentBrush(VoxelBrushTypes type)
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
            var values = Enum.GetValues(typeof(VoxelBrushTypes));
            var enumer = values.GetEnumerator();
            enumer.MoveNext();
            VoxelBrushTypes first = (VoxelBrushTypes)enumer.Current;

            if (currentBrush != null)
            {
                do
                {
                    if ((VoxelBrushTypes)enumer.Current == currentBrush.BrushType)
                    {
                        if (enumer.MoveNext())
                        {
                            SetCurrentBrush((VoxelBrushTypes)enumer.Current);
                            return;
                        }
                    }
                } while (enumer.MoveNext());
            }

            SetCurrentBrush(first);
        }

        public bool onselectionchanged(ClientInput input, QbMatrix matrix, RaycastHit hit)
        {
            return currentBrush.OnRaycastHitchanged(input, matrix, hit, ref brushColor);
        }
    }
}
