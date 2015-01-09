package stonevox.gui;

import java.util.ArrayList;

import org.newdawn.slick.Color;

import stonevox.Program;
import stonevox.data.GUIelement;
import stonevox.data.GUIlayout;
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

	// public VerticalScrollbar vs;

	private PlainBackground highlight;
	private PlainBorder border;
	private int activeIndex;
	private float fontHeight;
	private float maxelements;

	// hacks
	private int startvisibility;

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
				@Override
				public void mouseClick(int button)
				{
					int activeindex = (Integer) data.get("index");

					if (Program.model.matrixList.size() > activeIndex)
					{
						Program.model.setActiveMatrix(activeindex);
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
				@Override
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
						Program.model.getIndex(index).visible = vstate;
					}
					super.mouseClick(button);
				}

				@Override
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

		// vs = new VerticalScrollbar(GUI.getNextID(), 1, 20, getUnScaleHeight() / 2f);
		// GUI.AddElement(vs);

		// vs.setParent(this);
		// vs.setPositon(.95f, .0f, true);
		// vs.updateScrollbar();

		GUI.layout.add(new GUIlayout(ID, false));
		// GUI.layout.add(new GUIlayout(vs.getScrollbar().ID, true));
	}

	@Override
	public void setPositon(float x, float y, boolean scaleToParentBounds)
	{
		super.setPositon(x, y, scaleToParentBounds);

		for (int i = 0; i < elements.size(); i++)
		{
			elements.get(i).x -= this.x / 2f;
			elements.get(i).y -= this.y / 2f;
		}
	}

	@Override
	public void setParent(GUIelement el)
	{
		super.setParent(el);
	}

	public void updateNames()
	{
		for (int i = 0; i < elements.size() / 2; i++)
		{
			Label l = (Label) elements.get(i);
			if (i < Program.model.matrixList.size())
			{
				String name = Program.model.matrixList.get(i).getName();

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
		activeIndex = Program.model.getActiveIndex();
	}

	@Override
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
