using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using OpenTK.Graphics;
using System.ComponentModel;

namespace stonevox
{
    public class PlainBackgroundData : AppearenceData
    {
        public ColorData Color { get; set; }

        public PlainBackgroundData()
        {
            Color = new ColorData();
        }

        public override Appearence ToAppearence()
        {
            return new PlainBackground(Color);
        }
    }
}
