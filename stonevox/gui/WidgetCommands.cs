using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public static class WidgetCommands
    {
        public static Dictionary<string, WidgetEventHandler> handlers = new Dictionary<string, WidgetEventHandler>();

        static WidgetCommands()
        {
        }
    }
}
