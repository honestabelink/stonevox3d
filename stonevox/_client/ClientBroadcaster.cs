using OpenTK.Input;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public enum Message
    {
        WidgetEnable,
        WidgetMouseDown,
        WidgetMouseUp,
        WidgetMouseScroll,
        WidgetMouseEnter,
        WidgetMouseLeave,
        WidgetMouseOver,
        WidgetMouseMove,
        WidgetKeyPress,
        WidgetFocus,
        WidgetFocusLost,
        WidgetStartTranslation,
        WidgetEndTranslation,

        ColorSelectionChanged,
        ColorSelectionUpdate,
        ColorSelectionCommit,

        WindowOpened,
        WindowClosed,

        StatusStripUpdate,
    }

    // allows widgets or others to handle the broadcast and stop it from carring further
    public enum BroadcastMessageReturn
    {
        Funnel,
        Stop
    }

    public class BroadcastMessage
    {
        public Message messgae;
        public Widget widget;
        public object[] args;

        public bool HasWidget { get { return widget != null; } }

        public T Arg<T>(int index)
        {
            return (T)args[index];
        }
    }

    public delegate void BroadcastMessageHandler(Message message, Widget windget, object[] args);

    public class ClientBroadcaster : Singleton<ClientBroadcaster>
    {
        ClientGUI gui;

        public List<BroadcastMessageHandler> handlers;

        public ClientBroadcaster()
             : base()
        {
            handlers = new List<BroadcastMessageHandler>();
        }

        public void SetGUI(ClientGUI gui)
        {
            this.gui = gui;
        }

        public void Broadcast(Message message, params object[] args)
        {
            Broadcast(message, null, args);
        }

        public void Broadcast(Message message, Widget widget, params object[] args)
        {
            gui.Dirty = true;
            BroadcastMessage m = new BroadcastMessage()
            {
                messgae = message,
                widget = widget,
                args = args
            };

            handlers.ForEach(t => t(message, widget, args));

            foreach (var guiWidget in gui.widgets)
            {
                if (m.HasWidget)
                {
                    if (guiWidget.ID != m.widget.ID)
                        guiWidget.HandleMessageRecieved(m);
                }
                else
                    guiWidget.HandleMessageRecieved(m);
            }
        }
    }
}
