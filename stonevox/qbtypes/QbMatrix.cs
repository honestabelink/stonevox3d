using OpenTK;
using OpenTK.Graphics;
using OpenTK.Graphics.OpenGL4;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public class QbMatrix : IDisposable
{
    public string name;
    public Vector3 position;
    public Vector3 centerposition;
    public Vector3 size;
    public Colort highlight;

    public Colort[] colors;
    public ConcurrentDictionary<double, Voxel> voxels;
    public ConcurrentStack<VoxelModifier> modifiedvoxels;

    private QbMatrixSide left;
    private QbMatrixSide right;
    private QbMatrixSide top;
    private QbMatrixSide bottom;
    private QbMatrixSide front;
    private QbMatrixSide back;

    private Voxel voxel;
    private VoxelModifier modified;

    public QbMatrix()
    {
        highlight = new Colort(1f, 1f, 1f);
        colors = new Colort[0];
        voxels = new ConcurrentDictionary<double, Voxel>();
        modifiedvoxels = new ConcurrentStack<VoxelModifier>();

        left = new QbMatrixSide(Side.Left);
        right = new QbMatrixSide(Side.Right);
        top = new QbMatrixSide(Side.Top);
        bottom = new QbMatrixSide(Side.Bottom);
        front = new QbMatrixSide(Side.Front);
        back = new QbMatrixSide(Side.Back);
    }

    public void setsize(int x, int y, int z)
    {
        size = new Vector3(x, y, z);
        centerposition = new Vector3(x * .5f - .5f, y * .5f - .5f, z * .5f - .5f);
    }

    public int getcolorindex(float r, float g, float b)
    {
        Colort c;
        for (int i = 0; i < colors.Length; i++)
        {
            c = colors[i];
            if (c.R == r && c.G == g && c.B == b )
                return i;
        }
        var cc = colors.ToList();
        cc.Add(new Colort(r , g , b));
        colors = cc.ToArray();

        return colors.Length - 1;
    }

    public int getcolorindex(byte r, byte g, byte b)
    {
        return getcolorindex(r / 256f, g / 256f, b / 256f);
    }


    public void generatevertexbuffers()
    {
        left.generatevertexbuffers((int)(size.X * size.Y * size.Z));
        right.generatevertexbuffers((int)(size.X * size.Y * size.Z));
        top.generatevertexbuffers((int)(size.X * size.Y * size.Z));
        bottom.generatevertexbuffers((int)(size.X * size.Y * size.Z));
        front.generatevertexbuffers((int)(size.X * size.Y * size.Z));
        back.generatevertexbuffers((int)(size.X * size.Y * size.Z));
    }

    public void fillvertexbuffers()
    {
        foreach (var c in voxels.Values)
        {
            if (c.alphamask > 1)
            {
                //front
                if ((c.alphamask & 32) == 32)
                {
                    front.fillbuffer(ref c.front.bufferID, c.x, c.y, c.z, c.colorindex);
                }

                //back
                if ((c.alphamask & 64) == 64)
                {
                    back.fillbuffer(ref c.back.bufferID, c.x, c.y, c.z, c.colorindex);
                }

                //top
                if ((c.alphamask & 8) == 8)
                {
                    top.fillbuffer(ref c.top.bufferID, c.x, c.y, c.z, c.colorindex);
                }

                //bottom
                if ((c.alphamask & 16) == 16)
                {
                    bottom.fillbuffer(ref c.bottom.bufferID, c.x, c.y, c.z, c.colorindex);
                }

                //left
                if ((c.alphamask & 2) == 2)
                {
                    left.fillbuffer(ref c.left.bufferID, c.x, c.y, c.z, c.colorindex);
                }

                //right
                if ((c.alphamask & 4) == 4)
                {
                    right.fillbuffer(ref c.right.bufferID, c.x, c.y, c.z, c.colorindex);
                }
            }
        }
    }

    public void render(Shader shader)
    {
        while (modifiedvoxels.Count > 0)
        {
            if (modifiedvoxels.TryPop(out modified))
            {
                switch (modified.action)
                {
                    case VoxleModifierAction.NONE:
                        break;
                    case VoxleModifierAction.ADD:
                        break;
                    case VoxleModifierAction.REMOVE:
                        break;
                    case VoxleModifierAction.RECOLOR:
                        break;
                }
            }
        }

        shader.writeuniform("highlight", new Vector3(highlight.R, highlight.G, highlight.B));

        if (colors.Length > 0)
        {
            unsafe
            {
                fixed (float* pointer = &colors[0].R)
                {
                    ShaderUtil.getShader("qb").writeuniformarray("colors", colors.Length, pointer);
                }
            }
        }

        if (RayIntersectsPlane(ref front.normal, ref Client.window.camera.direction))
        {
            front.render(shader);
        }
        if (RayIntersectsPlane(ref back.normal, ref Client.window.camera.direction))
        {
            back.render(shader);
        }
        if (!Client.window.input.keydown(OpenTK.Input.Key.Up) && RayIntersectsPlane(ref top.normal, ref Client.window.camera.direction))
        {
            top.render(shader);
        }
        if (!Client.window.input.keydown(OpenTK.Input.Key.Down) && RayIntersectsPlane(ref bottom.normal, ref Client.window.camera.direction))
        {
            bottom.render(shader);
        }
        if (!Client.window.input.keydown(OpenTK.Input.Key.Left) && RayIntersectsPlane(ref right.normal, ref Client.window.camera.direction))
        {
            left.render(shader);
        }
        if (!Client.window.input.keydown(OpenTK.Input.Key.Right) && RayIntersectsPlane(ref left.normal, ref Client.window.camera.direction))
        {
            right.render(shader);
        }
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

    public void Dispose()
    {
    }

    // VOXEL MODIFIING
    //
    #region

    public void clean()
    {
        foreach (var v in voxels.Values)
        {
            if (v.dirty)
                v.dirty = false;
        }
    }

    public double gethash(int x, int y, int z)
    {
        return ((double)x * 23.457154879791d + (double)y) * 31.154879416546d + (double)z;
    }

    public bool isdirty(int x, int y, int z)
    {
        Voxel voxel;
        if (voxels.TryGetValue(gethash(x,y,z), out voxel))
        {
            return voxel.dirty;
        }
        else
            return false;
    }

    public void updatevoxel()
    {
        //front
        if ((voxel.alphamask & 32) == 32)
        {
            front.fillbuffer(ref voxel.front.bufferID, voxel.x, voxel.y, voxel.z, voxel.colorindex);
        }
        else
            front.removebuffer(ref voxel.front.bufferID);

        //back
        if ((voxel.alphamask & 64) == 64)
        {
            back.fillbuffer(ref voxel.back.bufferID, voxel.x, voxel.y, voxel.z, voxel.colorindex);
        }
        else
            back.removebuffer(ref voxel.back.bufferID);

        //top
        if ((voxel.alphamask & 8) == 8)
        {
            top.fillbuffer(ref voxel.top.bufferID, voxel.x, voxel.y, voxel.z, voxel.colorindex);
        }
        else
            top.removebuffer(ref voxel.top.bufferID);

        //bottom
        if ((voxel.alphamask & 16) == 16)
        {
            bottom.fillbuffer(ref voxel.bottom.bufferID, voxel.x, voxel.y, voxel.z, voxel.colorindex);
        }
        else
            bottom.removebuffer(ref voxel.bottom.bufferID);

        //left
        if ((voxel.alphamask & 2) == 2)
        {
            left.fillbuffer(ref voxel.left.bufferID, voxel.x, voxel.y, voxel.z, voxel.colorindex);
        }
        else
            left.removebuffer(ref voxel.left.bufferID);

        //right
        if ((voxel.alphamask & 4) == 4)
        {
            right.fillbuffer(ref voxel.right.bufferID, voxel.x, voxel.y, voxel.z, voxel.colorindex);
        }
        else
            right.removebuffer(ref voxel.right.bufferID);
    }

    public byte getAlphaMask(int x, int y, int z)
    {
        byte alpha = 1;

        if (!voxels.ContainsKey(gethash(x, y, z+1)))
            alpha += 32;

        if (!voxels.ContainsKey(gethash(x, y, z-1)))
            alpha += 64;

        if (!voxels.ContainsKey(gethash(x, y+1, z)))
            alpha += 8;

        if (!voxels.ContainsKey(gethash(x, y-1, z)))
            alpha += 16;

        if (!voxels.ContainsKey(gethash(x-1, y, z)))
            alpha += 4;

        if (!voxels.ContainsKey(gethash(x+1, y, z)))
            alpha += 2;

        return alpha > 1 ? alpha : (byte)0;
    }

    public void remove(int x, int y, int z)
    {
        if (voxels.TryGetValue(gethash(x,y,z), out voxel))
        {
            if (voxel.dirty) return;
            voxel.alphamask = 0;
            voxel.dirty = true;
            updatevoxel();

            // on clean code check for 0 alphas and remove
            //voxels.TryRemove(gethash(x, y, z), out voxel);

            if (voxels.TryGetValue(gethash(x, y, z + 1), out voxel))
            {
                if (voxel.alphamask > 0 && (voxel.alphamask & 64) != 64)
                {
                    voxel.alphamask += 64;
                    updatevoxel();
                }
            }


            if (voxels.TryGetValue(gethash(x, y, z - 1), out voxel))
            {
                if (voxel.alphamask > 0 && (voxel.alphamask & 32) != 32)
                {
                    voxel.alphamask += 32;
                    updatevoxel();
                }
            }

            if (voxels.TryGetValue(gethash(x, y + 1, z), out voxel))
            {
                if (voxel.alphamask > 0 && (voxel.alphamask & 16) != 16)
                {
                    voxel.alphamask += 16;
                    updatevoxel();
                }
            }

            if (voxels.TryGetValue(gethash(x, y - 1, z), out voxel))
            {
                if (voxel.alphamask > 0 && (voxel.alphamask & 8) != 8)
                {
                    voxel.alphamask += 8;
                    updatevoxel();
                }
            }

            if (voxels.TryGetValue(gethash(x + 1, y, z), out voxel))
            {
                if (voxel.alphamask > 0 && (voxel.alphamask & 4) != 4)
                {
                    voxel.alphamask += 4;
                    updatevoxel();
                }
            }

            if (voxels.TryGetValue(gethash(x - 1, y, z), out voxel))
            {
                if (voxel.alphamask > 0 && (voxel.alphamask & 2) != 2)
                {
                    voxel.alphamask += 2;
                    updatevoxel();
                }
            }
        }
    }

    public void add(int x, int y, int z, Colort color)
    {
        voxel = new Voxel(x, y, z, getAlphaMask(x, y, z), getcolorindex(color.R, color.G, color.B));
        if (voxels.TryAdd(gethash(x, y, z), voxel))
        {
            voxel.dirty = true;
            updatevoxel();

            if (voxels.TryGetValue(gethash(x, y, z + 1), out voxel))
            {
                if ((voxel.alphamask & 64) == 64)
                {
                    voxel.alphamask -= 64;
                    updatevoxel();
                }
            }

            if (voxels.TryGetValue(gethash(x, y, z - 1), out voxel))
            {
                if ((voxel.alphamask & 32) == 32)
                {
                    voxel.alphamask -= 32;
                    updatevoxel();
                }
            }

            if (voxels.TryGetValue(gethash(x, y + 1, z), out voxel))
            {
                if ((voxel.alphamask & 16) == 16)
                {
                    voxel.alphamask -= 16;
                    updatevoxel();
                }
            }

            if (voxels.TryGetValue(gethash(x, y - 1, z), out voxel))
            {
                if ((voxel.alphamask & 8) == 8)
                {
                    voxel.alphamask -= 8;
                    updatevoxel();
                }
            }

            if (voxels.TryGetValue(gethash(x + 1, y, z), out voxel))
            {
                if ((voxel.alphamask & 4) == 4)
                {
                    voxel.alphamask -= 4;
                    updatevoxel();
                }
            }

            if (voxels.TryGetValue(gethash(x - 1, y, z), out voxel))
            {
                if ((voxel.alphamask & 2) == 2)
                {
                    voxel.alphamask -= 2;
                    updatevoxel();
                }
            }
        }
    }

    public void recolor(Vector3 location, Colort color)
    {
        recolor((int)location.X, (int)location.Y, (int)location.Z, color);
    }

    public void recolor(int x, int y, int z, Colort color)
    {
        if (voxels.TryGetValue(gethash(x, y, z), out voxel) && !voxel.dirty)
        {
            voxel.dirty = true;
            if (voxel.alphamask > 1)
            {
                voxel.colorindex = getcolorindex(color.R, color.G, color.B);
                _recolor(x, y, z);
            }
        }
    }

    private void _recolor(int x, int y, int z)
    {
        //front
        if ((voxel.alphamask & 32) == 32)
        {
            front.updatevoxel(voxel);
        }

        //bavoxelk
        if ((voxel.alphamask & 64) == 64)
        {
            back.updatevoxel(voxel);
        }

        //top
        if ((voxel.alphamask & 8) == 8)
        {
            top.updatevoxel(voxel);
        }

        //bottom
        if ((voxel.alphamask & 16) == 16)
        {
            bottom.updatevoxel(voxel);
        }

        //left
        if ((voxel.alphamask & 2) == 2)
        {
            left.updatevoxel(voxel);
        }

        //right
        if ((voxel.alphamask & 4) == 4)
        {
            right.updatevoxel(voxel);
        }
    }

    #endregion
}
