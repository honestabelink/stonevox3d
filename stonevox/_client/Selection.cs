using OpenTK;
using OpenTK.Graphics.OpenGL4;
using OpenTK.Input;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public class Selection
{
    int indexbuffer;
    int vertexbuffer;
    int vertexarray;

    public bool dirty;

    float cubesize = .5f;
    float[] buffer;

    private ClientTools tools;
    private ClientInput input;

    private bool handledselectionchange = true;
    private bool needscleaning;

    private RaycastHit lasthit;

    private Colort color;

    public Selection(ClientTools tools, ClientInput input)
    {
        this.tools = tools;
        this.input = input;

        color = new Colort(1, 0, 0);
        GLUtils.CreateVertexArraysQBF(sizeof(float)*16, out vertexarray, out vertexbuffer);

        buffer = new float[16];
    }

    public void update()
    {
        if (dirty)
        {
            dirty = false;
            lasthit = Raycaster.lasthit;
            switch (lasthit.side)
            {
                case Side.Front:

                    buffer[0] = -cubesize + lasthit.x;
                    buffer[1] = -cubesize + lasthit.y;
                    buffer[2] = cubesize + lasthit.z;

                    buffer[4] = cubesize + lasthit.x;
                    buffer[5] = -cubesize + lasthit.y;
                    buffer[6] = cubesize + lasthit.z;

                    buffer[8] = cubesize + lasthit.x;
                    buffer[9] = cubesize + lasthit.y;
                    buffer[10] = cubesize + lasthit.z;

                    buffer[12] = -cubesize + lasthit.x;
                    buffer[13] = cubesize + lasthit.y;
                    buffer[14] = cubesize + lasthit.z;

                    break;
                case Side.Back:

                    buffer[0] = -cubesize + lasthit.x;
                    buffer[1] = -cubesize + lasthit.y;
                    buffer[2] = -cubesize + lasthit.z;

                    buffer[4] = cubesize + lasthit.x;
                    buffer[5] = -cubesize + lasthit.y;
                    buffer[6] = -cubesize + lasthit.z;

                    buffer[8] = cubesize + lasthit.x;
                    buffer[9] = cubesize + lasthit.y;
                    buffer[10] = -cubesize + lasthit.z;

                    buffer[12] = -cubesize + lasthit.x;
                    buffer[13] = cubesize + lasthit.y;
                    buffer[14] = -cubesize + lasthit.z;

                    break;
                case Side.Top:

                    buffer[0] = -cubesize + lasthit.x;
                    buffer[1] = cubesize + lasthit.y;
                    buffer[2] = cubesize + lasthit.z;

                    buffer[4] = cubesize + lasthit.x;
                    buffer[5] = cubesize + lasthit.y;
                    buffer[6] = cubesize + lasthit.z;

                    buffer[8] = cubesize + lasthit.x;
                    buffer[9] = cubesize + lasthit.y;
                    buffer[10] = -cubesize + lasthit.z;

                    buffer[12] = -cubesize + lasthit.x;
                    buffer[13] = cubesize + lasthit.y;
                    buffer[14] = -cubesize + lasthit.z;

                    break;
                case Side.Bottom:

                    buffer[0] = -cubesize + lasthit.x;
                    buffer[1] = -cubesize + lasthit.y;
                    buffer[2] = cubesize + lasthit.z;

                    buffer[4] = cubesize + lasthit.x;
                    buffer[5] = -cubesize + lasthit.y;
                    buffer[6] = cubesize + lasthit.z;

                    buffer[8] = cubesize + lasthit.x;
                    buffer[9] = -cubesize + lasthit.y;
                    buffer[10] = -cubesize + lasthit.z;

                    buffer[12] = -cubesize + lasthit.x;
                    buffer[13] = -cubesize + lasthit.y;
                    buffer[14] = -cubesize + lasthit.z;

                    break;
                case Side.Right:

                    buffer[0] = -cubesize + lasthit.x;
                    buffer[1] = -cubesize + lasthit.y;
                    buffer[2] = -cubesize + lasthit.z;

                    buffer[4] = -cubesize + lasthit.x;
                    buffer[5] = -cubesize + lasthit.y;
                    buffer[6] = cubesize + lasthit.z;

                    buffer[8] = -cubesize + lasthit.x;
                    buffer[9] = cubesize + lasthit.y;
                    buffer[10] = cubesize + lasthit.z;

                    buffer[12] = -cubesize + lasthit.x;
                    buffer[13] = cubesize + lasthit.y;
                    buffer[14] = -cubesize + lasthit.z;

                    break;
                case Side.Left:

                    buffer[0] = cubesize + lasthit.x;
                    buffer[1] = -cubesize + lasthit.y;
                    buffer[2] = -cubesize + lasthit.z;

                    buffer[4] = cubesize + lasthit.x;
                    buffer[5] = -cubesize + lasthit.y;
                    buffer[6] = cubesize + lasthit.z;

                    buffer[8] = cubesize + lasthit.x;
                    buffer[9] = cubesize + lasthit.y;
                    buffer[10] = cubesize + lasthit.z;

                    buffer[12] = cubesize + lasthit.x;
                    buffer[13] = cubesize + lasthit.y;
                    buffer[14] = -cubesize + lasthit.z;
                    break;
            }

            buffer[3] = 0;
            buffer[7] = 0;
            buffer[11] = 0;
            buffer[15] = 0;

            GL.BindBuffer(BufferTarget.ArrayBuffer, vertexbuffer);
            GL.BufferSubData(BufferTarget.ArrayBuffer, IntPtr.Zero, (IntPtr)(sizeof(float) * 16), buffer);
            GL.BindBuffer(BufferTarget.ArrayBuffer, 0);

            handledselectionchange = tools.onselectionchanged(input, QbManager.getactivematrix(), lasthit);

            if (handledselectionchange)
                needscleaning = true;
        }

        if (!handledselectionchange)
        {
            handledselectionchange = tools.onselectionchanged(input, QbManager.getactivematrix(), lasthit);
            if (handledselectionchange)
                needscleaning = true;
        }
        else if (needscleaning)
        {
            if (input.mouseup(MouseButton.Left))
            {
                // change this...  have the tool decide to clean or not
                needscleaning = false;
                QbManager.getactivematrix().clean();
            }
        }
    }

    public void render(Shader shader)
    {
        if (Raycaster.lasthit.distance != 10000)
        {
            shader.writeuniform("highlight", new Vector3(1, 1, 1));

            unsafe
            {
                fixed (float* pointer = &color.R)
                {
                    shader.writeuniformarray("colors", 1, pointer);
                }
            }

            GL.BindVertexArray(vertexarray);
            GL.DrawArrays(PrimitiveType.Quads, 0, 4);
            GL.BindVertexArray(0);
        }
    }
}