using OpenTK;
using OpenTK.Graphics;
using OpenTK.Graphics.OpenGL;
using OpenTK.Input;
using Polenter.Serialization;
using QuickFont;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace stonevox
{
    public class GUIID
    {
        public const int HSV_H = 500;
        public const int HSV_S = 501;
        public const int HSV_V = 502;
        public const int RGB_R = 503;
        public const int RGB_G = 504;
        public const int RGB_B = 505;

        public const int COLOR_PICKER_WINDOW = 600;
        public const int COLORQUAD = 601;
    
        public const int START_COLOR_SELECTORS = 1000;

        public const int MATRIX_LISTBOX_WINDOW = 700;
    }

    public class ClientGUI : Singleton<ClientGUI>
    {
        public float scale = 1.0f;

        public List<Widget> widgets;
        private ClientInput input;

        private int widgetIDs = 100000;
        public int NextAvailableWidgeID { get { widgetIDs++; return widgetIDs; } }
        
        private int lastWidgetOverIndex = -1;
        public Widget lastWidgetOver { get { return widgets[lastWidgetOverIndex]; } }

        private int lastWidgetFocusedID = -1;
        private Widget lastWidgetFocused { get { return widgets[lastWidgetFocusedID]; } }

        public bool OverWidget { get { return lastWidgetOverIndex != -1; } }

        public ClientGUI(GLWindow window, ClientInput input)
             : base()
        {
            Singleton<ClientBroadcaster>.INSTANCE.SetGUI(this);

            window.SVReizeEvent += (e, o) =>
            {
                    UISaveState();
                    ConfigureUI(window.Width);
            };

            this.input = input;
            widgets = new List<Widget>();

            input.AddHandler(new InputHandler()
            {
                Keydownhandler = (e) =>
                {
                    if (lastWidgetFocusedID != -1 && lastWidgetFocused.Enable)
                    {
                        lastWidgetFocused.HandleKeyDown(e);
                    }
                },
                Keypresshandler = (e) =>
                {
                    if (lastWidgetFocusedID != -1 && lastWidgetFocused.Enable)
                    {
                        lastWidgetFocused.HandleKeyPress(e);
                    }
                },

                mousedownhandler = (e) => 
                {
                    if (lastWidgetOverIndex != -1 && lastWidgetOver.Enable)
                    {
                        if (lastWidgetOverIndex != lastWidgetFocusedID)
                        {
                            if (lastWidgetFocusedID != -1)
                                lastWidgetFocused.HandleFocusedLost();

                            lastWidgetFocusedID = lastWidgetOverIndex;
                            lastWidgetOver.HandleFocusedGained();
                        }

                        lastWidgetOver.HandleMouseDown(e);
                    }
                    else
                    {
                        if (lastWidgetFocusedID != -1 && lastWidgetFocused.Enable)
                        {
                            lastWidgetFocused.HandleFocusedLost();
                            lastWidgetFocusedID = -1;
                        }
                    }
                },
                mousemovehandler = (e) =>
                {
                    if (lastWidgetFocusedID != -1 && lastWidgetFocused.Enable)
                    {
                        if (lastWidgetFocused.Drag)
                        {
                               lastWidgetFocused.HandleMouseMove(e);
                        }
                    }
                },
                mouseuphandler = (e) => 
                {
                    if (lastWidgetOverIndex != -1 && lastWidgetOver.Enable)
                    {
                        lastWidgetOver.Drag = false;
                        lastWidgetOver.HandleMouseUp(e);

                        //float scaledmouseX = (float)Scale.hPosScale(input.mousex);
                        //float scaledmousez = (float)Scale.vPosScale(input.mouseY);

                        //if (!isMouseWithin(scaledmouseX, scaledmousez, lastWidgetOver))
                        //{
                        //    Console.WriteLine("mouse leasve");
                        //    lastWidgetOver.HandleMouseLeave();
                        //    lastWidgetOverIndex = -1;
                        //}
                    }
                },
                mousewheelhandler = (e) => 
                {
                    if (lastWidgetOverIndex != -1 && lastWidgetOver.Enable)
                    {
                        lastWidgetOver.HandleMouseWheel(e);
                    }
                }
            });

            //SharpSerializer s = new SharpSerializer();

            //if (File.Exists(Application.StartupPath + @"\data\gui\Standard.svui"))
            //{
            //    GUIData data = s.Deserialize(Application.StartupPath + @"\data\gui\Standard.svui") as GUIData;
            //    foreach (var widget in data.widgets)
            //    {
            //        //widgets.Add(widget.)
            //    }
            //}
        }

        public void Update(FrameEventArgs e)
        {
            for (int i = widgets.Count - 1; i > -1; i--)
            {
                if (widgets[i].Enable)
                    widgets[i].Update(e);
            }

            if (input.mousedx != 0 || input.mousedy != 0)
            {
                float scaledmouseX = (float)Scale.hPosScale(input.mousex);
                float scaledmouseY = (float)Scale.vPosScale(input.mousey);


                // we were over a control
                if (lastWidgetOverIndex != -1)
                {
                    if (!lastWidgetOver.Drag)
                    {
                        bool change = false;
                        if (!isMouseWithin(scaledmouseX, scaledmouseY, lastWidgetOver) || !lastWidgetOver.Enable)
                        {
                            change = true;
                        }

                        // are within our lastoverwidget
                        if (!change)
                        {
                            // check widgets above, if we're over then reset lastover
                            for (int i = widgets.Count - 1; i > lastWidgetOverIndex; i--)
                            {
                                if (widgets[i].Enable)
                                    if (isMouseWithin(scaledmouseX, scaledmouseY, widgets[i]))
                                    {
                                        lastWidgetOver.HandleMouseLeave();
                                        lastWidgetOverIndex = i;
                                        lastWidgetOver.HandleMouseEnter();
                                        lastWidgetOver.HandleMouseOver();
                                        change = true;
                                        break;
                                    }
                            }
                        }
                        // not within our lastoverwidget
                        else
                        {
                            change = false;
                            // check all widgets including lower ones...
                            for (int i = widgets.Count - 1; i > -1; i--)
                            {
                                if (widgets[i].Enable)

                                    if (i != lastWidgetOverIndex && isMouseWithin(scaledmouseX, scaledmouseY, widgets[i]))
                                    {
                                        lastWidgetOver.HandleMouseLeave();
                                        lastWidgetOverIndex = i;
                                        lastWidgetOver.HandleMouseEnter();
                                        lastWidgetOver.HandleMouseOver();
                                        change = true;
                                        break;
                                    }
                            }
                            // not over anything
                            if (!change)
                            {
                                lastWidgetOver.HandleMouseLeave();
                                lastWidgetOverIndex = -1;
                            }
                        }
                    }
                    else
                    {
                        // were dragging... release left mouse to let go
                        if (!input.mousedown(MouseButton.Left))
                        {
                            lastWidgetOver.HandleMouseLeave();
                            lastWidgetOverIndex = -1;

                            // if we don't test again we'd have to wait till next frame to mouse over something...
                            // i think there a potential double mouse over / mouse enter here??
                            for (int i = widgets.Count - 1; i > -1; i--)
                            {
                                if (widgets[i].Enable)

                                    if (isMouseWithin(scaledmouseX, scaledmouseY, widgets[i]))
                                    {
                                        lastWidgetOverIndex = i;
                                        lastWidgetOver.HandleMouseEnter();
                                        lastWidgetOver.HandleMouseOver();
                                        break;
                                    }
                            }
                        }
                    }
                }
                else
                {
                    for (int i = widgets.Count - 1; i > -1; i--)
                    {
                        if (widgets[i].Enable)

                            if (isMouseWithin(scaledmouseX, scaledmouseY, widgets[i]))
                            {
                                lastWidgetOverIndex = i;
                                lastWidgetOver.HandleMouseEnter();
                                lastWidgetOver.HandleMouseOver();
                                break;
                            }
                    }
                }

                if (lastWidgetOverIndex == -1)
                {
                    Singleton<Raycaster>.INSTANCE.Enabled = true;

                    if (Client.window.Cursor != Singleton<ClientBrush>.INSTANCE.currentBrush.Cursor)
                        Client.window.Cursor = Singleton<ClientBrush>.INSTANCE.currentBrush.Cursor;
                }
                else
                {
                    Singleton<Raycaster>.INSTANCE.Enabled = false;
                    
                    MouseCursor cursor = lastWidgetOver.cursor != null ? lastWidgetOver.cursor : MouseCursor.Default;

                    if (Client.window.Cursor != cursor)
                        Client.window.Cursor = cursor;
                }
            }
        }

        public void Render()
        {
            GL.Disable(EnableCap.DepthTest);

            Setup2D();
            Render2D();


            GL.Enable(EnableCap.DepthTest);
        }

        void Setup2D()
        {
            ShaderUtil.resetshader();
            GL.MatrixMode(MatrixMode.Projection);
            GL.LoadIdentity();
            GL.Ortho(-1f, 1f, -1f, 1f, -1, 1);

            GL.MatrixMode(MatrixMode.Modelview);
            GL.LoadIdentity();
        }

        void Render2D()
        {
            for (int i =0; i < widgets.Count; i++)
            {
                widgets[i].Render();
            }
        }

        bool isMouseWithin(float x, float y, Widget widget)
        {
            // absolute x.z and sizes should alreadz be in scale
            float widget_x = widget.Absolute_X;
            float widget_y = widget.Absolute_Y;

            float widget_width = widget.size.X;
            float widget_height= widget.size.Y;

            return (x <= widget_x + widget_width && x >= widget_x && y >= widget_y && y <= widget_y + widget_height);
        }

        public T Get<T>(int ID) where  T : Widget
        {
            var widget = widgets.Where((e) => { return e.ID == ID; });

            if (widget.Count() > 0)
                return (T)widget.First();
            else
            {
                var w = Activator.CreateInstance<T>();
                w.ID = 10000000;
                return w;
            }
        }

        void UISaveState()
        {
            widgetIDs = 100000;
            object handler = null;
            Get<EmptyWidget>(GUIID.COLOR_PICKER_WINDOW).customData.TryGetValue("inputhandler", out handler);
            input.removehandler(handler as InputHandler);

            lastWidgetFocusedID = -1;
            lastWidgetOverIndex = -1;

            widgets.Clear();
        }

        void ConfigureUI(int width)
        {
            if (width <= 1280)
            {
                scale = .75f;
            }
            else if (width <= 1400)
                scale = .8f;
            else scale = 1f;

            BuildUI();   
        }

        void BuildUI()
        {
            Build_BrushToolbar();
            Build_ColorPicker();
            Build_ColorToolbar();
            Build_MatrixList();
        }

        void Build_BrushToolbar()
        {
            // background
            EmptyWidget background = new EmptyWidget();
            background.appearence.AddAppearence("background", new Picture("./data/images/toolmenu_background.png"));
            background.SetBoundsNoScaling(0 - background.size.X / 2f, -1, null, null);
            widgets.Add(background);

            background.handler = new WidgetEventHandler()
            {
                mouseleave = (e) =>
                {
                    e.cursor = null;
                }
            };

            // tools

            Button selection = new Button(NextAvailableWidgeID, "./data/images/selection.png",
                                                                "./data/images/selection_highlight.png");
            selection.SetBoundsNoScaling(background.location.X + selection.size.X / 2.6f, -1);
            widgets.Add(selection);

            Button recolor = new Button(NextAvailableWidgeID, "./data/images/brush.png",
                                                               "./data/images/brush_highlight.png");
            PlainBorder recolorb = new PlainBorder(2, Color.FromArgb(59, 56, 56));
            recolor.handler = new WidgetEventHandler()
            {
                mousedownhandler = (e, s) =>
                {
                    if (s.Button == MouseButton.Left)
                    {
                        Singleton<ClientBrush>.INSTANCE.SetCurrentBrush(VoxelBrushTypes.Recolor);
                        e.cursor = Singleton<ClientBrush>.INSTANCE.currentBrush.Cursor;
                        background.cursor = Singleton<ClientBrush>.INSTANCE.currentBrush.Cursor;
                        Client.window.Cursor = e.cursor;
                    }
                },
                mouseleave = (e) =>
                {
                    e.cursor = null;
                }
            };
            recolor.SetBoundsNoScaling(background.location.X + ((selection.size.X / 2.6f) * 2f) + selection.size.X, -1);
            widgets.Add(recolor);

            Button add = new Button(NextAvailableWidgeID, "./data/images/add.png",
                                                         "./data/images/add_highlight.png");
            add.StatusText = StatusText.button_add;
            add.handler = new WidgetEventHandler()
            {
                mousedownhandler = (e, s) =>
                {
                    if (s.Button == MouseButton.Left)
                    {
                        Singleton<ClientBrush>.INSTANCE.SetCurrentBrush(VoxelBrushTypes.Add);
                        e.cursor = Singleton<ClientBrush>.INSTANCE.currentBrush.Cursor;
                        background.cursor = Singleton<ClientBrush>.INSTANCE.currentBrush.Cursor;
                        Client.window.Cursor = e.cursor;
                    }
                },
                mouseleave = (e) =>
                {
                    e.cursor = null;
                }
            };
            add.SetBoundsNoScaling(background.location.X + ((selection.size.X / 2.6f) * 3f) + selection.size.X * 2f, -1);
            widgets.Add(add);

            Button remove = new Button(NextAvailableWidgeID, "./data/images/remove.png",
                                                         "./data/images/remove_highlight.png");
            remove.StatusText = StatusText.button_remove;
            remove.handler = new WidgetEventHandler()
            {
                mousedownhandler = (e, s) =>
                {
                    if (s.Button == MouseButton.Left)
                    {
                        Singleton<ClientBrush>.INSTANCE.SetCurrentBrush(VoxelBrushTypes.Remove);
                        e.cursor = Singleton<ClientBrush>.INSTANCE.currentBrush.Cursor;
                        background.cursor = Singleton<ClientBrush>.INSTANCE.currentBrush.Cursor;
                        Client.window.Cursor = e.cursor;
                    }
                },
                mouseleave = (e) =>
                {
                    e.cursor = null;
                }
            };
            remove.SetBoundsNoScaling(background.location.X + ((selection.size.X / 2.6f) * 4f) + selection.size.X * 3f, -1);
            widgets.Add(remove);

            Button save = new Button(NextAvailableWidgeID, "./data/images/save.png",
                                                         "./data/images/save_highlight.png");
            save.SetBoundsNoScaling(background.location.X + ((selection.size.X / 2.6f) * 5f) + selection.size.X * 4f, -1);
            widgets.Add(save);

            Label status = new Label("", Color.Yellow);
            status.SetBoundsNoScaling(-1, -1);
            status.handler = new WidgetEventHandler()
            {
                messagerecived = (e, message, widget, args) =>
                {
                    if (message == Message.WidgetMouseEnter && widget.ID != status.ID)
                    {
                        if (!string.IsNullOrEmpty(widget.StatusText))
                        {
                            status.text = widget.StatusText;
                        }
                        else status.text = "";
                    }
                    else if (message == Message.StatusStripUpdate)
                    {
                        string m = args[0].ToString();
                        status.text = m;
                    }
                }
            };
            widgets.Add(status);

            Button target = new Button(NextAvailableWidgeID, "./data/images/target.png",
                                                   "./data/images/target_highlight.png");
            target.SetBoundsNoScaling(background.location.X - target.size.X * .76f, -1, target.size.X * .75f, target.size.Y * .75f);
            target.StatusText = StatusText.button_target;

            target.customData.Add("activematrix", -1);

            var c = new Action( () =>
            {
                while (true)
                {
                    int lastid = (int)target.customData["activematrix"];
                    int id = -1;

                    RaycastHit hit = new RaycastHit()
                    {
                        distance = 10000
                    };

                    for (int i = 0; i < Client.window.model.numMatrices; i++)
                    {
                        Singleton<Raycaster>.INSTANCE.ScreenToMouseRay(input.mousex, input.mousey);
                        RaycastHit tempHit = Singleton<Raycaster>.INSTANCE.RaycastTest(Singleton<Camera>.INSTANCE.position, Client.window.model.matrices[i]);

                        Console.WriteLine(tempHit.distance.ToString() + " " + tempHit.ToString());

                        if (tempHit.distance < hit.distance)
                        {
                            id = i;
                            hit = tempHit;
                        }
                    }

                    if (id > -1 && lastid != id)
                    {
                        Client.window.model.getactivematrix.highlight = Color4.White;
                        target.customData["activematrix"] = id;
                        Client.window.model.activematrix = id;
                        Client.window.model.getactivematrix.highlight = new Colort(1.5f, 1.5f, 1.5f);
                        string name = Client.window.model.getactivematrix.name;
                        status.text = string.Format("Over Matrix : {0}", name);
                    }
                    else if (id == -1)
                    {
                        if (lastid != -1)
                        {
                            Client.window.model.matrices[lastid].highlight = Color4.White;
                            target.customData["activematrix"] = -1;
                            status.text = "";
                        }
                    }

                    Thread.Sleep(25);
                }
            });

            target.customData.Add("thread", new Thread(() =>c()));

            target.handler = new WidgetEventHandler()
            {
                mousedownhandler = (e, mouse) =>
                {
                    float mouseX = (float)Scale.hPosScale(input.mousex);
                    float mouseY = (float)Scale.vPosScale(input.mousey);

                    if (mouse.Button != MouseButton.Left || !isMouseWithin(mouseX, mouseY, e)) return;

                    Thread thread = new Thread(() => c());

                    target.customData["activematrix"] = -1;
                    target.customData["thread"] = thread;
                    thread.Start();
                },
                mouseuphandler = (e, mouse) =>
                {
                    if (mouse.Button == MouseButton.Right)
                        e.Drag = true;
                    else if (mouse.Button == MouseButton.Left)
                    {
                        int lastid = (int)target.customData["activematrix"];

                        var thread = (Thread)target.customData["thread"];
                        thread.Abort();

                        if (lastid > -1)
                            Client.window.model.matrices[lastid].highlight = Color4.White;
                        target.customData["activematrix"] = -1;
                        status.text = "";
                    }
                }
            };

            widgets.Add(target);
        }

        void Build_ColorPicker()
        {
            EmptyWidget background = new EmptyWidget(GUIID.COLOR_PICKER_WINDOW);
            background.appearence.AddAppearence("background", new Picture("./data/images/save_window_background.png"));
            background.SetBoundsNoScaling(0 - background.size.X /2f, 0 - background.size.Y / 2f, null, null);
            background.Enable = false;
            widgets.Add(background);

            EmptyWidget colorQuad = new EmptyWidget(GUIID.COLORQUAD);
            SmoothBackground  colorQuad_background = new SmoothBackground();
            colorQuad.appearence.AddAppearence("background", colorQuad_background);
            colorQuad.Parent = background;
            colorQuad.SetBoundsNoScaling(background.location.X+ background.size.X *.05f,
                                         background.location.Y + background.size.Y * .3f, background.size.X * .6f, background.size.Y - (background.size.Y * .38f));
            widgets.Add(colorQuad);

            EmptyWidget swatches = new EmptyWidget();
            swatches.appearence.AddAppearence("background", new Picture("./data/images/colorpicker_swatches.png"));
            swatches.Parent = background;
            swatches.SetBoundsNoScaling(colorQuad.Absolute_X+colorQuad.size.X - swatches.size.X, colorQuad.Absolute_Y - swatches.size.Y*1.04f);

            swatches.handler = new WidgetEventHandler()
            {
                mousedownhandler = (e, mouse) =>
                {
                    if (mouse.Button != MouseButton.Left) return;

                    float x = e.Absolute_X;
                    float y = e.Absolute_Y;

                    float mouseX = (float)Scale.hPosScale(input.mousex);
                    float mouseY = (float)Scale.vPosScale(input.mousey);

                    // without jumping through hoops... you can only do OpenGL calls from
                    // the main thread containing the Context
                    Client.OpenGLContextThread.Add(() =>
                    {
                        byte[] data = new byte[4];
                        GL.ReadBuffer(ReadBufferMode.Front);
                        GL.ReadPixels(input.mousex, input.mousey, 1, 1, OpenTK.Graphics.OpenGL.PixelFormat.Rgba, PixelType.UnsignedByte, data);

                        Color color = Color.FromArgb(data[0], data[1], data[2]);

                        // little bit of error checking
                        // i sample the whole texture to get color from the swatch texture
                        // the texture has some parts which shouldn't be selectable, hence
                        // returning early if the users selects any of those parts
                        if (color.R != 0 && color.G != 0 && color.B != 0 && color.R < 43 && color.G < 43 && color.B < 43) return;

                        double hu, sat, vi;

                        ColorConversion.ColorToHSV(color, out hu, out sat, out vi);

                        Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.ColorSelectionUpdate, (float)hu, (float)sat, (float)vi);
                    });
                }
            };

            widgets.Add(swatches);

            EmptyWidget colorQuadSelection = new EmptyWidget();
            Picture colorQuadImage = new Picture("./data/images/colorpicker_selectionlocation.png");
            colorQuadSelection.appearence.AddAppearence("background", colorQuadImage);
            colorQuadSelection.Parent = background;
            colorQuadSelection.SetBoundsNoScaling(colorQuad.Absolute_X + colorQuad.size.X - colorQuadSelection.size.X * .5f,
                                                  colorQuad.Absolute_Y + colorQuad.size.Y - colorQuadSelection.size.Y * .5f);

            colorQuadSelection.handler = new WidgetEventHandler()
            {
                mousedownhandler = (e, mouse) =>
                {
                    colorQuad.HandleMouseDown(mouse);
                },
                mousemovehandler= (e, mouse) =>
                {
                    colorQuad.HandleMouseMove(mouse);
                }
            };

            widgets.Add(colorQuadSelection);

            EmptyWidget hue = new EmptyWidget();
            hue.appearence.AddAppearence("background", new Picture("./data/images/hue.png"));
            hue.Parent = background;
            hue.SetBoundsNoScaling(background.location.X + background.size.X * .06f+ colorQuad.size.X *1.03f,
                                         background.location.Y + background.size.Y * .3f, hue.size.X, colorQuad.size.Y);
            widgets.Add(hue);

            Label hsv = new Label("H" + '\n' + '\n' + "S" + '\n' + '\n' + "V", Color.White);
            hsv.Parent = background;
            hsv.SetBoundsNoScaling(hue.Absolute_X+hsv.size.X*1.5f, hue.Absolute_Y - hsv.size.Y*.25f+ hue.size.Y);
            hsv.size.Y /= 5f;
            widgets.Add(hsv);

            Label rgb = new Label("R" + '\n' + '\n' + "G" + '\n' + '\n' + "B", Color.White);
            rgb.Parent = background;
            rgb.SetBoundsNoScaling(hue.Absolute_X + hsv.size.X * 1.5f, hue.Absolute_Y+ rgb.size.Y*.85f);
            rgb.size.Y /= 5f;
            widgets.Add(rgb);

            TextBox h = new TextBox(GUIID.HSV_H, "0", Color.White, 5);
            h.Parent = background;
            h.SetBoundsNoScaling(hsv.Absolute_X+ hsv.size.X *1.2f, hsv.Absolute_Y);
            h.customData.Add("hsv_value", 0f);
            widgets.Add(h);

            TextBox s = new TextBox(GUIID.HSV_S, "0", Color.White, 5);
            s.Parent = background;
            s.SetBoundsNoScaling(hsv.Absolute_X + hsv.size.X * 1.2f, hsv.Absolute_Y - h.size.Y *2f);
            s.customData.Add("hsv_value", 1f);
            widgets.Add(s);

            TextBox v = new TextBox(GUIID.HSV_V, "0", Color.White, 5);
            v.Parent = background;
            v.SetBoundsNoScaling(hsv.Absolute_X + hsv.size.X * 1.2f, hsv.Absolute_Y - h.size.Y * 4f);
            v.customData.Add("hsv_value", 1f);
            widgets.Add(v);

            TextBox r = new TextBox(GUIID.RGB_R, "0", Color.White, 5);
            r.Parent = background;
            r.SetBoundsNoScaling(hsv.Absolute_X + hsv.size.X * 1.2f, rgb.Absolute_Y);
            widgets.Add(r);

            TextBox g = new TextBox(GUIID.RGB_G,"0", Color.White, 5);
            g.Parent = background;
            g.SetBoundsNoScaling(hsv.Absolute_X + hsv.size.X * 1.2f, rgb.Absolute_Y - h.size.Y * 2f);
            widgets.Add(g);

            TextBox b = new TextBox(GUIID.RGB_B, "0", Color.White, 5);
            b.Parent = background;
            b.SetBoundsNoScaling(hsv.Absolute_X + hsv.size.X * 1.2f, rgb.Absolute_Y - h.size.Y * 4f);
            widgets.Add(b);

            var rgbHSV_textboxHandler = new WidgetEventHandler()
            {
                textboxfilter = (e, input_text) =>
                {
                    int value = input_text.SafeToInt32();
                    string currentText = e.Text + input_text;
                    int newvalue = currentText.SafeToInt32();

                    // key pressed was a number key or 0
                    if (value > 0 || input_text == "0")
                    {
                        if (e.ID == GUIID.HSV_H)
                        {
                            if (newvalue <= 360)
                            {
                                return input_text;
                            }
                            else
                            {
                                MessageBox.Show("Value must be within range [0-360].", "Invalid Value", MessageBoxButtons.OK);
                                return "";
                            }
                        }
                        else if (e.ID >= GUIID.HSV_S && e.ID <= GUIID.HSV_V)
                        {
                            if (newvalue <= 100)
                            {
                                return input_text;
                            }
                            else
                            {
                                MessageBox.Show("Value must be within range [0-100].", "Invalid Value", MessageBoxButtons.OK);
                                return "";
                            }
                        }
                        else if (e.ID >= GUIID.RGB_R && e.ID <= GUIID.RGB_B)
                        {
                            if (newvalue <= 255)
                                return input_text;
                            else
                            {
                                MessageBox.Show("Value must be within range [0-255].", "Invalid Value", MessageBoxButtons.OK);
                                return "";
                            }
                        }
                    }
                    return input_text;
                },

                textboxtextchange = (e) =>
                {
                    if ((e as TextBox).Text == "") return;

                    if (e.ID >= GUIID.HSV_H && e.ID <= GUIID.HSV_V)
                    {
                        float hu = h.Text.SafeToFloat();
                        float sat= s.Text.SafeToFloat()/100f;
                        float vi = v.Text.SafeToFloat()/100f;

                        Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.ColorSelectionUpdate, hu, sat, vi);
                    }
                    else if (e.ID >= GUIID.RGB_R && e.ID <= GUIID.RGB_B)
                    {
                        int red   = r.Text.SafeToInt32();
                        int green = g.Text.SafeToInt32();
                        int blue  = b.Text.SafeToInt32();

                        double hu;
                        double sat;
                        double vi;

                        ColorConversion.ColorToHSV(Color.FromArgb(red, green, blue), out hu, out sat, out vi);

                        Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.ColorSelectionUpdate, (float)hu, (float)sat, (float)vi);
                    }
                }
            };

            h.handler = rgbHSV_textboxHandler;
            s.handler = rgbHSV_textboxHandler;
            v.handler = rgbHSV_textboxHandler;
            r.handler = rgbHSV_textboxHandler;
            g.handler = rgbHSV_textboxHandler;
            b.handler = rgbHSV_textboxHandler;

            colorQuad.handler = new WidgetEventHandler()
            {
                mousedownhandler = (e, mouse) =>
                {
                    if (mouse.Button != MouseButton.Left) return;

                    float x = e.Absolute_X;
                    float y = e.Absolute_Y;

                    float mouseX = (float)Scale.hPosScale(input.mousex);
                    float mouseY = (float)Scale.vPosScale(input.mousey);

                    float hu = (float)Get<TextBox>(GUIID.HSV_H).customData["hsv_value"];
                    float sat = Scale.scale(mouseX, x, x + e.size.X, 0, 1);
                    float vi = Scale.scale(mouseY, y, y + e.size.Y, 0, 1);

                    Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.ColorSelectionUpdate,  hu, sat, vi);
                },
                mousemovehandler = (e, mouse) =>
                {
                    if (mouse.Mouse.LeftButton != OpenTK.Input.ButtonState.Pressed) return;

                    float mouseX = (float)Scale.hPosScale(input.mousex);
                    float mouseY = (float)Scale.vPosScale(input.mousey);

                    float x = e.Absolute_X;
                    float y = e.Absolute_Y;

                    if (mouseX < x)
                        mouseX = x;
                    else if (mouseX > x + e.size.X)
                        mouseX = x + e.size.X;

                    if (mouseY < y)
                        mouseY = y;
                    else if (mouseY > y + e.size.Y)
                        mouseY = y + e.size.Y;

                    float hu = (float)Get<TextBox>(GUIID.HSV_H).customData["hsv_value"];
                    float sat = Scale.scale(mouseX, x, x + e.size.X, 0, 1);
                    float vi = Scale.scale(mouseY, y, y + e.size.Y, 0, 1);

                    Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.ColorSelectionUpdate, hu, sat, vi);
                }
            };

            hue.handler = new WidgetEventHandler()
            {
                mousedownhandler = (e,mouse) =>
                {
                    if (mouse.Button != MouseButton.Left) return;

                    float x = e.Absolute_X;
                    float y = e.Absolute_Y;

                    float mouseX = (float)Scale.hPosScale(input.mousex);
                    float mouseY = (float)Scale.vPosScale(input.mousey);

                    float hu = Scale.scale(mouseY, y, y + e.size.Y, 0, 360);
                    float sat = (float)Get<TextBox>(GUIID.HSV_S).customData["hsv_value"];
                    float vi = (float)Get<TextBox>(GUIID.HSV_V).customData["hsv_value"];

                    Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.ColorSelectionUpdate, hu, sat, vi);
                },
                mousemovehandler = (e, mouse) =>
                {
                    if (mouse.Mouse.LeftButton != OpenTK.Input.ButtonState.Pressed) return;

                    float mouseX = (float)Scale.hPosScale(input.mousex);
                    float mouseY = (float)Scale.vPosScale(input.mousey);

                    float x = e.Absolute_X;
                    float y = e.Absolute_Y;

                    if (mouseX < x)
                        mouseX = x;
                    else if (mouseX > x + e.size.X)
                        mouseX = x + e.size.X;

                    if (mouseY < y)
                        mouseY = y;
                    else if (mouseY > y + e.size.Y)
                        mouseY = y + e.size.Y;

                    float hu = Scale.scale(mouseY, y, y + e.size.Y, 0, 360);
                    float sat = (float)Get<TextBox>(GUIID.HSV_S).customData["hsv_value"];
                    float vi = (float)Get<TextBox>(GUIID.HSV_V).customData["hsv_value"];

                    Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.ColorSelectionUpdate, hu, sat, vi);
                }
            };

            EmptyWidget currentColor = new EmptyWidget();
            currentColor.Parent = background;
            currentColor.SetBoundsNoScaling(colorQuad.Absolute_X,
                                             background.Absolute_Y + background.size.Y * .05f+ background.size.Y * .12f,
                                             background.size.X * .12f,
                                             background.size.Y * .12f);

            PlainBackground cur_bg = new PlainBackground(Color.Red);
            currentColor.appearence.AddAppearence("background", cur_bg);
            currentColor.appearence.AddAppearence("border", new PlainBorder(3, new Color4(122f / 256f, 106f / 256f, 70f / 256f, 1f)));

            widgets.Add(currentColor);

            EmptyWidget previousColor = new EmptyWidget();
            previousColor.Parent = background;
            previousColor.SetBoundsNoScaling(colorQuad.Absolute_X,
                                             background.Absolute_Y + background.size.Y * .05f,
                                             background.size.X * .12f,
                                             background.size.Y * .12f);

            PlainBackground pre_bg = new PlainBackground(Color.Red);
            previousColor.appearence.AddAppearence("background", pre_bg);
            previousColor.appearence.AddAppearence("border", new PlainBorder(3, new Color4(122f / 256f, 106f / 256f, 70f / 256f, 1f)));

            previousColor.handler = new WidgetEventHandler()
            {
                messagerecived = (e, message, widget, args) =>
                {
                    if (message == Message.ColorSelectionChanged)
                    {
                        pre_bg.color = (Color4)args[0];
                    }
                    else if (message == Message.ColorSelectionCommit)
                    {
                        pre_bg.color = (Color4)args[0];
                    }
                }
            };
            widgets.Add(previousColor);

            background.handler = new WidgetEventHandler()
            {
                messagerecived = (e, message, widget, args) =>
                {
                    if (message == Message.ColorSelectionChanged)
                    {
                        Color4 color = (Color4)args[0];

                        double hu;
                        double sat;
                        double vi;

                        ColorConversion.ColorToHSV(color.ToSystemDrawingColor(), out hu, out sat, out vi);
                        Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.ColorSelectionUpdate, (float)hu, (float)sat, (float)vi);
                    }
                    else if (message == Message.ColorSelectionUpdate)
                    {
                        float hu = (float)args[0];
                        float sat = (float)args[1];
                        float vi = (float)args[2];

                        Color color = ColorConversion.ColorFromHSV(hu, sat, vi);

                        Get<TextBox>(GUIID.RGB_R).Text = (color.R).ToString();
                        Get<TextBox>(GUIID.RGB_G).Text = (color.G).ToString();
                        Get<TextBox>(GUIID.RGB_B).Text = (color.B).ToString();

                        Get<TextBox>(GUIID.HSV_H).Text = Math.Round(hu).ToString();
                        Get<TextBox>(GUIID.HSV_S).Text = Math.Round(sat * 100f).ToString();
                        Get<TextBox>(GUIID.HSV_V).Text = Math.Round(vi * 100f).ToString();

                        Get<TextBox>(GUIID.HSV_H).customData["hsv_value"] = hu;
                        Get<TextBox>(GUIID.HSV_S).customData["hsv_value"] = sat;
                        Get<TextBox>(GUIID.HSV_V).customData["hsv_value"] = vi;

                        cur_bg.color = color.ToColor4();
                        colorQuad_background.SetColor(ColorConversion.ColorFromHSV(hu, 1, 1));

                        colorQuadSelection.SetBoundsNoScaling(colorQuad.Absolute_X + colorQuad.size.X * sat - colorQuadSelection.size.X * .5f,
                                                              colorQuad.Absolute_Y + colorQuad.size.Y * vi - colorQuadSelection.size.Y * .5f);

                        if (vi < .35f)
                            colorQuadImage.color = Color.White;
                        else
                            colorQuadImage.color = Color.Black;
                    }
                    else if (message == Message.WindowOpened)
                    {
                        background.Enable = false;
                    }
                }
            };

            background.customData["inputhandler"] = new InputHandler()
            {
                Keydownhandler = (e) =>
                {
                    if (background.Enable)
                    {
                        if (e.Key == Key.Enter)
                        {
                            Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.ColorSelectionCommit, cur_bg.color);
                            background.Enable = false;
                        }
                        else if (e.Key == Key.Escape)
                        {
                            background.Enable = false;
                        }
                    }
                }
            };

            input.AddHandler(background.customData["inputhandler"] as InputHandler);

            Button ok = new Button("./data/images/colorpicker_ok.png",
                                                        "./data/images/colorpicker_ok_highlight.png");
            ok.Parent = background;
            ok.SetBoundsNoScaling(h.Absolute_X + h.size.X - ok.size.X, hue.Absolute_Y - ok.size.Y *2.3f);

            ok.handler = new WidgetEventHandler()
            {
                mousedownhandler = (e, mouse) =>
                {
                    if (mouse.Button == MouseButton.Left)
                    {
                        Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.ColorSelectionCommit, cur_bg.color);
                        background.Enable = false;
                    }
                }
            };

            widgets.Add(ok);

            Button cancel = new Button("./data/images/colorpicker_cancel.png",
                                                        "./data/images/colorpicker_cancel_highlight.png");
            cancel.Parent = background;
            cancel.SetBoundsNoScaling(ok.Absolute_X, ok.Absolute_Y + ok.size.Y * 1.1f);

            cancel.handler = new WidgetEventHandler()
            {
                mousedownhandler = (e, mouse) =>
                {
                    if (mouse.Button == MouseButton.Left)
                    {
                        background.Enable = false;
                    }
                }
            };

            widgets.Add(cancel);

            Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.ColorSelectionUpdate, 0f, 1f, 1f);
        }

        void Build_ColorToolbar()
        {
            EmptyWidget background = new EmptyWidget();
            background.appearence.AddAppearence("background", new Picture("./data/images/colorselector_background.png"));
            background.SetBoundsNoScaling(-1, 0 - background.size.Y / 2f, null, null);
            widgets.Add(background);

            float startY = 0 - background.size.Y / 2f + (54f).ScaleVerticlSize();

            for (int i = 0; i < 10; i++)
            {
                EmptyWidget colorselector = new EmptyWidget(GUIID.START_COLOR_SELECTORS + i);

                PlainBackground bg = new PlainBackground(new Color4(1f - (10 - i) * .1f, 1f - ((10 - i) * .06f),
                            1f - ((10 - i) * .03f), 1f));

                colorselector.appearence.AddAppearence("background", bg);

                PlainBorder border = new PlainBorder(3f, Color4.Gray);

                colorselector.appearence.AddAppearence("border", border);
                colorselector.SetBoundsNoScaling(-.993f, startY, background.size.X / 1.3f, (background.size.Y - (70 * 2f).ScaleVerticlSize()) / 10f - (13f).ScaleVerticlSize());

                colorselector.customData.Add("active", false);

                colorselector.handler = new WidgetEventHandler()
                {
                    mousedownhandler = (e, s) =>
                    {
                        if (s.Button != MouseButton.Left) return;
                        bool active = (bool)e.customData["active"];
                        if (!active)
                        {
                            e.customData["active"] = true;
                            border.color = Color4.Yellow;
                            Singleton<ClientBrush>.INSTANCE.brushColor = bg.color;
                            Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.ColorSelectionChanged, e, bg.color);
                        }
                        else
                        {
                            var colorpicker = Get<EmptyWidget>(GUIID.COLOR_PICKER_WINDOW);

                            if (!colorpicker.Enable)
                            {
                                colorpicker.Enable = true;
                                Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.WindowOpened, colorpicker);
                            }
                        }
                    },
                    mouseover = (e) =>
                    {
                        bool active = (bool)e.customData["active"];

                        if (!active)
                        {
                            border.color = Color4.LightGray;
                        }
                    },
                    mouseleave = (e) =>
                    {
                        bool active = (bool)e.customData["active"];

                        if (!active)
                        {
                            border.color = Color4.Gray;
                        }
                    },
                    messagerecived = (e, message, sender, args) =>
                    {
                        if (message == Message.ColorSelectionChanged)
                        {
                            e.customData["active"] = false;
                            border.color = Color4.Gray;
                        }
                        else if (message == Message.ColorSelectionCommit)
                        {
                            bool active = (bool)e.customData["active"];

                            if (active)
                            {
                                Color4 color = (Color4)args[0];
                                bg.color = color;
                                Singleton<ClientBrush>.INSTANCE.brushColor = color;
                            }
                        }
                    }
                };

                widgets.Add(colorselector);
                startY += ((background.size.Y - (70 * 2f).ScaleVerticlSize()) / 10f) + (5f).ScaleVerticlSize();

                if (i == 9)
                    colorselector.HandleMouseDown(new MouseButtonEventArgs(0, 0, MouseButton.Left, true));
            }

        }
        void Build_MatrixList()
        {
            EmptyWidget background = new EmptyWidget(GUIID.MATRIX_LISTBOX_WINDOW);
            background.appearence.AddAppearence("background", new Picture("./data/images/project_settings.png"));
            background.SetBoundsNoScaling(1 - background.size.X * .145f,-background.size.Y /2f);
            background.translations.Add("transistion_on", new WidgetTranslation()
            {
                Destination = new Vector2(background.Absolute_X - background.size.X * .85f, background.Absolute_Y),
                translationTime = 2f
            });
            background.translations.Add("transistion_off", new WidgetTranslation()
            {
                Destination = new Vector2(1f - background.size.X * .145f, background.Absolute_Y),
                translationTime = 2f
            });
            widgets.Add(background);

            Button windowbuttonON = new Button("./data/images/project_settings_button_on.png",
                                                        "./data/images/project_settings_button_on_highlight.png");
            windowbuttonON.Parent = background;
            windowbuttonON.SetBoundsNoScaling(background.Absolute_X + background.size.X *.025f, background.Absolute_Y + background.size.Y * .838f);
            widgets.Add(windowbuttonON);

            Button windowbuttonOFF = new Button("./data/images/project_settings_button_off.png",
                                                      "./data/images/project_settings_button_off_highlight.png");
            windowbuttonOFF.Parent = background;
            windowbuttonOFF.Enable = false;
            windowbuttonOFF.SetBoundsNoScaling(background.Absolute_X + background.size.X * .025f, background.Absolute_Y + background.size.Y * .838f);
            widgets.Add(windowbuttonOFF);

            windowbuttonON.handler = new WidgetEventHandler()
            {
                mousedownhandler = (e, mouse) =>
                {
                    e.Enable = false;
                    windowbuttonOFF.Enable = true;
                    background.DoTranslation("transistion_on");

                    Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.WindowOpened, background);
                }
            };


            windowbuttonOFF.handler = new WidgetEventHandler()
            {
                mousedownhandler = (e, mouse) =>
                {
                    e.Enable = false;
                    windowbuttonON.Enable = true;
                    background.DoTranslation("transistion_off");

                    Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.WindowClosed, background);
                }
            };

            background.handler = new WidgetEventHandler()
            {
                messagerecived = (e, message, widget, args) =>
                {
                    if (message == Message.WindowOpened)
                    {
                        windowbuttonOFF.Enable = false;
                        windowbuttonON.Enable = true;
                        background.DoTranslation("transistion_off");
                    }
                }
            };

            Label martixListLabel = new Label("Matrix List", Color.White);
            martixListLabel.Parent = background;
            martixListLabel.SetBoundsNoScaling(background.Absolute_X + background.size.X * .2f, background.Absolute_Y + background.size.Y * .92f);

            martixListLabel.handler = new WidgetEventHandler()
            {
                mouseenter =  (e) =>
                {
                    martixListLabel.color = Color.Blue;
                },
                mouseleave= (e) =>
                {
                    martixListLabel.color = Color.White;
                }
            };

            widgets.Add(martixListLabel);
        }
    }
}
