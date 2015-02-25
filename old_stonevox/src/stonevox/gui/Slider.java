package stonevox.gui;

import org.newdawn.slick.Color;

import stonevox.data.GUIelement;
import stonevox.data.GUIlayout;
import stonevox.decorator.PlainBackground;
import stonevox.decorator.PlainBorder;
import stonevox.util.GUI;
import stonevox.util.Scale;

public class Slider extends GUIelement
{
	public float value;
	private GUIelement button;

	public Slider(int ID, float width, float height, Color background)
	{
		super(ID);

		this.setSize(width, height);

		this.appearence.Add("bg", new PlainBackground(background));
		this.appearence.Add("border", new PlainBorder(1.5f, Color.yellow));

		button = new GUIelement(GUI.getNextID())
		{
			public void mouseMove(float x, float y)
			{
				if (x == 0 && y == 0)
					return;
				this.x += x;

				if (this.x > this.getParent().width - this.width)
				{
					this.x = this.getParent().width - this.width;
				}
				else if (this.x < 0)
				{
					this.x = 0;
				}

				value = (float) Scale.scale(this.x, 0, this.getParent().width - this.width, 0f, 100f);
				valueChanged(value);
			}

			public void mouseEnter()
			{
				getPlainBorder("border").color = Color.lightGray.brighter(.2f);
				getPlainBackground("bg").color = Color.lightGray.brighter(.7f);
				super.mouseEnter();
			}

			public void mouseLeave()
			{
				getPlainBorder("border").color = Color.lightGray;
				getPlainBackground("bg").color = Color.lightGray;
				super.mouseLeave();
			}
		};

		button.appearence.Add("bg", new PlainBackground(Color.lightGray));
		button.appearence.Add("border", new PlainBorder(3f, Color.lightGray));

		button.setParent(this);
		button.setPositon(0, -.45f, true);
		button.setSize(12, 0);
		button.height = this.height * 1.75f + this.height * .1f;

		GUI.AddElement(button);
		GUI.layout.add(new GUIlayout(button.ID, false));
		GUI.layout.add(new GUIlayout(this.ID, false));
	}

	public void mouseClick(int b)
	{
		// float mouseX = (float) Scale.scale(Mouse.getX(), 0, Program.width, 0,
		// width);
		// float mouseY = (float) Scale.scale(Mouse.getY(), 0, Program.height,
		// 0, height);
		//
		// mouseX = (float) Scale.scale(mouseX, 0f, width, 0f, 1f);
		// mouseY = (float) Scale.scale(mouseY, 0f, height, 0f, 1f);
		//
		// button.setPositon(mouseX, mouseY, true);

		super.mouseClick(b);
	}

	public void valueChanged(float v)
	{
		// System.out.print(value + "\n");
	}

	public void setValue(float v)
	{
		this.value = v;
		this.button.x = (float) Scale.scale(v, 0f, 100f, 0f, width - button.width);
		valueChanged(v);
	}
}
