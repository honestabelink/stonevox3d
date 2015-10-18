using OpenTK.Graphics;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public class QbModelTab : Widget
    {
        public string Text { get { return label.text; } set { label.text = value; } }
        public string SelectedSizeText { get { return label.text + "     "; } }
        public string SizeText { get { return  label.text + "    "; } }

        Label label;
        Label closeButton;

        float vBorderOffset;
        float hBorderOffset;
        float height;

        float borderscale = 2.5f;

        PlainBackground background;
        PlainBorder border;

        public QbModel model;

        public QbModelTab(QbModel model)
            : base()
        {
            this.model = model;
            height = Client.window.Qfont.fontData.maxGlyphHeight;
            height = height.ScaleVerticlSize();

            vBorderOffset = -(3f).ScaleVerticlSize();
            hBorderOffset = (3f).ScaleHorizontalSize();

            label = new Label(GetNextAvailableID(), model.name, System.Drawing.Color.White, true);
            closeButton = new Label(GetNextAvailableID(), "x", System.Drawing.Color.DarkRed, true);
            var size = label.MeasureText(label.text + "    ");

            closeButton.Parent = this;
            label.Parent = this;

            closeButton.handler = new WidgetEventHandler()
            {
                mouseenter = (e) =>
                {
                    closeButton.color = Color.Red;
                },
                mouseleave= (e) =>
                {
                    closeButton.color = Color.DarkRed;
                },
                mousedownhandler = (e, mouse) =>
                {
                    if (mouse.IsPressed && mouse.Button == OpenTK.Input.MouseButton.Left)
                    {
                        Singleton<QbManager>.INSTANCE.RemoveModel(this.model);
                    }
                }
            };

            SetBounds(null, null, size.Width, height.UnScaleVerticlSize() * borderscale * 1.5f);
            closeButton.SetBoundsNoScaling(Absolute_X + this.size.X - closeButton.size.X * 1.5f, Absolute_Y + closeButton.size.Y * .9f);

            background = appearence.AddAppearence("background", new PlainBackground(new Color4(100 - 10, 87 - 10, 61 - 10, 255)));
            border = appearence.AddAppearence("border", new PlainBorder(4, new Color4(122 - 10, 106 - 10, 70 - 10, 255)));
        }

        public void SetSelected(bool sselected,ref  float startX)
        {
            Enable = true;

            label.text = model.name;

            if (sselected)
            {
                var size = label.MeasureText(SelectedSizeText);
                SetBounds(null, null, size.Width, height.UnScaleVerticlSize() * borderscale * 1.5f);

                SetBoundsNoScaling(startX, 1 - this.size.Y);

                label.SetBoundsNoScaling(Absolute_X + height *.5f,
                                         Absolute_Y + this.size.Y * .5f - height);

                background.Enabled = true;
                border.Enabled = true;
                label.Enable = true;
                closeButton.Enable = true;
            }
            else
            {
                var size = label.MeasureText(SizeText);
                SetBounds(null, null, size.Width, height.UnScaleVerticlSize() * borderscale*1.5f);

                SetBoundsNoScaling(startX, 1 - this.size.Y);

                label.SetBoundsNoScaling(Absolute_X + height*.5f,
                                         Absolute_Y + this.size.Y *.5f - height*.5f);

                background.Enabled = false;
                border.Enabled = false;
                label.Enable = true;
                closeButton.Enable = true;
            }

            closeButton.SetBoundsNoScaling(Absolute_X + size.X - closeButton.size.X * 1.5f, Absolute_Y + closeButton.size.Y *.9f);

            startX += size.X;
            if (startX > 1)
                startX = -2;
        }

        public SizeF GetBounds()
        {
            return new SizeF();
        }

        public void AddWidgets(ClientGUI gui)
        {
            gui.widgets.Add(this);
            gui.widgets.Add(label);
            gui.widgets.Add(closeButton);

            // hacks
            label.customData.Add("qbmodeltab", this);
            label.handler = this.handler;
        }
    }
}
