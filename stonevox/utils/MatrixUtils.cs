using OpenTK;
using OpenTK.Graphics.OpenGL4;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public static class MatrixUtils
{
    public static Vector4 UnProject(ref Matrix4 projection, Matrix4 view, Vector2 viewport, Vector2 mouse, float depth)
    {
        Vector4 vec = new Vector4();

        vec.X = 2.0f * mouse.X / (float)viewport.X - 1;
        vec.Y = -(2.0f * mouse.Y / (float)viewport.Y - 1);
        vec.Z = 0;
        vec.W = 1.0f;

        Matrix4 viewInv = Matrix4.Invert(view);
        Matrix4 projInv = Matrix4.Invert(projection);

        Vector4.Transform(ref vec, ref projInv, out vec);
        Vector4.Transform(ref vec, ref viewInv, out vec);

        if (vec.W > float.Epsilon || vec.W < float.Epsilon)
        {
            vec.X /= vec.W;
            vec.Y /= vec.W;
            vec.Z /= vec.W;
        }

        return vec;
    }

    private static bool WithinEpsilon(float a, float b)
    {
        float num = a - b;
        return -1.401298E-45f <= num && num <= 1.401298E-45f;
    }

    public static Vector3 Unproject(Vector3 source, Matrix4 projection, Matrix4 view, Matrix4 world)
    {
        Matrix4 matrix = world * view;
        matrix = matrix * projection;
        matrix = Matrix4.Invert(matrix);
        source.X = (source.X) / (float)1280f * 2f - 1f;
        source.Y = -((source.Y) / (float)800f * 2f - 1f);
        source.Z = (source.Z) / (1f);
        Vector3 vector = Vector3.Transform(source, matrix);
        float num = source.X * matrix.M14 + source.Y * matrix.M24 + source.Z * matrix.M34 + matrix.M44;
        if (!WithinEpsilon(num, 1f))
        {
            return Vector3.Divide(vector, num);
        }
        return vector;
    }

    static public int UnProject(Vector3 win, Matrix4 modelMatrix, Matrix4 projMatrix, ref Vector3 obj)
    {
        int[] viewport = new int[4];
        GL.GetInteger(GetPName.Viewport, viewport);

        return UnProject(win, modelMatrix, projMatrix, viewport, ref obj);
    }

    static public int UnProject(Vector3 win, Matrix4 modelMatrix, Matrix4 projMatrix, int[] viewport, ref Vector3 obj)
    {
        return gluUnProject(win.X, win.Y, win.Z, modelMatrix, projMatrix, viewport, ref obj.X, ref obj.Y, ref obj.Z);
    }

    static int gluUnProject(float winx, float winy, float winz, Matrix4 modelMatrix, Matrix4 projMatrix, int[] viewport, ref float objx, ref float objy, ref float objz)
    {
        Matrix4 finalMatrix;
        Vector4 _in;
        Vector4 _out;

        finalMatrix = Matrix4.Mult(modelMatrix, projMatrix);

        //if (!__gluInvertMatrixd(finalMatrix, finalMatrix)) return(GL_FALSE);
        finalMatrix.Invert();

        _in.X = winx;
        _in.Y = winy;
        _in.Z = winz;
        _in.W = 1.0f;

        /* Map x and y from window coordinates */
        _in.X = (_in.X - viewport[0]) / viewport[2];
        _in.Y = (_in.Y - viewport[1]) / viewport[3];

        /* Map to range -1 to 1 */
        _in.X = _in.X * 2 - 1;
        _in.Y = _in.Y * 2 - 1;
        _in.Z = _in.Z * 2 - 1;

        //__gluMultMatrixVecd(finalMatrix, _in, _out);
        // check if this works:
        _out = Vector4.Transform(_in, finalMatrix);

        if (_out.W == 0.0)
            return (0);
        _out.X /= _out.W;
        _out.Y /= _out.W;
        _out.Z /= _out.W;
        objx = _out.X;
        objy = _out.Y;
        objz = _out.Z;
        return (1);
    }


}
