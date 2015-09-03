using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.InteropServices;
using System.Runtime.InteropServices.ComTypes;
using System.Text;

namespace SVPThumbnailer
{
    public class StreamWrapper : Stream
    {
        public IStream m_stream;

        private void CheckDisposed()
        {
            if (m_stream == null)
            {
                throw new ObjectDisposedException("StreamWrapper");
            }
        }

        protected override void Dispose(bool disposing)
        {
            if (m_stream != null)
            {
                Marshal.ReleaseComObject(m_stream);
                m_stream = null;
            }
        }

        public StreamWrapper(IStream stream)
        {
            if (stream == null)
            {
                throw new ArgumentNullException();
            }

            m_stream = stream;
        }

        public override bool CanRead
        {
            get
            {
                return true;
            }
        }

        public override bool CanSeek
        {
            get
            {
                return true;
            }
        }

        public override bool CanWrite
        {
            get
            {
                return false;
            }
        }

        public override void Flush()
        {
            throw new NotImplementedException();
        }

        public override long Length
        {
            get
            {
                CheckDisposed();

                System.Runtime.InteropServices.ComTypes.STATSTG stat;
                m_stream.Stat(out stat, 1); //STATFLAG_NONAME

                return stat.cbSize;
            }
        }

        public override long Position
        {
            get
            {
                return Seek(0, SeekOrigin.Current);
            }
            set
            {
                Seek(value, SeekOrigin.Begin);
            }
        }

        public override int Read(byte[] buffer, int offset, int count)
        {
            CheckDisposed();

            if (offset < 0 || count < 0 || offset + count > buffer.Length)
            {
                throw new ArgumentOutOfRangeException();
            }

            byte[] localBuffer = buffer;

            if (offset > 0)
            {
                localBuffer = new byte[count];
            }

            IntPtr bytesReadPtr = Marshal.AllocCoTaskMem(sizeof(int));

            try
            {
                m_stream.Read(localBuffer, count, bytesReadPtr);
                int bytesRead = Marshal.ReadInt32(bytesReadPtr);

                if (offset > 0)
                {
                    Array.Copy(localBuffer, 0, buffer, offset, bytesRead);
                }

                return bytesRead;
            }
            finally
            {
                Marshal.FreeCoTaskMem(bytesReadPtr);
            }
        }

        public override long Seek(long offset, SeekOrigin origin)
        {
            CheckDisposed();

            int dwOrigin;

            switch (origin)
            {
                case SeekOrigin.Begin:

                    dwOrigin = 0;   // STREAM_SEEK_SET
                    break;

                case SeekOrigin.Current:

                    dwOrigin = 1;   // STREAM_SEEK_CUR
                    break;

                case SeekOrigin.End:

                    dwOrigin = 2;   // STREAM_SEEK_END
                    break;

                default:

                    throw new ArgumentOutOfRangeException();

            }

            IntPtr posPtr = Marshal.AllocCoTaskMem(sizeof(long));

            try
            {
                m_stream.Seek(offset, dwOrigin, posPtr);
                return Marshal.ReadInt64(posPtr);
            }
            finally
            {
                Marshal.FreeCoTaskMem(posPtr);
            }
        }

        public override void SetLength(long value)
        {
            throw new NotImplementedException();
        }

        public override void Write(byte[] buffer, int offset, int count)
        {
            throw new NotImplementedException();
        }
    }
}
