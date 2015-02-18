package stonevox.gui;

import java.util.ArrayList;

import org.newdawn.slick.Color;

import stonevox.Program;
import stonevox.data.GUIelement;
import stonevox.data.GUIlayout;
import stonevox.data.Vector3;
import stonevox.decorator.PlainBackground;
import stonevox.decorator.PlainBorder;
import stonevox.decorator.Sprite;
import stonevox.util.GUI;
import stonevox.util.Tips;

public class Listbox extends GUIelement
{
	// needs just a bit more work for scrolling the vbar and updating the name and indexs
	// whats here now should be enough for a while

	public ArrayList<GUIelement> elements = new ArrayList<GUIelement>();

	public VerticalScrollbar vs;

	private PlainBackground highlight;
	private PlainBorder border;
	private int activeIndex;
	private float fontHeight;
	private float maxelements;

	// hacks
	private int startvisibility;
	private int startindex;
	private int laststartindex;

	private boolean showvs;

	public Listbox(int ID, float width, float height)
	{
		super(ID);
		setSize(width, height);

		highlight = new PlainBackground(Color.black);
		highlight.color.a = 55;
		border = new PlainBorder(3, new Color(122f / 256f, 106f / 256f, 70f / 256f));

		this.appearence.Add("ba", new PlainBackground(Color.gray.darker(.3f)));

		Label g = new Label(1, "Tonion", Color.red);
		g.setEnable(false);

		fontHeight = g.height;
		maxelements = this.height / fontHeight;
		this.height = (maxelements - 1) * (fontHeight) + fontHeight + fontHeight / 2f;
		elements.clear();

		for (int i = 0; i < maxelements; i++)
		{
			Label s = new Label(GUI.getNextID(), "", Color.white)
			{
				public void mouseEnter()
				{
					// TODO Auto-generated method stub
					super.mouseEnter();

					int index = (Integer) data.get("index") + startindex;
					if (Program.model.matrixList.size() > index)
					{
						Program.model.getIndex(index).highlight = new Vector3(1.8f, 1.8f, 1.8f);
					}
				}

				public void mouseLeave()
				{
					// TODO Auto-generated method stub
					super.mouseLeave();

					int index = (Integer) data.get("index") + startindex;
					if (Program.model.matrixList.size() > index)
					{
						Program.model.getIndex(index).highlight = new Vector3(1f, 1f, 1f);
					}
				}

				public void mouseClick(int button)
				{
					int activeindex = (Integer) data.get("index");

					if (Program.model.matrixList.size() > activeIndex + startindex)
					{
						Program.model.setActiveMatrix(activeindex + startindex);
						Program.floor.updatemesh();

						Textbox name = (Textbox) GUI.get(GUI.MATRIX_NAME);
						name.setText(Program.model.GetActiveMatrix().getName());

						Textbox size = (Textbox) GUI.get(GUI.MATRIX_SIZE);
						size.setText(Program.model.GetActiveMatrix().getSizeString());

						activeIndex = activeindex;
					}

					super.mouseClick(button);
				}
			};
			s.statusTip = Tips.selectmatrix;
			s.data.put("index", (int) (i));
			s.setParent(this);
			s.y = (maxelements - i) * fontHeight - fontHeight + fontHeight / 2f;
			s.getPlainText("text").xoffset = 35;
			s.getPlainText("text").yoffset = 2;
			elements.add(s);
			GUI.AddElement(s);
		}

		startvisibility = elements.size();

		for (int i = 0; i < maxelements; i++)
		{
			GUIelement s = new GUIelement(GUI.getNextID())
			{
				public void mouseClick(int button)
				{
					if ((Boolean) data.get("allow"))
					{
						int index = (Integer) data.get("index");
						boolean vstate = (Boolean) data.get("vstate");
						vstate = !vstate;

						data.put("vstate", vstate);

						if (vstate)
						{
							getSprite("on").setEnabled(true);
							getSprite("off").setEnabled(false);
						}
						else
						{
							getSprite("on").setEnabled(false);
							getSprite("off").setEnabled(true);
						}
						Program.model.getIndex(index + startindex).visible = vstate;
					}
					super.mouseClick(button);
				}

				public void setEnable(boolean enabled)
				{
					super.setEnable(enabled);

					boolean e = (Boolean) data.get("allow");
					boolean vstate = (Boolean) data.get("vstate");

					if (e && enabled)
					{
						if (vstate)
						{
							getSprite("on").setEnabled(true);
							getSprite("off").setEnabled(false);
						}
						else
						{
							getSprite("on").setEnabled(false);
							getSprite("off").setEnabled(true);
						}
					}
					else if (!e && enabled)
					{
						getSprite("on").setEnabled(false);
						getSprite("off").setEnabled(false);
					}
					else
					{
						getSprite("on").setEnabled(false);
						getSprite("off").setEnabled(false);
					}
				}
			};
			s.statusTip = Tips.visibility;
			s.appearence.Add("on", new Sprite("/data/qb_matrix_visibility_on.png", s));
			s.appearence.Add("off", new Sprite("/data/qb_matrix_visibility_off.png"));
			s.getSprite("on").setEnabled(false);
			s.getSprite("off").setEnabled(false);
			s.data.put("index", (int) i);
			s.data.put("allow", true);
			s.data.put("vstate", false);
			s.setParent(this);
			s.x = .01f;
			s.y = (maxelements - i) * fontHeight - fontHeight + fontHeight / 2f;
			elements.add(s);
			GUI.AddElement(s);
		}

		vs = new VerticalScrollbar(GUI.getNextID(), Program.model.numMatrices, 20, getUnScaleHeight() / 2f)
		{
			public void setEnable(boolean enabled)
			{
				if (Program.model.numMatrices > maxelements)
				{
					super.setEnable(true);
					this.getScrollbar().setEnable(true);
				}
				else
				{
					super.setEnable(false);
					this.getScrollbar().setEnable(false);
				}
			}

			public void valueChanged(float value)
			{
				super.valueChanged(value);

				if (Program.model.numMatrices > maxelements && (int) value != startindex)
				{
					startindex = (int) value;
					updateNames();
				}
			}

			public void onMessageRecieved(String message, Object... args)
			{
				super.onMessageRecieved(message, args);

				if (message == GUI.MESSAGE_TAB_SELECTED)
				{
					int id = (Integer) args[1];

					if (id == GUI.MATRIXTAB)
					{
						super.setEnable(false);
						this.getScrollbar().setEnable(false);
					}
					else if (id == GUI.MAINTAB)
					{
						if (Program.model.numMatrices > maxelements)
						{
							super.setEnable(true);
							this.getScrollbar().setEnable(true);
						}
						else
						{
							super.setEnable(false);
							this.getScrollbar().setEnable(false);
						}
					}
				}

				if (message == GUI.MESSAGE_QB_LOADED)
				{
					if (Program.model.numMatrices > maxelements)
					{
						this.setEnable(true);
						this.getScrollbar().setEnable(true);
					}
					else
					{
						this.setEnable(false);
						this.getScrollbar().setEnable(false);
					}

					this.maxValue = Program.model.numMatrices;
					this.updateScrollbar();
				}

				if (message == GUI.MESSAGE_QB_MATRIX_ADDED)
				{
					if (Program.model.numMatrices > maxelements)
					{
						this.setEnable(true);
						this.getScrollbar().setEnable(true);
					}
					else
					{
						this.setEnable(false);
						this.getScrollbar().setEnable(false);
					}

					this.maxValue = Program.model.numMatrices;
					this.updateScrollbar();
				}
				if (message == GUI.MESSAGE_QB_MATRIX_REMOVED)
				{
					if (Program.model.numMatrices > maxelements)
					{
						this.setEnable(true);
						this.getScrollbar().setEnable(true);
					}
					else
					{
						this.setEnable(false);
						this.getScrollbar().setEnable(false);
					}

					this.maxValue = Program.model.numMatrices;
					this.updateScrollbar();
				}
			}
		};
		GUI.AddElement(vs);

		vs.setParent(this);
		vs.setPositon(.95f, .0f, true);
		vs.getScrollbar().setParent(this);
		vs.getScrollbar().setPositon(.94f, 0, true);
		vs.updateScrollbar();

		vs.setEnable(false);
		vs.getScrollbar().setEnable(false);

		GUI.layout.add(new GUIlayout(ID, false));
		GUI.layout.add(new GUIlayout(vs.getScrollbar().ID, true));
	}

	public void setPositon(float x, float y, boolean scaleToParentBounds)
	{
		super.setPositon(x, y, scaleToParentBounds);

		for (int i = 0; i < elements.size(); i++)
		{
			elements.get(i).x -= this.x / 2f;
			elements.get(i).y -= this.y / 2f;
		}

		vs.x -= this.x / 2f;
		vs.y -= this.y / 2f;

		vs.getScrollbar().x -= this.x / 2f;
		vs.getScrollbar().y -= this.y / 2f;
	}

	public void setParent(GUIelement el)
	{
		super.setParent(el);
	}

	public void updateNames()
	{
		if (!this.getEnabled())
			return;

		for (int i = 0; i < elements.size() / 2; i++)
		{
			Label l = (Label) elements.get(i);
			if (i + startindex < Program.model.matrixList.size())
			{
				String name = Program.model.matrixList.get(i + startindex).getName();

				l.setText(name);
				if (name != "")
				{
					int index = (int) (i + startvisibility);
					if (Program.model.getIndex(i).visible)
					{
						elements.get(index).data.replace("vstate", true);
						elements.get(index).data.replace("allow", true);
						elements.get(index).setEnable(true);
					}
					else
					{
						elements.get(index).data.replace("vstate", false);
						elements.get(index).data.replace("allow", true);
						elements.get(index).setEnable(true);
					}
				}
				else
				{
					int index = (int) (i + startvisibility);
					elements.get(index).data.replace("allow", false);
					elements.get(index).data.replace("mindex", (int) i + startindex);
					elements.get(index).setEnable(false);
				}
			}
			else
			{
				l.setText("");
				int index = (int) (i + startvisibility);
				elements.get(index).data.replace("allow", false);
				elements.get(index).setEnable(false);
			}
		}
		activeIndex = Program.model.getActiveIndex() - startindex;

		if (activeIndex < 0 || activeIndex > maxelements)
			activeIndex = -100;
	}

	public void render()
	{
		if (getEnabled() && hasParent())
		{
			float parX = getParentsX();
			float parY = getParentsY();

			int index = this.activeIndex - 1;
			index = (int) (maxelements - index);
			border.paint(parX + x, parY + y, this.width, this.height);
			highlight.paint(parX + x, parY + y + (index) * fontHeight - fontHeight, this.width, fontHeight);
		}
		else if (getEnabled())
		{
			int index = this.activeIndex - 1;
			index = (int) (maxelements - index);
			highlight.paint(x, y + (index) * fontHeight - fontHeight, this.width, fontHeight);
		}
		super.render();
	}
}
