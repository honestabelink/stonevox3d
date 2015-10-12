using OpenTK.Graphics;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using OpenTK.Input;

namespace stonevox
{
    public class ToggleButton : Widget
    {
        Picture backgroundImage;
        Picture highlightBackgroundImage;

        PlainBackground background;
        PlainBackground highlightBackground;

        ButtonType Type;

        //super hacks
        // issue with the matirx list box forcing things to become visible
        public bool MaintainEnabled;
        public override bool Enable
        {
            get
            {
                return base.Enable;
            }

            set
            {
                if (MaintainEnabled)
                    base.Enable = base.Enable;
                else
                    base.Enable = value;
            }
        }

        public ToggleButton() : base()
        {
            this.ID = GetNextAvailableID();
        }

        public ToggleButton(string backgroundImagePath, string hightlightBackgroundImagePath) : this(0, backgroundImagePath, hightlightBackgroundImagePath)
        {
            this.ID = GetNextAvailableID();
        }

        public ToggleButton(Color4 backgroundColor, Color4 highlightBackgroundColor) : this(0, backgroundColor, highlightBackgroundColor)
        {
            this.ID = GetNextAvailableID();
        }

        public ToggleButton(int id, string backgroundImagePath, string highlightBackgroundImagePath) : base(id)
        {
            Type = ButtonType.Image;
            AddAppearenceImage(backgroundImagePath, highlightBackgroundImagePath);
        }

        public ToggleButton(int id, Color4 backgroundColor, Color4 highlightBackgroundColor) : base(id)
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

        public override void HandleMouseDown(MouseButtonEventArgs e)
        {
            if (e.IsPressed && e.Button == MouseButton.Left)
            {
                switch (Type)
                {
                    case ButtonType.Image:
                        highlightBackgroundImage.Enabled = !highlightBackgroundImage.Enabled;
                        backgroundImage.Enabled = !backgroundImage.Enabled;
                        break;
                    case ButtonType.ColoredBackground:
                        highlightBackground.Enabled = !highlightBackground.Enabled;
                        background.Enabled = !background.Enabled;
                        break;
                    default:
                        break;
                }
                Singleton<ClientGUI>.INSTANCE.Dirty = true;
            }

            base.HandleMouseDown(e);
        }

        public void Toggle()
        {
            switch (Type)
            {
                case ButtonType.Image:
                    highlightBackgroundImage.Enabled = !highlightBackgroundImage.Enabled;
                    backgroundImage.Enabled = !backgroundImage.Enabled;
                    break;
                case ButtonType.ColoredBackground:
                    highlightBackground.Enabled = !highlightBackground.Enabled;
                    background.Enabled = !background.Enabled;
                    break;
                default:
                    break;
            }
        }

        public void Toggle(int visible)
        {
            if (visible == 0)
            {
                switch (Type)
                {
                    case ButtonType.Image:
                        highlightBackgroundImage.Enabled = false;
                        backgroundImage.Enabled = true;
                        break;
                    case ButtonType.ColoredBackground:
                        highlightBackground.Enabled = false;
                        background.Enabled = true;
                        break;
                    default:
                        break;
                }
            }
            else
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
            }
        }

        // wrong
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
}
