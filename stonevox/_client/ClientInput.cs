using OpenTK;
using OpenTK.Input;
using stonevox;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public class ClientInput
{
    public delegate bool KeyHandler(KeyboardKeyEventArgs e);
    public delegate bool KeyPressHandler(KeyPressEventArgs e);
    public delegate bool MouseHandler(MouseButtonEventArgs e);
    public delegate void MouseMoveHandler(MouseMoveEventArgs e);
    public delegate void MouseWheelHandler(MouseWheelEventArgs e);

    public class InputHandler
    {
        public KeyHandler keydownhandler;
        public KeyHandler keyuphandler;
        public KeyPressHandler keypresshandler;
        public MouseHandler mousedownhandler;
        public MouseHandler mouseuphandler;
        public MouseMoveHandler mousemovehandler;
        public MouseWheelHandler mousewheelhandler;
    }

    public List<InputHandler> messaginghandlers = new List<InputHandler>();
    public List<InputHandler> driverhandlers = new List<InputHandler>();

    public int mousedx;
    public int mousedy;

    private KeyboardState lastkeyboardstate;
    private KeyboardState currentkeyboardstate;

    private MouseState lastmousestate;
    private MouseState currentmousestate;

    public int mousex;
    public int mousey;

    public int mouseX { get { return currentmousestate.X; } }
    public int mouseY { get { return currentmousestate.Y; } }

    private GLWindow window;

    private bool isFocused { get { return window.isfocused; } }

    public ClientInput(GLWindow window)
    {
        this.window = window;
    }

    public void update()
    {
        currentkeyboardstate = Keyboard.GetState();
        currentmousestate = Mouse.GetState();

        mousedx = currentmousestate.X - lastmousestate.X;
        mousedy = currentmousestate.Y - lastmousestate.Y;

        lastkeyboardstate = currentkeyboardstate;
        lastmousestate = currentmousestate;
    }

    public void addhandler(InputHandler handler)
    {
        messaginghandlers.Add(handler);
    }

    public void removehandler(InputHandler handler)
    {
        messaginghandlers.Remove(handler);
    }

    public bool keydown(Key key)
    {
        return isFocused && currentkeyboardstate.IsKeyDown(key);
    }
    public bool keyup(Key key)
    {
        return isFocused && currentkeyboardstate.IsKeyUp(key);
    }
    public bool keypressed(Key key)
    {
        return isFocused && currentkeyboardstate.IsKeyDown(key) && lastkeyboardstate.IsKeyUp(key);
    }

    public bool mousedown(MouseButton button)
    {
        return isFocused && currentmousestate.IsButtonDown(button);
    }
    public bool mouseup(MouseButton button)
    {
        return isFocused && currentmousestate.IsButtonUp(button);
    }
    public bool mousepressed(MouseButton button)
    {
        return isFocused && currentmousestate.IsButtonDown(button) && lastmousestate.IsButtonUp(button);
    }

    public void handlekeydown(KeyboardKeyEventArgs e)
    {
        if (!isFocused) return;
        messaginghandlers.ForEach(delegate(InputHandler t)
        {
            if (t.keydownhandler != null)
                t.keydownhandler(e);
        });
    }

    public void handlekeyup(KeyboardKeyEventArgs e)
    {
        //Client.print("info", "keyup");
        if (!isFocused) return;

        messaginghandlers.ForEach(delegate(InputHandler t)
        {
            if (t.keyuphandler != null)
                t.keyuphandler(e);
        });
    }

    public void handlekeypress(KeyPressEventArgs e)
    {
        ///Client.print("info", "keypress");
        if (!isFocused) return;
        messaginghandlers.ForEach(delegate(InputHandler t)
        {
            if (t.keypresshandler != null)
                t.keypresshandler(e);
        });
    }

    public void handlemousedown(MouseButtonEventArgs e)
    {
        //Client.print("info", "mousedown");
        if (!isFocused) return;

        messaginghandlers.ForEach(delegate(InputHandler t)
        {
            if (t.mousedownhandler != null)
                t.mousedownhandler(e);
        });
    }

    public void handlemouseup(MouseButtonEventArgs e)
    {
        ///Client.print("info", "mouseup");
        if (!isFocused) return;
        messaginghandlers.ForEach(delegate(InputHandler t)
        {
            if (t.mouseuphandler != null)
                t.mouseuphandler(e);
        });
    }

    public void handlemousewheel(MouseWheelEventArgs e)
    {
        // Client.print("info", "mousewheel");
        if (!isFocused) return;

        messaginghandlers.ForEach(delegate(InputHandler t)
        {
            if (t.mousewheelhandler != null)
                t.mousewheelhandler(e);
        });
    }
}
