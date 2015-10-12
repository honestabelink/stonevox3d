using OpenTK.Graphics;
using QuickFont;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    [GUIWidgetName("Label")]
    [GUIWidgetDataType(typeof(WidgetData))]
    public class Label : Widget
    {
        public string text { get { return textAppearence.Text; } set { textAppearence.Text = value; } }
        public Color color { get { return textAppearence.Color; } set { textAppearence.Color = value; } }
        public QFontAlignment TextAlignment { get { return textAppearence.Alignment; } set { textAppearence.Alignment = value; } }

        private PlainText textAppearence;

        public Label()
            : base()
        {
            textAppearence = new PlainText(true, "", Color.White);
        }

        public Label(string text)
            : this(-1, text, Color.White, true)
        {
        }

        public Label(string text, Color color)
            : this(-1, text, color, true)
        {
        }

        public Label(int id, string text, Color color, bool autoresize)
            : base(id)
        {
            textAppearence = appearence.Add<PlainText>("text");
            textAppearence.Color = color;
            textAppearence.Text = text;
            textAppearence.AutoSize = autoresize;
            this.text = text;
            this.color = color;

        }

        public SizeF MeasureText()
        {
            return textAppearence.MesaureString();
        }

        public SizeF MeasureText(string text)
        {
            return textAppearence.MesaureString(text);
        }
    }
}
