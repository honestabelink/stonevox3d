using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public class ButtonData : WidgetData
    {
        public ButtonType Type { get; set; }

        public ButtonData(ButtonType Type)
        {
            this.Type = Type;
        }

        public override Widget ToWidget()
        {
            Button button = new Button();
            return button.FromWidgetData(this) as Button;
        }
    }
}
