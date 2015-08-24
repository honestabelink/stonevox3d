using OpenTK.Graphics;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public class VerticalScrollbar : Widget
    {
        public Button button;

        private float maxValue = 25;
        private float barStep;
        private float valueStep;
        private float startValue;
        private float maxTextShow;
        private float value;

        public VerticalScrollbar() : base()
        {
            this.ID = GetNextAvailableID();
            SetBounds(0, 0, 40, 250);
            AddAppearence();
        }

        public VerticalScrollbar(float width, float height, float maxvalue) : this(0, width, height, maxvalue)
        {
            this.ID = GetNextAvailableID();
        }

        public VerticalScrollbar(int id, float width, float height, float maxvalue) : base(id)
        {
            maxValue = maxvalue;
            SetBounds(null, null, width, height);
            AddAppearence();
        }

        void AddAppearence()
        {
            appearence.AddAppearence<PlainBackground>("background", new PlainBackground(Color4.DarkGray));
            appearence.AddAppearence<PlainBorder>("border", new PlainBorder(1.5f, Color4.Yellow));

            button = new Button(Color4.Gray, Color4.LightGray);
            button.handler = new WidgetEventHandler()
            {
                mousemovehandler = (widget, e) =>
                {
                    widget.location.Y += Scale.vSizeScale(e.YDelta) * -2f;

                    if (widget.location.Y > widget.Parent.size.Y - widget.size.Y)
                    {
                        widget.location.Y = widget.Parent.size.Y - widget.size.Y;
                    }
                    else if (widget.Absolute_Y < widget.Parent.Absolute_Y)
                    {
                        widget.location.Y = 0;
                    }

                    value =
                            Math.Abs((float)Scale.scale(widget.location.Y, 0, widget.Parent.size.Y - widget.size.Y, startValue,
                                    maxValue) - (startValue + (maxValue - maxTextShow)));
                }
            };

            button.Parent = this;
            button.size.X = this.size.X * 1.3f;

            UpdateScrollbar(maxValue);
        }

        public override void SetBounds(float? x, float? z, float? width, float? height)
        {
            base.SetBounds(x, z, width, height);

            if ((width.HasValue||height.HasValue) && button != null)
            {
                UpdateScrollbar(maxValue);
            }
        }

        public override void SetBoundsNoScaling(float? x, float? z, float? width, float? height)
        {
            base.SetBoundsNoScaling(x, z, width, height);

            if (width.HasValue || height.HasValue)
            {
                UpdateScrollbar(maxValue);
            }
        }

        void UpdateScrollbar(float maxvalue)
        {
            this.maxValue = maxvalue;
            float fontHeight = (float)Scale.vSizeScale(Client.window.Qfont.fontData.maxGlyphHeight);
            maxTextShow =  size.Y / fontHeight;

            this.barStep = size.Y / 100f;
            this.valueStep = this.maxValue / 100f;

            float maxShow = fontHeight * maxTextShow;
            float totalShow = fontHeight * maxvalue;

            float percentShown = maxShow / totalShow;

            if (maxShow < totalShow)
                button.size.Y = size.Y * percentShown;
            else
                button.size.Y = size.Y;

            startValue = (float)Scale.scale(button.size.Y, 0, size.Y, 0f, maxValue);

            button.SetBounds(Scale.hUnPosScale(Absolute_X) - Scale.hUnSizeScale(button.size.X) / 16f, Scale.vUnPosScale(Absolute_Y) + ((Scale.vUnSizeScale(this.size.Y) - Scale.vUnSizeScale(button.size.Y)) /2f), null, null);
        }
    }
}
