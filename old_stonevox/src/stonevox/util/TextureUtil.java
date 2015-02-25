package stonevox.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import stonevox.data.Vector3;
import de.matthiasmann.twl.utils.PNGDecoder;

public class TextureUtil
{
	public static int allocateTexture()
	{
		int texturehandle = GL11.glGenTextures();
		return texturehandle;
	}

	public static int makeTexture(ByteBuffer pixels, int w, int h)
	{
		int textureHandle = allocateTexture();

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureHandle);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR); // GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); // GL11.GL_NEAREST);

		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		return textureHandle;
	}

	public static Vector3 loadFile(String path)
	{
		try
		{
			DataInputStream in =
					new DataInputStream(new BufferedInputStream(new FileInputStream(GetPath.getPath(path))));

			PNGDecoder decoder = null;
			try
			{
				decoder = new PNGDecoder(in);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			ByteBuffer buffer = BufferUtils.createByteBuffer(4 * decoder.getWidth() * decoder.getHeight());
			try
			{
				decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			buffer.flip();

			return new Vector3(makeTexture(buffer, decoder.getWidth(), decoder.getHeight()), decoder.getWidth(),
					decoder.getHeight());
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		return null;
	}
}
