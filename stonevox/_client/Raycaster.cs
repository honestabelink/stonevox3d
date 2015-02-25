using OpenTK;
using OpenTK.Graphics.OpenGL4;
using stonevox;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

public static class Raycaster
{
    static Vector3 top = new Vector3(0,1,0);
    static Vector3 bottom =new Vector3(0,-1,0);
    static Vector3 left = new Vector3(-1, 0, 0);
    static Vector3 right = new Vector3(1, 0, 0);
    static Vector3 front = new Vector3(0,0,1);
    static Vector3 back = new Vector3(0,0,-1);

    static float cubesize = .5f;
    static float d = 0;

    static float clientwidth;
    static float clientheight;

    static Vector3 testout;

    static Vector3 near = new Vector3();
    static Vector3 far = new Vector3();

    static Camera camera;
    static ClientInput input;
    static GLWindow window;
    static Selection selection;
    public static Thread raycastthread;
    static bool closerequest;

    public static RaycastHit lasthit = new RaycastHit(0,0,0, 10000);

    //static Vector2 test;

    public static bool enabled = true;
    public static bool testdirt = false;

    public static ConcurrentStack<Vector2> testlocations = new ConcurrentStack<Vector2>();
    static Vector2 test;

    public static void init(GLWindow window, Camera camera, Selection selection, ClientInput input)
    {
        Raycaster.window = window;
        Raycaster.camera = camera;
        Raycaster.input = input;
        Raycaster.selection = selection;

        raycastthread = new Thread(run);
        raycastthread.Start();

        clientwidth = window.Width;
        clientheight = window.Height;
        window.Resize += window_Resize;
    }

    static void window_Resize(object sender, EventArgs e)
    {
        clientwidth = window.Width;
        clientheight = window.Height;
    }

    public static void Dispose()
    {
        closerequest = true;
    }

    static void run()
    {
        while(!closerequest)
        {
            if (!enabled || QbManager.models.Count == 0)
            {
                Thread.Sleep(100);
                continue;
            }

            //while (testlocations.Count > 0)
            //{
            //    if (testlocations.TryPop(out test))
            //    {
            //        unproject(test);
            //        RaycastHit hit = raycast(camera.position, QbManager.getactivematrix());

            //        if (!hit.matches(lasthit))
            //        {
            //            // horrible statics from another thread
            //            Console.WriteLine(hit.ToString());
            //            selection.dirty = true;
            //        }
            //        lasthit = hit;
            //    }
            //}
            //Thread.Sleep(6);

            unproject();
            RaycastHit hit = raycast(camera.position, QbManager.getactivematrix());
            if (hit.distance != 10000 && !hit.matches(lasthit))
            {
                selection.dirty = true;
                //Console.WriteLine(hit.ToString());
            }
            lasthit = hit;
            Thread.Sleep(8);
        }
    }

    public static void unproject()
    {
        UnProject(input.mousex, input.mousey, 0f, ref camera.view, ref camera.projection, out near);
        UnProject(input.mousex, input.mousey, 1f, ref camera.view, ref camera.projection, out far);
        Vector3.Subtract(ref far, ref near, out rayDirection);
    }

    public static void unproject(Vector2 loc)
    {
        UnProject(loc.X, loc.Y, 0f, ref camera.view, ref camera.projection, out near);
        UnProject(loc.X, loc.Y, 1f, ref camera.view, ref camera.projection, out far);
        Vector3.Subtract(ref far ,ref near, out rayDirection);
    }

    static Matrix4 a;
    static Vector4 _in = new Vector4();
    static Vector4 _out;
    public static void UnProject(float x, float y, float z, ref Matrix4 modelview, ref Matrix4 projection, out Vector3 value)
    {
        Matrix4.Mult(ref modelview , ref projection, out a);
        a.Invert();

        _in.X = (x) / clientwidth * 2f - 1f;
        _in.Y = (y) / clientheight * 2f - 1f;
        _in.Z = 2f * z - 1f;
        _in.W = 1;

        Vector4.Transform(ref _in, ref a, out _out);

        if (_out.W != 0f)
            _out.W = 1f / _out.W;

        value.X = _out.X * _out.W;
        value.Y = _out.Y * _out.W;
        value.Z = _out.Z * _out.W;
    }

    public static float distance(float x, float y, float z)
    {
        float num = rayOrigin.X - x;
        float num2 = rayOrigin.Y - y;
        float num3 = rayOrigin.Z - z;
        float num4 = num * num + num2 * num2 + num3 * num3;
        return (float)Math.Sqrt((double)num4);
    }

    private static bool RayIntersectsPlane(ref Vector3 normal, ref Vector3 rayVector)
    {
        double denom = Vector3.Dot(normal, rayVector);
        if (denom < .3d)
        {
            return true;
        }

        return false;
    }

    static bool _front;
    static bool _back;
    static bool _left;
    static bool _right;
    static bool _top;
    static bool _bottom;

    public static RaycastHit raycast(Vector3 camerapos, QbMatrix m)
    {
        RaycastHit hitpoint = new RaycastHit();
        hitpoint.distance = 10000;
        rayOrigin = camerapos;

        _front = RayIntersectsPlane(ref front, ref camera.direction);
        _back = RayIntersectsPlane(ref back, ref camera.direction);
        _top = RayIntersectsPlane(ref top, ref camera.direction);
        _bottom = RayIntersectsPlane(ref bottom, ref camera.direction);
        _right = RayIntersectsPlane(ref left, ref camera.direction);
        _left = RayIntersectsPlane(ref right, ref camera.direction);

        // raytest region... kinda think of like quad treeing
        //Vector3[] f1 = new Vector3[] { new Vector3(0, 0, 0), new Vector3(m.size.X/2f, m.size.Y /2f, m.size.Z/2f) };
        //Vector3[] f2 = new Vector3[] { new Vector3(0, 0, 0), new Vector3(0, 0, 0) };
        //Vector3[] f3 = new Vector3[] { new Vector3(0, 0, 0), new Vector3(0, 0, 0) };
        //Vector3[] f4 = new Vector3[] { new Vector3(0, 0, 0), new Vector3(0, 0, 0) };
        //Vector3[] f5 = new Vector3[] { new Vector3(0, 0, 0), new Vector3(0, 0, 0) };
        //Vector3[] f6 = new Vector3[] { new Vector3(0, 0, 0), new Vector3(0, 0, 0) };
        //Vector3[] f7 = new Vector3[] { new Vector3(0, 0, 0), new Vector3(0, 0, 0) };
        //Vector3[] f8 = new Vector3[] { new Vector3(0, 0, 0), new Vector3(0, 0, 0) };

        foreach (var v in m.voxels.Values)
        {
            if (testdirt)
            {
                if (v.dirty)
                {
                    ////front
                    if (_front && (rayTest(ref front, -cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                      cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                      cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                      out testout)||rayTest(ref front, -cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                          cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                          -cubesize + v.x, cubesize + v.y, cubesize + v.z, out testout)))
                    {
                            d = distance(v.x *.5f, v.y *.5f, v.z *.5f + .5f);
                            if (d < hitpoint.distance)
                            {
                                hitpoint.distance = d;
                                hitpoint.x = v.x;
                                hitpoint.y = v.y;
                                hitpoint.z = v.z;
                                hitpoint.side = Side.Front;
                            }
                    }

                    ////bavk
                    if (_back && (rayTest(ref back, -cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                     cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                     cubesize + v.x, cubesize + v.y, -cubesize + v.z,
                                     out testout)||rayTest(ref back, -cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                           cubesize + v.x, cubesize + v.y, -cubesize + v.z,
                                           -cubesize + v.x, cubesize + v.y, -cubesize + v.z, out testout)))
                    {
                            d = distance(v.x *.5f, v.y *.5f, v.z *.5f - .5f);
                            if (d < hitpoint.distance)
                            {
                                hitpoint.distance = d;
                                hitpoint.x = v.x;
                                hitpoint.y = v.y;
                                hitpoint.z = v.z;
                                hitpoint.side = Side.Back;
                            }
                    }


                    //top
                    if (_top && (rayTest(ref top, -cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                         cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                         cubesize + v.x, cubesize + v.y, -cubesize + v.z,
                                         out testout) ||rayTest(ref top, -cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                         cubesize + v.x, cubesize + v.y, -cubesize + v.z,
                                         -cubesize + v.x, cubesize + v.y, -cubesize + v.z, out testout)))
                    {
                        d = distance(v.x *.5f, v.y *.5f + .5f, v.z *.5f);
                        if (d < hitpoint.distance)
                        {
                            hitpoint.distance = d;
                            hitpoint.x = v.x;
                            hitpoint.y = v.y;
                            hitpoint.z = v.z;
                            hitpoint.side = Side.Top;
                        }
                    }

                    ////bottom
                    if (_bottom && (rayTest(ref bottom, -cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                         cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                         cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                         out testout)||rayTest(ref bottom, -cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                            cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                            -cubesize + v.x, -cubesize + v.y, -cubesize + v.z, out testout)))
                    {
                            d = distance(v.x *.5f, v.y *.5f - .5f, v.z *.5f);
                            if (d < hitpoint.distance)
                            {
                                hitpoint.distance = d;
                                hitpoint.x = v.x;
                                hitpoint.y = v.y;
                                hitpoint.z = v.z;
                                hitpoint.side = Side.Bottom;
                            }
                    }

                    ////left
                    if (_left && (rayTest(ref left, cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                         cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                         cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                         out testout)||rayTest(ref left, cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                          cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                          cubesize + v.x, cubesize + v.y, -cubesize + v.z, out testout)))
                    {
                            d = distance(v.x *.5f + .5f, v.y *.5f, v.z *.5f);
                            if (d < hitpoint.distance)
                            {
                                hitpoint.distance = d;
                                hitpoint.x = v.x;
                                hitpoint.y = v.y;
                                hitpoint.z = v.z;
                                hitpoint.side = Side.Left;
                            }
                    }

                    ////right
                    if (_right && (rayTest(ref right, -cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                         -cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                         -cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                         out testout) || rayTest(ref right, -cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                           -cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                           -cubesize + v.x, cubesize + v.y, -cubesize + v.z, out testout)))
                    {
                        d = distance(v.x *.5f - .5f, v.y *.5f, v.z *.5f);
                        if (d < hitpoint.distance)
                        {
                            hitpoint.distance = d;
                            hitpoint.x = v.x;
                            hitpoint.y = v.y;
                            hitpoint.z = v.z;
                            hitpoint.side = Side.Right;
                        }
                    }
                }
                else
                {
                    //front
                    if (_front && ((v.alphamask & 32) == 32))
                    {
                        if ((rayTest(ref front, -cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                     cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                     cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                     out testout) || rayTest(ref front, -cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                         cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                         -cubesize + v.x, cubesize + v.y, cubesize + v.z, out testout)))
                        {
                            d = distance(v.x * .5f, v.y * .5f, v.z * .5f + .5f);
                            if (d < hitpoint.distance)
                            {
                                hitpoint.distance = d;
                                hitpoint.x = v.x;
                                hitpoint.y = v.y;
                                hitpoint.z = v.z;
                                hitpoint.side = Side.Front;
                            }
                        }
                    }

                    //bavk
                    if (((v.alphamask & 64) == 64))
                    {
                        if (_back && (rayTest(ref back, -cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                    cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                    cubesize + v.x, cubesize + v.y, -cubesize + v.z,
                                    out testout) || rayTest(ref back, -cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                          cubesize + v.x, cubesize + v.y, -cubesize + v.z,
                                          -cubesize + v.x, cubesize + v.y, -cubesize + v.z, out testout)))
                        {
                            d = distance(v.x * .5f, v.y * .5f, v.z * .5f - .5f);
                            if (d < hitpoint.distance)
                            {
                                hitpoint.distance = d;
                                hitpoint.x = v.x;
                                hitpoint.y = v.y;
                                hitpoint.z = v.z;
                                hitpoint.side = Side.Back;
                            }
                        }
                    }

                    //top
                    if (((v.alphamask & 8) == 8))
                    {
                        if (_top && (rayTest(ref top, -cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                         cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                         cubesize + v.x, cubesize + v.y, -cubesize + v.z,
                                         out testout) || rayTest(ref top, -cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                         cubesize + v.x, cubesize + v.y, -cubesize + v.z,
                                         -cubesize + v.x, cubesize + v.y, -cubesize + v.z, out testout)))
                        {
                            d = distance(v.x * .5f, v.y * .5f + .5f, v.z * .5f);
                            if (d < hitpoint.distance)
                            {
                                hitpoint.distance = d;
                                hitpoint.x = v.x;
                                hitpoint.y = v.y;
                                hitpoint.z = v.z;
                                hitpoint.side = Side.Top;
                            }
                        }
                    }

                    //bottom
                    if (((v.alphamask & 16) == 16))
                    {
                        if (_bottom && (rayTest(ref bottom, -cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                        cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                        cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                        out testout) || rayTest(ref bottom, -cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                           cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                           -cubesize + v.x, -cubesize + v.y, -cubesize + v.z, out testout)))
                        {
                            d = distance(v.x * .5f, v.y * .5f - .5f, v.z * .5f);
                            if (d < hitpoint.distance)
                            {
                                hitpoint.distance = d;
                                hitpoint.x = v.x;
                                hitpoint.y = v.y;
                                hitpoint.z = v.z;
                                hitpoint.side = Side.Bottom;
                            }
                        }
                    }

                    //left
                    if (_left && ((v.alphamask & 2) == 2))
                    {
                        if (_left && (rayTest(ref left, cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                         cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                         cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                         out testout) || rayTest(ref left, cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                          cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                          cubesize + v.x, cubesize + v.y, -cubesize + v.z, out testout)))
                        {
                            d = distance(v.x * .5f + .5f, v.y * .5f, v.z * .5f);
                            if (d < hitpoint.distance)
                            {
                                hitpoint.distance = d;
                                hitpoint.x = v.x;
                                hitpoint.y = v.y;
                                hitpoint.z = v.z;
                                hitpoint.side = Side.Left;
                            }
                        }
                    }

                    //right
                    if (_right && ((v.alphamask & 4) == 4))
                    {
                        if ((rayTest(ref right, -cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                        -cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                        -cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                        out testout) || rayTest(ref right, -cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                          -cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                          -cubesize + v.x, cubesize + v.y, -cubesize + v.z, out testout)))
                        {
                            d = distance(v.x * .5f - .5f, v.y * .5f, v.z * .5f);
                            if (d < hitpoint.distance)
                            {
                                hitpoint.distance = d;
                                hitpoint.x = v.x;
                                hitpoint.y = v.y;
                                hitpoint.z = v.z;
                                hitpoint.side = Side.Right;
                            }
                        }
                    }
                }
            }
            else if (v.alphamask > 0)
            {
                if (v.dirty)
                    continue;
                //front
                if (_front && ((v.alphamask & 32) == 32 || m.isdirty(v.x, v.y, v.z + 1)))
                {
                    if ((rayTest(ref front, -cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                     cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                     cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                     out testout) || rayTest(ref front, -cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                         cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                         -cubesize + v.x, cubesize + v.y, cubesize + v.z, out testout)))
                    {
                        d = distance(v.x * .5f, v.y * .5f, v.z * .5f + .5f);
                        if (d < hitpoint.distance)
                        {
                            hitpoint.distance = d;
                            hitpoint.x = v.x;
                            hitpoint.y = v.y;
                            hitpoint.z = v.z;
                            hitpoint.side = Side.Front;
                        }
                    }
                }

                //bavk
                if (_back && ((v.alphamask & 64) == 64 || m.isdirty(v.x, v.y, v.z - 1)))
                {
                    if (_back && (rayTest(ref back, -cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                    cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                    cubesize + v.x, cubesize + v.y, -cubesize + v.z,
                                    out testout) || rayTest(ref back, -cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                          cubesize + v.x, cubesize + v.y, -cubesize + v.z,
                                          -cubesize + v.x, cubesize + v.y, -cubesize + v.z, out testout)))
                    {
                        d = distance(v.x * .5f, v.y * .5f, v.z * .5f - .5f);
                        if (d < hitpoint.distance)
                        {
                            hitpoint.distance = d;
                            hitpoint.x = v.x;
                            hitpoint.y = v.y;
                            hitpoint.z = v.z;
                            hitpoint.side = Side.Back;
                        }
                    }
                }


                //top
                if (_top && ((v.alphamask & 8) == 8) || m.isdirty(v.x, v.y + 1, v.z))
                {
                    if (_top && (rayTest(ref top, -cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                        cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                        cubesize + v.x, cubesize + v.y, -cubesize + v.z,
                                        out testout) || rayTest(ref top, -cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                        cubesize + v.x, cubesize + v.y, -cubesize + v.z,
                                        -cubesize + v.x, cubesize + v.y, -cubesize + v.z, out testout)))
                    {
                        d = distance(v.x * .5f, v.y * .5f + .5f, v.z * .5f);
                        if (d < hitpoint.distance)
                        {
                            hitpoint.distance = d;
                            hitpoint.x = v.x;
                            hitpoint.y = v.y;
                            hitpoint.z = v.z;
                            hitpoint.side = Side.Top;
                        }
                    }
                }

                //bottom
                if (_bottom && ((v.alphamask & 16) == 16) || m.isdirty(v.x, v.y - 1, v.z))
                {
                    if (_bottom && (rayTest(ref bottom, -cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                       cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                       cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                       out testout) || rayTest(ref bottom, -cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                          cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                          -cubesize + v.x, -cubesize + v.y, -cubesize + v.z, out testout)))
                    {
                        d = distance(v.x * .5f, v.y * .5f - .5f, v.z * .5f);
                        if (d < hitpoint.distance)
                        {
                            hitpoint.distance = d;
                            hitpoint.x = v.x;
                            hitpoint.y = v.y;
                            hitpoint.z = v.z;
                            hitpoint.side = Side.Bottom;
                        }
                    }
                }

                //left
                if (_left && ((v.alphamask & 2) == 2 || m.isdirty(v.x + 1, v.y, v.z)))
                {
                    if (_left && (rayTest(ref left, cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                  cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                  cubesize + v.x, cubesize + v.y, cubesize + v.z,
                  out testout) || rayTest(ref left, cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                   cubesize + v.x, cubesize + v.y, cubesize + v.z,
                   cubesize + v.x, cubesize + v.y, -cubesize + v.z, out testout)))
                    {
                        d = distance(v.x * .5f + .5f, v.y * .5f, v.z * .5f);
                        if (d < hitpoint.distance)
                        {
                            hitpoint.distance = d;
                            hitpoint.x = v.x;
                            hitpoint.y = v.y;
                            hitpoint.z = v.z;
                            hitpoint.side = Side.Left;
                        }
                    }
                }

                //right
                if (_right && ((v.alphamask & 4) == 4 || m.isdirty(v.x - 1, v.y, v.z)))
                {
                    if ((rayTest(ref right, -cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                        -cubesize + v.x, -cubesize + v.y, cubesize + v.z,
                                        -cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                        out testout) || rayTest(ref right, -cubesize + v.x, -cubesize + v.y, -cubesize + v.z,
                                          -cubesize + v.x, cubesize + v.y, cubesize + v.z,
                                          -cubesize + v.x, cubesize + v.y, -cubesize + v.z, out testout)))
                    {
                        d = distance(v.x * .5f - .5f, v.y * .5f, v.z * .5f);
                        if (d < hitpoint.distance)
                        {
                            hitpoint.distance = d;
                            hitpoint.x = v.x;
                            hitpoint.y = v.y;
                            hitpoint.z = v.z;
                            hitpoint.side = Side.Right;
                        }
                    }   
                }
            }
        }
            return hitpoint;
    }

    static float dot;
    static float t;
    static float coordRatio;
    static Vector3 intPoint = new Vector3();
    static float intX;
    static float intY;
    static float intZ;
    public static Vector3 rayOrigin;
    public static Vector3 rayDirection;

    static double accuraty = 0.0008d;
    static double fullArea;
    static double subTriangle1;
    static double subTriangle2;
    static double subTriangle3;
    static double totalSubAreas;

    public static bool rayTest(ref Vector3 planeNormal, float p1x, float p1y, float p1z, float p2x, float p2y,
        float p2z, float p3x, float p3y, float p3z, out Vector3 _out)
    {
        dot = rayDirection.X * planeNormal.X + rayDirection.Y * planeNormal.Y + rayDirection.Z * planeNormal.Z;
        t = 0;
        if (dot == 0)
        {
            _out = Vector3.Zero;
            return false;
        }
        coordRatio =
                p1x * planeNormal.X + p1y * planeNormal.Y + p1z * planeNormal.Z - planeNormal.X * rayOrigin.X
                        - planeNormal.Y * rayOrigin.Y - planeNormal.Z * rayOrigin.Z;
        t = coordRatio / dot;
        if (t < 0)
        {
            _out = Vector3.Zero;
            return false;
        }

        intPoint.X = rayOrigin.X + t * rayDirection.X;
        intPoint.Y = rayOrigin.Y + t * rayDirection.Y;
        intPoint.Z = rayOrigin.Z + t * rayDirection.Z;

        fullArea = calculateTriangleArea(p1x, p1y, p1z, p2x, p2y, p2z, p3x, p3y, p3z);
        subTriangle1 = calculateTriangleArea(p1x, p1y, p1z, p2x, p2y, p2z, intPoint.X, intPoint.Y, intPoint.Z);
        subTriangle2 = calculateTriangleArea(p2x, p2y, p2z, p3x, p3y, p3z, intPoint.X, intPoint.Y, intPoint.Z);
        subTriangle3 = calculateTriangleArea(p1x, p1y, p1z, p3x, p3y, p3z, intPoint.X, intPoint.Y, intPoint.Z);

        totalSubAreas = subTriangle1 + subTriangle2 + subTriangle3;

        if (Math.Abs(fullArea - totalSubAreas) < accuraty)
        {
            _out = intPoint;
            return true;
        }
        else
        {
            _out = Vector3.Zero;
            return false;
        }
    }

    private static double calculateTriangleArea(float p1x, float p1y, float p1z, float p2x, float p2y, float p2z,
        float p3x, float p3y, float p3z)
    {
        double a = Math.Sqrt((p2x - p1x) * (p2x - p1x) + (p2y - p1y) * (p2y - p1y) + (p2z - p1z) * (p2z - p1z));
        double b = Math.Sqrt((p3x - p2x) * (p3x - p2x) + (p3y - p2y) * (p3y - p2y) + (p3z - p2z) * (p3z - p2z));
        double c = Math.Sqrt((p3x - p1x) * (p3x - p1x) + (p3y - p1y) * (p3y - p1y) + (p3z - p1z) * (p3z - p1z));
        double s = (a + b + c) / 2d;
        return Math.Sqrt(s * (s - a) * (s - b) * (s - c));
    }

}
public class RaycastHit
{
    public float distance;
    public int x;
    public int y;
    public int z;
    public Side side;

    public RaycastHit()
    {
    }

    public RaycastHit(int x, int y, int z, float distance)
    {

    }

    public bool matches(RaycastHit other)
    {
        return this.x == other.x && this.y == other.y && this.z == other.z && this.side == other.side;
    }

    public override string ToString()
    {
        return string.Format("{0} : {1} : {2} , SIDE : {3}", x, y, z, side.ToString());
    }
}
