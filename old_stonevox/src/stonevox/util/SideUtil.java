package stonevox.util;

import stonevox.data.Side;

public class SideUtil
{
	public static int getVisibilityMask(Side side)
	{
		switch (side)
		{
			case BACK:
				return 64;
			case FRONT:
				return 32;
			case BOTTOM:
				return 16;
			case TOP:
				return 8;
			case RIGHT:
				return 4;
			case LEFT:
				return 2;
		}

		return 0;
	}
}
