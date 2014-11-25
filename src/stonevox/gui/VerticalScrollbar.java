package stonevox.gui;

import org.newdawn.slick.Color;

import stonevox.data.GUIelement;
import stonevox.data.GUIlayout;
import stonevox.decorator.PlainBackground;
import stonevox.decorator.PlainBorder;
import stonevox.util.FontUtil;
import stonevox.util.GUI;
import stonevox.util.Scale;

public class VerticalScrollbar extends GUIelement
{
	private GUIelement	button;

	private float		maxValue	= 10;
	private float		barStep;
	private float		valueStep;
	private float		currentPos;
	private float		startValue;
	private float		maxTextShow;

	public float		value;

	public VerticalScrollbar(int ID, int maxvalue, float width, float height)
	{
		super(ID);

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

				if (this.y > this.getParent().height - this.height)
				{
					this.y = this.getParent().height - this.height;
				}
				else if (this.y < 0)
				{
					this.y = 0;
				}

				value =
						Math.abs((float) Scale.scale(this.y, 0, this.getParent().height - this.height, startValue,
								maxValue) - (startValue + (maxValue - maxTextShow)));
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
		};

		button.appearence.Add("bg", new PlainBackground(Color.lightGray));

		button.setParent(this);
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
		float fontHeight = (float) Scale.vSizeScale(FontUtil.GetFont("default").height);
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

		button.setPositon(this.getUnScaleX() - button.getUnScaleWidth() / 16f,
				this.getUnScaleY() + (this.getUnScaleHeight() - button.getUnScaleHeight()) / 2f, true);
	}

	public void valueChanged(float value)
	{
		System.out.print((Math.round(value) + "\n"));
	}

	public GUIelement getScrollbar()
	{
		return button;
	}
}
