using OpenTK;
using OpenTK.Graphics;
using OpenTK.Graphics.OpenGL4;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public class QbMatrixSide : IDisposable
{
    public QbMatrix owner;
    private int vertexarrayID;
    private int vertexbufferID;

    private float[] buffer;
    private List<int> bufferholes;

    private float cubesize = .5f;
    private int bufferposition;
    private float lightscale = 1;

    public Vector3 normal;

    private Side side;

    public QbMatrixSide(Side side)
    {
        this.side = side;
        buffer = new float[16];
        bufferholes = new List<int>();

        switch (side)
        {
            case Side.Left:
                lightscale = .85f;
                break;
            case Side.Right:
                lightscale = .85f;
                break;
            case Side.Top:
                lightscale = 1f;
                break;
            case Side.Bottom:
                lightscale = 1f;
                break;
            case Side.Front:
                lightscale = .7f;
                break;
            case Side.Back:
                lightscale = .7f;
                break;
        }
    }

    public int getnextbufferindex()
    {
        if (bufferholes.Count > 0)
        {
            int toreturn = bufferholes[bufferholes.Count -1];
            bufferholes.RemoveAt(bufferholes.Count -1);
            return toreturn;
        }
        int t = bufferposition;
        bufferposition++;
        return t;
    }

    public void generatevertexbuffers(int size)
    {
        GLUtils.CreateVertexArraysQBF(sizeof(float) * 16 *size, out vertexarrayID, out vertexbufferID);
    }

    public void removebuffer(ref int voxelbufferindex)
    {
        if (voxelbufferindex == -1) return;

        Array.Clear(buffer, 0, buffer.Length);
        GL.BindBuffer(BufferTarget.ArrayBuffer, vertexbufferID);
        GL.BufferSubData(BufferTarget.ArrayBuffer, (IntPtr)(sizeof(float) * 16 * voxelbufferindex), (IntPtr)(sizeof(float) * 16), buffer);
        GL.BindBuffer(BufferTarget.ArrayBuffer, 0);
        int reff = voxelbufferindex;
        this.bufferholes.Add(reff);
        voxelbufferindex = -1;
    }

    public void fillbuffer(int bufferindex, int x, int y, int z, int color)
    {
        switch (side)
        {
            case Side.Front:

                buffer[0] = -cubesize + x;
                buffer[1] = -cubesize + y;
                buffer[2] = cubesize + z;

                buffer[4] = cubesize + x;
                buffer[5] = -cubesize + y;
                buffer[6] = cubesize + z;

                buffer[8] = cubesize + x;
                buffer[9] = cubesize + y;
                buffer[10] = cubesize + z;

                buffer[12] = -cubesize + x;
                buffer[13] = cubesize + y;
                buffer[14] = cubesize + z;

                break;
            case Side.Back:

                buffer[0] = -cubesize + x;
                buffer[1] = -cubesize + y;
                buffer[2] = -cubesize + z;

                buffer[4] = cubesize + x;
                buffer[5] = -cubesize + y;
                buffer[6] = -cubesize + z;

                buffer[8]  = cubesize + x;
                buffer[9]  = cubesize + y;
                buffer[10] = -cubesize + z;

                buffer[12] = -cubesize + x;
                buffer[13] = cubesize + y;
                buffer[14] = -cubesize + z;

                break;
            case Side.Top:

                buffer[0] = -cubesize + x;
                buffer[1] = cubesize + y;
                buffer[2] = cubesize + z;

                buffer[4] = cubesize + x;
                buffer[5] = cubesize + y;
                buffer[6] = cubesize + z;

                buffer[8]  = cubesize + x;
                buffer[9]  = cubesize + y;
                buffer[10] = -cubesize + z;

                buffer[12] = -cubesize + x;
                buffer[13] = cubesize + y;
                buffer[14] = -cubesize + z;

                break;
            case Side.Bottom:

                buffer[0] = -cubesize + x;
                buffer[1] = -cubesize + y;
                buffer[2] = cubesize + z;

                buffer[4] = cubesize + x;
                buffer[5] = -cubesize + y;
                buffer[6] = cubesize + z;

                buffer[8]  = cubesize + x;
                buffer[9]  = -cubesize + y;
                buffer[10] = -cubesize + z;

                buffer[12] = -cubesize + x;
                buffer[13] = -cubesize + y;
                buffer[14] = -cubesize + z;

                break;
            case Side.Right:

                buffer[0] = -cubesize + x;
                buffer[1] = -cubesize + y;
                buffer[2] = -cubesize + z;

                buffer[4] = -cubesize + x;
                buffer[5] = -cubesize + y;
                buffer[6] = cubesize + z;

                buffer[8]  = -cubesize + x;
                buffer[9]  = cubesize + y;
                buffer[10] = cubesize + z;

                buffer[12] = -cubesize + x;
                buffer[13] = cubesize + y;
                buffer[14] = -cubesize + z;

                break;
            case Side.Left:

                buffer[0] = cubesize + x;
                buffer[1] = -cubesize + y;
                buffer[2] = -cubesize + z;

                buffer[4] = cubesize + x;
                buffer[5] = -cubesize + y;
                buffer[6] = cubesize + z;

                buffer[8]  = cubesize + x;
                buffer[9]  = cubesize + y;
                buffer[10] = cubesize + z;

                buffer[12] = cubesize + x;
                buffer[13] = cubesize + y;
                buffer[14] = -cubesize + z;
                break;
        }

        buffer[3] = color;
        buffer[7] = color;
        buffer[11] = color;
        buffer[15] = color;

        GL.BindBuffer(BufferTarget.ArrayBuffer, vertexbufferID);
        GL.BufferSubData(BufferTarget.ArrayBuffer, (IntPtr)(sizeof(float) * 16 * bufferindex), (IntPtr)(sizeof(float) * 16), buffer);
        GL.BindBuffer(BufferTarget.ArrayBuffer, 0);
    }

    public void fillbuffer(ref int voxelbufferindex, int x, int y, int z, int color)
    {
        if (voxelbufferindex > -1) return;

        int bufferindex = getnextbufferindex();
        voxelbufferindex = bufferindex;

        fillbuffer(voxelbufferindex, x, y, z, color);

        #region // mapbuffer
        //IntPtr pointer = GL.MapBuffer(BufferTarget.ArrayBuffer, BufferAccess.WriteOnly);

        //if ((int)pointer > (int)IntPtr.Zero)
        //{
        //    unsafe
        //    {
        //        fixed (float* SystemMemory = &buffer[0])
        //        {
        //            float* VideoMemory = (float*)pointer;
        //            for (int i = 0; i < buffer.Length; i++)
        //                VideoMemory[i] = buffer[i+4*28*bufferindex];
        //        }
        //    }
        //}
        //GL.UnmapBuffer(BufferTarget.ElementArrayBuffer);
        //GL.BindBuffer(BufferTarget.ArrayBuffer, 0);
        #endregion
    }

    public void updatevoxel(Voxel voxel)
    {
        int bufferindex = -1;

        switch (side)
        {
            case Side.Front:
                bufferindex = voxel.front.bufferID;
                break;
            case Side.Back:
                bufferindex = voxel.back.bufferID;
                break;
            case Side.Top:
                bufferindex = voxel.top.bufferID;
                break;
            case Side.Bottom:
                bufferindex = voxel.bottom.bufferID;
                break;
            case Side.Right:
                bufferindex = voxel.right.bufferID;
                break;
            case Side.Left:
                bufferindex = voxel.left.bufferID;
                break;
        }

        fillbuffer(bufferindex, voxel.x, voxel.y, voxel.z, voxel.colorindex);
    }

    public void beginvoxelediting()
    {
        GL.BindBuffer(BufferTarget.ArrayBuffer, vertexbufferID);
    }

    public void endvoxelediting()
    {
        GL.BindBuffer(BufferTarget.ArrayBuffer, 0);
    }

    public void render(Shader shader)
    {
        shader.writeuniform("light", lightscale);
        GL.BindVertexArray(vertexarrayID);
        GL.DrawArrays(PrimitiveType.Quads, 0, bufferposition*4);
        GL.BindVertexArray(0);
    }

    public void Dispose()
    {
        GL.DeleteBuffer(vertexbufferID);
        GL.DeleteVertexArray(vertexarrayID);
    }
}
