package stonevox.gui;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;

import stonevox.data.GUIelement;
import stonevox.decorator.PlainBackground;
import stonevox.decorator.PlainBorder;
import stonevox.decorator.PlainText;
import stonevox.util.GUI;
import stonevox.util.Scale;

public class Textbox extends GUIelement
{
	public String text = "";
	public String lasttext = "";
	private String renderText = "";

	public Textbox(int ID, float width)
	{
		super(ID);

		this.appearence.Add("background", new PlainBackground(new Color(41f / 256f, 39f / 256f, 39f / 256f)));
		this.appearence.Add("text", new PlainText("default", "", Color.white));
		this.appearence.Add("border", new PlainBorder(3, new Color(122f / 256f, 106f / 256f, 70f / 256f)));

		this.getPlainText("text").xoffset = 3;
		this.getPlainText("text").yoffset = 1;

		float[] textsize = this.getPlainText("text").measureString("W");
		this.setSize(width, textsize[1]);
	}

	public void setText(String text)
	{
		this.text = text;
		this.renderText = clipText(text);
		this.getPlainText("text").setText(renderText);
	}

	public void setTextColor(Color color)
	{
		this.getPlainText("text").setColor(color);
	}

	public void focusGained()
	{
		Keyboard.enableRepeatEvents(true);
		lasttext = text;
		super.focusGained();
	}

	public void focusLost()
	{
		Keyboard.enableRepeatEvents(false);
		Broadcast(GUI.MESSAGE_TEXTBOX_COMMITED);
		super.focusLost();
	}

	public boolean keyPress(int key)
	{
		if (focused)
		{
			if (key == Keyboard.KEY_BACK)
			{
				if (text.length() > 0)
				{
					text = removeLastChar(text);
					setText(text);
					Broadcast(GUI.MESSAGE_TEXTBOX_CHANGED);
				}
			}
			else if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER)
			{
				OnReturnKey();
			}
			else
			{
				setText(text + keytoString(key));
				Broadcast(GUI.MESSAGE_TEXTBOX_CHANGED);
			}
			return true;
		}
		return false;
	}

	public void OnReturnKey()
	{
		lasttext = text;
		Broadcast(GUI.MESSAGE_TEXTBOX_COMMITED);
	}

	private String clipText(String text)
	{
		String currentText = text;

		PlainText pt = getPlainText("text");
		float unscaledwidth = (float) Scale.hUnSizeScale(width) / 2f;

		while (pt.measureString(currentText)[0] > unscaledwidth)
		{
			currentText = currentText.substring(1);
		}

		return currentText;
	}

	private String removeLastChar(String str)
	{
		return str.substring(0, str.length() - 1);
	}

	private String keytoString(int key)
	{
		if (key == Keyboard.KEY_SPACE)
			return "_";
		else if (key == Keyboard.KEY_UP || key == Keyboard.KEY_LEFT || key == Keyboard.KEY_RIGHT
				|| key == Keyboard.KEY_DOWN || key == Keyboard.KEY_LSHIFT || key == Keyboard.KEY_RSHIFT
				|| key == Keyboard.KEY_GRAVE || key == Keyboard.KEY_NUMPADENTER)
		{
			return "";
		}

		if (key == Keyboard.KEY_NUMPAD0)
			return "0";
		else if (key == Keyboard.KEY_NUMPAD1)
			return "1";
		else if (key == Keyboard.KEY_NUMPAD2)
			return "2";
		else if (key == Keyboard.KEY_NUMPAD3)
			return "3";
		else if (key == Keyboard.KEY_NUMPAD4)
			return "4";
		else if (key == Keyboard.KEY_NUMPAD5)
			return "5";
		else if (key == Keyboard.KEY_NUMPAD6)
			return "6";
		else if (key == Keyboard.KEY_NUMPAD7)
			return "7";
		else if (key == Keyboard.KEY_NUMPAD8)
			return "8";
		else if (key == Keyboard.KEY_NUMPAD9)
			return "9";
		else if (key == Keyboard.KEY_SUBTRACT)
			return "-";

		if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			if (key == Keyboard.KEY_MINUS)
			{
				return "-";
			}
			else
				return Keyboard.getKeyName(key).toLowerCase();
		}
		else
		{
			if (key == Keyboard.KEY_MINUS)
			{
				return "_";
			}
			else
				return Keyboard.getKeyName(key);
		}
	}
}
