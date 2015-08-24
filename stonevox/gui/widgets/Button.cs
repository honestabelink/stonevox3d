using OpenTK.Graphics;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    [GUIWidgetName("Button")]
    [GUIWidgetDataType(typeof(ButtonData))]
    public class Button : Widget
    {
        Picture backgroundImage;
        Picture highlightBackgroundImage;

        PlainBackground background;
        PlainBackground highlightBackground;

        ButtonType Type;

        public Button() : base()
        {
            this.ID = GetNextAvailableID();
        }

        public Button(string backgroundImagePath, string hightlightBackgroundImagePath) : this(0, backgroundImagePath,hightlightBackgroundImagePath)
        {
            this.ID = GetNextAvailableID();
        }

        public Button(Color4 backgroundColor, Color4 highlightBackgroundColor) : this(0, backgroundColor, highlightBackgroundColor)
        {
            this.ID = GetNextAvailableID();
        }

        public Button(int id, string backgroundImagePath, string highlightBackgroundImagePath) : base(id)
        {
            Type = ButtonType.Image;
            AddAppearenceImage(backgroundImagePath, highlightBackgroundImagePath);
        }

        public Button(int id, Color4 backgroundColor, Color4 highlightBackgroundColor) : base(id)
        {
            Type = ButtonType.ColoredBackground;
            AddAppearenceColored(backgroundColor, highlightBackgroundColor);
        }

        void AddAppearenceImage(string backgroundImagePath, string highlightBackgroundImagePath)
        {
            if (highlightBackground != null)
            {
                background.Enabled = false;
                highlightBackground.Enabled = false;
            }

            if (highlightBackgroundImage != null)
            {
                highlightBackgroundImage.SetImage(highlightBackgroundImagePath);
                backgroundImage.SetImage(backgroundImagePath);
                backgroundImage.Enabled = true;
                backgroundImage.MatchTextureBounds(this);
                return;
            }

            highlightBackgroundImage = appearence.AddAppearence<Picture>("highlight", new Picture(highlightBackgroundImagePath));
            highlightBackgroundImage.Enabled = false;

            backgroundImage = appearence.AddAppearence<Picture>("picture", new Picture(backgroundImagePath));

            backgroundImage.MatchTextureBounds(this);
        }

        void AddAppearenceColored(Color4 backgroundColor, Color4 highlightBackgroundColor)
        {
            if (backgroundImage != null)
            {
                backgroundImage.Enabled = false;
                highlightBackgroundImage.Enabled = false;
            }

            if (highlightBackground != null)
            {
                highlightBackground.color = highlightBackgroundColor;
                background.color = backgroundColor;
                background.Enabled = true;
                return;
            }

            highlightBackground = appearence.AddAppearence<PlainBackground>("highlightbackground", new PlainBackground(highlightBackgroundColor));
            highlightBackground.Enabled = false;

            background = appearence.AddAppearence<PlainBackground>("background", new PlainBackground(backgroundColor));
        }

        public override void HandleMouseEnter()
        {
            switch (Type)
            {
                case ButtonType.Image:
                    highlightBackgroundImage.Enabled = true;
                    backgroundImage.Enabled = false;
                    break;
                case ButtonType.ColoredBackground:
                    highlightBackground.Enabled = true;
                    background.Enabled = false;
                    break;
                default:
                    break;
            }

            base.HandleMouseEnter();
        }

        public override void HandleMouseLeave()
        {
            switch (Type)
            {
                case ButtonType.Image:
                    highlightBackgroundImage.Enabled = false;
                    backgroundImage.Enabled = true;
                    break;
                case ButtonType.ColoredBackground:
                    background.Enabled = true;
                    highlightBackground.Enabled = false;
                    break;
                default:
                    break;
            }

            base.HandleMouseLeave();
        }

        public override void Render()
        {
            base.Render();
        }

        public override Widget FromWidgetData(WidgetData data)
        {
            ButtonData _data = data as ButtonData;
            Type = _data.Type;
            return this;
        }

        public override WidgetData ToWidgetData()
        {
            return new ButtonData(Type);
        }
    }

    public enum ButtonType
    {
        Image,
        ColoredBackground
    }
}
