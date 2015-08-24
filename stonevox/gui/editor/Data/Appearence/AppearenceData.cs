using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public abstract class AppearenceData
    {
        public string Name { get; set; }
        public bool Enabled { get; set; }

        public AppearenceData()
        {
            Name = "";
        }

        public abstract Appearence ToAppearence();
    }
}
