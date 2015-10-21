namespace stonevox
{
    public class PlainTextData : AppearenceData
    {
        public string Text { get; set; }
        public ColorData Color { get; set; }
        public bool AutoSize { get; set; }

        public PlainTextData()
        {
            Text = "";
            Color = new ColorData(System.Drawing.Color.White);
        }

        public PlainTextData(bool autosize, string text, ColorData color)
        {
            Text = text;
            AutoSize = autosize;
            Color = color;
        }

        public override Appearence ToAppearence()
        {
            return new PlainText(AutoSize, Text, Color);
        }
    }
}
