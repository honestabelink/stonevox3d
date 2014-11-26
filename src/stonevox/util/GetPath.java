package stonevox.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class GetPath
{
	public static GetPath get = new GetPath();

	public static String getPath(String path)
	{
		String path1 = get.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
		try
		{
			path1 = URLDecoder.decode(path1, "utf-8");
		}
		catch (UnsupportedEncodingException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		path1 = path1.substring(6);

		String hack = "StoneVox3D-0.0.3.1.jar";

		if (path1.endsWith(".jar"))
		{
			path1 = path1.substring(0, path1.length() - hack.length());
		}
		return (path1 + path);
	}
}
