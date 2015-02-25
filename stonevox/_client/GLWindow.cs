using OpenTK;
using OpenTK.Graphics;
using OpenTK.Graphics.OpenGL4;
using OpenTK.Input;
using OpenTK.Platform;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace stonevox
{
    public class GLWindow : SyncWindow
    {
        [DllImport("user32.dll", SetLastError = true)]
        static extern IntPtr FindWindow(string lpClassName, string lpWindowName);

        [DllImport("user32.dll", EntryPoint = "FindWindow", SetLastError = true)]
        public static extern IntPtr FindWindowByCaption(IntPtr ZeroOnly, string lpWindowName);

        [DllImport("ole32.dll")]
        public static extern int OleInitialize(IntPtr pvReserved);

        [DllImport("ole32.dll")]
        public static extern int RegisterDragDrop(IntPtr hwnd, stonevox.IDropTarget pDropTarget);

        public ClientInput input;
        public Camera camera;
        public Selection selection;
        public ClientTools tools;

        public QbModel model;
        public Shader shader;

        public Color4 backcolor;

        public bool isfocused = true;

        public GLWindow(int width, int height, GraphicsMode graphicsmode)
            : base(width, height, graphicsmode)
        {
            
        }

        public DragDropTarget dnd;

        // open/closing
        protected override void OnLoad(EventArgs e)
        {
            string version = Assembly.GetExecutingAssembly().GetName().Version.ToString();
           // version = version.Remove(version.Length - 2);
            Title = String.Format("StoneVox 3D - version {0}", version);

            shader = ShaderUtil.createshader("qb", "./data/shaders/vertexshader.txt", "./data/shaders/fragmentshader.txt");
            shader.useshader();

            input = new ClientInput(this);
            camera = new Camera(this, input);
            tools = new ClientTools();
            selection = new Selection(tools, input);

            ImportExportUtil.import(@"C:\Users\daniel\Desktop\dining_table.qb", out model);

            backcolor = new Color4(0, 0, 0, 256);

            GL.Enable(EnableCap.DepthTest);
            GL.Enable(EnableCap.Blend);
            GL.BlendFunc(BlendingFactorSrc.SrcAlpha, BlendingFactorDest.OneMinusSrcAlpha);

            //GL11.glEnable(GL11.GL_ALPHA_TEST);
            //GL11.glAlphaFunc(GL11.GL._GREATER, 0.6f);
            //GL11.glEnable(GL11.GL_BLEND);
            //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            int ole_hresult = OleInitialize(IntPtr.Zero);
            IntPtr handle = FindWindowByCaption(IntPtr.Zero, Title);
            dnd = new DragDropTarget();
            int dnd_hresult = RegisterDragDrop(handle, dnd);

            Raycaster.init(this, camera, selection, input);

            Client.Initialized = true;
            base.OnLoad(e);
        }
        protected override void OnClosing(CancelEventArgs e)
        {
            Environment.Exit(0);
            base.OnClosing(e);
        }
        public override void Dispose()
        {
            Raycaster.Dispose();
            base.Dispose();
        }

        // window change
        protected override void OnFocusedChanged(EventArgs e)
        {
            if (!Focused)
            {
                isfocused = false;
            }
            else
            {
                isfocused = true;
            }
            base.OnFocusedChanged(e);
        }
        protected override void OnWindowStateChanged(EventArgs e)
        {
            base.OnWindowStateChanged(e);
        }
        protected override void OnResize(EventArgs e)
        {
            base.OnResize(e);
            GL.Viewport(0, 0, Width, Height);
        }

        // keyboard

        protected override void OnKeyDown(KeyboardKeyEventArgs e)
        {
            input.handlekeydown(e);

            if (e.Modifiers == KeyModifiers.Control && e.Key == Key.O)
            {
                var open = new OpenFileDialog();
                open.Multiselect = false;
                open.Title = "Open .qb File";
                open.DefaultExt = ".qb";

                var result = open.ShowDialog();

                if (result == DialogResult.OK)
                {
                    Client.stonevoxcalls.Add(() => { ImportExportUtil.import(open.FileName, out model); });
                }
            }

            base.OnKeyDown(e);
        }
        protected override void OnKeyUp(KeyboardKeyEventArgs e)
        {
            input.handlekeyup(e);
            base.OnKeyUp(e);
        }
        protected override void OnKeyPress(OpenTK.KeyPressEventArgs e)
        {
            input.handlekeypress(e);
            base.OnKeyPress(e);
        }

        // mouse
        protected override void OnMouseDown(MouseButtonEventArgs e)
        {
            input.handlemousedown(e);
            base.OnMouseDown(e);
        }
        protected override void OnMouseUp(MouseButtonEventArgs e)
        {
            input.handlemouseup(e);
            base.OnMouseUp(e);
        }
        protected override void OnMouseMove(MouseMoveEventArgs e)
        {
            input.mousex = e.X;
            input.mousey = Height - e.Y;

            // when i implement something good for following the mouse around quickly
            //Raycaster.testlocations.Push(new Vector2(e.X, Height - e.Y));

            base.OnMouseMove(e);
        }
        protected override void OnMouseWheel(MouseWheelEventArgs e)
        {
            input.handlemousewheel(e);
            base.OnMouseWheel(e);
        }

        // program loop
        protected override void OnUpdateFrame(FrameEventArgs e)
        {
            Client.update();
            if (model == null) return;

            input.update();
            camera.update((float)e.Time);
            selection.update();

            base.OnUpdateFrame(e);
        }

        double ee = 0;
        int fps = 0;

        protected override void OnRenderFrame(FrameEventArgs e)
        {
            GL.ClearColor(backcolor.R, backcolor.G, backcolor.B, 1);
            GL.Clear(ClearBufferMask.ColorBufferBit | ClearBufferMask.DepthBufferBit);

            ee += e.Time;
            fps++;

            if (ee > 1)
            {
                ee = 0;
                Title = "StoneVox fps : " + fps.ToString();
                fps = 0;
            }

            if (model != null)
            {
                shader.useshader();
                shader.writeuniform("modelview", camera.modelviewprojection);
                selection.render(shader);
                model.render(shader);
            }

            //ShaderUtil.resetshader();

            //OpenTK.Graphics.OpenGL.GL.LineWidth(4f);
            //OpenTK.Graphics.OpenGL.GL.MatrixMode(OpenTK.Graphics.OpenGL.MatrixMode.Projection);
            //OpenTK.Graphics.OpenGL.GL.LoadIdentity();
            //OpenTK.Graphics.OpenGL.GL.MultMatrix(ref camera.projection);
            //OpenTK.Graphics.OpenGL.GL.Viewport(0, 0, Width, Height);

            //OpenTK.Graphics.OpenGL.GL.MatrixMode(OpenTK.Graphics.OpenGL.MatrixMode.Modelview);
            //OpenTK.Graphics.OpenGL.GL.LoadIdentity();
            //OpenTK.Graphics.OpenGL.GL.MultMatrix(ref camera.modelviewprojection);

            //OpenTK.Graphics.OpenGL.GL.Begin(OpenTK.Graphics.OpenGL.BeginMode.Lines);
            //OpenTK.Graphics.OpenGL.GL.Color3(1f,0f, 0f);
            //OpenTK.Graphics.OpenGL.GL.Vertex3(camera.position.X, camera.position.Y, camera.position.Z );
            //OpenTK.Graphics.OpenGL.GL.Vertex3(camera.position.X + Raycaster.rayDirection.X,
            //    camera.position.Y+Raycaster.rayDirection.Y ,
            //    camera.position.Z+ Raycaster.rayDirection.Z);
            //OpenTK.Graphics.OpenGL.GL.End();

            //OpenTK.Graphics.OpenGL.GL.MatrixMode(OpenTK.Graphics.OpenGL.MatrixMode.Modelview);
            //OpenTK.Graphics.OpenGL.GL.LoadIdentity();
            //OpenTK.Graphics.OpenGL.GL.MultMatrix(ref camera.view);

            //Vector3 t = new Vector3();

            //Raycaster.rayTest(new Vector3(0, 0, 1), -5*2, -5*2, 3*2, -6*2, -5*2, 3*2, -6*2, -6*2, 3*2, out t);


            //if (t.X == 0 && t.Y == 0 & t.Z == 0)
            //{
            //    OpenTK.Graphics.OpenGL.GL.Begin(OpenTK.Graphics.OpenGL.BeginMode.Triangles);
            //    OpenTK.Graphics.OpenGL.GL.Color3(1f, 0f, 0f);
            //    OpenTK.Graphics.OpenGL.GL.Vertex3(-5*2, -5*2, 3*2);
            //    OpenTK.Graphics.OpenGL.GL.Vertex3(-6*2, -5*2, 3*2);
            //    OpenTK.Graphics.OpenGL.GL.Vertex3(-6*2, -6*2, 3*2);
            //    OpenTK.Graphics.OpenGL.GL.End();
            //}
            //else
            //{
            //    OpenTK.Graphics.OpenGL.GL.Begin(OpenTK.Graphics.OpenGL.BeginMode.Triangles);
            //    OpenTK.Graphics.OpenGL.GL.Color3(0f, 0f, 1f);
            //    OpenTK.Graphics.OpenGL.GL.Vertex3(-5 * 2, -5 * 2, 3 * 2);
            //    OpenTK.Graphics.OpenGL.GL.Vertex3(-6 * 2, -5 * 2, 3 * 2);
            //    OpenTK.Graphics.OpenGL.GL.Vertex3(-6 * 2, -6 * 2, 3 * 2);
            //    OpenTK.Graphics.OpenGL.GL.End();
            //}

            SwapBuffers();
            //Thread.Sleep(8);
            base.OnRenderFrame(e);
        }
    }
}
