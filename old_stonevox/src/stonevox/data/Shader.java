package stonevox.data;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import stonevox.util.GetPath;

public class Shader
{
	public String vertexshader_path;
	public String fragmentshader_path;

	public Map<String, Integer> uniforms;
	public Map<String, Integer> attributes;

	public int programID;
	public int vertexShaderID;
	public int fragmentShaderID;

	public Shader()
	{
		uniforms = new HashMap<String, Integer>();
		attributes = new HashMap<String, Integer>();
	}

	public void Load()
	{
		vertexShaderID = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		fragmentShaderID = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);

		byte[] vertexShaderBytes = null;
		byte[] fragmentShaderBytes = null;

		String path = GetPath.getPath("");

		try
		{
			vertexShaderBytes = Files.readAllBytes(new File(path + vertexshader_path).toPath());
			fragmentShaderBytes = Files.readAllBytes(new File(path + fragmentshader_path).toPath());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		ByteBuffer vertexShaderBuffer = BufferUtils.createByteBuffer(vertexShaderBytes.length);
		ByteBuffer fragmentShaderBuffer = BufferUtils.createByteBuffer(fragmentShaderBytes.length);

		vertexShaderBuffer.put(vertexShaderBytes);
		vertexShaderBuffer.flip();

		fragmentShaderBuffer.put(fragmentShaderBytes);
		fragmentShaderBuffer.flip();

		GL20.glShaderSource(vertexShaderID, vertexShaderBuffer);
		GL20.glShaderSource(fragmentShaderID, fragmentShaderBuffer);

		GL20.glCompileShader(vertexShaderID);
		GL20.glCompileShader(fragmentShaderID);
	}

	public void InstallShader()
	{
		programID = GL20.glCreateProgram();
		GL20.glAttachShader(programID, vertexShaderID);
		GL20.glAttachShader(programID, fragmentShaderID);

		GL20.glLinkProgram(programID);
	}

	public void UseShader()
	{
		GL20.glUseProgram(programID);
	}

	public int GetAttributeAcces(String name)
	{
		if (attributes.containsKey(name))
			return (int) attributes.get(name);
		else
			return (int) CreateAttributeAccess(name);
	}

	public int CreateAttributeAccess(String name)
	{
		byte[] bytes = name.getBytes();
		ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
		buffer.put(bytes);
		buffer.flip();
		int location = GL20.glGetAttribLocation(programID, buffer);

		attributes.put(name, location);
		return location;
	}

	public void CreateUniformAccess(String name)
	{
		byte[] bytes = name.getBytes();
		ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
		buffer.put(bytes);
		buffer.flip();
		int location = GL20.glGetUniformLocation(programID, buffer);

		uniforms.put(name, location);
	}

	public void WriteUniformMatrix4(String name, FloatBuffer buffer)
	{
		GL20.glUniformMatrix4(uniforms.get(name), false, buffer);
	}

	public void WriteUniformVec3(String name, float x, float y, float z)
	{
		GL20.glUniform3f(uniforms.get(name), x, y, z);
	}

	public void WriteUniformBuffer(String name, FloatBuffer buffer)
	{
		GL20.glUniform2(uniforms.get(name), buffer);
	}

	public static void ResetUseShader()
	{
		GL20.glUseProgram(0);
	}
}
