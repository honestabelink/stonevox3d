using OpenTK;
using OpenTK.Graphics.OpenGL4;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

public static class ShaderUtil
{
    static Dictionary<string,Shader> _shaders = new Dictionary<string,Shader>();

    public static Shader createshader(string name, string vertexshaderpath, string fragmentshaderpath)
    {
        int vertexshaderid = GL.CreateShader(ShaderType.VertexShader);
        int fragmentshaderid = GL.CreateShader(ShaderType.FragmentShader);

        using (StreamReader r = new StreamReader(vertexshaderpath))
            GL.ShaderSource(vertexshaderid, r.ReadToEnd());
        using (StreamReader r = new StreamReader(fragmentshaderpath))
            GL.ShaderSource(fragmentshaderid, r.ReadToEnd());

        GL.CompileShader(vertexshaderid);

        string vertexshaderlog  =GL.GetShaderInfoLog(vertexshaderid);
        if (!string.IsNullOrEmpty(vertexshaderlog))
        {
            string renderer = GL.GetString(StringName.Renderer);
            Client.print("info", vertexshaderlog);
            MessageBox.Show("Error occured in StoneVox. Your graphics card seems to be unsupported. You are welcome to continue to use StoneVox, but likely run into visual errors. Try updating your graphics card driver. Here's the name of your graphics card - " + renderer, "Error - Vertex Shader", MessageBoxButtons.OK);
        }

        GL.CompileShader(fragmentshaderid);

        string fragmentshaderlog = GL.GetShaderInfoLog(fragmentshaderid);
        if (!string.IsNullOrEmpty(fragmentshaderlog))
        {
            string renderer = GL.GetString(StringName.Renderer);
            Client.print("info", fragmentshaderlog);
            MessageBox.Show("Error occured in StoneVox. Your graphics card seems to be unsupported. You are welcome to continue to use StoneVox, but likely run into visual errors. Try updating your graphics card driver. Here's the name of your graphics card - " + renderer, "Error - Fragment Shader", MessageBoxButtons.OK);
        }

        int shaderid = GL.CreateProgram();
        GL.AttachShader(shaderid, vertexshaderid);
        GL.AttachShader(shaderid, fragmentshaderid);
        GL.LinkProgram(shaderid);

       string shaderlog = GL.GetProgramInfoLog(shaderid);

        if (!string.IsNullOrEmpty(shaderlog))
        {
            string renderer = GL.GetString(StringName.Renderer);
            Client.print("info", shaderlog);
            MessageBox.Show("Error occured in StoneVox. Your graphics card seems to be unsupported. You are welcome to continue to use StoneVox, but likely run into visual errors. Try updating your graphics card driver. Here's the name of your graphics card - " + renderer, "Error - Linking Shader", MessageBoxButtons.OK);
        }

        Shader shader = new Shader(shaderid, vertexshaderid, fragmentshaderid);
        _shaders.Add(name, shader);
        return shader;
    }

    public static void resetshader()
    {
        GL.UseProgram(0);
    }

    public static Shader getShader(string name)
    {
        return _shaders[name];
    }
}

public class Shader
{
    public int shaderid;
    public int vertextshaderid;
    public int fragmentshaderid;
    private Dictionary<string, int> uniforms = new Dictionary<string, int>();
    private Dictionary<string, int> attributes = new Dictionary<string, int>();

    public Shader(int id, int vertexid, int fragmentid)
    {
        this.shaderid = id;
        this.vertextshaderid = vertexid;
        this.fragmentshaderid = fragmentid;
    }

    public void useshader()
    {
        GL.UseProgram(shaderid);
    }

    public int getartributelocation(string name)
    {
        int id = 0;

        if (attributes.TryGetValue(name, out id))
        {
            return id;
        }
        else
            return createAtrributeAccess(name);
    }

    private int createAtrributeAccess(string name)
    {
        int id = GL.GetAttribLocation(shaderid, name);
        attributes.Add(name, id);
        return id;
    }

    public int getuniform(string name)
    {
        int id = 0;

        if (uniforms.TryGetValue(name, out id))
        {
            return id;
        }
        else
            return createUniformAccess(name);
    }

    private int createUniformAccess(string name)
    {
        int id = GL.GetUniformLocation(shaderid, name);
        uniforms.Add(name, id);
        return id;
    }

    public void writeuniform(string name, Matrix4 matrix)
    {
        GL.UniformMatrix4(getuniform(name), false, ref matrix);
    }

    public void writeuniform(string name, float value)
    {
        GL.Uniform1(getuniform(name), value);
    }

    public void writeuniform(string name, Vector3 value)
    {
        GL.Uniform3(getuniform(name), ref value);
    }

    public unsafe void writeuniformarray(string name, int count, float* pointer)
    {
        GL.Uniform3(getuniform(name), count, pointer);
    }
}