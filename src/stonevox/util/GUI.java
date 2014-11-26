package stonevox.util;

import java.io.File;
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
import stonevox.data.Keyhook;
import stonevox.data.TextDisplay;
import stonevox.decorator.MultiColorBackground;
import stonevox.decorator.PlainBorder;
import stonevox.decorator.PlainMarker;
import stonevox.decorator.Sprite;
import stonevox.gui.ColorOption;
import stonevox.gui.Label;
import stonevox.gui.Listbox;
import stonevox.gui.SpriteButton;
import stonevox.gui.Tab;
import stonevox.gui.TabGroup;
import stonevox.gui.Textbox;

public class GUI
{
	public static int COLOR_PICKER_BACKGROUND = 900;
	public static int PAINTER_BACKGROUND = 901;
	public static int EDITER_ADD_BACKGROUND = 902;
	public static int EDITER_REMVOE_BACKGROUND = 903;
	public static int SAVE_BACKGROUND = 904;
	public static int PROJECTSETTINGS_BACKGROUND = 905;
	public static int PROJECTSETTINGS_BUTTON = 906;
	public static int PROJECTSETTINGS_NAME = 907;
	public static int MATRIX_NAME = 908;
	public static int MATRIX_SIZE = 909;
	public static int STATUS_LABEL = 910;
	public static int COLOR_PICKER_COLORSQARE = 911;
	public static int COLOR_PICKER_HUESLIDER = 912;
	public static int MAINTAB = 913;
	public static int MATRIXTAB = 914;
	public static int MATRIXLISTBOX = 915;
	public static int SCREENSHOT_BACKGROUND = 916;
	public static int SCREENSHOT_CAMERA_BOUNDS = 917;
	public static int SCREENSHOT_WIDTH = 918;
	public static int SCREENSHOT_HEIGHT = 919;

	public static float hackscalex = 1.0f;
	public static float hackscaley = 1.0f;
	public static float offsetx = (float) Scale.hSizeScale(6);
	public static float offsety = (float) Scale.vSizeScale(6);

	public static String MESSAGE_GUI_MENU_TRANS_ON = "gui_menu_trans_on";
	public static String MESSAGE_GUI_MENU_TRANS_OFF = "gui_menu_trans_off";
	public static String MESSAGE_MOUSE_CLICK = "gui_mouse_click";
	public static String MESSAGE_MOUSE_UP = "gui_mouse_up";
	public static String MESSAGE_MOUSE_DOUBLE_CLICK = "gui_mouse_double_click";
	public static String MESSAGE_MOUSE_ENTER = "gui_mouse_enter";
	public static String MESSAGE_MOUSE_LEAVE = "gui_mouse_leave";
	public static String MESSAGE_QB_LOADED = "qb_loaded";
	public static String MESSAGE_QB_MATRIX_RENAMED = "qb_matrix_renamed";
	public static String MESSAGE_QB_MATRIX_RESIZED = "qb_matrix_resized";
	public static String MESSAGE_QB_MATRIX_ADDED = "qb_matrix_added";
	public static String MESSAGE_QB_MATRIX_REMOVED = "qb_matrix_removed";
	public static String MESSAGE_TEXTBOX_CHANGED = "textbox_changed";
	public static String MESSAGE_TEXTBOX_COMMITED = "textbox_commited";

	static int lastControlOver = -1;
	static int lastControlFocused = -1;
	static int lastButton = -1;

	static int lastelementid = -1;
	static ArrayList<GUIelement> elements = new ArrayList<GUIelement>();
	public static ArrayList<GUIlayout> layout = new ArrayList<GUIlayout>();
	public static ArrayList<TextDisplay> text = new ArrayList<TextDisplay>();

	private static float tooloffset;
	private static float clocationscale;

	public static int coloroptionStartID;

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
					if (lastControlOver != i)
					{
						elements.get(lastControlOver).mouseLeave();

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
		if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
			return;

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
		if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
			return false;

		if (keyState && lastControlFocused != -1)
		{
			return elements.get(lastControlFocused).keyPress(key);
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
		if (el.getEnabled() && !el.hasParent())
			return x >= el.x && x <= el.x + el.width && y >= el.y && y <= el.y + el.height;
		else if (el.getEnabled() && el.hasParent())
		{
			float parsX = el.getParentsX();
			float parsY = el.getParentsY();
			return x >= parsX + el.x && x <= parsX + el.x + el.width && y >= parsY + el.y
					&& y <= parsY + el.y + el.height;
		}
		else
			return false;
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
		lastControlFocused = -1;
		lastButton = -1;
		lastControlOver = -1;

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

	public static void StandardGUI(boolean _720p)
	{
		float width = (float) Program.width;
		float height = (float) Program.height;

		GUIelement.baseTransSpeedX = (float) Scale.hSizeScale(4f);
		GUIelement.baseTransSpeedY = (float) Scale.vSizeScale(4f);

		text.clear();

		Textbox proTextbox = (Textbox) GUI.get(GUI.PROJECTSETTINGS_NAME);
		String proName = "";
		Textbox proSizeTextbox = (Textbox) GUI.get(GUI.MATRIX_SIZE);
		String proSize = "";

		if (proTextbox != null)
			proName = proTextbox.text;
		if (proSizeTextbox != null)
			proSize = proSizeTextbox.text;

		GUI.dispose();

		if (_720p)
			Hacked720pPreGUI(width, height);
		else
			Hacked1080pPreGUI(width, height);

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

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// MAIN TAB
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		Label projectname = new Label(getNextID(), "File Name :", Color.white);
		projectname.setParent(projectSettingsBG);
		projectname.setPositon(.2f, .90f, true);
		GUI.AddElement(projectname);

		Label matrixlistlabel = new Label(getNextID(), "Matrix List :", Color.white);
		matrixlistlabel.setParent(projectSettingsBG);
		matrixlistlabel.setPositon(.2f, .77f, true);
		GUI.AddElement(matrixlistlabel);

		SpriteButton addmatrixbutton =
				new SpriteButton(GUI.getNextID(), "/data/matrix_add.png", "/data/matrix_add_highlight.png")
				{
					@Override
					public void mouseClick(int button)
					{
						Program.model.addMatrix();
						super.mouseClick(button);
					}
				};
		addmatrixbutton.statusTip = Tips.addnewmatrix;
		addmatrixbutton.setParent(projectSettingsBG);
		addmatrixbutton.setPositon(.75f, .028f, true);
		GUI.AddElement(addmatrixbutton);

		SpriteButton removematrixbutton =
				new SpriteButton(GUI.getNextID(), "/data/matrix_remove.png", "/data/matrix_remove_highlight.png")
				{
					@Override
					public void mouseClick(int button)
					{
						Program.model.removeActiveMatrix();
						super.mouseClick(button);
					}
				};
		removematrixbutton.statusTip = Tips.removeselectedmatrix;
		removematrixbutton.setParent(projectSettingsBG);
		removematrixbutton.setPositon(.85f, .028f, true);
		GUI.AddElement(removematrixbutton);

		Textbox projectnametextbox = new Textbox(GUI.PROJECTSETTINGS_NAME, 350f)
		{
			@Override
			public void onMessageRecieved(String message, Object... args)
			{
				if (message == GUI.MESSAGE_QB_LOADED)
				{
					File f = new File((String) args[0]);
					this.setText(f.getName().substring(0, f.getName().length() - 3));
				}
				super.onMessageRecieved(message, args);
			}
		};
		projectnametextbox.statusTip = Tips.filename;
		projectnametextbox.setParent(projectSettingsBG);
		projectnametextbox.setPositon(.2f, .845f, true);
		projectnametextbox.setText("untitled");
		GUI.AddElement(projectnametextbox);

		if (proName != "")
		{
			projectnametextbox.setText(proName);
		}

		float fontheight = FontUtil.GetFont("default").font.getHeight("T");
		Listbox matrixlistbox = new Listbox(GUI.MATRIXLISTBOX, 350, fontheight * 14f)
		{
			@Override
			public void mouseClick(int button)
			{

				super.mouseClick(button);
			}

			@Override
			public void onMessageRecieved(String message, Object... args)
			{
				if (message == GUI.MESSAGE_QB_LOADED || message == GUI.MESSAGE_QB_MATRIX_RENAMED
						|| message == GUI.MESSAGE_QB_MATRIX_ADDED || message == GUI.MESSAGE_QB_MATRIX_REMOVED)
				{
					this.updateNames();
				}

				super.onMessageRecieved(message, args);
			}
		};
		matrixlistbox.setParent(projectSettingsBG);
		matrixlistbox.setPositon(.2f, .15f, true);
		matrixlistbox.updateNames();
		GUI.AddElement(matrixlistbox);

		// the hacks...
		ArrayList<GUIelement> maintabelements = new ArrayList<GUIelement>();
		maintabelements.add(projectname);
		maintabelements.add(projectnametextbox);
		maintabelements.add(matrixlistbox);
		maintabelements.add(matrixlistlabel);
		maintabelements.add(addmatrixbutton);
		maintabelements.add(removematrixbutton);
		// maintabelements.add(matrixlistbox.vs);
		// maintabelements.add(matrixlistbox.vs.getScrollbar());

		for (int i = 0; i < matrixlistbox.elements.size(); i++)
		{
			maintabelements.add(matrixlistbox.elements.get(i));
		}

		Tab maintab = new Tab(MAINTAB, true, maintabelements)
		{
			@Override
			public void mouseEnter()
			{
				getSprite("low_bg").setEnabled(false);
				getSprite("high_bg").setEnabled(true);

				super.mouseEnter();
			}

			@Override
			public void mouseLeave()
			{
				getSprite("low_bg").setEnabled(true);
				getSprite("high_bg").setEnabled(false);

				super.mouseLeave();
			}
		};
		maintab.setParent(projectSettingsBG);
		maintab.appearence.Add("low_bg", new Sprite("/data/tab_main.png", maintab));
		maintab.appearence.Add("high_bg", new Sprite("/data/tab_main_highlight.png"));
		maintab.getSprite("high_bg").setEnabled(false);
		maintab.setPositon(.2f, .95f, true);
		maintab.transitions.put("offtabs", new GUItransition(maintab.y, false));
		maintab.transitions.put("ontabs", new GUItransition(maintab.y + maintab.height * .25f, false));
		GUI.AddElement(maintab);

		GUI.layout.add(new GUIlayout(GUI.PROJECTSETTINGS_BACKGROUND, false));
		GUI.layout.add(new GUIlayout(MAINTAB, false));
		GUI.layout.add(new GUIlayout(GUI.PROJECTSETTINGS_BUTTON, true));

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// MATRIX TAB
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		Label matrixSize = new Label(getNextID(), "Size : (xyz)", Color.white);
		matrixSize.setParent(projectSettingsBG);
		matrixSize.setPositon(.2f, .785f, true);
		GUI.AddElement(matrixSize);

		Label matrixName = new Label(getNextID(), "Name :", Color.white);
		matrixName.setParent(projectSettingsBG);
		matrixName.setPositon(.2f, .90f, true);
		GUI.AddElement(matrixName);

		Textbox matrixNameTextbox = new Textbox(GUI.MATRIX_NAME, 350f)
		{
			public void OnReturnKey()
			{
				Program.model.GetActiveMatrix().setName(this.text);
				super.OnReturnKey();
			}

			public void focusLost()
			{
				Program.model.GetActiveMatrix().setName(this.text);
				super.focusLost();
			}

			@Override
			public void onMessageRecieved(String message, Object... args)
			{
				if (message == GUI.MESSAGE_QB_LOADED)
				{
					File f = new File((String) args[0]);
					this.setText(f.getName().substring(0, f.getName().length() - 3));
				}
				super.onMessageRecieved(message, args);
			}
		};
		matrixNameTextbox.statusTip = Tips.matrixname;
		matrixNameTextbox.setParent(projectSettingsBG);
		matrixNameTextbox.setPositon(.2f, .845f, true);
		matrixNameTextbox.setText(Program.model.GetActiveMatrix().getName());
		GUI.AddElement(matrixNameTextbox);

		Textbox matrixSizeTextbox = new Textbox(GUI.MATRIX_SIZE, 350f)
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

			@Override
			public void onMessageRecieved(String message, Object... args)
			{
				if (message == GUI.MESSAGE_QB_LOADED)
				{
					this.setText(Program.model.GetActiveMatrix().getSizeString());
				}
				super.onMessageRecieved(message, args);
			}
		};
		matrixSizeTextbox.statusTip = Tips.martixsize;
		matrixSizeTextbox.setParent(projectSettingsBG);
		matrixSizeTextbox.setPositon(.2f, .730f, true);
		matrixSizeTextbox.setText("10_10_10");
		GUI.AddElement(matrixSizeTextbox);

		if (proSize != "")
		{
			matrixSizeTextbox.setText(proSize);
		}

		Tab matrixtab = new Tab(MATRIXTAB, false, matrixSizeTextbox, matrixNameTextbox, matrixSize, matrixName)
		{
			@Override
			public void mouseEnter()
			{
				getSprite("low_bg").setEnabled(false);
				getSprite("high_bg").setEnabled(true);

				super.mouseEnter();
			}

			@Override
			public void mouseLeave()
			{
				getSprite("low_bg").setEnabled(true);
				getSprite("high_bg").setEnabled(false);

				super.mouseLeave();
			}
		};
		matrixtab.setParent(projectSettingsBG);
		matrixtab.appearence.Add("low_bg", new Sprite("/data/tab_matrix.png", matrixtab));
		matrixtab.appearence.Add("high_bg", new Sprite("/data/tab_matrix_highlight.png"));
		matrixtab.getSprite("high_bg").setEnabled(false);
		matrixtab.setPositon(.43f, .95f, true);
		matrixtab.transitions.put("offtabs", new GUItransition(matrixtab.y, false));
		matrixtab.transitions.put("ontabs", new GUItransition(matrixtab.y + matrixtab.height * .25f, false));
		GUI.AddElement(matrixtab);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// TAB GROUP
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		TabGroup tabgroup = new TabGroup(GUI.getNextID(), maintab, matrixtab)
		{
			@Override
			public void onMessageRecieved(String message, Object... args)
			{
				if (message == GUI.MESSAGE_MOUSE_CLICK)
				{
					if ((Integer) args[1] == GUI.MAINTAB)
					{
						this.select((Integer) args[1]);
					}
					else if ((Integer) args[1] == GUI.MATRIXTAB)
					{
						this.select((Integer) args[1]);
					}
				}
				super.onMessageRecieved(message, args);
			}

			@Override
			public void select(int id)
			{
				for (int i = 0; i < tabs.size(); i++)
				{
					if (tabs.get(i).ID == id)
					{
						tabs.get(i).doTrans("ontabs");
					}
					else
					{
						tabs.get(i).doTrans("offtabs");
					}
				}

				super.select(id);
			}
		};
		tabgroup.select(maintab.ID);
		GUI.AddElement(tabgroup);
		GUI.layout.add(new GUIlayout(MATRIXTAB, false));

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
		colorpickerSwatches.statusTip = Tips.colorpickswatches;
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
		colorpickerColorSquare.statusTip = Tips.colorpick;
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
		colorpickerHueSlider.statusTip = Tips.colorpick;
		// colorpickerHueSlider.setSize(27, colorpickerBG.getUnScaleHeight() / 2f * .72f);
		// colorpickerHueSlider.appearence.Add("gradientBG", new GradientBackground(new GradientStop(.45f, Color.red),
		// new GradientStop(.15f, Color.green), new GradientStop(.35f, Color.blue), new GradientStop(.05f,
		// Color.red)));
		// colorpickerHueSlider.getGradientBackground("gradientBG").updateGradient(colorpickerHueSlider.width,
		// colorpickerHueSlider.height);
		colorpickerHueSlider.appearence.Add("bg", new Sprite("/data/hue.png", colorpickerHueSlider));
		colorpickerHueSlider.appearence.Add("border", new PlainBorder(2f, Color.yellow.darker(.4f)));
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
		painterPerVox.statusTip = Tips.toolpaint;
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
		painterButton.statusTip = Tips.toolpaint;
		painterButton.setPositon(width / 2f - 1f * 75f - (float) Scale.hUnSizeScale(painterButton.width * .25f), 0f);
		GUI.AddElement(painterButton);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// ADD TOOL
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		GUIelement addOptionsBG = new GUIelement(GUI.EDITER_ADD_BACKGROUND)
		{

		};
		addOptionsBG.setPositon(width / 2f, -100f);
		addOptionsBG.appearence.Add("bg", new Sprite("/data/sub_toolmenu_oneoption.png", addOptionsBG));
		addOptionsBG.x -= addOptionsBG.width / 2f;
		addOptionsBG.data.put("state", false);
		addOptionsBG.transitions.put("ontrans", new GUItransition((float) Scale.vPosScale(75), false));
		addOptionsBG.transitions.put("offtrans", new GUItransition((float) Scale.vPosScale(-100), false));
		GUI.AddElement(addOptionsBG);

		SpriteButton editAddVox =
				new SpriteButton(GUI.getNextID(), "/data/voxel_add.png", "/data/voxel_add_highlight.png")
				{
					public void mouseClick(int button)
					{
						GUIelement el = GUI.get(GUI.EDITER_ADD_BACKGROUND);
						el.doTrans("offtrans");
						el.data.replace("state", false);
						Program.setTool(Program.tooladd);
						super.mouseClick(button);
					}
				};
		editAddVox.statusTip = Tips.tooladd;
		editAddVox.setParent(addOptionsBG);
		// editAddVox.setPositon(.15f, .14f, true);
		editAddVox.setPositon(.22f, .14f, true);
		GUI.AddElement(editAddVox);

		GUI.layout.add(new GUIlayout(editAddVox.ID, false));
		GUI.layout.add(new GUIlayout(GUI.EDITER_ADD_BACKGROUND, false));

		SpriteButton addbutton = new SpriteButton(GUI.getNextID(), "/data/add.png", "/data/add_highlight.png")
		{
			public void mouseClick(int button)
			{
				GUIelement p = GUI.get(GUI.EDITER_ADD_BACKGROUND);
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
		addbutton.statusTip = Tips.tooladd;
		addbutton.setPositon(width / 2f - (float) Scale.hUnSizeScale(addbutton.width * .25f), 0f);
		GUI.AddElement(addbutton);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// REMOVE TOOL
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		GUIelement removeOptionsBG = new GUIelement(GUI.EDITER_REMVOE_BACKGROUND)
		{

		};
		removeOptionsBG.setPositon(width / 2f + 1f * tooloffset, -100f);
		removeOptionsBG.appearence.Add("bg", new Sprite("/data/sub_toolmenu_oneoption.png", removeOptionsBG));
		removeOptionsBG.x -= removeOptionsBG.width / 2f;
		removeOptionsBG.data.put("state", false);
		removeOptionsBG.transitions.put("ontrans", new GUItransition((float) Scale.vPosScale(75), false));
		removeOptionsBG.transitions.put("offtrans", new GUItransition((float) Scale.vPosScale(-100), false));
		GUI.AddElement(removeOptionsBG);

		SpriteButton editRemoveVox =
				new SpriteButton(GUI.getNextID(), "/data/voxel_remove.png", "/data/voxel_remove_highlight.png")
				{
					public void mouseClick(int button)
					{
						GUIelement el = GUI.get(GUI.EDITER_REMVOE_BACKGROUND);
						el.doTrans("offtrans");
						el.data.replace("state", false);
						Program.setTool(Program.toolremove);
						super.mouseClick(button);
					}
				};
		editRemoveVox.statusTip = Tips.toolremove;
		editRemoveVox.setParent(removeOptionsBG);
		// editRemoveVox.setPositon(.60f, .14f, true);
		editRemoveVox.setPositon(.22f, .14f, true);
		GUI.AddElement(editRemoveVox);

		GUI.layout.add(new GUIlayout(editRemoveVox.ID, false));
		GUI.layout.add(new GUIlayout(GUI.EDITER_REMVOE_BACKGROUND, false));

		SpriteButton removebutton = new SpriteButton(GUI.getNextID(), "/data/remove.png", "/data/remove_highlight.png")
		{
			public void mouseClick(int button)
			{
				GUIelement p = GUI.get(GUI.EDITER_REMVOE_BACKGROUND);
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
		removebutton.statusTip = Tips.toolremove;
		removebutton.setPositon(width / 2f + 1f * 75f - (float) Scale.hUnSizeScale(removebutton.width * .25f), 0f);
		GUI.AddElement(removebutton);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// SAVE TOOL
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		GUIelement saveBG = new GUIelement(GUI.SAVE_BACKGROUND)
		{

		};
		saveBG.setPositon(width / 2f + 2f * tooloffset, -100f);
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
		saveqb.statusTip = Tips.toolsaveqb;
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
		savebutton.statusTip = Tips.toolsave;
		savebutton.setPositon(width / 2f + 2f * 75f - (float) Scale.hUnSizeScale(savebutton.width * .25f), 0f);
		GUI.AddElement(savebutton);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// SETTINGS TOOL
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// SpriteButton settingsbutton =
		// new SpriteButton(GUI.getNextID(), "/data/settings.png", "/data/settings_highlight.png")
		// {
		// };
		// settingsbutton.setPositon(
		// width / 2f + 2f * tooloffset - (float) Scale.hUnSizeScale(settingsbutton.width * .25f), 0f);
		// GUI.AddElement(settingsbutton);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// SCREENSHOT OPTIONS
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		GUIelement screenshot_background_bg = new GUIelement(GUI.SCREENSHOT_BACKGROUND)
		{
			@Override
			public void setEnable(boolean enabled)
			{
				for (int i = 0; i < children.size(); i++)
					children.get(i).setEnable(enabled);

				GUI.get(GUI.SCREENSHOT_CAMERA_BOUNDS).setEnable(enabled);

				Program.floor.visible = !enabled;

				super.setEnable(enabled);
			}

			@Override
			public void onMessageRecieved(String message, Object... args)
			{
				if (message == GUI.MESSAGE_GUI_MENU_TRANS_ON)
				{
					setEnable(false);
				}

				super.onMessageRecieved(message, args);
			}
		};
		screenshot_background_bg.appearence.Add("bg", new Sprite("/data/screenshot_bg.png", screenshot_background_bg));
		screenshot_background_bg.setPositon(width - screenshot_background_bg.getUnScaleWidth() / 2f, height / 2f
				- screenshot_background_bg.getUnScaleHeight() / 4f);
		GUI.AddElement(screenshot_background_bg);

		GUIelement camerabounds = new GUIelement(GUI.SCREENSHOT_CAMERA_BOUNDS)
		{
			@Override
			public void onMessageRecieved(String message, Object... args)
			{
				if (message == GUI.MESSAGE_TEXTBOX_COMMITED)
				{
					if ((Integer) args[1] == GUI.SCREENSHOT_WIDTH)
					{
						float width = this.getUnScaleWidth() / 2f;
						Textbox t = (Textbox) GUI.get(GUI.SCREENSHOT_WIDTH);
						try
						{
							width = Integer.parseInt(t.text) + 6;
						}
						catch (Exception ex)
						{
							t.setText(String.valueOf((int) width));
						}
						float height = this.getUnScaleHeight() / 2f;
						this.setSize(width, height);
						setPositon(Program.width / 2f - width / 2f, Program.height / 2f - height / 2f);
					}
					if ((Integer) args[1] == GUI.SCREENSHOT_HEIGHT)
					{
						float height = this.getUnScaleHeight() / 2f;
						Textbox t = (Textbox) GUI.get(GUI.SCREENSHOT_HEIGHT);
						float width = this.getUnScaleWidth() / 2f;
						try
						{
							height = Integer.parseInt(t.text) + 6;
						}
						catch (Exception ex)
						{
							t.setText(String.valueOf((int) height));
						}
						this.setSize(width, height);
						setPositon(Program.width / 2f - width / 2f, Program.height / 2f - height / 2f);
					}
				}
				super.onMessageRecieved(message, args);
			}

			@Override
			public void mouseEnter()
			{
				this.getPlainBorder("border").color = Color.white;
				getPlainBorder("border1").setEnabled(true);
				getPlainBorder("border2").setEnabled(true);
				super.mouseEnter();
			}

			@Override
			public void mouseLeave()
			{
				this.getPlainBorder("border").color = Color.gray;
				getPlainBorder("border1").setEnabled(false);
				getPlainBorder("border2").setEnabled(false);
				super.mouseLeave();
			}

			@Override
			public void mouseMove(float x, float y)
			{
				super.mouseMove(x, y);

				if (Mouse.isButtonDown(0))
				{
					this.x += x;
					this.y += y;
				}
			}

			@Override
			public void setEnable(boolean enabled)
			{
				super.setEnable(enabled);

				getPlainBorder("border1").setEnabled(false);
				getPlainBorder("border2").setEnabled(false);
			}
		};
		camerabounds.statusTip = Tips.camerabounds;
		camerabounds.setPositon(width / 2f - 153, height / 2f - 153);
		camerabounds.setSize(306, 306);
		camerabounds.appearence.Add("border", new PlainBorder(2.4f, Color.gray));
		camerabounds.appearence.Add("border1", new PlainBorder(2.4f, Color.gray)
		{
			@Override
			public void paint(float x, float y, float width, float height)
			{
				super.paint(x, y, width / 2f + offsetx, height / 2f + offsety);
			}
		});
		camerabounds.appearence.Add("border2", new PlainBorder(2.4f, Color.gray)
		{
			@Override
			public void paint(float x, float y, float width, float height)
			{
				super.paint(x + width / 2f + offsetx / 4f, y + height / 2f + offsety, width / 2f, height / 2f - offsety
						/ 2f);
			}
		});
		camerabounds.getPlainBorder("border1").setEnabled(false);
		camerabounds.getPlainBorder("border2").setEnabled(false);
		GUI.AddElement(camerabounds);

		Label screenshot_label_sizex = new Label(GUI.getNextID(), "Width : ", Color.white);
		screenshot_label_sizex.setParent(screenshot_background_bg);
		screenshot_label_sizex.setPositon(.1f, .85f, true);
		GUI.AddElement(screenshot_label_sizex);
		Label screenshot_label_sizey = new Label(GUI.getNextID(), "Height : ", Color.white);
		screenshot_label_sizey.setParent(screenshot_background_bg);
		screenshot_label_sizey.setPositon(.1f, .60f, true);
		GUI.AddElement(screenshot_label_sizey);

		Textbox screenshot_tb_sizex = new Textbox(GUI.SCREENSHOT_WIDTH, 150);
		screenshot_tb_sizex.setParent(screenshot_background_bg);
		screenshot_tb_sizex.setPositon(.1f, .75f, true);
		screenshot_tb_sizex.setText("300");
		screenshot_tb_sizex.statusTip = Tips.camerawidth;
		GUI.AddElement(screenshot_tb_sizex);
		Textbox screenshot_tb_sizey = new Textbox(GUI.SCREENSHOT_HEIGHT, 150);
		screenshot_tb_sizey.setParent(screenshot_background_bg);
		screenshot_tb_sizey.setPositon(.1f, .50f, true);
		screenshot_tb_sizey.setText("300");
		screenshot_tb_sizey.statusTip = Tips.cameraheight;
		GUI.AddElement(screenshot_tb_sizey);

		SpriteButton screenshot_viewReset =
				new SpriteButton(GUI.getNextID(), "/data/view_reset.png", "/data/view_reset_highlight.png")
				{
					@Override
					public void mouseClick(int button)
					{
						super.mouseClick(button);

						Program.camera.LookAtModel();
					}
				};
		screenshot_viewReset.setParent(screenshot_background_bg);
		screenshot_viewReset.setPositon(.25f, .05f, true);
		screenshot_viewReset.width /= 1.5f;
		screenshot_viewReset.height /= 1.5f;
		screenshot_viewReset.statusTip = Tips.cameraviewreset;
		GUI.AddElement(screenshot_viewReset);

		SpriteButton screenshot_save_button =
				new SpriteButton(GUI.getNextID(), "/data/screenshot_save.png", "/data/screenshot_save_highlight.png")
				{
					@Override
					public void mouseClick(int button)
					{
						super.mouseClick(button);

						GUIelement g = GUI.get(GUI.SCREENSHOT_CAMERA_BOUNDS);
						Textbox twidth = (Textbox) GUI.get(GUI.SCREENSHOT_WIDTH);
						Textbox theight = (Textbox) GUI.get(GUI.SCREENSHOT_HEIGHT);

						int x = (int) g.getUnScaleX() + 3;
						int y = (int) g.getUnScaleY() + 3;
						int width = Integer.parseInt(twidth.text);
						int height = Integer.parseInt(theight.text);

						ScreenShot.screenShot(x, y, width, height);

						GUI.get(GUI.SCREENSHOT_BACKGROUND).setEnable(false);
					}
				};
		screenshot_save_button.statusTip = Tips.camerasave;
		screenshot_save_button.setParent(screenshot_background_bg);
		screenshot_save_button.setPositon(.47f, .05f, true);
		GUI.AddElement(screenshot_save_button);

		screenshot_background_bg.setEnable(false);

		SpriteButton camera_button =
				new SpriteButton(GUI.getNextID(), "/data/camera.png", "/data/camera_highlight.png")
				{
					@Override
					public void mouseClick(int button)
					{
						GUIelement e = GUI.get(GUI.SCREENSHOT_BACKGROUND);
						if (e != null)
						{
							e.setEnable(!e.getEnabled());
						}
						super.mouseClick(button);
					}
				};
		camera_button.statusTip = Tips.cameratool;
		camera_button.setPositon(width - camera_button.getUnScaleWidth() / 2f - camera_button.getUnScaleWidth() * .1f,
				.05f);
		GUI.AddElement(camera_button);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// COLOR OPTIONS
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		for (int i = 0; i < 10; i++)
		{
			float location = height / 2f - clocationscale;

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
				coloroptionStartID = t.ID;

				colorpickerBG.y = t.y + t.height / 2f - colorpickerBG.height / 2f;
				t.select();

				t.statusTip = "Color Option : shortcut " + 1;

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
				t.statusTip = "Color Option : shortcut " + (i == 9 ? 0 : i);
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

		if (_720p)
			Hacked720pGUI(width, height);
		else
			Hacked1080pGUI(width, height);

		GUI.Layout();
	}

	private static void Hacked720pPreGUI(float width, float height)
	{
		hackscalex = 1.0f;
		hackscaley = 1.0f;
		tooloffset = 75f;

		clocationscale = 44f;
	}

	private static void Hacked720pGUI(float width, float height)
	{
		GUIelement projectSettingsBG = GUI.get(GUI.PROJECTSETTINGS_BACKGROUND);

		projectSettingsBG.setPositon(width - 70f,
				height / 2f - (float) Scale.vUnSizeScale(projectSettingsBG.height * .25f));
		projectSettingsBG.transitions.put(
				"ontrans",
				new GUItransition((float) Scale.hPosScale(width - (float) Scale.hUnSizeScale(projectSettingsBG.width)
						/ 2f), true));
		projectSettingsBG.transitions.put("offtrans", new GUItransition((float) Scale.hPosScale(width - 70f), true));
	}

	private static void Hacked1080pPreGUI(float width, float height)
	{
		hackscalex = .95f;
		hackscaley = .95f;
		tooloffset = 73f;

		clocationscale = 45f;
	}

	private static void Hacked1080pGUI(float width, float height)
	{
		GUIelement projectSettingsBG = GUI.get(GUI.PROJECTSETTINGS_BACKGROUND);

		projectSettingsBG.setPositon(width - 65f,
				height / 2f - (float) Scale.vUnSizeScale(projectSettingsBG.height * .25f));
		projectSettingsBG.transitions.put(
				"ontrans",
				new GUItransition((float) Scale.hPosScale(width - (float) Scale.hUnSizeScale(projectSettingsBG.width)
						/ 2f - .5f), true));
		projectSettingsBG.transitions.put("offtrans", new GUItransition((float) Scale.hPosScale(width - 65f), true));

		Textbox projectnametextbox = (Textbox) GUI.get(GUI.PROJECTSETTINGS_NAME);
		Textbox matrixNameTextbox = (Textbox) GUI.get(GUI.MATRIX_NAME);
		Textbox matrixSizeTextbox = (Textbox) GUI.get(GUI.MATRIX_SIZE);

		projectnametextbox.getPlainText("text").yoffset = 2;
		matrixNameTextbox.getPlainText("text").yoffset = 2;
		matrixSizeTextbox.getPlainText("text").yoffset = 2;
	}
}
