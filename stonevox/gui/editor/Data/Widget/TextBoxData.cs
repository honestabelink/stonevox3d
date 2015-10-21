namespace stonevox
{
    public class TextBoxData : WidgetData
    {
		public string Text { get; set; }
        public ColorData Color { get; set; }
        public int CharDisplaySize { get; set; }

        public TextBoxData() { }

        public TextBoxData(string text, ColorData color, int charDisplaySize)
        {
            Text = text;
            Color = color;
            CharDisplaySize = charDisplaySize;
        }

        public override Widget ToWidget()
        {
            TextBox textbox = new TextBox();
            textbox = textbox.FromWidgetData(this) as TextBox;
            return textbox;
        }
    }

    // dont need this much datat erere
}
