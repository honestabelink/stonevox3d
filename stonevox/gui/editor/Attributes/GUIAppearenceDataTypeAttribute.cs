using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public class GUIAppearenceDataTypeAttribute : Attribute
    {
        public Type Type;

        public GUIAppearenceDataTypeAttribute(Type Type)
        {
            this.Type = Type;
        }

        public override string ToString()
        {
            return Type.ToString();
        }
    }
}
