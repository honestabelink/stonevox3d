using OpenTK;
using OpenTK.Graphics;
using OpenTK.Input;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows.Forms;

namespace stonevox
{
    public class QbModelMatrixListbox : Widget
    {
        List<Label> labels;
        List<ToggleButton> buttons;

        VerticalScrollbar scrollbar;
        EmptyWidget background;

        int offsetindex;

        int lastActiveIndex;

        TextBox textbox;

        Label editinglable;

        //Label lastover;

        bool edit;
        string previousText;

        //ClientInput input;

        int linecount;

        public QbModelMatrixListbox(float width, float height)
            : base(-1)
        {
            //input = Singleton<ClientInput>.INSTANCE;

            ID = GetNextAvailableID();
            appearence.AddAppearence("border", new PlainBorder(6, new Color4(122f / 256f, 106f / 256f, 70f / 256f, 1f)));
            appearence.AddAppearence("background", new PlainBackground(new Color4(66, 63, 63, 255)));

            float realheight = Client.window.Qfont.fontData.maxGlyphHeight;
            float fontheight = (float)realheight*2f;
            linecount = (int)((height.UnScaleVerticlSize()) / fontheight);

            //float labelspacing = 25f;

            textbox = new TextBox(100000000, "", Color4.Wheat.ToSystemDrawingColor(), 1000);
            textbox.focused = true;

            fontheight = fontheight.ScaleVerticlSize();

            labels = new List<Label>();
            buttons = new List<ToggleButton>();
            size.X = width;
            size.Y = linecount * fontheight;// + linecount * (labelspacing).ScaleVerticlSize();

            QbModel model = null;
            bool hasmodel = Singleton<QbManager>.INSTANCE.HasModel;

            if (hasmodel)
                model = Singleton<QbManager>.INSTANCE.ActiveModel;

            float previouseY = Absolute_Y + size.Y - realheight.ScaleVerticlSize() * 2f;

            background = new EmptyWidget();
            background.Parent = this;
            PlainBackground bg = new PlainBackground(Color4.Black);
            background.appearence.AddAppearence("background", bg);

            for (int i = 0; i < linecount; i++)
            {
                string labeltext = "";

                if (hasmodel)
                    labeltext = i < model.matrices.Count ? model.matrices[i].name : "";

                Label label = new Label(GetNextAvailableID(), labeltext, System.Drawing.Color.White, true);
                label.Parent = this;
                label.SetBoundsNoScaling(Absolute_X + size.X * .08f, previouseY);

                Boolean editing = false;
                label.customData.Add("edit", editing);

                label.handler = new WidgetEventHandler()
                {
                    mousedownhandler = (e, mouse) =>
                    {
                        if (!mouse.IsPressed || mouse.Button != OpenTK.Input.MouseButton.Left) return;

                        background.Enable = true;
                        bg.color = Color4.Black;
                        background.SetBoundsNoScaling(Absolute_X, label.Absolute_Y, size.X, label.size.Y - (4f).ScaleVerticlSize());

                        QbModel m = Singleton<QbManager>.INSTANCE.ActiveModel;
                        int index = labels.IndexOf(label);
                        int offset = index + offsetindex;

                        Singleton<QbManager>.INSTANCE.ActiveMatrixIndex = offset;

                        lastActiveIndex = labels.ToList().IndexOf(label);
                    },
                    mousedoubleclick = (e, mouse) =>
                    {
                        if (!mouse.IsPressed || mouse.Button != OpenTK.Input.MouseButton.Left) return;

                        QbModel m = Singleton<QbManager>.INSTANCE.ActiveModel;
                        int index = labels.IndexOf(label);
                        int offset = index + offsetindex;

                        Singleton<QbManager>.INSTANCE.ActiveMatrixIndex = offset;
                        Singleton<Camera>.INSTANCE.TransitionToMatrix();
                    },
                    mouseenter =  (e) =>
                    {
                        //lastover = label;

                        int index = labels.IndexOf(label);
                        int offset = index + offsetindex;

                        QbModel m = Singleton<QbManager>.INSTANCE.ActiveModel;

                        m.matrices[offset].highlight = new Colort(1.5f, 1.5f, 1.5f);
                    },
                    mouseleave = (e) =>
                    {
                        int index = labels.IndexOf(label);
                        int offset = index + offsetindex;

                        QbModel m = Singleton<QbManager>.INSTANCE.ActiveModel;

                        m.matrices[offset].highlight = new Colort(1f, 1f, 1f);
                    },
                    Keydownhandler = (e, k) =>
                    {
                        if (!edit && k.Key == Key.F2)
                        {
                            previousText = label.text;
                            editinglable = label;
                            textbox.Text = label.text;
                            textbox.HandleFocusedGained();
                            label.text += "|";
                            editing = true;
                            edit = true;
                            Singleton<GUI>.INSTANCE.Dirty = true;
                        }
                        else if (edit && k.Key == Key.Enter || k.Key == Key.KeypadEnter)
                        {
                            editing = false;
                            edit = false;

                            label.text = textbox.Text;
                            if (string.IsNullOrEmpty(label.text))
                            {
                                label.text = previousText;
                                MessageBox.Show("Matrix names MUST contain at least one character", "Matrix Name Limits");
                            }

                            Singleton<GUI>.INSTANCE.Dirty = true;

                            int index = labels.IndexOf(label);
                            int offset = index + offsetindex;

                            QbModel m = Singleton<QbManager>.INSTANCE.ActiveModel;

                            m.matrices[offset].name = label.text;
                            Singleton<GUI>.INSTANCE.Get<TextBox>(GUIID.ACTIVE_MATRIX_NAME).Text = label.text;
                        }
                        else if (edit && k.Key == Key.Escape)
                        {
                            label.text = previousText.Replace("|", "");
                            edit = false;
                            editing = false;
                            Singleton<GUI>.INSTANCE.Get<TextBox>(GUIID.ACTIVE_MATRIX_NAME).Text = label.text;
                            Singleton<GUI>.INSTANCE.Dirty = true;
                        }
                        else if (editing)
                        {
                            textbox.HandleKeyDown(k);
                            label.text = textbox.Text;
                        }
                    },
                    Keypresshandler  = (e, k) =>
                    {
                        if (editing)
                        {
                            textbox.HandleKeyPress(k);
                            label.text = textbox.Text;
                        }
                    },
                    focuslost = (e) =>
                    {
                        if (editing)
                        {
                            editing = false;
                            edit = false;

                            label.text = textbox.Text;
                            if (string.IsNullOrEmpty(label.text))
                            {
                                label.text = previousText;
                                MessageBox.Show("Matrix names MUST contain at least one character", "Matrix Name Limits");
                            }

                            label.color = System.Drawing.Color.White;
                            Singleton<GUI>.INSTANCE.Dirty = true;

                            int index = labels.IndexOf(label);
                            int offset = index + offsetindex;

                            QbModel m = Singleton<QbManager>.INSTANCE.ActiveModel;

                            m.matrices[offset].name = label.text;
                            Singleton<GUI>.INSTANCE.Get<TextBox>(GUIID.ACTIVE_MATRIX_NAME).Text = label.text;
                        }

                    }
                };

                if (i == 0)
                {
                    background.Enable = true;
                    bg.color = Color4.Black;
                    background.SetBoundsNoScaling(Absolute_X, label.Absolute_Y, size.X, label.size.Y - (4f).ScaleVerticlSize());

                }

                label.StatusText = stonevox.StatusText.label_matrixlistbox;

                labels.Add(label);

                ToggleButton button = new ToggleButton("./data/images/qb_matrix_visibility_on.png", 
                                                       "./data/images/qb_matrix_visibility_off.png");
                button.Parent = this;
                button.SetBoundsNoScaling(Absolute_X + size.X * .01f, previouseY+fontheight*.02f, size.X*.05f, fontheight);

                button.handler = new WidgetEventHandler()
                {
                    mousedownhandler = (e, mouse) =>
                    {
                        if (mouse.IsPressed && mouse.Button == MouseButton.Left)
                        {
                            int index = buttons.IndexOf(button);
                            int offset = index + offsetindex;

                            QbModel m = Singleton<QbManager>.INSTANCE.ActiveModel;
                            m.matrices[offset].Visible = !m.matrices[offset].Visible;
                        }
                    },
                    mouseenter = (e) =>
                    {
                        int index = labels.IndexOf(label);
                        int offset = index + offsetindex;

                        QbModel m = Singleton<QbManager>.INSTANCE.ActiveModel;

                        m.matrices[offset].highlight = new Colort(1.5f, 1.5f, 1.5f);
                    },
                    mouseleave = (e) =>
                    {
                        int index = labels.IndexOf(label);
                        int offset = index + offsetindex;

                        QbModel m = Singleton<QbManager>.INSTANCE.ActiveModel;

                        m.matrices[offset].highlight = new Colort(1f, 1f, 1f);
                    }
                };

                button.StatusText = stonevox.StatusText.button_matrixvisibiliy;

                buttons.Add(button);
                previouseY -= fontheight; // + (labelspacing).ScaleVerticlSize();

                if (hasmodel)
                {
                    if (labeltext == "")
                    {
                        label.Enable = false;
                        button.Enable = false;
                        button.MaintainEnabled = true;
                    }
                    else
                    {
                        if (model.matrices[i].Visible)
                            button.Toggle(0);
                        else
                            button.Toggle(1);
                    }
                }
                else
                {
                    label.Enable = false;
                    button.Enable = false;
                    button.MaintainEnabled = true;
                }
            }

            int matrixcount = 10;
            if (hasmodel)
                matrixcount = model.matrices.Count;

            scrollbar = new VerticalScrollbar((size.X * .055f).UnScaleHorizontalSize(),
                                               size.Y.UnScaleVerticlSize(),
                                               matrixcount);
            scrollbar.fontHeight = size.Y / linecount;
            scrollbar.UpdateScrollbar();
            scrollbar.Parent = this;
            scrollbar.SetBoundsNoScaling(Absolute_X + size.X - scrollbar.size.X, Absolute_Y);

            scrollbar.handler = new WidgetEventHandler()
            {
                scrollbarchanged = (e, value, delta) =>
                {
                    offsetindex = (int)value;
                    UpdateWidgets();

                    if (delta > 0)
                    {
                        background.location.Y += (fontheight /*+ (labelspacing).ScaleVerticlSize()*/) * (int)delta;
                    }
                    else
                    {
                        background.location.Y -= (fontheight /* + (labelspacing).ScaleVerticlSize()*/) * -(int)delta;
                    }

                    bool backgroundOutofBounds = true;
                    if (background.Absolute_Y > Absolute_Y + size.Y- fontheight*.5f)
                        backgroundOutofBounds = false;
                    else if (background.Absolute_Y < Absolute_Y)
                        backgroundOutofBounds = false;
                    background.Enable = backgroundOutofBounds;
                }
            };
        }

        public override void Update(FrameEventArgs e)
        {
            base.Update(e);

            //if (!edit && lastover != null && input.Keydown(Key.F2))
            //{
            //    lastover.HandleMouseDown(new MouseButtonEventArgs(0, 0, MouseButton.Left, true));
            //    editinglable = lastover;
            //    textbox.Text = lastover.text;
            //    lastover.customData["edit"] = true;
            //    edit = true;
            //    Singleton<ClientGUI>.INSTANCE.Dirty = true;
            //}

            if (edit)
            {
                textbox.Update(e);
                string text = textbox.Text + textbox.AppendedText;

                if (text != editinglable.text)
                    editinglable.text = text;
            }
        }

        public void UpdateWidgets()
        {
            QbModel model = Singleton<QbManager>.INSTANCE.ActiveModel;
            for (int i = 0; i < labels.Count; i++)
            {
                Label label = labels[i];
                ToggleButton button = buttons[i];

                string labeltext = i + offsetindex < model.matrices.Count ? model.matrices[i + offsetindex].name : "";
                if (labeltext == "")
                {
                    label.Enable = false;
                    button.MaintainEnabled = false;
                    button.Enable = false;
                    button.MaintainEnabled = true;
                    continue;
                }
                else
                {
                    label.Enable = true;
                    label.text = labeltext;

                    button.MaintainEnabled = false;
                    button.Enable = true;
                    button.MaintainEnabled = true;
                    if (model.matrices[i + offsetindex].Visible)
                        button.Toggle(0);
                    else
                        button.Toggle(1);
                }
            }
        }

        public void AddWidgets(GUI gui)
        {
            gui.widgets.Add(this);
            gui.widgets.Add(background);

            gui.widgets.AddRange(labels);
            gui.widgets.AddRange(buttons);

            gui.widgets.Add(scrollbar);
            gui.widgets.Add(scrollbar.button);
        }

        public override void HandleMessageRecieved(BroadcastMessage message)
        {
            if (message.messgae == Message.ModelImported)
            {
                Refresh();
            }
            else if (message.messgae == Message.ActiveModelChanged)
            {
                offsetindex = 0;
                lastActiveIndex = 0;
            }
            else if (message.messgae == Message.ActiveMatrixChanged)
            {
                Select((int)message.args[1]);
            }
            base.HandleMessageRecieved(message);
        }

        public void Select(int index)
        {
            if (index < offsetindex + linecount && index >= offsetindex)
            {
                int nindex = index - offsetindex;
                background.Enable = true;
                background.SetBoundsNoScaling(Absolute_X, labels[nindex].Absolute_Y, size.X, labels[nindex].size.Y - (4f).ScaleVerticlSize());
                UpdateWidgets();
            }
            else
            {
                QbModel model = Singleton<QbManager>.INSTANCE.ActiveModel;
                int max = model.matrices.Count - linecount;
                int nindex = Math.Min(max, index);

                offsetindex = nindex;

                int bindex = 0;

                if (index > offsetindex)
                {
                    bindex = index - offsetindex;
                }

                background.Enable = true;
                background.SetBoundsNoScaling(Absolute_X, labels[bindex].Absolute_Y, size.X, labels[bindex].size.Y - (4f).ScaleVerticlSize());
                UpdateWidgets();
            }

            scrollbar.SetValue(offsetindex);
            Singleton<GUI>.INSTANCE.Dirty = true;
        }

        public void Refresh()
        {
            offsetindex = 0;
            QbModel model = Singleton<QbManager>.INSTANCE.ActiveModel;
            scrollbar.UpdateScrollbar(model.matrices.Count);
            UpdateWidgets();
            background.Enable = true;
            background.SetBoundsNoScaling(Absolute_X, labels[0].Absolute_Y, size.X, labels[0].size.Y - (4f).ScaleVerticlSize());

            Singleton<GUI>.INSTANCE.Dirty = true;
        }
    }
}
