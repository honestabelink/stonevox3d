package stonevox.data;

import java.util.ArrayList;
import java.util.HashMap;

import stonevox.Program;
import stonevox.decorator.GradientBackground;
import stonevox.decorator.MultiColorBackground;
import stonevox.decorator.PlainBackground;
import stonevox.decorator.PlainBorder;
import stonevox.decorator.PlainMarker;
import stonevox.decorator.PlainText;
import stonevox.decorator.Sprite;
import stonevox.util.CursorUtil;
import stonevox.util.GUI;
import stonevox.util.Scale;

public class GUIelement
{
	public static float baseTransSpeedX = (float) Scale.hSizeScale(4f);
	public static float baseTransSpeedY = (float) Scale.vSizeScale(4f);

	public float x;
	public float y;

	public float width;
	public float height;
	public boolean isInit = false;
	public boolean focused = false;

	public float transSpeedX = baseTransSpeedX;
	public float transSpeedY = baseTransSpeedY;
	public float transDirectionX = 0;
	public float transDirectionY = 0;

	public float transX;
	public boolean isTransX;

	public float transY;
	public boolean isTransY;

	public String statusTip = "";

	private int lastClickButton = -1;
	private long lastClickTime;
	public boolean dragged;

	public GUIappearence appearence = new GUIappearence();

	private GUIelement parent;
	public ArrayList<GUIelement> children = new ArrayList<GUIelement>();
	public HashMap<String, Object> data = new HashMap<String, Object>();
	public HashMap<String, GUItransition> transitions = new HashMap<String, GUItransition>();

	public int ID = -1;

	private boolean enabled = true;

	public GUIelement(int ID)
	{
		this.ID = ID;
	}

	public void init()
	{
		isInit = true;
	}

	public boolean keyPress(int key)
	{
		return false;
	}

	public void mouseDoubleClick(int button)
	{
		Broadcast(GUI.MESSAGE_MOUSE_DOUBLE_CLICK);
	}

	public void mouseUp(int button)
	{
		dragged = false;
		Broadcast(GUI.MESSAGE_MOUSE_UP);
	}

	public void mouseClick(int button)
	{
		long time = Program.getTime();
		dragged = true;

		if (Math.abs(time - lastClickTime) < 250)
		{
			mouseDoubleClick(button);
		}
		else
			Broadcast(GUI.MESSAGE_MOUSE_CLICK);

		lastClickTime = time;
		lastClickButton = button;
	}

	public void mouseMove(float x, float y)
	{
	}

	public void mouseLeave()
	{
		Broadcast(GUI.MESSAGE_MOUSE_LEAVE);
		Program.rayCaster.enableRaycaster();
		GUI.setStatus("");
		CursorUtil.SetCursor(CursorUtil.getDefault(true));
	}

	public void mouseEnter()
	{
		Broadcast(GUI.MESSAGE_MOUSE_ENTER);
		Program.rayCaster.disableRaycaster();
		GUI.setStatus(statusTip);
		CursorUtil.SetCursor(getCursor());
	}

	public void mouseOver()
	{

	}

	public void focusGained()
	{
		focused = true;
	}

	public void focusLost()
	{
		focused = false;
	}

	public void dispose()
	{
		appearence.dispose();
	}

	public void onBroadcastMessage(String message, Object... args)
	{

	}

	public void onMessageRecieved(String message, Object... args)
	{
		if (GUI.MESSAGE_GUI_MENU_TRANS_ON == message)
		{
			if (this.transitions.size() > 0)
			{
				GUItransition trans = transitions.get("offtrans");

				if (trans != null)
				{
					this.doTrans("offtrans");
				}

				Boolean state = (Boolean) data.get("state");

				if (state != null && state)
					data.replace("state", false);
			}
		}
	}

	public void Broadcast(String message)
	{
		GUI.Broadcast(message, this, ID);
	}

	public void transisitionDoneX()
	{
	}

	public void startTransititionX()
	{

	}

	public void transisitionDoneY()
	{
	}

	public void startTransititionY()
	{

	}

	public void onDoTransistionByName(String name)
	{

	}

	public void update()
	{
		if (isTransX)
		{
			x += transDirectionX * transSpeedX * Program.delta;
			if (transDirectionX > 0 && x >= transX)
			{
				x = transX;
				isTransX = false;
				transisitionDoneX();
			}
			else if (transDirectionX < 0 && x <= transX)
			{
				x = transX;
				isTransX = false;
				transisitionDoneX();
			}
		}

		if (isTransY)
		{
			y += transDirectionY * transSpeedY * Program.delta;

			if (transDirectionY > 0 && y >= transY)
			{
				y = transY;
				isTransY = false;
				transisitionDoneY();
			}
			else if (transDirectionY < 0 && y <= transY)
			{
				y = transY;
				isTransY = false;
				transisitionDoneY();
			}
		}
	}

	public void doTransX(float transX)
	{
		startTransititionX();
		this.isTransX = true;
		this.transX = transX;

		float unX = (float) Scale.hUnPosScale(x);
		float unTransX = (float) Scale.hUnPosScale(transX);
		float diff = unTransX - unX;
		transDirectionX = Math.signum(diff);
	}

	public void doTransX(float speed, float transX)
	{
		this.isTransX = true;
		this.transSpeedX = speed;
		this.transX = transX;

		float unX = (float) Scale.hUnPosScale(x);
		float diff = transX - unX;
		transDirectionX = Math.signum(diff);
	}

	public void doTransY(float transY)
	{
		startTransititionY();
		this.isTransY = true;
		this.transY = transY;

		float unY = (float) Scale.vUnPosScale(y);
		float unTransY = (float) Scale.vUnPosScale(transY);
		float diff = unTransY - unY;
		transDirectionY = Math.signum(diff);
	}

	public void doTransY(float speed, float transY)
	{
		this.isTransY = true;
		this.transSpeedY = speed;
		this.transY = transY;

		float unY = (float) Scale.vUnPosScale(y);
		float diff = transY - unY;
		transDirectionY = Math.signum(diff);
	}

	public void doTrans(String name)
	{
		if (transitions.containsKey(name))
		{
			GUItransition g = transitions.get(name);
			if (g.isX)
				this.doTransX(g.location);
			else
				this.doTransY(g.location);

			onDoTransistionByName(name);
		}
	}

	public void render()
	{
		// so trash, all i needed for now...
		if (enabled && hasParent())
		{
			float parx = getParentsX();
			float pary = getParentsY();
			appearence.render(parx + x, pary + y, width, height);
		}
		else if (enabled)
			appearence.render(x, y, width, height);
	}

	public void setPositon(float x, float y)
	{
		this.x = (float) Scale.hPosScale(x);
		this.y = (float) Scale.vPosScale(y);
	}

	public void setPositon(float x, float y, boolean scaleToParentBounds)
	{
		if (!scaleToParentBounds)
			setPositon(x, y);
		else
		{
			if (hasParent())
			{
				float parsWidth = getParentsWidth();
				float parsHeight = getParentsHeight();

				this.x = (float) Scale.scale(x, 0, 1f, 0f, parsWidth);
				this.y = (float) Scale.scale(y, 0, 1f, 0f, parsHeight);
			}
		}
	}

	public void setSize(float w, float h)
	{
		this.width = (float) Scale.hSizeScale(w * 2f * GUI.hackscalex);
		this.height = (float) Scale.vSizeScale(h * 2f * GUI.hackscaley);
	}

	public void setParent(GUIelement el)
	{
		el.children.add(this);
		this.parent = el;
	}

	public void setEnable(boolean enabled)
	{
		this.enabled = enabled;
		this.appearence.SetEnabled(enabled);
	}

	public GUIelement getParent()
	{
		return parent;
	}

	public String getCursor()
	{
		return CursorUtil.DEFAULT;
	}

	public float getParentsX()
	{
		float value = 0;
		return getparentx(this, value);
	}

	private float getparentx(GUIelement par, float value)
	{
		if (par.hasParent())
		{
			value += par.getParent().x / 2f;
			value += getparentx(par.getParent(), value);
		}

		return value;
	}

	public float getParentsY()
	{
		float value = 0;
		return getparenty(this, value);
	}

	private float getparenty(GUIelement par, float value)
	{
		if (par.hasParent())
		{
			value += par.getParent().y / 2f;
			value += getparenty(par.getParent(), value);
		}

		return value;
	}

	public float getParentsWidth()
	{
		float value = 0;
		return getparentwidth(this, value);
	}

	private float getparentwidth(GUIelement par, float value)
	{
		if (par.hasParent())
		{
			value += par.getParent().width / 2f;
			value += getparentwidth(par.getParent(), value);
		}

		return value;
	}

	public float getParentsHeight()
	{
		float value = 0;
		return getparentheight(this, value);
	}

	private float getparentheight(GUIelement par, float value)
	{
		if (par.hasParent())
		{
			value += par.getParent().height / 2f;
			value += getparentheight(par.getParent(), value);
		}

		return value;
	}

	public boolean getEnabled()
	{
		return enabled;
	}

	public boolean hasParent()
	{
		return parent != null ? true : false;
	}

	public float getUnScaleX()
	{
		return (float) Scale.hUnPosScale(x);
	}

	public float getUnScaleY()
	{
		return (float) Scale.vUnPosScale(y);
	}

	public float getUnScaleWidth()
	{
		return (float) Scale.hUnSizeScale(width);
	}

	public float getUnScaleHeight()
	{
		return (float) Scale.vUnSizeScale(height);
	}

	public PlainBackground getPlainBackground(String name)
	{
		return (PlainBackground) this.appearence.Get(name);
	}

	public PlainBorder getPlainBorder(String name)
	{
		return (PlainBorder) this.appearence.Get(name);
	}

	public Sprite getSprite(String name)
	{
		return (Sprite) this.appearence.Get(name);
	}

	public PlainText getPlainText(String name)
	{
		return (PlainText) this.appearence.Get(name);
	}

	public MultiColorBackground getMultiColorBackground(String name)
	{
		return (MultiColorBackground) this.appearence.Get(name);
	}

	public GradientBackground getGradientBackground(String name)
	{
		return (GradientBackground) this.appearence.Get(name);
	}

	public PlainMarker getPlainMarker(String name)
	{
		return (PlainMarker) this.appearence.Get(name);
	}
}
