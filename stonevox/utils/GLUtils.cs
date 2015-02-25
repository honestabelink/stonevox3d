using OpenTK.Graphics.OpenGL4;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public class GLUtils
{
    public static int CreateIndexBuffer(int size)
    {
        ushort[] data = new ushort[size*6];

        for(ushort i =0;i < size; i++)
        {
            data[i*6] =   (ushort)(i*4   );
            data[i*6+1] = (ushort)(i*4 +1);
            data[i*6+2] = (ushort)(i*4 +2);
            data[i*6+3] = (ushort)(i*4   );
            data[i*6+4] = (ushort)(i*4 +2);
            data[i*6+5] = (ushort)(i*4 +3);
        }

        int id = GL.GenBuffer();
        GL.BindBuffer(BufferTarget.ElementArrayBuffer, id);
        GL.BufferData(BufferTarget.ElementArrayBuffer, (IntPtr)(sizeof(ushort)*data.Length), data, BufferUsageHint.DynamicDraw);
        GL.BindBuffer(BufferTarget.ElementArrayBuffer, 0);

        return id;
    }


    public static void CreateVertexArraysQBF(int size, out int vertexArrayID, out int vertexBufferID)
    {
        Shader s = ShaderUtil.getShader("qb");

        vertexBufferID = GL.GenBuffer();
        GL.BindBuffer(BufferTarget.ArrayBuffer, vertexBufferID);
        GL.BufferData(BufferTarget.ArrayBuffer, (IntPtr)(size), IntPtr.Zero, BufferUsageHint.DynamicDraw);
        GL.BindBuffer(BufferTarget.ArrayBuffer, 0);

        vertexArrayID = GL.GenVertexArray();
        GL.BindVertexArray(vertexArrayID);
        GL.BindBuffer(BufferTarget.ArrayBuffer, vertexBufferID);

        GL.EnableVertexAttribArray(0);
        GL.VertexAttribPointer(s.getartributelocation("position"), 3, VertexAttribPointerType.Float, false, sizeof(float) * 4, 0);

        GL.EnableVertexAttribArray(1);
        GL.VertexAttribPointer(s.getartributelocation("color"), 3, VertexAttribPointerType.Float, false, sizeof(float) * 4, sizeof(float) * 3);

        //GL.EnableVertexAttribArray(2);
        //GL.VertexAttribPointer(s.getartributelocation("light"), 1, VertexAttribPointerType.Float, false, sizeof(float) * 7, sizeof(float) * 6);

        //GL.BindBuffer(BufferTarget.ElementArrayBuffer, indexBuffer);
        //GL.BindVertexArray(0);
    }
}
