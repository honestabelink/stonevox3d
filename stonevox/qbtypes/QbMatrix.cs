using OpenTK;
using OpenTK.Graphics;
using OpenTK.Graphics.OpenGL4;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public class QbMatrix : IDisposable
    {
        public string name;
        public Vector3 position;
        public Vector3 centerposition;
        public Vector3 size;
        public Colort highlight;

        public Colort[] colors;
        public Colort[] matrixcolors;
        //public Colort[] wireframecolors;
        //public Colort[] outlinecolors;
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

        private int colorIndex = 0;

        public QbMatrix()
        {
            highlight = new Colort(1f, 1f, 1f);
            matrixcolors = new Colort[64];
            colors = matrixcolors;
            //wireframecolors = new Colort[64];
            //outlinecolors = new Colort[64];
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

        public int GetColorIndex(float r, float g, float b)
        {
            Colort c;
            for (int i = 0; i < colors.Length; i++)
            {
                c = colors[i];
                if (c.R == r && c.G == g && c.B == b)
                    return (int)i;
            }
            matrixcolors[colorIndex] = new Colort(r, g, b);

            // wireframes and outline....
            // right now my hsv to rbg is not working correctly and fails generating a few colors
            // later on this will be used over doing the conversion on a shader

            //Color4 color = new Color4(r, g, b, 1);

            //double hue, sat, vi;
            //ColorConversion.ColorToHSV(color.ToSystemDrawingColor(), out hue, out sat, out vi);

            //var outline = ColorConversion.ColorFromHSV(hue, (sat + .5d).Clamp(0, 1), vi );
            //outlinecolors[colorIndex] = outline.ToColor4();

            //var wireframe = ColorConversion.ColorFromHSV(hue, (sat + .1d).Clamp(0,1), (vi+.1d).Clamp(0,1));
            //wireframecolors[colorIndex] = wireframe.ToColor4();

            colorIndex++;

            return colorIndex - 1;
        }

        public int GetColorIndex(float r, float g, float b, uint colorFormat)
        {
            if (colorFormat == 1)
            {
                float tmp = r;
                r = b;
                b = tmp;
            }

            Colort c;
            for (int i = 0; i < colors.Length; i++)
            {
                c = colors[i];
                if (c.R == r && c.G == g && c.B == b)
                    return (int)i;
            }
            matrixcolors[colorIndex] = new Colort(r, g, b);

            // wireframes and outline....
            // right now my hsv to rbg is not working correctly and fails generating a few colors
            // later on this will be used over doing the conversion on a shader

            //Color4 color = new Color4(r, g, b, 1);

            //double hue, sat, vi;
            //ColorConversion.ColorToHSV(color.ToSystemDrawingColor(), out hue, out sat, out vi);

            //var outline = ColorConversion.ColorFromHSV(hue, (sat + .5d).Clamp(0, 1), vi );
            //outlinecolors[colorIndex] = outline.ToColor4();

            //var wireframe = ColorConversion.ColorFromHSV(hue, (sat + .1d).Clamp(0,1), (vi+.1d).Clamp(0,1));
            //wireframecolors[colorIndex] = wireframe.ToColor4();

            colorIndex++;

            return colorIndex-1;
        }

        public int getcolorindex(byte r, byte g, byte b)
        {
            return GetColorIndex(r / 256f, g / 256f, b / 256f);
        }

        public int getcolorindex(byte r, byte g, byte b, uint colorFormat)
        {
            return GetColorIndex(r / 256f, g / 256f, b / 256f, colorFormat);
        }

        public bool GetColorIndex_Alphamask(int x, int y, int z, out int colorindex, out byte alphamask)
        {
            if (voxels.TryGetValue(GetHash(x, y, z), out voxel))
            {
                colorindex = voxel.colorindex;
                alphamask = voxel.alphamask;
                return true;
            }
            colorindex = -1;
            alphamask = 0;
            return false;
        }

        public void GenerateVertexBuffers()
        {
            left.GenerateVertexBuffers();
            right.GenerateVertexBuffers();
            front.GenerateVertexBuffers();
            back.GenerateVertexBuffers();
            top.GenerateVertexBuffers();
            bottom.GenerateVertexBuffers();
        }

        public void FillVertexBuffers()
        {
            foreach (var c in voxels.Values)
            {
                if (c.alphamask > 1)
                {
                    //front
                    if ((c.alphamask & 32) == 32)
                    {
                        front.fillbuffer(ref c.front, c.x, c.y, c.z, c.colorindex);
                    }

                    //back
                    if ((c.alphamask & 64) == 64)
                    {
                        back.fillbuffer(ref c.back, c.x, c.y, c.z, c.colorindex);
                    }

                    //top
                    if ((c.alphamask & 8) == 8)
                    {
                        top.fillbuffer(ref c.top, c.x, c.y, c.z, c.colorindex);
                    }

                    //bottom
                    if ((c.alphamask & 16) == 16)
                    {
                        bottom.fillbuffer(ref c.bottom, c.x, c.y, c.z, c.colorindex);
                    }

                    //left
                    if ((c.alphamask & 2) == 2)
                    {
                        left.fillbuffer(ref c.left, c.x, c.y, c.z, c.colorindex);
                    }

                    //right
                    if ((c.alphamask & 4) == 4)
                    {
                        right.fillbuffer(ref c.right, c.x, c.y, c.z, c.colorindex);
                    }
                }
            }
        }

        public void Render(Shader shader)
        {
            //foreach(var c in voxels.Values)
            //{
            ////    Debug.Print(c.front.ToString());
            ////    Debug.Print(c.back.ToString());
            ////    Debug.Print(c.left.ToString());
            ////    Debug.Print(c.right.ToString());
            //    Debug.Print(c.top.ToString());
            //    //Debug.Print(c.bottom.ToString());
            //}

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

            shader.WriteUniform("highlight", new Vector3(highlight.R, highlight.G, highlight.B));

            if (colors.Length > 0)
            {
                unsafe
                {
                    fixed (float* pointer = &colors[0].R)
                    {
                        ShaderUtil.GetShader("qb").WriteUniformArray("colors", colors.Length, pointer);
                    }
                }
            }

            if (RayIntersectsPlane(ref front.normal, ref Singleton<Camera>.INSTANCE.direction))
            {
                front.Render(shader);
            }
            if (RayIntersectsPlane(ref back.normal, ref Singleton<Camera>.INSTANCE.direction))
            {
                back.Render(shader);
            }
            if (RayIntersectsPlane(ref top.normal, ref Singleton<Camera>.INSTANCE.direction))
            {
                top.Render(shader);
            }
            if (RayIntersectsPlane(ref bottom.normal, ref Singleton<Camera>.INSTANCE.direction))
            {
                bottom.Render(shader);
            }
            if (RayIntersectsPlane(ref left.normal, ref Singleton<Camera>.INSTANCE.direction))
            {
                left.Render(shader);
            }
            if (RayIntersectsPlane(ref right.normal, ref Singleton<Camera>.INSTANCE.direction))
            {
                right.Render(shader);
            }
        }

        public void Render()
        {
            if (RayIntersectsPlane(ref front.normal, ref Singleton<Camera>.INSTANCE.direction))
            {
                front.Render();
            }
            if (RayIntersectsPlane(ref back.normal, ref Singleton<Camera>.INSTANCE.direction))
            {
                back.Render();
            }
            if (RayIntersectsPlane(ref top.normal, ref Singleton<Camera>.INSTANCE.direction))
            {
                top.Render();
            }
            if (RayIntersectsPlane(ref bottom.normal, ref Singleton<Camera>.INSTANCE.direction))
            {
                bottom.Render();
            }
            if (RayIntersectsPlane(ref left.normal, ref Singleton<Camera>.INSTANCE.direction))
            {
                left.Render();
            }
            if (RayIntersectsPlane(ref right.normal, ref Singleton<Camera>.INSTANCE.direction))
            {
                right.Render();
            }
        }

        public void RenderAll(Shader shader)
        {
            shader.WriteUniform("highlight", new Vector3(highlight.R, highlight.G, highlight.B));

            {
                unsafe
                {
                    fixed (float* pointer = &colors[0].R)
                    {
                        ShaderUtil.GetShader("qb").WriteUniformArray("colors", colors.Length, pointer);
                    }
                }
            }

            front.Render(shader);
            back.Render(shader);
            top.Render(shader);
            bottom.Render(shader);
            left.Render(shader);
            right.Render(shader);
        }

        public void RenderAll()
        {
            front.Render();
            back.Render();
            top.Render();
            bottom.Render();
            left.Render();
            right.Render();
        }

        public void UseMatrixColors()
        {
            colors = matrixcolors;
        }

        //public void UseWireframeColors()
        //{
        //    colors = wireframecolors;
        //}

        //public void UseOutlineColors()
        //{
        //    colors = outlinecolors;
        //}

        private static bool RayIntersectsPlane(ref Vector3 normal, ref Vector3 rayVector)
        {
            float denom = 0;
            Vector3.Dot(ref normal, ref rayVector, out denom);
            if (denom < .3f)
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

        public void Clean()
        {
            foreach (var v in voxels.Values)
            {
                if (v.dirty)
                    v.dirty = false;
            }
        }

        public double GetHash(int x, int y, int z)
        {
            return ((double)x * 23.457154879791d + (double)y) * 31.154879416546d + (double)z;
        }

        public bool IsDirty(int x, int y, int z)
        {
            Voxel voxel;
            if (voxels.TryGetValue(GetHash(x, y, z), out voxel))
            {
                return voxel.dirty;
            }
            else
                return false;
        }

        public void UpdateVoxel()
        {
            //front
            if ((voxel.alphamask & 32) == 32)
            {
                front.fillbuffer(ref voxel.front, voxel.x, voxel.y, voxel.z, voxel.colorindex);
            }
            else
                front.removebuffer(ref voxel.front);

            //back
            if ((voxel.alphamask & 64) == 64)
            {
                back.fillbuffer(ref voxel.back, voxel.x, voxel.y, voxel.z, voxel.colorindex);
            }
            else
                back.removebuffer(ref voxel.back);

            //top
            if ((voxel.alphamask & 8) == 8)
            {
                top.fillbuffer(ref voxel.top, voxel.x, voxel.y, voxel.z, voxel.colorindex);
            }
            else
                top.removebuffer(ref voxel.top);

            //bottom
            if ((voxel.alphamask & 16) == 16)
            {
                bottom.fillbuffer(ref voxel.bottom, voxel.x, voxel.y, voxel.z, voxel.colorindex);
            }
            else
                bottom.removebuffer(ref voxel.bottom);

            //left
            if ((voxel.alphamask & 2) == 2)
            {
                left.fillbuffer(ref voxel.left, voxel.x, voxel.y, voxel.z, voxel.colorindex);
            }
            else
                left.removebuffer(ref voxel.left);

            //right
            if ((voxel.alphamask & 4) == 4)
            {
                right.fillbuffer(ref voxel.right, voxel.x, voxel.y, voxel.z, voxel.colorindex);
            }
            else
                right.removebuffer(ref voxel.right);
        }

        public byte GetAlphaMask(int x, int y, int z)
        {
            byte alpha = 1;

            if (voxels.TryGetValue(GetHash(x, y, z + 1), out voxel))
            {
                if (voxel.alphamask <= 1)
                    alpha += 32;
            }
            else alpha += 32;

            if (voxels.TryGetValue(GetHash(x, y, z - 1), out voxel))
            {
                if (voxel.alphamask <= 1)
                    alpha += 64;
            }
            else alpha += 64;

            if (voxels.TryGetValue(GetHash(x, y + 1, z), out voxel))
            {
                if (voxel.alphamask <= 1)
                    alpha += 8;
            }
            else alpha += 8;

            if (voxels.TryGetValue(GetHash(x, y - 1, z), out voxel))
            {
                if (voxel.alphamask <= 1)
                    alpha += 16;
            }
            else alpha += 16;

            if (voxels.TryGetValue(GetHash(x - 1, y, z), out voxel))
            {
                if (voxel.alphamask <= 1)
                    alpha += 4;
            }
            else alpha += 4;

            if (voxels.TryGetValue(GetHash(x + 1, y, z), out voxel))
            {
                if (voxel.alphamask <= 1)
                    alpha += 2;
            }
            else alpha += 2;

            return alpha >= 1 ? alpha : (byte)0;
        }

        public bool Remove(int x, int y, int z, bool setDirty = true, bool ignoreDirt = true)
        {
            if (voxels.TryGetValue(GetHash(x, y, z), out voxel))
            {
                if (ignoreDirt && voxel.dirty || voxel.alphamask == 0) return false;

                if (voxel.alphamask > 1)
                {
                    voxel.alphamask = 0;
                    UpdateVoxel();
                }
                else
                    voxel.alphamask = 0;

                voxel.dirty = setDirty;

                if (voxels.TryGetValue(GetHash(x, y, z + 1), out voxel))
                {
                    if (voxel.alphamask > 0 && (voxel.alphamask & 64) != 64)
                    {
                        voxel.alphamask += 64;
                        UpdateVoxel();
                    }
                }


                if (voxels.TryGetValue(GetHash(x, y, z - 1), out voxel))
                {
                    if (voxel.alphamask > 0 && (voxel.alphamask & 32) != 32)
                    {
                        voxel.alphamask += 32;
                        UpdateVoxel();
                    }
                }

                if (voxels.TryGetValue(GetHash(x, y + 1, z), out voxel))
                {
                    if (voxel.alphamask > 0 && (voxel.alphamask & 16) != 16)
                    {
                        voxel.alphamask += 16;
                        UpdateVoxel();
                    }
                }

                if (voxels.TryGetValue(GetHash(x, y - 1, z), out voxel))
                {
                    if (voxel.alphamask > 0 && (voxel.alphamask & 8) != 8)
                    {
                        voxel.alphamask += 8;
                        UpdateVoxel();
                    }
                }

                if (voxels.TryGetValue(GetHash(x + 1, y, z), out voxel))
                {
                    if (voxel.alphamask > 0 && (voxel.alphamask & 4) != 4)
                    {
                        voxel.alphamask += 4;
                        UpdateVoxel();
                    }
                }

                if (voxels.TryGetValue(GetHash(x - 1, y, z), out voxel))
                {
                    if (voxel.alphamask > 0 && (voxel.alphamask & 2) != 2)
                    {
                        voxel.alphamask += 2;
                        UpdateVoxel();
                    }
                }

                return true;
                //voxels.TryRemove(gethash(x, y, z), out voxel);
            }

            return false;
        }

        public void Remove(int x, int y, int z)
        {
            Remove(x, y, z, true, false);
        }

        public bool Add(int x, int y, int z, Colort color)
        {
            if (voxels.TryGetValue(GetHash(x, y, z), out voxel))
            {
                if (voxel.alphamask != 0) return false;
                else
                {
                    if (!voxels.TryRemove(GetHash(x, y, z), out voxel)) return false;
                    voxel = new Voxel(x, y, z, GetAlphaMask(x, y, z), GetColorIndex(color.R, color.G, color.B));
                    if (voxels.TryAdd(GetHash(x, y, z), voxel))
                    {
                        voxel.dirty = true;
                        UpdateVoxel();

                        if (voxels.TryGetValue(GetHash(x, y, z + 1), out voxel))
                        {
                            if ((voxel.alphamask & 64) == 64)
                            {
                                voxel.alphamask -= 64;
                                UpdateVoxel();
                            }
                        }

                        if (voxels.TryGetValue(GetHash(x, y, z - 1), out voxel))
                        {
                            if ((voxel.alphamask & 32) == 32)
                            {
                                voxel.alphamask -= 32;
                                UpdateVoxel();
                            }
                        }

                        if (voxels.TryGetValue(GetHash(x, y + 1, z), out voxel))
                        {
                            if ((voxel.alphamask & 16) == 16)
                            {
                                voxel.alphamask -= 16;
                                UpdateVoxel();
                            }
                        }

                        if (voxels.TryGetValue(GetHash(x, y - 1, z), out voxel))
                        {
                            if ((voxel.alphamask & 8) == 8)
                            {
                                voxel.alphamask -= 8;
                                UpdateVoxel();
                            }
                        }

                        if (voxels.TryGetValue(GetHash(x + 1, y, z), out voxel))
                        {
                            if ((voxel.alphamask & 4) == 4)
                            {
                                voxel.alphamask -= 4;
                                UpdateVoxel();
                            }
                        }

                        if (voxels.TryGetValue(GetHash(x - 1, y, z), out voxel))
                        {
                            if ((voxel.alphamask & 2) == 2)
                            {
                                voxel.alphamask -= 2;
                                UpdateVoxel();
                            }
                        }

                        return true;
                    }
                    return false;
                }
            }
            else
            {
                voxel = new Voxel(x, y, z, GetAlphaMask(x, y, z), GetColorIndex(color.R, color.G, color.B));
                if (voxels.TryAdd(GetHash(x, y, z), voxel))
                {
                    voxel.dirty = true;
                    UpdateVoxel();

                    if (voxels.TryGetValue(GetHash(x, y, z + 1), out voxel))
                    {
                        if ((voxel.alphamask & 64) == 64)
                        {
                            voxel.alphamask -= 64;
                            UpdateVoxel();
                        }
                    }

                    if (voxels.TryGetValue(GetHash(x, y, z - 1), out voxel))
                    {
                        if ((voxel.alphamask & 32) == 32)
                        {
                            voxel.alphamask -= 32;
                            UpdateVoxel();
                        }
                    }

                    if (voxels.TryGetValue(GetHash(x, y + 1, z), out voxel))
                    {
                        if ((voxel.alphamask & 16) == 16)
                        {
                            voxel.alphamask -= 16;
                            UpdateVoxel();
                        }
                    }

                    if (voxels.TryGetValue(GetHash(x, y - 1, z), out voxel))
                    {
                        if ((voxel.alphamask & 8) == 8)
                        {
                            voxel.alphamask -= 8;
                            UpdateVoxel();
                        }
                    }

                    if (voxels.TryGetValue(GetHash(x + 1, y, z), out voxel))
                    {
                        if ((voxel.alphamask & 4) == 4)
                        {
                            voxel.alphamask -= 4;
                            UpdateVoxel();
                        }
                    }

                    if (voxels.TryGetValue(GetHash(x - 1, y, z), out voxel))
                    {
                        if ((voxel.alphamask & 2) == 2)
                        {
                            voxel.alphamask -= 2;
                            UpdateVoxel();
                        }
                    }

                    return true;
                }

                return false;
            }
        }

        public void Color(Vector3 location, Colort color)
        {
            Color((int)location.X, (int)location.Y, (int)location.Z, color);
        }

        public void Color(int x, int y, int z, Colort color)
        {
            Color(x, y, z, GetColorIndex(color.R, color.G, color.B));
        }

        public void Color(int x, int y, int z, int colorindex, bool setDirty = true, bool ignoreDirt = true)
        {
            if (voxels.TryGetValue(GetHash(x, y, z), out voxel) && (ignoreDirt || !voxel.dirty))
            {
                voxel.dirty = setDirty;
                if (voxel.alphamask > 1)
                {
                    if (voxel.colorindex != colorindex)
                    {
                        voxel.colorindex = colorindex;
                        _color(x, y, z);
                    }
                }
            }
        }

        private void _color(int x, int y, int z)
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
}