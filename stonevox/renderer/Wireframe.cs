using OpenTK;
using OpenTK.Graphics.OpenGL4;
using OpenTK.Input;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public class Wireframe : Singleton<Wireframe>, IRenderer
    {
        // eventually i'll get around to implementing a single-pass wireframe shader

        Shader voxelShader;
        Shader wireframeShader;

        Camera camera;
        Selection selection;

        public bool drawWireframe = true;
        public WireframeType wireFrameType = WireframeType.Outline;

        public Wireframe(Camera camera, Selection selection, ClientInput input)
            : base()
        {
            voxelShader = ShaderUtil.CreateShader("qb", "./data/shaders/voxel.vs", "./data/shaders/voxel.fs");
            wireframeShader = ShaderUtil.CreateShader("wireframe_qb", "./data/shaders/wireframe.vs", "./data/shaders/wireframe.fs");
            Shader s = ShaderUtil.CreateShader("s", "./data/shaders/QuadInterpolation.vs", "./data/shaders/QuadInterpolation.fs");

            this.camera = camera;
            this.selection = selection;

            input.AddHandler(new InputHandler()
            {
                Keydownhandler = (e) =>
                {
                    bool shift = Singleton<ClientInput>.INSTANCE.Keydown(OpenTK.Input.Key.ShiftLeft);

                    if (e.Key == Key.G && !e.Shift)
                    {
                        drawWireframe = !drawWireframe;
                    }
                    else if (e.Key == Key.G && e.Shift)
                    {
                        var values =Enum.GetValues(typeof(WireframeType));

                        var enumer = values.GetEnumerator();
                        while(enumer.MoveNext())
                        {
                            if ((WireframeType)enumer.Current == wireFrameType)
                            {
                                if (enumer.MoveNext())
                                {
                                    wireFrameType = (WireframeType)enumer.Current;
                                    return;
                                }
                            }
                        }

                        wireFrameType = WireframeType.Black;
                    }
                }
            });
        }

        public void Render(QbModel model)
        {
            switch (wireFrameType)
            {
                case WireframeType.Black:

                    if (drawWireframe)
                    {
                        GL.PolygonMode(MaterialFace.FrontAndBack, PolygonMode.Line);
                        GL.LineWidth(1);
                        wireframeShader.UseShader();
                        wireframeShader.WriteUniform("vHSV", new Vector3(1, 0f, 0));
                        wireframeShader.WriteUniform("modelview", camera.modelviewprojection);
                        model.RenderAll(wireframeShader);
                    }

                    GL.PolygonMode(MaterialFace.FrontAndBack, PolygonMode.Fill);
                    voxelShader.UseShader();
                    voxelShader.WriteUniform("modelview", camera.modelviewprojection);
                    selection.render(voxelShader);
                    model.RenderAll(voxelShader);

                    break;
                case WireframeType.ColorMatch:

                    if (drawWireframe)
                    {
                        GL.PolygonMode(MaterialFace.FrontAndBack, PolygonMode.Line);
                        GL.LineWidth(1);
                        wireframeShader.UseShader();
                        wireframeShader.WriteUniform("vHSV", new Vector3(1, 1.1f, 1.1f));
                        wireframeShader.WriteUniform("modelview", camera.modelviewprojection);
                        model.RenderAll(wireframeShader);
                    }

                    GL.PolygonMode(MaterialFace.FrontAndBack, PolygonMode.Fill);
                    voxelShader.UseShader();
                    voxelShader.WriteUniform("modelview", camera.modelviewprojection);
                    selection.render(voxelShader);
                    model.RenderAll(voxelShader);

                    break;
                case WireframeType.Outline:

                    if (drawWireframe)
                    {
                        GL.CullFace(CullFaceMode.Front);
                        GL.PolygonMode(MaterialFace.FrontAndBack, PolygonMode.Line);
                        GL.LineWidth(2);
                        wireframeShader.UseShader();
                        wireframeShader.WriteUniform("vHSV", new Vector3(1, 1.5f, 1.0f));
                        wireframeShader.WriteUniform("modelview", camera.modelviewprojection);
                        model.RenderAll(wireframeShader);
                        GL.CullFace(CullFaceMode.Back);
                        selection.render(voxelShader);
                    }

                    GL.CullFace(CullFaceMode.Back);
                    GL.PolygonMode(MaterialFace.FrontAndBack, PolygonMode.Fill);
                    voxelShader.UseShader();
                    voxelShader.WriteUniform("modelview", camera.modelviewprojection);
                    model.RenderAll(voxelShader);

                    break;
            }
        }
    }

    public enum WireframeType
    {
        Black,
        ColorMatch,
        Outline
    }
}
