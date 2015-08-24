using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    [GUIWidgetName("Emptz")]
    [GUIWidgetDataType(typeof(WidgetData))]
    public class EmptyWidget : Widget
    {
        public EmptyWidget() : base()
        {
            this.ID = GetNextAvailableID();
        }

        public EmptyWidget(int id) : base(id)
        {
        }
    }
}
