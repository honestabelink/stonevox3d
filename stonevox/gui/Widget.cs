using OpenTK;
using OpenTK.Input;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public delegate void WidgetKeyHandler(Widget widget, KeyboardKeyEventArgs e);
    public delegate void WidgetKeyPressHandler(Widget widget, KeyPressEventArgs e);
    public delegate void WidgetMouseHandler(Widget widget, MouseButtonEventArgs e);
    public delegate void WidgetMouseMoveHandler(Widget widget, MouseMoveEventArgs e);
    public delegate void WidgetMouseWheelHandler(Widget widget, MouseWheelEventArgs e);

    public delegate void WidgetBroadcastMessageHandler(Widget widget, Message m, Widget w, object[] args);

    public delegate void WidgetAction(Widget widget);

    public delegate string TextFilterAction(TextBox widget, string inputValue);

    public class WidgetEventHandler
    {
        public WidgetKeyHandler Keydownhandler;
        public WidgetKeyHandler Keyuphandler;
        public WidgetKeyPressHandler Keypresshandler;
        public WidgetMouseHandler mousedownhandler;
        public WidgetMouseHandler mouseuphandler;
        public WidgetMouseMoveHandler mousemovehandler;
        public WidgetMouseWheelHandler mousewheelhandler;
        public WidgetAction mouseenter;
        public WidgetAction mouseleave;
        public WidgetAction mouseover;
        public WidgetAction focusgained;
        public WidgetAction focuslost;
        public WidgetBroadcastMessageHandler messagerecived;

        public WidgetAction textboxtextcommit;
        public WidgetAction textboxtextchange;
        public TextFilterAction textboxfilter;
    }

    public class WidgetTranslation
    {
        public string name { get; set; }

        public Vector2 destination;

        // in seconds
        public float translationTime { get; set; }
        public Vector2 Destination { get { return destination; } set { destination = value; } }

        public float time;

        public WidgetTranslation()
        {
            name = "";
        }
    }

    public abstract class Widget
    {
        private bool enabled;
        private Widget parent;

        public int ID;
        public string Name;
        public WidgetEventHandler handler;

        public Vector2 location;
        public Vector2 size;
        public WidgetAppearence appearence;

        public Widget Parent { get { return parent; } set { parent = value;  parent.children.Add(this); Enable = parent.Enable; } }

        public float Absolute_X { get { return DetermineLocationX(); } }
        public float Absolute_Y { get { return DetermineLocationY(); } }
        public float Width { get { return size.X; } set { size.X = value; } }
        public float Height { get { return size.Y; } set { size.Y = value; } }

        public bool Drag;
        public bool Enable {  get { return enabled; } set { enabled = value; UpdateEnable(value); } }

        public WidgetTranslation translation;

        private List<WidgetTranslation> translations;

        public WidgetData data;

        public Dictionary<string, object> customData = new Dictionary<string, object>();

        public List<Widget> children = new List<Widget>();

        public MouseCursor cursor;

        void UpdateEnable(bool value)
        {
            children.ForEach(t => t.Enable = value);
        }

        float DetermineLocationX()
        {
            if (Parent == null)
                return location.X;
            else
            {
                float parentX = 0;
                return RecursiveParentLocationX(Parent, ref parentX) + location.X;
            }
        }

        float RecursiveParentLocationX(Widget widget, ref float value)
        {
            if (widget.Parent == null)
                return value + widget.location.X;
            else
            {
                value += widget.location.X;
                return (RecursiveParentLocationX(widget.Parent, ref value));
            }
        }

        float DetermineLocationY()
        {
            if (Parent == null)
                return location.Y;
            else
            {
                float parentz = 0;
                return RecursiveParentLocationz(Parent, ref parentz) + location.Y;
            }
        }

        float RecursiveParentLocationz(Widget widget, ref float value)
        {
            if (widget.Parent == null)
                return value + widget.location.Y;
            else
            {
                value += widget.location.Y;
                return (RecursiveParentLocationz(widget.Parent, ref value));
            }
        }

        public Widget()
        {
            Enable = true;
            ID = -1;
            handler = new WidgetEventHandler();
            appearence = new WidgetAppearence(this);
            translations = new List<WidgetTranslation>();
        }

        public Widget(int id) : this()
        {
            ID = id;
        }

        public virtual void HandleKeyDown(KeyboardKeyEventArgs e)
        {
            handler.Keydownhandler?.Invoke(this, e);

        }

        public virtual void HandleKeyUp(KeyboardKeyEventArgs e)
        {
            handler.Keyuphandler?.Invoke(this, e);
        }

        public virtual void HandleKeyPress(KeyPressEventArgs e)
        {
            handler.Keypresshandler?.Invoke(this, e);

            Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.WidgetKeyPress, this, e);
        }

        public virtual void HandleMouseMove(MouseMoveEventArgs e)
        {
            handler.mousemovehandler?.Invoke(this, e);

            Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.WidgetMouseMove, this, e);
        }

        public virtual void HandleMouseDown(MouseButtonEventArgs e)
        {
            Drag = true;

            handler.mousedownhandler?.Invoke(this, e);

            Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.WidgetMouseDown, this, e);
        }

        public virtual void HandleMouseUp(MouseButtonEventArgs e)
        {
            handler.mouseuphandler?.Invoke(this, e);

            Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.WidgetMouseUp, this, e);
        }

        public virtual void HandleMouseWheel(MouseWheelEventArgs e)
        {
            handler.mousewheelhandler?.Invoke(this, e);

            Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.WidgetMouseScroll, this, e);
        }

        public virtual void HandleMouseEnter()
        {
            handler.mouseenter?.Invoke(this);

            Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.WidgetMouseEnter, this);
        }

        public virtual void HandleMouseLeave()
        {
            handler.mouseleave?.Invoke(this);

            Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.WidgetMouseLeave, this);
        }

        public virtual void HandleMouseOver()
        {
            handler.mouseover?.Invoke(this);

            Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.WidgetMouseOver, this);
        }

        public virtual void HandleFocusedGained()
        {
            handler.focusgained?.Invoke(this);

            Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.WidgetFocus, this);
        }

        public virtual void HandleFocusedLost()
        {
            handler.focuslost?.Invoke(this);

            Singleton<ClientBroadcaster>.INSTANCE.Broadcast(Message.WidgetFocusLost, this);
        }

        public virtual void HandleMessageRecieved(BroadcastMessage message)
        {
            handler.messagerecived?.Invoke(this, message.messgae, message.widget, message.args);
        }

        public virtual void SetBounds(float? x, float? z, float? width = null, float? height = null)
        {
            float parentX = Parent == null ? 0 : Parent.Absolute_X;
            float parentz = Parent == null ? 0 : Parent.Absolute_Y;

            if (x.HasValue)
                location.X = Scale.hPosScale(x.Value) - parentX;

            if (z.HasValue)
                location.Y = Scale.vPosScale(z.Value) - parentz;

            if (width.HasValue)
                size.X = Scale.hSizeScale(width.Value);

            if (height.HasValue)
                size.Y = Scale.vSizeScale(height.Value);
        }

        public virtual void SetBoundsNoScaling(float? x, float? z, float? width = null, float? height = null)
        {
            float parentX = Parent == null ? 0 : Parent.Absolute_X;
            float parentz = Parent == null ? 0 : Parent.Absolute_Y;

            if (x.HasValue)
                location.X = x.Value - parentX;

            if (z.HasValue)
                location.Y = z.Value - parentz;

            if (width.HasValue)
                size.X = width.Value;

            if (height.HasValue)
                size.Y = height.Value;
        }

        public void Update(FrameEventArgs e)
        {
            if (translation != null)
            {
                Vector2 newLocation;
                translation.time += (float)e.Time;
                Vector2.Lerp(ref location, ref translation.destination, (float)translation.time / translation.translationTime, out newLocation);
                SetBoundsNoScaling(newLocation.X, newLocation.Y, null, null);


                if ((translation.Destination - location).Length < .00001f)
                {
                    translation.time = 0;
                    SetBoundsNoScaling(translation.Destination.X, translation.Destination.Y, null, null);

                    // call events

                    translation = null;
                }
            }
        }

        public virtual void Render()
        {
            if (Enable)
            {
                appearence.Render(Absolute_X, Absolute_Y, size.X, size.Y);
            }
        }

        // this should return a new instnace.. but for now
        public virtual Widget FromWidgetData(WidgetData data)
        {
            this.ID = data.ID;
            this.Name = data.Name;
            // enable

            if (data.ParentID != -1)
            {
                Parent = Singleton<ClientGUI>.INSTANCE.widgets.Find((e) => e.ID == data.ParentID);
            }

            this.SetBounds(data.Location.X, data.Location.Y, data.Size.X, data.Size.Y);

            appearence.Clear();
            foreach (var appData in data.appearenceData)
            {
                appearence.AddAppearence(appData.Name, appData.ToAppearence());
            }

            translations.Clear();
            translations.AddRange(data.translations);

            WidgetCommands.handlers.TryGetValue(Name, out handler);

            return this;
        }

        public virtual WidgetData ToWidgetData()
        {
            WidgetData data = new WidgetData();
            data.Name = this.Name;
            data.ID = this.ID;
            data.Enable = this.Enable;
            data.Location = new Vector2(Scale.hUnPosScale(Absolute_X), Scale.vUnPosScale(Absolute_Y));
            data.Size = new Vector2(Scale.hUnSizeScale(size.X), Scale.vUnSizeScale(size.Y));
            appearence.Foreach((e) => { data.appearenceData.Add(e.ToData()); });
            return data;
        }

        public int GetNextAvailableID()
        {
            return Singleton<ClientGUI>.INSTANCE.NextAvailableWidgeID;
        }
    }
}
