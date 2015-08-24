using OpenTK.Graphics;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public class PlainBorderData : AppearenceData
    {
        public float BorderWidth { get; set; }
        public ColorData Color { get; set; }

        public PlainBorderData()
        {
            Color = new ColorData();
            BorderWidth = 1f;
        }

        public override Appearence ToAppearence()
        {
            return new PlainBorder(BorderWidth, Color);
        }
    }
}
