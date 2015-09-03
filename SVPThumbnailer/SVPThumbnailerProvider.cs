using System;
using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Runtime.InteropServices;
using System.Runtime.InteropServices.ComTypes;
using System.Security.Permissions;
using System.Text;
using System.Threading;

namespace SVPThumbnailer
{
    [ComVisible(true), ClassInterface(ClassInterfaceType.None)]
    [ProgId("SVPThumbnailer.SVPThumbnailerProvider"), Guid("A83C0F7D-37A3-4253-896F-29926081E8C4")]
    public class SVPThumbnailerProvider : IThumbnailProvider, IInitializeWithStream
    {
        #region IInitializeWithStream

        private StreamWrapper BaseStream { get; set; }

        public void Initialize(IStream stream, int grfMode)
        {
            this.BaseStream = new StreamWrapper(stream);
        }

        #endregion

        #region IThumbnailProvider

        public void GetThumbnail(int cx, out IntPtr hBitmap, out WTS_ALPHATYPE bitmapType)
        {
            hBitmap = IntPtr.Zero;
            bitmapType = WTS_ALPHATYPE.WTSAT_ARGB;

            try
            {
                unsafe
                {
                    byte[] buffer = new byte[256];
                    int bytesRead = 0;
                    int* ptr = &bytesRead;
                    BaseStream.m_stream.Read(buffer, 4, (IntPtr)ptr);
                    int length = BitConverter.ToInt32(buffer, 0);

                    buffer = new byte[length];
                    BaseStream.m_stream.Read(buffer, length, (IntPtr)ptr);

                    using (MemoryStream steam = new MemoryStream(buffer))
                    using (var image = new Bitmap(steam))
                    using (var scaled = new Bitmap(image, cx, cx))
                    {
                        hBitmap = scaled.GetHbitmap();
                    }
                }
            }
            catch (Exception ex)
            {
            }

            BaseStream.Dispose();
        }
        #endregion
    }
}
