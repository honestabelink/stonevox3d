using OpenTK;
using OpenTK.Graphics.OpenGL;
using QuickFont;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace stonevox
{
    [GUIAppearenceName("Plain Text")]
    [GUIAppearenceDataType(typeof(PlainTextData))]
    public class PlainText : Appearence
    {
        public static SizeF MeasureString(string text)
        {
            SizeF _return = Client.window.Qfont.Measure(text);
            _return.Width *= 2f;
            _return.Height *= 2f;

            return _return;
        }

        public override bool Enabled
        {
            get
            {
                return base.Enabled;
            }

            set
            {
                base.Enabled = value;
            }
        }

        public bool AutoSize;
        public string AppendText = "";

        private string text = "";
        private Color color = Color.White;
        public QFontAlignment Alignment;
        public Color Color { get { return color; } set { color = value; } }
        public string Text { get { return text; } set { text = value; ResizeWidget(); } }

        public PlainText()
        {
        }
        
        public PlainText(bool autoSize, string text, Color color)
            :this(autoSize, text, color, QFontAlignment.Left)
        {
        }

        public PlainText(bool autoSize, string text, Color color, QFontAlignment alignment)
        {
            this.AutoSize = autoSize;
            this.text = text;
            this.Color = color;
            this.Alignment = alignment;
        }

        public override void Initialize()
        {
        }

        public override void Render(float x, float y, float width, float height)
        {
            float xx = x.UnScaleHorizontal();
            float yy = Client.window.Height - Scale.vUnPosScale(y) - height.UnScaleVerticlSize()*.50f;

            QFont.Begin();
            Client.window.Qfont.Options.Colour = color;
            Client.window.Qfont.Print(text + AppendText, Alignment, new Vector2(xx, yy));
            QFont.End();
        }

        void ResizeWidget()
        {
            if (AutoSize)
                MatchWidgetToBounds(Owner);
        }

        public void MatchWidgetToBounds(Widget widget)
        {
            var size = Client.window.Qfont.Measure(text);

            widget.SetBounds(null, null, size.Width*2f , size.Height*2f);
        }

        public SizeF MesaureString()
        {
            return MeasureString(this.text);
        }

        public SizeF MesaureString(string text)
        {
            var size = Client.window.Qfont.Measure(text);
            size.Width *= 2f;
            size.Height *= 2f;
            return size;
        }

        public override Appearence FromData(AppearenceData data)
        {
            PlainTextData _data = data as PlainTextData;
            return new PlainText(_data.AutoSize, _data.Text, _data.Color);
        }
        public override AppearenceData ToData()
        {
            return new PlainTextData(AutoSize, text, color);
        }
    }
}
