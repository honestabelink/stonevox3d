package stonevox.util;

import stonevox.data.Side;

public class SideUtil
{
	public static int getVisibilityMask(Side side)
	{
		switch (side)
		{
			case BACK:
				return 32;
			case FRONT:
				return 64;
			case BOTTOM:
				return 16;
			case TOP:
				return 8;
			case RIGHT:
				return 2;
			case LEFT:
				return 4;
		}

		return 0;
	}
}
