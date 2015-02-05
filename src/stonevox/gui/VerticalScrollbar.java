package stonevox.gui;

import org.newdawn.slick.Color;

import stonevox.data.GUIelement;
import stonevox.data.GUIlayout;
import stonevox.decorator.PlainBackground;
import stonevox.decorator.PlainBorder;
import stonevox.util.GUI;
import stonevox.util.Scale;

public class VerticalScrollbar extends GUIelement
{
	private GUIelement button;

	public float maxValue = 10;
	private float barStep;
	private float valueStep;
	private float currentPos;
	private float startValue;
	private float maxTextShow;

	public float value;

	private VerticalScrollbar ref;
	private float fontHeight;

	public VerticalScrollbar(int ID, int maxvalue, float width, float height)
	{
		super(ID);

		Label g = new Label(1, "Tonion", Color.red);
		g.setEnable(false);

		fontHeight = g.height;

		ref = this;

		this.setSize(width, height);

		this.appearence.Add("bg", new PlainBackground(Color.gray.darker(.4f)));
		this.appearence.Add("border", new PlainBorder(1.5f, Color.yellow));

		button = new GUIelement(GUI.getNextID())
		{
			public void mouseMove(float x, float y)
			{
				if (x == 0 && y == 0)
					return;
				this.y += y;

				if (this.y > ref.height - this.height - getParent().y / 2f)
				{
					this.y = ref.height - this.height - getParent().y / 2f;
				}
				else if (this.y < -getParent().y / 2f)
				{
					this.y = -getParent().y / 2f;
				}

				value =
						Math.abs((float) Scale.scale(this.y + getParent().y / 2f, 0, ref.height - this.height,
								startValue, maxValue) - (startValue + (maxValue - maxTextShow)));
				valueChanged(value);
			}

			@Override
			public void mouseEnter()
			{
				getPlainBackground("bg").color = Color.lightGray.brighter(.7f);
				super.mouseEnter();
			}

			@Override
			public void mouseLeave()
			{
				getPlainBackground("bg").color = Color.lightGray;
				super.mouseLeave();
			}

			@Override
			public void setEnable(boolean enabled)
			{
				super.setEnable(ref.getEnabled());
			}
		};

		button.appearence.Add("bg", new PlainBackground(Color.lightGray));

		// button.setParent(this);
		button.width = this.width * 1.3f;

		updateScrollbar((float) maxvalue);

		GUI.AddElement(button);
		GUI.layout.add(new GUIlayout(button.ID, false));
		GUI.layout.add(new GUIlayout(this.ID, false));
	}

	public void updateScrollbar()
	{
		updateScrollbar(maxValue);
	}

	public void updateScrollbar(float maxvalue)
	{
		this.maxValue = maxvalue;
		maxTextShow = this.height / fontHeight;

		this.barStep = this.height / 100f;
		this.valueStep = this.maxValue / 100f;

		float maxShow = fontHeight * maxTextShow;
		float totalShow = fontHeight * maxvalue;

		float percentShown = maxShow / totalShow;

		if (maxShow < totalShow)
			button.height = this.height * percentShown;
		else
			button.height = this.height;

		startValue = (float) Scale.scale(button.height, 0, this.height, 0f, maxValue);

		if (this.getParent() != null)
		{
			button.y = (this.height - button.height - button.getParent().y / 2f) * .999f;
			valueChanged(0f);
		}
	}

	public void setParent(GUIelement el)
	{
		super.setParent(el);
	}

	public void valueChanged(float value)
	{
		// System.out.print((int) value + " " + '\r');
	}

	public GUIelement getScrollbar()
	{
		return button;
	}
}
