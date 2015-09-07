using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using OpenTK;
using System.Drawing;
using OpenTK.Graphics;
using OpenTK.Input;

namespace stonevox
{
    [GUIWidgetName("Textbox")]
    [GUIWidgetDataType(typeof(TextBoxData))]
    public class TextBox : Widget
    {
        string text = "";
        public string Text { get { return text; } set { text = value; SetText(value); } }

        private string renderText;
        private string lastText = "";

        private PlainText textAppearence;

        private int charDisplaySize = 20;

        private Color Color { get { return textAppearence.Color; } set { textAppearence.Color = value; } }

        public TextBox() : base()
        {
            this.ID = GetNextAvailableID();
            AddAppearence();
            BaseSize(20);
        }

        public TextBox(string text, Color fontColor, int charDisplayWidth) : this(0, text, fontColor, charDisplayWidth)
        {
            this.ID = GetNextAvailableID();
        }

        public TextBox(int id, string text, Color fontColor, int charDisplayWidth) : base(id)
        {
            AddAppearence();

            Color = fontColor;
            charDisplaySize = charDisplayWidth;
            BaseSize(charDisplaySize);
            Text = text;
        }

        void AddAppearence()
        {
            appearence.AddAppearence("background", new PlainBackground(new Color4(41f / 256f, 39f / 256f, 39f / 256f,1f)));
            appearence.AddAppearence("border", new PlainBorder(3,new Color4(122f / 256f, 106f / 256f, 70f / 256f,1f)));
            textAppearence = appearence.Add<PlainText>("plaintext");
            textAppearence.AutoSize = false;
            textAppearence.Color = Color.White;
        }

        void BaseSize(int size)
        {
            string matchSize = "O";

            for (int i = 0; i < size - 1; i++)
            {
                matchSize += "k";
            }

            SizeF s = PlainText.MeasureString(matchSize);
            this.SetBounds(null, null, s.Width, s.Height);
        }

        void SetText(string text)
        {
            renderText = text;
            textAppearence.Text = text;

            float unscaledWidth = (this.size.X).UnScaleHorizontalSize(); 

            while(PlainText.MeasureString(renderText).Width > unscaledWidth)
            {
                renderText = renderText.Remove(0, 1);
            }

            textAppearence.Text = renderText;
        }

        public override void HandleKeyPress(KeyPressEventArgs e)
        {
            if (handler.textboxfilter != null)
                Text += handler.textboxfilter(this, e.KeyChar.ToString());
            else
                Text += e.KeyChar.ToString();
            HandleTextChanged();
            base.HandleKeyPress(e);
        }

        // opentk doesn't pass backspace,eneter, etc in press >_>
        public override void HandleKeyDown(KeyboardKeyEventArgs e)
        {
            if (e.Key == Key.BackSpace)
            {
                if (Text.Length > 0)
                {
                    Text = Text.Remove(text.Length -1, 1);
                    HandleTextChanged();
                }
            }
            else if (e.Key == Key.Enter || e.Key == Key.KeypadEnter)
            {
                if (Text != lastText)
                {
                    HandleTextCommit();
                    lastText = Text;
                }
            }
            base.HandleKeyDown(e);
        }

        public override void HandleFocusedGained()
        {
            appearence.Get<PlainBorder>("border").color = new Color4(164f / 256f, 146f / 256f, 110f / 256f, 1f);

            base.HandleFocusedGained();
        }

        public override void HandleFocusedLost()
        {
            appearence.Get<PlainBorder>("border").color = new Color4(122f / 256f, 106f / 256f, 70f / 256f, 1f);

            HandleTextCommit();
            base.HandleFocusedLost();
        }

        public void HandleTextChanged()
        {
            if (handler.textboxtextchange != null)
                handler.textboxtextchange(this);

            Singleton<ClientGUI>.INSTANCE.Dirty = true;
        }

        public void HandleTextCommit()
        {
            if (handler.textboxtextcommit != null)
                handler.textboxtextcommit(this);

            Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.TextboxTextCommited, this, text);
        }

        public override Widget FromWidgetData(WidgetData data)
        {
            this.data = data;
            TextBoxData _data = data as TextBoxData;
            //TextBox textbox = new TextBox(data.ID, _data.Text, _data.Color, _data.CharDisplaySize);
            //return textbox;

            Color = _data.Color;
            Text = _data.Text;
            charDisplaySize = _data.CharDisplaySize;

            BaseSize(charDisplaySize);

            return this;
        }

        public override WidgetData ToWidgetData()
        {
            return new TextBoxData(Text, Color, charDisplaySize);
        }
    }
}
