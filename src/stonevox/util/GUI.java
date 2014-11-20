package stonevox.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;

import stonevox.Program;
import stonevox.data.GUIelement;
import stonevox.data.GUIlayout;
import stonevox.data.GUItransition;
import stonevox.data.GradientStop;
import stonevox.data.Keyhook;
import stonevox.data.TextDisplay;
import stonevox.decorator.GradientBackground;
import stonevox.decorator.MultiColorBackground;
import stonevox.decorator.PlainBorder;
import stonevox.decorator.PlainMarker;
import stonevox.decorator.Sprite;
import stonevox.gui.ColorOption;
import stonevox.gui.Label;
import stonevox.gui.SpriteButton;
import stonevox.gui.Textbox;
import stonevox.tools.ToolEdit;

public class GUI
{
	public static int						COLOR_PICKER_BACKGROUND		= 100;
	public static int						PAINTER_BACKGROUND			= 101;
	public static int						EDITER_BACKGROUND			= 102;
	public static int						SAVE_BACKGROUND				= 103;
	public static int						PROJECTSETTINGS_BACKGROUND	= 104;
	public static int						PROJECTSETTINGS_BUTTON		= 105;
	public static int						PROJECTSETTINGS_NAME		= 106;
	public static int						PROJECTSETTINGS_SIZE		= 107;
	public static int						STATUS_LABEL				= 108;
	public static int						COLOR_PICKER_COLORSQARE		= 109;
	public static int						COLOR_PICKER_HUESLIDER		= 110;

	public static float						hackscalex					= 1.0f;
	public static float						hackscaley					= 1.0f;

	public static String					MESSAGE_GUI_MENU_TRANS_ON	= "gui_menu_trans_on";

	static int								lastControlOver				= -1;
	static int								lastControlFocused			= -1;
	static int								lastButton					= -1;

	static int								lastelementid				= -1;
	static ArrayList<GUIelement>			elements					= new ArrayList<GUIelement>();
	public static ArrayList<GUIlayout>		layout						= new ArrayList<GUIlayout>();
	public static ArrayList<TextDisplay>	text						= new ArrayList<TextDisplay>();

	public static void logic(float x, float y, float dx, float dy)
	{
		for (int i = elements.size() - 1; i > -1; i--)
		{
			elements.get(i).update();

			if (elements.get(i).dragged)
			{
				elements.get(i).mouseMove(dx, dy);
			}
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
			return;

		if (lastControlOver != -1)
		{
			if (!isMouseWith(x, y, elements.get(lastControlOver)))
			{
				if (elements.get(lastControlOver).dragged)
				{
					if (lastButton != -1 && !Mouse.isButtonDown(lastButton))
					{
						elements.get(lastControlOver).mouseLeave();
						lastControlOver = -1;
					}
				}
				else
				{
					elements.get(lastControlOver).mouseLeave();
					lastControlOver = -1;
				}
			}
		}

		if (lastControlOver == -1)
		{
			for (int i = elements.size() - 1; i > -1; i--)
			{
				if (isMouseWith(x, y, elements.get(i)))
				{
					if (lastControlOver != i || lastControlOver == -1)
					{
						lastControlOver = i;
						elements.get(i).mouseEnter();
						elements.get(i).mouseOver();
					}
					else
					{
						elements.get(i).mouseOver();
					}

					break;
				}
			}
		}
		else if (elements.get(lastControlOver).dragged == false)
		{
			for (int i = elements.size() - 1; i > -1; i--)
			{
				if (isMouseWith(x, y, elements.get(i)))
				{
					if (lastControlOver != i || lastControlOver == -1)
					{
						lastControlOver = i;
						elements.get(i).mouseEnter();
						elements.get(i).mouseOver();
					}
					else
					{
						elements.get(i).mouseOver();
					}
					break;
				}
			}
		}
	}

	public static void handleMouseInput(int button, boolean buttonstate)
	{
		if (buttonstate)
		{
			if (lastControlOver > -1)
			{
				elements.get(lastControlOver).mouseClick(button);

				if (lastControlFocused != -1)
					elements.get(lastControlFocused).focusLost();
				elements.get(lastControlOver).focusGained();
				lastControlFocused = lastControlOver;
			}
			else
			{
				if (lastControlFocused != -1)
					elements.get(lastControlFocused).focusLost();
			}
			lastButton = button;
		}
		else if (lastButton != -1 && !Mouse.isButtonDown(lastButton))
		{
			if (lastControlOver > -1)
			{
				elements.get(lastControlOver).mouseUp(button);
			}
			lastButton = -1;
		}
	}

	public static boolean handleKeyboardInput(int key, boolean keyState)
	{
		if (keyState && lastControlFocused != -1)
		{
			elements.get(lastControlFocused).keyPress(key);
			return true;
		}
		else
			return false;
	}

	public static void render()
	{
		Program.ready2DText();

		for (int i = text.size() - 1; i > -1; i--)
		{
			text.get(i).render();
		}

		Program.ready2D();

		for (int i = elements.size() - 1; i > -1; i--)
		{
			elements.get(i).render();
		}
	}

	public static int AddElement(GUIelement el)
	{
		if (!el.isInit)
			el.init();
		if (el.ID == -1)
		{
			el.ID = getNextID();
		}
		elements.add(el);
		return el.ID;
	}

	public static void SendBack(int id)
	{
		for (int i = 0; i < elements.size(); i++)
			if (elements.get(i).ID == id)
			{
				GUIelement el = elements.get(i);
				elements.remove(i);
				elements.add(0, el);
				break;
			}
	}

	public static void SendFront(int id)
	{
		for (int i = 0; i < elements.size(); i++)
			if (elements.get(i).ID == id)
			{
				GUIelement el = elements.get(i);
				elements.remove(i);
				elements.add(el);
				break;
			}
	}

	static boolean isMouseWith(float x, float y, GUIelement el)
	{
		if (!el.hasParent())
			return x >= el.x && x <= el.x + el.width && y >= el.y && y <= el.y + el.height;
		else
		{
			if (el.getParent().getParent() != null)
				return x >= el.getParent().getParent().x + el.getParent().x + el.x
						&& x <= el.getParent().getParent().x + el.getParent().x + el.x + el.width
						&& y >= el.getParent().getParent().y + el.getParent().y + el.y
						&& y <= el.getParent().getParent().y + el.getParent().y + el.y + el.height;
			else
				return x >= el.getParent().x + el.x && x <= el.getParent().x + el.x + el.width
						&& y >= el.getParent().y + el.y && y <= el.getParent().y + el.y + el.height;
		}
	}

	public static int getNextID()
	{
		lastelementid++;
		return lastelementid;
	}

	public static GUIelement get(int id)
	{
		for (int i = 0; i < elements.size(); i++)
			if (elements.get(i).ID == id)
				return elements.get(i);

		return null;
	}

	public static void Layout()
	{
		for (int i = 0; i < layout.size(); i++)
		{
			if (layout.get(i).front)
			{
				SendFront(layout.get(i).id);
			}
			else
				SendBack(layout.get(i).id);
		}

		layout.clear();
	}

	public static void Broadcast(String message, Object... objects)
	{
		for (int i = 0; i < elements.size(); i++)
			if (elements.get(i).ID != (Integer) objects[1])
				elements.get(i).onMessageRecieved(message, objects);
	}

	public static void dispose()
	{
		for (int i = 0; i < elements.size(); i++)
		{
			elements.get(i).dispose();
		}

		elements.clear();
	}

	public static void BeginWindowSizeChange()
	{
		for (int i = 0; i < elements.size(); i++)
		{
			GUIelement el = elements.get(i);

			el.x = (float) Scale.hUnPosScale(el.x);
			el.y = (float) Scale.vUnPosScale(el.y);
			el.width = (float) Scale.hUnSizeScale(el.width);
			el.height = (float) Scale.vUnSizeScale(el.height);
		}
	}

	public static void RescaleWindowChange(float width, float height, float nwidth, float nheight)
	{
		for (int i = 0; i < elements.size(); i++)
		{
			GUIelement el = elements.get(i);

			el.x = (float) Scale.scale(el.x, 0, width, 0, nwidth);
			el.y = (float) Scale.scale(el.y, 0, height, 0, nheight);
			el.width = (float) Scale.scale(el.width, 0, width, 0, nwidth);
			el.height = (float) Scale.scale(el.height, 0, height, 0, nheight);
		}
	}

	public static void EndWindowSizeChange()
	{
		for (int i = 0; i < elements.size(); i++)
		{
			GUIelement el = elements.get(i);

			el.x = (float) Scale.hPosScale(el.x);
			el.y = (float) Scale.vPosScale(el.y);
			el.width = (float) Scale.hSizeScale(el.width);
			el.height = (float) Scale.vSizeScale(el.height);
		}
	}

	public static void setStatus(String text)
	{
		Label label = (Label) (GUI.get(GUI.STATUS_LABEL));
		if (label != null)
			label.setText(text);
	}

	public static void Hacked720pGUI()
	{
		float width = (float) Program.width;
		float height = (float) Program.height;

		GUIelement.baseTransSpeedX = (float) Scale.hSizeScale(4f);
		GUIelement.baseTransSpeedY = (float) Scale.vSizeScale(4f);

		text.clear();

		hackscalex = 1.0f;
		hackscaley = 1.0f;

		Textbox proTextbox = (Textbox) GUI.get(GUI.PROJECTSETTINGS_NAME);
		String proName = "";
		Textbox proSizeTextbox = (Textbox) GUI.get(GUI.PROJECTSETTINGS_SIZE);
		String proSize = "";

		if (proTextbox != null)
			proName = proTextbox.text;
		if (proSizeTextbox != null)
			proSize = proSizeTextbox.text;

		GUI.dispose();

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// PROJECT STATUS
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		Label statuslabel = new Label(GUI.STATUS_LABEL, "", Color.yellow);
		statuslabel.setPositon(0, 0);
		GUI.AddElement(statuslabel);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// PROJECT SETTINGS
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		GUIelement projectSettingsBG = new GUIelement(GUI.PROJECTSETTINGS_BACKGROUND)
		{
			@Override
			public void onMessageRecieved(String message, Object... args)
			{
				if (message == MESSAGE_GUI_MENU_TRANS_ON)
				{
					GUIelement button = GUI.get(PROJECTSETTINGS_BUTTON);
					if (button != null && button.data.containsKey("buttonstate")
							&& !(Boolean) button.data.get("buttonstate"))// somesort of a problem here
					{
						button.data.replace("buttonstate", true);

						button.getSprite("off_bg").setEnabled(false);
						button.getSprite("off_bg_high").setEnabled(false);
						button.getSprite("on_bg").setEnabled(true);
					}
				}
				super.onMessageRecieved(message, args);
			}
		};
		projectSettingsBG.data.put("state", true);
		projectSettingsBG.appearence.Add("background", new Sprite("/data/project_settings.png", projectSettingsBG));
		projectSettingsBG.setPositon(width - 70f,
				height / 2f - (float) Scale.vUnSizeScale(projectSettingsBG.height * .25f));
		projectSettingsBG.transitions.put(
				"ontrans",
				new GUItransition((float) Scale.hPosScale(width - (float) Scale.hUnSizeScale(projectSettingsBG.width)
						/ 2f), true));
		projectSettingsBG.transitions.put("offtrans", new GUItransition((float) Scale.hPosScale(width - 70f), true));
		GUI.AddElement(projectSettingsBG);

		GUIelement projectButton = new GUIelement(GUI.PROJECTSETTINGS_BUTTON)
		{
			public void mouseClick(int button)
			{
				if ((Boolean) data.get("buttonstate"))
				{
					data.replace("buttonstate", false);

					this.getSprite("on_bg").setEnabled(false);
					this.getSprite("on_bg_high").setEnabled(false);
					this.getSprite("off_bg").setEnabled(true);

					GUI.get(GUI.PROJECTSETTINGS_BACKGROUND).doTrans("ontrans");
					GUI.get(GUI.PROJECTSETTINGS_BACKGROUND).Broadcast(MESSAGE_GUI_MENU_TRANS_ON);
				}
				else
				{
					data.replace("buttonstate", true);

					this.getSprite("off_bg").setEnabled(false);
					this.getSprite("off_bg_high").setEnabled(false);
					this.getSprite("on_bg").setEnabled(true);

					GUI.get(GUI.PROJECTSETTINGS_BACKGROUND).doTrans("offtrans");
				}

				super.mouseClick(button);
			}

			public void mouseEnter()
			{
				if ((Boolean) data.get("buttonstate"))
				{
					this.getSprite("on_bg").setEnabled(false);
					this.getSprite("on_bg_high").setEnabled(true);
				}
				else
				{
					this.getSprite("off_bg").setEnabled(false);
					this.getSprite("off_bg_high").setEnabled(true);
				}

				super.mouseEnter();

			}

			public void mouseLeave()
			{
				if ((Boolean) data.get("buttonstate"))
				{
					this.getSprite("on_bg").setEnabled(true);
					this.getSprite("on_bg_high").setEnabled(false);
				}
				else
				{
					this.getSprite("off_bg").setEnabled(true);
					this.getSprite("off_bg_high").setEnabled(false);
				}

				super.mouseLeave();
			}
		};

		projectButton.data.put("buttonstate", true);
		projectButton.appearence.Add("on_bg", new Sprite("/data/project_settings_button_on.png", projectButton));
		projectButton.appearence.Add("on_bg_high", new Sprite("/data/project_settings_button_on_highlight.png",
				projectButton));
		projectButton.appearence.Add("off_bg", new Sprite("/data/project_settings_button_off.png", projectButton));
		projectButton.appearence.Add("off_bg_high", new Sprite("/data/project_settings_button_off_highlight.png",
				projectButton));
		projectButton.getSprite("on_bg_high").setEnabled(false);
		projectButton.getSprite("off_bg").setEnabled(false);
		projectButton.getSprite("off_bg_high").setEnabled(false);
		projectButton.setParent(projectSettingsBG);
		projectButton.setPositon(.03f, .838f, true);
		GUI.AddElement(projectButton);

		Label projectname = new Label(getNextID(), "File Name :", Color.white);
		projectname.setParent(projectSettingsBG);
		projectname.setPositon(.2f, .90f, true);
		GUI.AddElement(projectname);

		Textbox projectnametextbox = new Textbox(GUI.PROJECTSETTINGS_NAME, 350f);
		projectnametextbox.setParent(projectSettingsBG);
		projectnametextbox.setPositon(.2f, .845f, true);
		projectnametextbox.setText("untitled");
		GUI.AddElement(projectnametextbox);

		Label projectsize = new Label(getNextID(), "Size : (xyz)", Color.white);
		projectsize.setParent(projectSettingsBG);
		projectsize.setPositon(.2f, .785f, true);
		GUI.AddElement(projectsize);

		Textbox projectsizetextbox = new Textbox(GUI.PROJECTSETTINGS_SIZE, 350f)
		{
			public void OnReturnKey()
			{
				int _index1 = text.indexOf("_");
				int _index2 = text.indexOf("_", _index1 + 1);

				if (_index1 == -1 || _index2 == -1)
				{
					setText(lasttext);
					return;
				}

				int x = Integer.parseInt(text.substring(0, _index1));
				int y = Integer.parseInt(text.substring(_index1 + 1, _index2));
				int z = Integer.parseInt(text.substring(_index2 + 1));
				Program.model.GetActiveMatrix().reSize(x, y, z);
				super.OnReturnKey();
			}

			@Override
			public void focusLost()
			{
				int _index1 = text.indexOf("_");
				int _index2 = text.indexOf("_", _index1 + 1);

				if (_index1 == -1 || _index2 == -1)
				{
					setText(lasttext);
					return;
				}

				int x = Integer.parseInt(text.substring(0, _index1));
				int y = Integer.parseInt(text.substring(_index1 + 1, _index2));
				int z = Integer.parseInt(text.substring(_index2 + 1));
				Program.model.GetActiveMatrix().reSize(x, y, z);
				super.focusLost();
			}
		};
		projectsizetextbox.setParent(projectSettingsBG);
		projectsizetextbox.setPositon(.2f, .730f, true);
		projectsizetextbox.setText("10_10_10");
		GUI.AddElement(projectsizetextbox);

		GUI.layout.add(new GUIlayout(GUI.PROJECTSETTINGS_BACKGROUND, false));
		GUI.layout.add(new GUIlayout(GUI.PROJECTSETTINGS_BUTTON, true));

		if (proName != "")
		{
			projectnametextbox.setText(proName);
		}
		if (proSize != "")
		{
			projectsizetextbox.setText(proSize);
		}

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// BACKGROUNDS
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		// color options bg
		GUIelement colorOptionBG = new GUIelement(GUI.getNextID());
		colorOptionBG.appearence.Add("bg", new Sprite("/data/colorselector_background.png", colorOptionBG));
		colorOptionBG.setPositon(0, height / 2f - (float) Scale.vUnSizeScale(colorOptionBG.height * .25f));
		GUI.AddElement(colorOptionBG);

		// tool menu bg
		GUIelement toolMenuBG = new GUIelement(GUI.getNextID());
		toolMenuBG.appearence.Add("bg", new Sprite("/data/toolmenu_background.png", toolMenuBG));
		toolMenuBG.setPositon(width / 2f - (float) Scale.hUnSizeScale(toolMenuBG.width * .25f), 0f);
		GUI.AddElement(toolMenuBG);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// COLOR PICKER TOOL
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		GUIelement colorpickerBG = new GUIelement(GUI.COLOR_PICKER_BACKGROUND)
		{
			@Override
			public void onDoTransistionByName(String name)
			{
				if (name == "offtrans")
				{
					GUIelement g = GUI.get(GUI.COLOR_PICKER_COLORSQARE);
					if (g != null)
					{
						g.getPlainMarker("marker").setPosition(-10, 10);
					}
				}
				super.onDoTransistionByName(name);
			}
		};
		colorpickerBG.appearence.Add("bg", new Sprite("/data/colorselector.png", colorpickerBG));
		colorpickerBG.setPositon(-300, 0);
		colorpickerBG.transitions.put("ontrans", new GUItransition((float) Scale.hPosScale(95f), true));
		colorpickerBG.transitions.put("offtrans", new GUItransition((float) Scale.hPosScale(-300f), true));
		colorpickerBG.data.put("state", false);
		GUI.AddElement(colorpickerBG);

		int colorswatches = GUI.getNextID();

		GUIelement colorpickerSwatches = new GUIelement(colorswatches)
		{
			@Override
			public void mouseMove(float x, float y)
			{
				GUIelement colorpicker = GUI.get(GUI.COLOR_PICKER_COLORSQARE);
				if (colorpicker != null)
				{
					GL11.glReadBuffer(GL11.GL_FRONT);
					ByteBuffer buffer = BufferUtils.createByteBuffer(4);
					GL11.glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

					int r = buffer.get(0) & 0xFF;
					int g = buffer.get(1) & 0xFF;
					int b = buffer.get(2) & 0xFF;

					ColorOption.lastOption.setColor(new Color(r, g, b));
					ColorOption.lastOption.huecolor = new Color(r, g, b);
					Program.toolpainter.paintColor = ColorOption.lastOption.color;

					colorpicker.getMultiColorBackground("mcBG").setColor(2, new Color(r, g, b));
					colorpicker.getMultiColorBackground("mcBG").updateGradient();
					colorpicker.getPlainMarker("marker").setPosition(-10, 10);
				}
				super.mouseMove(x, y);
			}
		};
		colorpickerSwatches.setParent(colorpickerBG);
		colorpickerSwatches.setPositon(0, 0, true);
		colorpickerSwatches.appearence.Add("bg2", new Sprite("/data/colorpicker_swatches.png", colorpickerSwatches));
		GUI.AddElement(colorpickerSwatches);

		GUIelement colorpickerColorSquare = new GUIelement(GUI.COLOR_PICKER_COLORSQARE)
		{
			@Override
			public void mouseMove(float x, float y)
			{
				GUIelement colorpicker = GUI.get(GUI.COLOR_PICKER_COLORSQARE);
				if (colorpicker != null)
				{
					GL11.glReadBuffer(GL11.GL_FRONT);
					ByteBuffer buffer = BufferUtils.createByteBuffer(4);
					GL11.glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

					int r = buffer.get(0) & 0xFF;
					int g = buffer.get(1) & 0xFF;
					int b = buffer.get(2) & 0xFF;

					ColorOption.lastOption.setColor(new Color(r, g, b));
					Program.toolpainter.paintColor = ColorOption.lastOption.color;

					getPlainMarker("marker").setPosition(-10, 10);
				}
				super.mouseMove(x, y);
			}

			@Override
			public void mouseUp(int button)
			{
				GL11.glReadBuffer(GL11.GL_FRONT);
				ByteBuffer buffer = BufferUtils.createByteBuffer(4);
				GL11.glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

				int r = buffer.get(0) & 0xFF;
				int g = buffer.get(1) & 0xFF;
				int b = buffer.get(2) & 0xFF;

				float nx = (float) Scale.scale(Mouse.getX(), 0, Program.width, -1, 1);
				float ny = (float) Scale.scale(Mouse.getY(), 0, Program.height, -1, 1);

				Color c = new Color(1, 1, 1);
				float average = r + g + b / 3f;

				if (average > 256f / 2f)
				{
					c = Color.black;
				}
				else
					c = Color.white;

				ColorOption.lastOption.setColoredSquareLocation(nx - (x + getParent().x), ny - (y + getParent().y));

				getPlainMarker("marker").setPositionColor(nx - (x + getParent().x), ny - (y + getParent().y), c);
				super.mouseUp(button);
			}
		};
		colorpickerColorSquare.setParent(colorpickerBG);
		colorpickerColorSquare.setSize(colorpickerBG.getUnScaleWidth() / 2f * .63f,
				colorpickerBG.getUnScaleHeight() / 2f * .72f);
		colorpickerColorSquare.setPositon(.12f, .2f, true);
		colorpickerColorSquare.appearence.Add("mcBG", new MultiColorBackground(Color.black, Color.black, Color.blue,
				Color.white));
		colorpickerColorSquare.appearence.Add("marker", new PlainMarker());
		colorpickerColorSquare.appearence.Add("border", new PlainBorder(2f, Color.yellow.darker(.4f)));
		colorpickerColorSquare.getMultiColorBackground("mcBG").updateGradient(colorpickerColorSquare.width,
				colorpickerColorSquare.height);
		GUI.AddElement(colorpickerColorSquare);

		GUIelement colorpickerHueSlider = new GUIelement(GUI.COLOR_PICKER_HUESLIDER)
		{
			@Override
			public void mouseMove(float x, float y)
			{
				GUIelement colorpicker = GUI.get(GUI.COLOR_PICKER_COLORSQARE);
				if (colorpicker != null)
				{
					GL11.glReadBuffer(GL11.GL_FRONT);
					ByteBuffer buffer = BufferUtils.createByteBuffer(4);
					GL11.glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

					int r = buffer.get(0) & 0xFF;
					int g = buffer.get(1) & 0xFF;
					int b = buffer.get(2) & 0xFF;

					ColorOption.lastOption.huecolor = new Color(r, g, b);

					colorpicker.getMultiColorBackground("mcBG").setColor(2, new Color(r, g, b));
					colorpicker.getMultiColorBackground("mcBG").updateGradient();
				}
				super.mouseMove(x, y);
			}
		};
		colorpickerHueSlider.setSize(27, colorpickerBG.getUnScaleHeight() / 2f * .72f);
		colorpickerHueSlider.appearence.Add("gradientBG", new GradientBackground(new GradientStop(.45f, Color.red),
				new GradientStop(.15f, Color.green), new GradientStop(.35f, Color.blue), new GradientStop(.05f,
						Color.red)));
		colorpickerHueSlider.appearence.Add("border", new PlainBorder(2f, Color.yellow.darker(.4f)));
		colorpickerHueSlider.getGradientBackground("gradientBG").updateGradient(colorpickerHueSlider.width,
				colorpickerHueSlider.height);
		colorpickerHueSlider.setParent(colorpickerBG);
		colorpickerHueSlider.setPositon(.80f, .2f, true);
		GUI.AddElement(colorpickerHueSlider);

		GUI.layout.add(new GUIlayout(GUI.COLOR_PICKER_HUESLIDER, false));
		GUI.layout.add(new GUIlayout(GUI.COLOR_PICKER_COLORSQARE, false));
		GUI.layout.add(new GUIlayout(colorswatches, false));
		GUI.layout.add(new GUIlayout(GUI.COLOR_PICKER_BACKGROUND, false));

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// SELECTION TOOL
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		SpriteButton selbutton =
				new SpriteButton(GUI.getNextID(), "/data/selection.png", "/data/selection_highlight.png")
				{
					public void mouseDoubleClick(int button)
					{
						// this.doTransX(1f);
						// this.doTransY(0f);
						super.mouseEnter();
					}
				};
		selbutton.setPositon(width / 2f - 2f * 75f - (float) Scale.hUnSizeScale(selbutton.width * .25f), 0);
		GUI.AddElement(selbutton);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// PAINTER TOOL
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		GUIelement painterOptionsBG = new GUIelement(GUI.PAINTER_BACKGROUND)
		{

		};
		painterOptionsBG.setPositon(width / 2f - 1f * 75f, -100f);
		painterOptionsBG.appearence.Add("bg", new Sprite("/data/sub_toolmenu_someoptions.png", painterOptionsBG));
		painterOptionsBG.x -= painterOptionsBG.width / 2f;
		painterOptionsBG.data.put("state", false);
		painterOptionsBG.transitions.put("ontrans", new GUItransition((float) Scale.vPosScale(75), false));
		painterOptionsBG.transitions.put("offtrans", new GUItransition((float) Scale.vPosScale(-100), false));
		GUI.AddElement(painterOptionsBG);

		SpriteButton painterPerVox =
				new SpriteButton(GUI.getNextID(), "/data/voxel_brush.png", "/data/voxel_brush_highlight.png")
				{
					public void mouseClick(int button)
					{
						GUIelement el = GUI.get(GUI.PAINTER_BACKGROUND);
						el.doTrans("offtrans");
						el.data.replace("state", false);
						Program.setTool(Program.toolpainter);
						super.mouseClick(button);
					}
				};
		painterPerVox.setParent(painterOptionsBG);
		painterPerVox.setPositon(.15f, .14f, true);
		GUI.AddElement(painterPerVox);

		SpriteButton painterVolumeVox =
				new SpriteButton(GUI.getNextID(), "/data/voxel_bucket.png", "/data/voxel_bucket_highlight.png")
				{
					public void mouseClick(int button)
					{
						GUIelement el = GUI.get(GUI.PAINTER_BACKGROUND);
						el.doTrans("offtrans");
						el.data.replace("state", false);
						Program.setTool(Program.toolpainter);
						super.mouseClick(button);
					}
				};
		painterVolumeVox.setParent(painterOptionsBG);
		painterVolumeVox.setPositon(.60f, .14f, true);
		GUI.AddElement(painterVolumeVox);

		GUI.layout.add(new GUIlayout(painterVolumeVox.ID, false));
		GUI.layout.add(new GUIlayout(painterPerVox.ID, false));
		GUI.layout.add(new GUIlayout(GUI.PAINTER_BACKGROUND, false));

		SpriteButton painterButton = new SpriteButton(GUI.getNextID(), "/data/brush.png", "/data/brush_highlight.png")
		{
			public void mouseClick(int button)
			{
				GUIelement p = GUI.get(GUI.PAINTER_BACKGROUND);
				if (!(Boolean) p.data.get("state"))
				{
					p.doTrans("ontrans");
					p.Broadcast(GUI.MESSAGE_GUI_MENU_TRANS_ON);
					p.data.replace("state", true);
				}
				else
				{
					p.doTrans("offtrans");
					p.data.replace("state", false);
				}
				super.mouseClick(button);
			}
		};
		painterButton.setPositon(width / 2f - 1f * 75f - (float) Scale.hUnSizeScale(painterButton.width * .25f), 0f);
		GUI.AddElement(painterButton);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// EDIT TOOL
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		GUIelement editOptionsBG = new GUIelement(GUI.EDITER_BACKGROUND)
		{

		};
		editOptionsBG.setPositon(width / 2f, -100f);
		editOptionsBG.appearence.Add("bg", new Sprite("/data/sub_toolmenu_someoptions.png", editOptionsBG));
		editOptionsBG.x -= editOptionsBG.width / 2f;
		editOptionsBG.data.put("state", false);
		editOptionsBG.transitions.put("ontrans", new GUItransition((float) Scale.vPosScale(75), false));
		editOptionsBG.transitions.put("offtrans", new GUItransition((float) Scale.vPosScale(-100), false));
		GUI.AddElement(editOptionsBG);

		SpriteButton editAddVox =
				new SpriteButton(GUI.getNextID(), "/data/voxel_add.png", "/data/voxel_add_highlight.png")
				{
					public void mouseClick(int button)
					{
						GUIelement el = GUI.get(GUI.EDITER_BACKGROUND);
						el.doTrans("offtrans");
						el.data.replace("state", false);
						Program.setTool(Program.tooledit);
						Program.tooledit.setState(ToolEdit.STATE_ADD);
						super.mouseClick(button);
					}
				};
		editAddVox.setParent(editOptionsBG);
		editAddVox.setPositon(.15f, .14f, true);
		GUI.AddElement(editAddVox);

		SpriteButton editRemoveVox =
				new SpriteButton(GUI.getNextID(), "/data/voxel_remove.png", "/data/voxel_remove_highlight.png")
				{
					public void mouseClick(int button)
					{
						GUIelement el = GUI.get(GUI.EDITER_BACKGROUND);
						el.doTrans("offtrans");
						el.data.replace("state", false);
						Program.setTool(Program.tooledit);
						Program.tooledit.setState(ToolEdit.STATE_REMOVE);
						super.mouseClick(button);
					}
				};
		editRemoveVox.setParent(editOptionsBG);
		editRemoveVox.setPositon(.60f, .14f, true);
		GUI.AddElement(editRemoveVox);

		GUI.layout.add(new GUIlayout(editAddVox.ID, false));
		GUI.layout.add(new GUIlayout(editRemoveVox.ID, false));
		GUI.layout.add(new GUIlayout(GUI.EDITER_BACKGROUND, false));

		SpriteButton editbutton = new SpriteButton(GUI.getNextID(), "/data/edit.png", "/data/edit_highlight.png")
		{
			public void mouseClick(int button)
			{
				GUIelement p = GUI.get(GUI.EDITER_BACKGROUND);
				if (!(Boolean) p.data.get("state"))
				{
					p.doTrans("ontrans");
					p.Broadcast(GUI.MESSAGE_GUI_MENU_TRANS_ON);
					p.data.replace("state", true);
				}
				else
				{
					p.doTrans("offtrans");
					p.data.replace("state", false);
				}
				super.mouseClick(button);
			}
		};
		editbutton.setPositon(width / 2f - (float) Scale.hUnSizeScale(editbutton.width * .25f), 0f);
		GUI.AddElement(editbutton);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// SAVE TOOL
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		GUIelement saveBG = new GUIelement(GUI.SAVE_BACKGROUND)
		{

		};
		saveBG.setPositon(width / 2f + 1f * 75f, -100f);
		saveBG.appearence.Add("bg", new Sprite("/data/sub_toolmenu_oneoption.png", saveBG));
		saveBG.x -= saveBG.width / 2f;
		saveBG.data.put("state", false);
		saveBG.transitions.put("ontrans", new GUItransition((float) Scale.vPosScale(75), false));
		saveBG.transitions.put("offtrans", new GUItransition((float) Scale.vPosScale(-100), false));
		GUI.AddElement(saveBG);

		SpriteButton saveqb = new SpriteButton(GUI.getNextID(), "/data/qbsave.png", "/data/qbsave_highlight.png")
		{
			public void mouseClick(int button)
			{
				GUIelement el = GUI.get(GUI.SAVE_BACKGROUND);
				el.doTrans("offtrans");
				el.data.replace("state", false);
				QbUtil.writeQB();
				super.mouseClick(button);
			}
		};
		saveqb.setParent(saveBG);
		saveqb.setPositon(.22f, .14f, true);
		GUI.AddElement(saveqb);

		GUI.layout.add(new GUIlayout(saveqb.ID, false));
		GUI.layout.add(new GUIlayout(GUI.SAVE_BACKGROUND, false));

		SpriteButton savebutton = new SpriteButton(GUI.getNextID(), "/data/save.png", "/data/save_highlight.png")
		{
			public void mouseClick(int button)
			{
				GUIelement p = GUI.get(GUI.SAVE_BACKGROUND);
				if (!(Boolean) p.data.get("state"))
				{
					p.doTrans("ontrans");
					p.Broadcast(GUI.MESSAGE_GUI_MENU_TRANS_ON);
					p.data.replace("state", true);
				}
				else
				{
					p.doTrans("offtrans");
					p.data.replace("state", false);
				}
				super.mouseClick(button);
			}
		};
		savebutton.setPositon(width / 2f + 1f * 75f - (float) Scale.hUnSizeScale(savebutton.width * .25f), 0f);
		GUI.AddElement(savebutton);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// SETTINGS TOOL
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		SpriteButton settingsbutton =
				new SpriteButton(GUI.getNextID(), "/data/settings.png", "/data/settings_highlight.png")
				{
				};
		settingsbutton.setPositon(width / 2f + 2f * 75f - (float) Scale.hUnSizeScale(settingsbutton.width * .25f), 0f);
		GUI.AddElement(settingsbutton);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// COLOR OPTIONS
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		for (int i = 0; i < 10; i++)
		{
			float location = height / 2f - 45f;

			if (i < 5)
			{
				location += (5 - i) * 50f;
			}
			else
			{
				location -= (i - 5) * 50f;
			}

			ColorOption t =
					new ColorOption(GUI.getNextID(), location, new Color(1f - (10 - i) * .1f, 1f - ((10 - i) * .06f),
							1f - ((10 - i) * .03f)))
					{
						public void mouseEnter()
						{
							PlainBorder border = this.getBorder();
							border.color = Color.gray.brighter(.75f);
							super.mouseEnter();
						}

						public void mouseLeave()
						{
							PlainBorder border = this.getBorder();
							border.color = Color.gray;
							super.mouseLeave();
						}

						public void mouseClick(int button)
						{
							if (button == 0)
							{
								GUIelement el = GUI.get(GUI.COLOR_PICKER_BACKGROUND);
								if (isActive() && !(Boolean) el.data.get("state"))
								{
									el.y = y + height / 2f - el.height / 2f;
									el.doTrans("ontrans");
									el.Broadcast(GUI.MESSAGE_GUI_MENU_TRANS_ON);
									el.data.replace("state", true);
								}
								else if ((Boolean) el.data.get("state"))
								{
									float location = y + height / 2f - el.height / 2f;
									if (el.y != location)
									{
										el.doTransY(location);
									}
									else
									{
										el.doTrans("offtrans");
										el.data.replace("state", false);
									}
								}
								select();
							}
							super.mouseClick(button);
						}
					};

			GUI.AddElement(t);

			if (i == 0)
			{
				colorpickerBG.y = t.y + t.height / 2f - colorpickerBG.height / 2f;
				t.select();

				KeyboardUtil.Add(2, new Keyhook(t)
				{
					@Override
					public void down()
					{
						ColorOption g = (ColorOption) this.obj;

						GUIelement el = GUI.get(GUI.COLOR_PICKER_BACKGROUND);
						if (g.isActive() && !(Boolean) el.data.get("state"))
						{
							el.y = g.y + g.height / 2f - el.height / 2f;
							el.doTrans("ontrans");
							el.Broadcast(GUI.MESSAGE_GUI_MENU_TRANS_ON);
							el.data.replace("state", true);
						}
						else if ((Boolean) el.data.get("state"))
						{
							float location = g.y + g.height / 2f - el.height / 2f;
							if (el.y != location)
							{
								el.doTransY(location);
							}
							else
							{
								el.doTrans("offtrans");
								el.data.replace("state", false);
							}
						}

						g.select();
						super.down();
					}
				});
			}
			else
			{
				KeyboardUtil.Add(i + 2, new Keyhook(t)
				{
					@Override
					public void down()
					{
						ColorOption g = (ColorOption) this.obj;

						GUIelement el = GUI.get(GUI.COLOR_PICKER_BACKGROUND);
						if (g.isActive() && !(Boolean) el.data.get("state"))
						{
							el.y = g.y + g.height / 2f - el.height / 2f;
							el.doTrans("ontrans");
							el.Broadcast(GUI.MESSAGE_GUI_MENU_TRANS_ON);
							el.data.replace("state", true);
						}
						else if ((Boolean) el.data.get("state"))
						{
							float location = g.y + g.height / 2f - el.height / 2f;
							if (el.y != location)
							{
								el.doTransY(location);
							}
							else
							{
								el.doTrans("offtrans");
								el.data.replace("state", false);
							}
						}

						g.select();
						super.down();
					}
				});
			}
		}

		GUI.Layout();

	}

	public static void Hacked1080pGUI()
	{
		float width = (float) Program.width;
		float height = (float) Program.height;

		GUIelement.baseTransSpeedX = (float) Scale.hSizeScale(4f);
		GUIelement.baseTransSpeedY = (float) Scale.vSizeScale(4f);

		text.clear();

		hackscalex = .95f;
		hackscaley = .95f;

		Textbox proTextbox = (Textbox) GUI.get(GUI.PROJECTSETTINGS_NAME);
		String proName = "";
		Textbox proSizeTextbox = (Textbox) GUI.get(GUI.PROJECTSETTINGS_SIZE);
		String proSize = "";

		if (proTextbox != null)
			proName = proTextbox.text;
		if (proSizeTextbox != null)
			proSize = proSizeTextbox.text;

		GUI.dispose();

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// PROJECT STATUS
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		Label statuslabel = new Label(GUI.STATUS_LABEL, "", Color.yellow);
		statuslabel.setPositon(0, 0);
		GUI.AddElement(statuslabel);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// PROJECT SETTINGS
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		GUIelement projectSettingsBG = new GUIelement(GUI.PROJECTSETTINGS_BACKGROUND)
		{
			@Override
			public void onMessageRecieved(String message, Object... args)
			{
				if (message == MESSAGE_GUI_MENU_TRANS_ON)
				{
					GUIelement button = GUI.get(PROJECTSETTINGS_BUTTON);
					if (!(Boolean) button.data.get("buttonstate"))
					{
						button.data.replace("buttonstate", true);

						button.getSprite("off_bg").setEnabled(false);
						button.getSprite("off_bg_high").setEnabled(false);
						button.getSprite("on_bg").setEnabled(true);
					}
				}
				super.onMessageRecieved(message, args);
			}
		};
		projectSettingsBG.data.put("state", true);
		projectSettingsBG.appearence.Add("background", new Sprite("/data/project_settings.png", projectSettingsBG));
		projectSettingsBG.setPositon(width - 65f,
				height / 2f - (float) Scale.vUnSizeScale(projectSettingsBG.height * .25f));
		projectSettingsBG.transitions.put(
				"ontrans",
				new GUItransition((float) Scale.hPosScale(width - (float) Scale.hUnSizeScale(projectSettingsBG.width)
						/ 2f), true));
		projectSettingsBG.transitions.put("offtrans", new GUItransition((float) Scale.hPosScale(width - 65f), true));
		GUI.AddElement(projectSettingsBG);

		GUIelement projectButton = new GUIelement(GUI.PROJECTSETTINGS_BUTTON)
		{
			public void mouseClick(int button)
			{
				if ((Boolean) data.get("buttonstate"))
				{
					data.replace("buttonstate", false);

					this.getSprite("on_bg").setEnabled(false);
					this.getSprite("on_bg_high").setEnabled(false);
					this.getSprite("off_bg").setEnabled(true);

					GUI.get(GUI.PROJECTSETTINGS_BACKGROUND).doTrans("ontrans");
					GUI.get(GUI.PROJECTSETTINGS_BACKGROUND).Broadcast(MESSAGE_GUI_MENU_TRANS_ON);
				}
				else
				{
					data.replace("buttonstate", true);

					this.getSprite("off_bg").setEnabled(false);
					this.getSprite("off_bg_high").setEnabled(false);
					this.getSprite("on_bg").setEnabled(true);

					GUI.get(GUI.PROJECTSETTINGS_BACKGROUND).doTrans("offtrans");
				}

				super.mouseClick(button);
			}

			public void mouseEnter()
			{
				if ((Boolean) data.get("buttonstate"))
				{
					this.getSprite("on_bg").setEnabled(false);
					this.getSprite("on_bg_high").setEnabled(true);
				}
				else
				{
					this.getSprite("off_bg").setEnabled(false);
					this.getSprite("off_bg_high").setEnabled(true);
				}

				super.mouseEnter();
			}

			public void mouseLeave()
			{
				if ((Boolean) data.get("buttonstate"))
				{
					this.getSprite("on_bg").setEnabled(true);
					this.getSprite("on_bg_high").setEnabled(false);
				}
				else
				{
					this.getSprite("off_bg").setEnabled(true);
					this.getSprite("off_bg_high").setEnabled(false);
				}

				super.mouseLeave();
			}
		};

		projectButton.data.put("buttonstate", true);
		projectButton.appearence.Add("on_bg", new Sprite("/data/project_settings_button_on.png", projectButton));
		projectButton.appearence.Add("on_bg_high", new Sprite("/data/project_settings_button_on_highlight.png",
				projectButton));
		projectButton.appearence.Add("off_bg", new Sprite("/data/project_settings_button_off.png", projectButton));
		projectButton.appearence.Add("off_bg_high", new Sprite("/data/project_settings_button_off_highlight.png",
				projectButton));
		projectButton.getSprite("on_bg_high").setEnabled(false);
		projectButton.getSprite("off_bg").setEnabled(false);
		projectButton.getSprite("off_bg_high").setEnabled(false);
		projectButton.setParent(projectSettingsBG);
		projectButton.setPositon(.0286f, .838f, true);
		GUI.AddElement(projectButton);

		Label projectname = new Label(getNextID(), "File Name :", Color.white);
		projectname.setParent(projectSettingsBG);
		projectname.setPositon(.2f, .90f, true);
		GUI.AddElement(projectname);

		Textbox projectnametextbox = new Textbox(GUI.PROJECTSETTINGS_NAME, 350f);
		projectnametextbox.setParent(projectSettingsBG);
		projectnametextbox.setPositon(.2f, .845f, true);
		projectnametextbox.setText("untitled");
		GUI.AddElement(projectnametextbox);

		Label projectsize = new Label(getNextID(), "Size : (xyz)", Color.white);
		projectsize.setParent(projectSettingsBG);
		projectsize.setPositon(.2f, .785f, true);
		GUI.AddElement(projectsize);

		Textbox projectsizetextbox = new Textbox(GUI.PROJECTSETTINGS_SIZE, 350f)
		{
			public void OnReturnKey()
			{
				int _index1 = text.indexOf("_");
				int _index2 = text.indexOf("_", _index1 + 1);

				if (_index1 == -1 || _index2 == -1)
				{
					setText(lasttext);
					return;
				}

				int x = Integer.parseInt(text.substring(0, _index1));
				int y = Integer.parseInt(text.substring(_index1 + 1, _index2));
				int z = Integer.parseInt(text.substring(_index2 + 1));
				Program.model.GetActiveMatrix().reSize(x, y, z);
				super.OnReturnKey();
			}

			@Override
			public void focusLost()
			{
				int _index1 = text.indexOf("_");
				int _index2 = text.indexOf("_", _index1 + 1);

				if (_index1 == -1 || _index2 == -1)
				{
					setText(lasttext);
					return;
				}

				int x = Integer.parseInt(text.substring(0, _index1));
				int y = Integer.parseInt(text.substring(_index1 + 1, _index2));
				int z = Integer.parseInt(text.substring(_index2 + 1));
				Program.model.GetActiveMatrix().reSize(x, y, z);
				super.focusLost();
			}
		};
		projectsizetextbox.setParent(projectSettingsBG);
		projectsizetextbox.setPositon(.2f, .730f, true);
		projectsizetextbox.setText("10_10_10");
		GUI.AddElement(projectsizetextbox);

		GUI.layout.add(new GUIlayout(GUI.PROJECTSETTINGS_BACKGROUND, false));
		GUI.layout.add(new GUIlayout(GUI.PROJECTSETTINGS_BUTTON, true));

		if (proName != "")
		{
			projectnametextbox.setText(proName);
		}
		if (proSize != "")
		{
			projectsizetextbox.setText(proSize);
		}

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// BACKGROUNDS
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		// color options bg
		GUIelement colorOptionBG = new GUIelement(GUI.getNextID());
		colorOptionBG.appearence.Add("bg", new Sprite("/data/colorselector_background.png", colorOptionBG));
		colorOptionBG.setPositon(0, height / 2f - (float) Scale.vUnSizeScale(colorOptionBG.height * .25f));
		GUI.AddElement(colorOptionBG);

		// tool menu bg
		GUIelement toolMenuBG = new GUIelement(GUI.getNextID());
		toolMenuBG.appearence.Add("bg", new Sprite("/data/toolmenu_background.png", toolMenuBG));
		toolMenuBG.setPositon(width / 2f - (float) Scale.hUnSizeScale(toolMenuBG.width * .25f), 0f);
		GUI.AddElement(toolMenuBG);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// COLOR PICKER TOOL
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		GUIelement colorpickerBG = new GUIelement(GUI.COLOR_PICKER_BACKGROUND)
		{
			@Override
			public void onDoTransistionByName(String name)
			{
				if (name == "offtrans")
				{
					GUIelement g = GUI.get(GUI.COLOR_PICKER_COLORSQARE);
					if (g != null)
					{
						g.getPlainMarker("marker").setPosition(-10, 10);
					}
				}
				super.onDoTransistionByName(name);
			}
		};
		colorpickerBG.appearence.Add("bg", new Sprite("/data/colorselector.png", colorpickerBG));
		colorpickerBG.setPositon(-300, 0);
		colorpickerBG.transitions.put("ontrans", new GUItransition((float) Scale.hPosScale(95f), true));
		colorpickerBG.transitions.put("offtrans", new GUItransition((float) Scale.hPosScale(-300f), true));
		colorpickerBG.data.put("state", false);
		GUI.AddElement(colorpickerBG);

		int colorswatches = GUI.getNextID();

		GUIelement colorpickerSwatches = new GUIelement(colorswatches)
		{
			@Override
			public void mouseMove(float x, float y)
			{
				GUIelement colorpicker = GUI.get(GUI.COLOR_PICKER_COLORSQARE);
				if (colorpicker != null)
				{
					GL11.glReadBuffer(GL11.GL_FRONT);
					ByteBuffer buffer = BufferUtils.createByteBuffer(4);
					GL11.glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

					int r = buffer.get(0) & 0xFF;
					int g = buffer.get(1) & 0xFF;
					int b = buffer.get(2) & 0xFF;

					ColorOption.lastOption.setColor(new Color(r, g, b));
					ColorOption.lastOption.huecolor = new Color(r, g, b);
					Program.toolpainter.paintColor = ColorOption.lastOption.color;

					colorpicker.getMultiColorBackground("mcBG").setColor(2, new Color(r, g, b));
					colorpicker.getMultiColorBackground("mcBG").updateGradient();
				}
				super.mouseMove(x, y);
			}
		};
		colorpickerSwatches.setParent(colorpickerBG);
		colorpickerSwatches.setPositon(0, 0, true);
		colorpickerSwatches.appearence.Add("bg2", new Sprite("/data/colorpicker_swatches.png", colorpickerSwatches));
		GUI.AddElement(colorpickerSwatches);

		GUIelement colorpickerColorSquare = new GUIelement(GUI.COLOR_PICKER_COLORSQARE)
		{
			@Override
			public void mouseMove(float x, float y)
			{
				GUIelement colorpicker = GUI.get(GUI.COLOR_PICKER_COLORSQARE);
				if (colorpicker != null)
				{
					GL11.glReadBuffer(GL11.GL_FRONT);
					ByteBuffer buffer = BufferUtils.createByteBuffer(4);
					GL11.glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

					int r = buffer.get(0) & 0xFF;
					int g = buffer.get(1) & 0xFF;
					int b = buffer.get(2) & 0xFF;

					ColorOption.lastOption.setColor(new Color(r, g, b));
					Program.toolpainter.paintColor = ColorOption.lastOption.color;

					getPlainMarker("marker").setPosition(-10, 10);
				}
				super.mouseMove(x, y);
			}

			@Override
			public void mouseUp(int button)
			{
				GL11.glReadBuffer(GL11.GL_FRONT);
				ByteBuffer buffer = BufferUtils.createByteBuffer(4);
				GL11.glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

				int r = buffer.get(0) & 0xFF;
				int g = buffer.get(1) & 0xFF;
				int b = buffer.get(2) & 0xFF;

				float nx = (float) Scale.scale(Mouse.getX(), 0, Program.width, -1, 1);
				float ny = (float) Scale.scale(Mouse.getY(), 0, Program.height, -1, 1);

				Color c = new Color(1, 1, 1);
				float average = r + g + b / 3f;

				if (average > 256f / 2f)
				{
					c = Color.black;
				}
				else
					c = Color.white;

				ColorOption.lastOption.setColoredSquareLocation(nx - (x + getParent().x), ny - (y + getParent().y));

				getPlainMarker("marker").setPositionColor(nx - (x + getParent().x), ny - (y + getParent().y), c);
				super.mouseUp(button);
			}
		};
		colorpickerColorSquare.setParent(colorpickerBG);
		colorpickerColorSquare.setSize(colorpickerBG.getUnScaleWidth() / 2f * .63f,
				colorpickerBG.getUnScaleHeight() / 2f * .72f);
		colorpickerColorSquare.setPositon(.12f, .2f, true);
		colorpickerColorSquare.appearence.Add("mcBG", new MultiColorBackground(Color.black, Color.black, Color.blue,
				Color.white));
		colorpickerColorSquare.appearence.Add("marker", new PlainMarker());
		colorpickerColorSquare.appearence.Add("border", new PlainBorder(2f, Color.yellow.darker(.4f)));
		colorpickerColorSquare.getMultiColorBackground("mcBG").updateGradient(colorpickerColorSquare.width,
				colorpickerColorSquare.height);
		GUI.AddElement(colorpickerColorSquare);

		GUIelement colorpickerHueSlider = new GUIelement(GUI.COLOR_PICKER_HUESLIDER)
		{
			@Override
			public void mouseMove(float x, float y)
			{
				GUIelement colorpicker = GUI.get(GUI.COLOR_PICKER_COLORSQARE);
				if (colorpicker != null)
				{
					GL11.glReadBuffer(GL11.GL_FRONT);
					ByteBuffer buffer = BufferUtils.createByteBuffer(4);
					GL11.glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

					int r = buffer.get(0) & 0xFF;
					int g = buffer.get(1) & 0xFF;
					int b = buffer.get(2) & 0xFF;

					ColorOption.lastOption.huecolor = new Color(r, g, b);

					colorpicker.getMultiColorBackground("mcBG").setColor(2, new Color(r, g, b));
					colorpicker.getMultiColorBackground("mcBG").updateGradient();
				}
				super.mouseMove(x, y);
			}
		};
		colorpickerHueSlider.setSize(27, colorpickerBG.getUnScaleHeight() / 2f * .72f);
		colorpickerHueSlider.appearence.Add("gradientBG", new GradientBackground(new GradientStop(.45f, Color.red),
				new GradientStop(.15f, Color.green), new GradientStop(.35f, Color.blue), new GradientStop(.05f,
						Color.red)));
		colorpickerHueSlider.appearence.Add("border", new PlainBorder(2f, Color.yellow.darker(.4f)));
		colorpickerHueSlider.getGradientBackground("gradientBG").updateGradient(colorpickerHueSlider.width,
				colorpickerHueSlider.height);
		colorpickerHueSlider.setParent(colorpickerBG);
		colorpickerHueSlider.setPositon(.80f, .2f, true);
		GUI.AddElement(colorpickerHueSlider);

		GUI.layout.add(new GUIlayout(GUI.COLOR_PICKER_HUESLIDER, false));
		GUI.layout.add(new GUIlayout(GUI.COLOR_PICKER_COLORSQARE, false));
		GUI.layout.add(new GUIlayout(colorswatches, false));
		GUI.layout.add(new GUIlayout(GUI.COLOR_PICKER_BACKGROUND, false));

		float tooloffset = 73f;

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// SELECTION TOOL
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		SpriteButton selbutton =
				new SpriteButton(GUI.getNextID(), "/data/selection.png", "/data/selection_highlight.png")
				{
					public void mouseDoubleClick(int button)
					{
						// this.doTransX(1f);
						// this.doTransY(0f);
						super.mouseEnter();
					}
				};
		selbutton.setPositon(width / 2f - 2f * tooloffset - (float) Scale.hUnSizeScale(selbutton.width * .25f), 0);
		GUI.AddElement(selbutton);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// PAINTER TOOL
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		GUIelement painterOptionsBG = new GUIelement(GUI.PAINTER_BACKGROUND)
		{

		};
		painterOptionsBG.setPositon(width / 2f - 1f * tooloffset, -100f);
		painterOptionsBG.appearence.Add("bg", new Sprite("/data/sub_toolmenu_someoptions.png", painterOptionsBG));
		painterOptionsBG.x -= painterOptionsBG.width / 2f;
		painterOptionsBG.data.put("state", false);
		painterOptionsBG.transitions.put("ontrans", new GUItransition((float) Scale.vPosScale(75), false));
		painterOptionsBG.transitions.put("offtrans", new GUItransition((float) Scale.vPosScale(-100), false));
		GUI.AddElement(painterOptionsBG);

		SpriteButton painterPerVox =
				new SpriteButton(GUI.getNextID(), "/data/voxel_brush.png", "/data/voxel_brush_highlight.png")
				{
					public void mouseClick(int button)
					{
						GUIelement el = GUI.get(GUI.PAINTER_BACKGROUND);
						el.doTrans("offtrans");
						el.data.replace("state", false);
						Program.setTool(Program.toolpainter);
						super.mouseClick(button);
					}
				};
		painterPerVox.setParent(painterOptionsBG);
		painterPerVox.setPositon(.15f, .14f, true);
		GUI.AddElement(painterPerVox);

		SpriteButton painterVolumeVox =
				new SpriteButton(GUI.getNextID(), "/data/voxel_bucket.png", "/data/voxel_bucket_highlight.png")
				{
					public void mouseClick(int button)
					{
						GUIelement el = GUI.get(GUI.PAINTER_BACKGROUND);
						el.doTrans("offtrans");
						el.data.replace("state", false);
						Program.setTool(Program.toolpainter);
						super.mouseClick(button);
					}
				};
		painterVolumeVox.setParent(painterOptionsBG);
		painterVolumeVox.setPositon(.60f, .14f, true);
		GUI.AddElement(painterVolumeVox);

		GUI.layout.add(new GUIlayout(painterVolumeVox.ID, false));
		GUI.layout.add(new GUIlayout(painterPerVox.ID, false));
		GUI.layout.add(new GUIlayout(GUI.PAINTER_BACKGROUND, false));

		SpriteButton painterButton = new SpriteButton(GUI.getNextID(), "/data/brush.png", "/data/brush_highlight.png")
		{
			public void mouseClick(int button)
			{
				GUIelement p = GUI.get(GUI.PAINTER_BACKGROUND);
				if (!(Boolean) p.data.get("state"))
				{
					p.doTrans("ontrans");
					p.Broadcast(GUI.MESSAGE_GUI_MENU_TRANS_ON);
					p.data.replace("state", true);
				}
				else
				{
					p.doTrans("offtrans");
					p.data.replace("state", false);
				}
				super.mouseClick(button);
			}
		};
		painterButton.setPositon(width / 2f - 1f * 75f - (float) Scale.hUnSizeScale(painterButton.width * .25f), 0f);
		GUI.AddElement(painterButton);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// EDIT TOOL
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		GUIelement editOptionsBG = new GUIelement(GUI.EDITER_BACKGROUND)
		{

		};
		editOptionsBG.setPositon(width / 2f, -100f);
		editOptionsBG.appearence.Add("bg", new Sprite("/data/sub_toolmenu_someoptions.png", editOptionsBG));
		editOptionsBG.x -= editOptionsBG.width / 2f;
		editOptionsBG.data.put("state", false);
		editOptionsBG.transitions.put("ontrans", new GUItransition((float) Scale.vPosScale(75), false));
		editOptionsBG.transitions.put("offtrans", new GUItransition((float) Scale.vPosScale(-100), false));
		GUI.AddElement(editOptionsBG);

		SpriteButton editAddVox =
				new SpriteButton(GUI.getNextID(), "/data/voxel_add.png", "/data/voxel_add_highlight.png")
				{
					public void mouseClick(int button)
					{
						GUIelement el = GUI.get(GUI.EDITER_BACKGROUND);
						el.doTrans("offtrans");
						el.data.replace("state", false);
						Program.setTool(Program.tooledit);
						Program.tooledit.setState(ToolEdit.STATE_ADD);
						super.mouseClick(button);
					}
				};
		editAddVox.setParent(editOptionsBG);
		editAddVox.setPositon(.15f, .14f, true);
		GUI.AddElement(editAddVox);

		SpriteButton editRemoveVox =
				new SpriteButton(GUI.getNextID(), "/data/voxel_remove.png", "/data/voxel_remove_highlight.png")
				{
					public void mouseClick(int button)
					{
						GUIelement el = GUI.get(GUI.EDITER_BACKGROUND);
						el.doTrans("offtrans");
						el.data.replace("state", false);
						Program.setTool(Program.tooledit);
						Program.tooledit.setState(ToolEdit.STATE_REMOVE);
						super.mouseClick(button);
					}
				};
		editRemoveVox.setParent(editOptionsBG);
		editRemoveVox.setPositon(.60f, .14f, true);
		GUI.AddElement(editRemoveVox);

		GUI.layout.add(new GUIlayout(editAddVox.ID, false));
		GUI.layout.add(new GUIlayout(editRemoveVox.ID, false));
		GUI.layout.add(new GUIlayout(GUI.EDITER_BACKGROUND, false));

		SpriteButton editbutton = new SpriteButton(GUI.getNextID(), "/data/edit.png", "/data/edit_highlight.png")
		{
			public void mouseClick(int button)
			{
				GUIelement p = GUI.get(GUI.EDITER_BACKGROUND);
				if (!(Boolean) p.data.get("state"))
				{
					p.doTrans("ontrans");
					p.Broadcast(GUI.MESSAGE_GUI_MENU_TRANS_ON);
					p.data.replace("state", true);
				}
				else
				{
					p.doTrans("offtrans");
					p.data.replace("state", false);
				}
				super.mouseClick(button);
			}
		};
		editbutton.setPositon(width / 2f - (float) Scale.hUnSizeScale(editbutton.width * .25f), 0f);
		GUI.AddElement(editbutton);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// SAVE TOOL
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		GUIelement saveBG = new GUIelement(GUI.SAVE_BACKGROUND)
		{

		};
		saveBG.setPositon(width / 2f + 1f * tooloffset, -100f);
		saveBG.appearence.Add("bg", new Sprite("/data/sub_toolmenu_oneoption.png", saveBG));
		saveBG.x -= saveBG.width / 2f;
		saveBG.data.put("state", false);
		saveBG.transitions.put("ontrans", new GUItransition((float) Scale.vPosScale(75), false));
		saveBG.transitions.put("offtrans", new GUItransition((float) Scale.vPosScale(-100), false));
		GUI.AddElement(saveBG);

		SpriteButton saveqb = new SpriteButton(GUI.getNextID(), "/data/qbsave.png", "/data/qbsave_highlight.png")
		{
			public void mouseClick(int button)
			{
				GUIelement el = GUI.get(GUI.SAVE_BACKGROUND);
				el.doTrans("offtrans");
				el.data.replace("state", false);
				QbUtil.writeQB();
				super.mouseClick(button);
			}
		};
		saveqb.setParent(saveBG);
		saveqb.setPositon(.22f, .14f, true);
		GUI.AddElement(saveqb);

		GUI.layout.add(new GUIlayout(saveqb.ID, false));
		GUI.layout.add(new GUIlayout(GUI.SAVE_BACKGROUND, false));

		SpriteButton savebutton = new SpriteButton(GUI.getNextID(), "/data/save.png", "/data/save_highlight.png")
		{
			public void mouseClick(int button)
			{
				GUIelement p = GUI.get(GUI.SAVE_BACKGROUND);
				if (!(Boolean) p.data.get("state"))
				{
					p.doTrans("ontrans");
					p.Broadcast(GUI.MESSAGE_GUI_MENU_TRANS_ON);
					p.data.replace("state", true);
				}
				else
				{
					p.doTrans("offtrans");
					p.data.replace("state", false);
				}
				super.mouseClick(button);
			}
		};
		savebutton.setPositon(width / 2f + 1f * 75f - (float) Scale.hUnSizeScale(savebutton.width * .25f), 0f);
		GUI.AddElement(savebutton);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// SETTINGS TOOL
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		SpriteButton settingsbutton =
				new SpriteButton(GUI.getNextID(), "/data/settings.png", "/data/settings_highlight.png")
				{
				};
		settingsbutton.setPositon(
				width / 2f + 2f * tooloffset - (float) Scale.hUnSizeScale(settingsbutton.width * .25f), 0f);
		GUI.AddElement(settingsbutton);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// COLOR OPTIONS
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		for (int i = 0; i < 10; i++)
		{
			float location = height / 2f - 43f;

			if (i < 5)
			{
				location += (5 - i) * 48f;
			}
			else
			{
				location -= (i - 5) * 48f;
			}

			ColorOption t =
					new ColorOption(GUI.getNextID(), location, new Color(1f - (10 - i) * .1f, 1f - ((10 - i) * .06f),
							1f - ((10 - i) * .03f)))
					{
						public void mouseEnter()
						{
							PlainBorder border = this.getBorder();
							border.color = Color.gray.brighter(.75f);
							super.mouseEnter();
						}

						public void mouseLeave()
						{
							PlainBorder border = this.getBorder();
							border.color = Color.gray;
							super.mouseLeave();
						}

						public void mouseClick(int button)
						{
							if (button == 0)
							{
								GUIelement el = GUI.get(GUI.COLOR_PICKER_BACKGROUND);
								if (isActive() && !(Boolean) el.data.get("state"))
								{
									el.y = y + height / 2f - el.height / 2f;
									el.doTrans("ontrans");
									el.Broadcast(GUI.MESSAGE_GUI_MENU_TRANS_ON);
									el.data.replace("state", true);
								}
								else if ((Boolean) el.data.get("state"))
								{
									float location = y + height / 2f - el.height / 2f;
									if (el.y != location)
									{
										el.doTransY(location);
									}
									else
									{
										el.doTrans("offtrans");
										el.data.replace("state", false);
									}
								}

								select();
							}
							super.mouseClick(button);
						}
					};

			GUI.AddElement(t);

			if (i == 0)
			{
				KeyboardUtil.Add(2, new Keyhook(t)
				{
					@Override
					public void down()
					{
						ColorOption g = (ColorOption) this.obj;

						GUIelement el = GUI.get(GUI.COLOR_PICKER_BACKGROUND);
						if (g.isActive() && !(Boolean) el.data.get("state"))
						{
							el.y = g.y + g.height / 2f - el.height / 2f;
							el.doTrans("ontrans");
							el.Broadcast(GUI.MESSAGE_GUI_MENU_TRANS_ON);
							el.data.replace("state", true);
						}
						else if ((Boolean) el.data.get("state"))
						{
							float location = g.y + g.height / 2f - el.height / 2f;
							if (el.y != location)
							{
								el.doTransY(location);
							}
							else
							{
								el.doTrans("offtrans");
								el.data.replace("state", false);
							}
						}

						g.select();
						super.down();
					}
				});
			}
			else
			{
				KeyboardUtil.Add(i + 2, new Keyhook(t)
				{
					@Override
					public void down()
					{
						ColorOption g = (ColorOption) this.obj;

						GUIelement el = GUI.get(GUI.COLOR_PICKER_BACKGROUND);
						if (g.isActive() && !(Boolean) el.data.get("state"))
						{
							el.y = g.y + g.height / 2f - el.height / 2f;
							el.doTrans("ontrans");
							el.Broadcast(GUI.MESSAGE_GUI_MENU_TRANS_ON);
							el.data.replace("state", true);
						}
						else if ((Boolean) el.data.get("state"))
						{
							float location = g.y + g.height / 2f - el.height / 2f;
							if (el.y != location)
							{
								el.doTransY(location);
							}
							else
							{
								el.doTrans("offtrans");
								el.data.replace("state", false);
							}
						}

						g.select();
						super.down();
					}
				});
			}
		}
		GUI.Layout();

	}
}
