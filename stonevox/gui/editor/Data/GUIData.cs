using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public class GUIData
    {
        public List<WidgetData> widgets { get; set; }

        public GUIData()
        {
            widgets = new List<WidgetData>();
        }
    }
}
