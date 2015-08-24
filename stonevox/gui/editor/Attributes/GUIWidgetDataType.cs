using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public class GUIWidgetDataType : Attribute
    {
        public Type Type;

        public GUIWidgetDataType(Type Type)
        {
            this.Type = Type;
        }

        public override string ToString()
        {
            return Type.ToString();
        }
    }
}
