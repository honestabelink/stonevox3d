﻿using OpenTK;
using OpenTK.Graphics;
using OpenTK.Graphics.OpenGL4;
using OpenTK.Input;
using OpenTK.Platform;
using QuickFont;
using stonevox.gui.editor;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace stonevox
{
    // personally i'm thinking nothing should go here to access stuff, ie backcolor, input, gui, ect
    // added singleton classes to start this change
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

        // this goes along with the comment below
        // these are all singeltons now
        ClientInput input;
        Camera camera;
        Selection selection;
        ClientBrush brushes;
        ClientGUI gui;
        ClientBroadcaster broadcaster;
        UndoRedo undoredo;
        Raycaster raycaster;
        Floor floor;
        QbManager manager;
        IRenderer renderer;

        // not sure about these yet, need another place
        // ... also the whole QbManager/Importer/Exporter thing i don't like
        // thinking some sort of qb manager class should be put in the _client space
        // following Singleton<?? "qb manager">
        //public QbModel model;
        public Shader voxelShader;

        public Color4 backcolor;

        public bool isfocused = true;

        public QFont Qfont;
        public QFont Qfont_1280;
        public QFont Qfont_1400;
        public QFont Qfont_1920;

        public event EventHandler SVReizeEvent;
        private int lastWidth;

        public GLWindow(int width, int height, GraphicsMode graphicsmode)
            : base(width, height, graphicsmode)
        {
            WindowState = WindowState.Maximized;
        }

        public DragDropTarget dnd;

        protected override void OnLoad(EventArgs e)
        {
            string version = Assembly.GetExecutingAssembly().GetName().Version.ToString();
            Title = String.Format("StoneVox 3D - version {0}", version);

            GL.Viewport(0, 0, Width, Height);
            Qfont_1280 = new QFont("data\\fonts\\Bigfish.ttf", 11.2f, new QFontBuilderConfiguration(true, false));
            Qfont_1400 = new QFont("data\\fonts\\Bigfish.ttf", 12f, new QFontBuilderConfiguration(true, false));
            Qfont_1920 = new QFont("data\\fonts\\Bigfish.ttf", 15, new QFontBuilderConfiguration(true, false));
            if (Width <= 1280)
            {
                Qfont = Qfont_1280;
            }
            else if (Width < 1400)
            {
                Qfont = Qfont_1400;
            }
            else
            {
                Qfont = Qfont_1920;
            }

            this.Qfont.Options.Colour = Color.White;
            //this.Qfont.Options.TransformToViewport = new TransformViewport(-1,-1,2,2);

            Scale.SetHScaling(0, Width);
            Scale.SetVScaling(0, Height);

            ShaderUtil.CreateShader("quad_interpolation", "./data/shaders/QuadInterpolation.vs", "./data/shaders/QuadInterpolation.fs");

            broadcaster = new ClientBroadcaster();
            manager = new QbManager(broadcaster);
            input = new ClientInput(this);
            camera = new Camera(this, input, manager);
            brushes = new ClientBrush(this, input);
            floor = new Floor(camera, broadcaster);
            gui = new ClientGUI(this, manager, input);
            selection = new Selection(this,brushes, input, manager, floor, gui);
            renderer = new Wireframe(camera, selection, floor, input);
            undoredo = new UndoRedo(input);

            selection.GenerateVertexArray();

            if(!manager.HasModel)
                manager.AddEmpty();
            camera.LookAtModel(true);

            backcolor = new Color4(0, 0, 0, 0);

            GL.Enable(EnableCap.DepthTest);
            GL.Enable(EnableCap.Blend);
            GL.BlendFunc(BlendingFactorSrc.SrcAlpha, BlendingFactorDest.OneMinusSrcAlpha);

            GL.Enable(EnableCap.CullFace);
            GL.CullFace(CullFaceMode.Back);

            int ole_hresult = OleInitialize(IntPtr.Zero);
            IntPtr handle = FindWindowByCaption(IntPtr.Zero, Title);
            dnd = new DragDropTarget();
            int dnd_hresult = RegisterDragDrop(handle, dnd);

            raycaster = new Raycaster(this, camera, selection, floor, input, manager, gui);
            selection.raycaster = raycaster;

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
            Qfont_1280.Dispose();
            Qfont_1400.Dispose();
            Qfont_1920.Dispose();
            manager.Dispose();
            base.Dispose();
        }

        protected override void OnFocusedChanged(EventArgs e)
        {
            if (!Focused)
            {
                isfocused = false;
                raycaster.Enabled = false;
            }
            else
            {
                isfocused = true;
                raycaster.Enabled = true;
            }
            base.OnFocusedChanged(e);
        }
        protected override void OnWindowStateChanged(EventArgs e)
        {
            if (WindowState == WindowState.Minimized)
            {
                raycaster.Enabled = false;

            }
            else if (WindowState == WindowState.Normal || WindowState == WindowState.Maximized)
            {
                raycaster.Enabled = true;
            }
            base.OnWindowStateChanged(e);
        }

        protected override void OnResize(EventArgs e)
        {
            GL.Viewport(0, 0, Width, Height);
            QFont.ForceViewportRefresh();

            Scale.SetHScaling(0, Width);
            Scale.SetVScaling(0, Height);

            if (Width <= 1280 && lastWidth != 1280)
            {
                lastWidth = 1280;
                Qfont = Qfont_1280;
                SVReizeEvent?.Invoke(this, EventArgs.Empty);
            }
            else if (Width >1280 && Width <= 1400 && lastWidth !=1400)
            {
                lastWidth = 1400;
                Qfont = Qfont_1400;
                SVReizeEvent?.Invoke(this, EventArgs.Empty);
            }
            else if (Width > 1400 && lastWidth != 1920)
            {
                lastWidth = 1920;
                Qfont = Qfont_1920;
                SVReizeEvent?.Invoke(this, EventArgs.Empty);
            }

            base.OnResize(e);
        }

        protected override void OnKeyDown(KeyboardKeyEventArgs e)
        {
            input.handleKeydown(e);

            if (e.Modifiers == KeyModifiers.Control && e.Key == Key.O)
            {
                var open = new OpenFileDialog();
                open.Multiselect = false;
                open.Title = "Open .qb File";
                open.DefaultExt = ".qb";

                var result = open.ShowDialog();

                if (result == DialogResult.OK)
                {
                    Client.OpenGLContextThread.Add(() => 
                    {
                        ImportExportUtil.Import(open.FileName);
                    });
                }
            }
            if (e.Modifiers == KeyModifiers.Control && e.Key == Key.S)
            {
                var save = new SaveFileDialog();
                save.Title = "Save File";
                save.Filter = "StoneVox Project (.svp)|*.svp|Qubicle Binary (.qb)|*.qb|Wavefront OBJ (.obj)|*.obj|All files (*.*)|*.*";
                save.DefaultExt = ".svp";

                var result = save.ShowDialog();

                if (result == DialogResult.OK)
                {
                    QbModel model = manager.ActiveModel;
                    model.name = save.FileName.Split('\\').Last();
                    if (model.name.Contains('.'))
                        model.name = model.name.Split('.').First();
                    ImportExportUtil.Export(save.FileName.Split('\\').Last().Replace(model.name, ""), model.name, Path.GetDirectoryName(save.FileName), model);
                }
            }
            else if (e.Modifiers == KeyModifiers.Control && e.Key == Key.F12)
            {
                GUIEditor editor = new GUIEditor();
                editor.Show();
            }
            //else if (e.Key == Key.T)
            //{
            //    Stopwatch w = Stopwatch.StartNew();
            //    var model = Singleton<QbManager>.INSTANCE.ActiveMatrix;

            //    Colort color = new Colort(1f, 0f, 0f);
            //    for (int x = 0; x < 100; x++)
            //        for (int z = 0; z < 100; z++)
            //        for (int y = 0; y< 100; y++)
            //                model.Add(x, y, z, color);

            //    w.Stop();
            //    Console.WriteLine("add " +w.ElapsedMilliseconds.ToString());
            //}

            //else if (e.Key == Key.Y)
            //{
            //    Stopwatch w = Stopwatch.StartNew();
            //    var model = Singleton<QbManager>.INSTANCE.ActiveMatrix;

            //    Colort color = new Colort(1f, 0f, 0f);
            //    for (int x = 0; x < 100; x++)
            //        for (int z = 0; z < 100; z++)
            //            for (int y = 0; y < 100; y++)

            //                model.Remove(x, y, z);

            //    w.Stop();
            //    Console.WriteLine("earse " + w.ElapsedMilliseconds.ToString());
            //}

            base.OnKeyDown(e);
        }
        protected override void OnKeyUp(KeyboardKeyEventArgs e)
        {
            input.handleKeyup(e);
            base.OnKeyUp(e);
        }
        protected override void OnKeyPress(OpenTK.KeyPressEventArgs e)
        {
            input.handleKeypress(e);
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
            if (input != null)
            {
                input.mousex = e.X;
                input.mousey = Height - e.Y;

                input.mousedx = e.XDelta;
                input.mousedy = e.YDelta;

                // when i implement something good for following the mouse around quickly
                //Raycaster.testlocations.Push(new Vector2(e.X, Height - e.Y));

                input.handlemousemove(e);

            }

            base.OnMouseMove(e);
        }
        protected override void OnMouseWheel(MouseWheelEventArgs e)
        {
            input.handlemousewheel(e);
            base.OnMouseWheel(e);
        }

        protected override void OnUpdateFrame(FrameEventArgs e)
        {
            if (!isfocused)
            {
                input.update();
                base.OnUpdateFrame(e);
                return;
            }

            input.update();
            gui.Update(e);

            camera.update((float)e.Time);
            selection.update();

            base.OnUpdateFrame(e);
        }

        double ee = 0;
        int fps = 0;

        protected override void OnRenderFrame(FrameEventArgs e)
        {
            if (!isfocused)
            {
                Client.update();
                base.OnRenderFrame(e);
                return;
            }
              //  Console.WriteLine(raycaster.Enabled.ToString());
            GL.ClearColor(backcolor.R, backcolor.G, backcolor.B, backcolor.A);
            GL.Clear(ClearBufferMask.ColorBufferBit | ClearBufferMask.DepthBufferBit);

            ee += e.Time;
            fps++;

            if (ee > 1)
            {
                ee = 0;
                Title = "StoneVox fps : " + fps.ToString();
                fps = 0;
            }

            renderer.Render(manager.ActiveModel);

            ShaderUtil.ResetShader();

            floor.Render();

            gui.Render();

            SwapBuffers();

            Client.update();

            base.OnRenderFrame(e);
        }
    }
}
