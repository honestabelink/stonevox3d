package stonevox;

import java.awt.Canvas;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JList;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.newdawn.slick.Color;

import stonevox.data.Keyhook;
import stonevox.data.Matrix;
import stonevox.data.QbMatrix;
import stonevox.data.QbModel;
import stonevox.data.Shader;
import stonevox.data.Tool;
import stonevox.data.Vector3;
import stonevox.gui.ColorOption;
import stonevox.tools.ToolAdd;
import stonevox.tools.ToolColorPicker;
import stonevox.tools.ToolPainter;
import stonevox.tools.ToolRemove;
import stonevox.tools.ToolSelection;
import stonevox.util.DNDHandler;
import stonevox.util.FontUtil;
import stonevox.util.GUI;
import stonevox.util.KeyboardUtil;
import stonevox.util.PaletteUtil;
import stonevox.util.QbUtil;
import stonevox.util.Scale;
import stonevox.util.UndoUtil;

public class Program
{

	// (float) (720.0 * (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL))

	String windowtitle = "StoneVox 3D";
	boolean running = true;
	long lastLoopTime;
	long lastFpsTime;
	int mousedx;
	int mousedy;
	boolean firstRun = true;

	public static QbModel model;
	public static float fov = 45f;
	public static float nearPlane = 1f;
	public static float farPlane = 300f;
	public static int fps;
	public static long delta = 1l;
	public static int height = 800;
	public static int width = 800;

	public static Camera camera;
	public static Shader shader;
	public static Raycaster rayCaster;
	public static Floor floor;
	public static boolean multithreading_raycasting = true;

	public static ToolPainter toolpainter;
	public static ToolColorPicker toolcolorpicker;
	public static ToolSelection toolselection;
	public static ToolRemove toolremove;
	public static ToolAdd tooladd;

	public static Tool lastTool;
	public static Tool currentTool;

	public static String filepath = "";

	static Canvas openglSurface;
	static JFrame frame;
	static JList<File> list = new JList<File>();

	// to get rid of
	boolean wasLeftClickDown;
	int lastkey;
	boolean lastkeystate;
	public static Color clearColor = new Color(0, 0, 0, 0);

	public static boolean debug = false;

	public static void main(String[] args)
	{
		new Program().execute(args);
		frame.dispose();
		frame = null;
		System.exit(0);
	}

	public void execute(String[] args)
	{
		init(args);
		loadContent();
		programLoop();
	}

	void init(String[] args)
	{
		openglSurface = new Canvas();
		openglSurface.setSize(1280, 800);
		openglSurface.setVisible(true);
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1280, 800);
		frame.setTransferHandler(new DNDHandler(list));
		frame.add(openglSurface);
		frame.setVisible(true);
		try
		{
			Display.setParent(openglSurface);
		}
		catch (LWJGLException e1)
		{
			e1.printStackTrace();
		}

		setDisplayMode();

		frame.setTitle(windowtitle);

		try
		{
			Display.setFullscreen(false);
			Display.setResizable(true);
			Display.create(new PixelFormat(8, 8, 0, 8));

			width = Display.getWidth();
			height = Display.getHeight();

			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.6f);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
			GL20.glEnableVertexAttribArray(2);

			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
		}

	}

	void loadContent()
	{
		Scale.SetHScaling(0, width);
		Scale.SetVScaling(0, height);
		Scale.SetAspectRatio(width, height);

		shader = new Shader();

		shader.vertexshader_path = "shaders/VertexShader.txt";
		shader.fragmentshader_path = "shaders/FragmentShader.txt";
		shader.Load();
		shader.InstallShader();
		shader.UseShader();

		shader.CreateUniformAccess("modelview\0");
		shader.CreateUniformAccess("highlight\0");
		shader.CreateUniformAccess("colors\0");

		toolcolorpicker = new ToolColorPicker();
		toolremove = new ToolRemove();
		tooladd = new ToolAdd();
		toolpainter = new ToolPainter();
		toolselection = new ToolSelection();

		model = QbUtil.GetDefault();

		camera = new Camera();
		camera.projection =
				Matrix.CreatePerspectiveFieldOfView((float) Math.toRadians(fov), (float) width / (float) height,
						nearPlane, farPlane);
		camera.LookAtModel();

		rayCaster = new Raycaster();
		rayCaster.Setup(camera);

		floor = new Floor();

		FontUtil.loadFont("default", "/data/fonts/Bigfish.ttf");

		GUI.StandardGUI(width < 1500);

		KeyboardUtil.Add(Keyboard.KEY_E, new Keyhook()
		{
			public void down()
			{
				setTool(toolremove);
				super.down();
			}
		});

		KeyboardUtil.Add(Keyboard.KEY_A, new Keyhook()
		{
			public void down()
			{
				setTool(tooladd);
				super.down();
			}
		});

		KeyboardUtil.Add(Keyboard.KEY_B, new Keyhook()
		{
			public void down()
			{
				setTool(toolpainter);
				super.down();
			}
		});

		KeyboardUtil.Add(Keyboard.KEY_Z, new Keyhook()
		{
			public void down()
			{
				if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
				{
					UndoUtil.undo();
				}
				super.down();
			}
		});
		KeyboardUtil.Add(Keyboard.KEY_Y, new Keyhook()
		{
			public void down()
			{
				if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
				{
					UndoUtil.redo();
				}
				super.down();
			}
		});

		int cores = Runtime.getRuntime().availableProcessors();

		if (multithreading_raycasting && cores > 1)
			rayCaster.Start();
		if (cores <= 1)
		{
			multithreading_raycasting = false;
		}
	}

	void programLoop()
	{
		while (running)
		{
			logic();
			frameRendering();

			Display.update();
		}

		rayCaster.stop();
		try
		{
			Display.setParent(null);
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
		}
	}

	void frameRendering()
	{
		Display.sync(120);

		delta = getTime() - lastLoopTime;
		lastLoopTime = getTime();
		lastFpsTime += delta;
		fps++;

		if (lastFpsTime >= 1000)
		{
			frame.setTitle(windowtitle + " (FPS: " + fps + ")");
			lastFpsTime = 0;
			fps = 0;
		}

		GL11.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		shader.UseShader();

		rayCaster.render();
		model.draw();
		floor.render();

		Shader.ResetUseShader();

		GUI.render();

		if (currentTool != null)
			currentTool.logic();

		if (Display.isCloseRequested())
		{
			running = false;
		}
	}

	void logic()
	{
		// hacks - opengl context not available from thread handling drag and
		// drop, what do i do???
		if (filepath != "")
		{
			if (filepath.matches(".*.qb"))
				LoadQB(filepath);
			else if (filepath.matches(".*.scp"))
				LoadPal(filepath);
			filepath = "";
		}

		mousedx = Mouse.getDX();
		mousedy = Mouse.getDY();

		inputLogic();

		rayCaster.Update(fps);

		float mouseX = (float) Scale.hPosScale(Mouse.getX());
		float mouseY = (float) Scale.vPosScale(Mouse.getY());
		float dx = (float) Scale.hSizeScale(mousedx);
		float dy = (float) Scale.vSizeScale(mousedy);

		dx *= 2.0f;
		dy *= 2.0f;

		GUI.logic(mouseX, mouseY, dx, dy);

		camera.Update();

		if (Display.wasResized() && !Display.isCloseRequested())
		{
			int w = Display.getWidth();
			int h = Display.getHeight();

			width = w;
			height = h;

			GL11.glViewport(0, 0, width, height);
			Scale.SetHScaling(0, width);
			Scale.SetVScaling(0, height);

			GUI.StandardGUI(width > 1500);

			camera.projection =
					Matrix.CreatePerspectiveFieldOfView((float) Math.toRadians(fov), (float) width / (float) height,
							nearPlane, farPlane);
		}
	}

	// to be gutted
	void inputLogic()
	{
		int wheel = Mouse.getDWheel();

		if (Mouse.isButtonDown(1) && Keyboard.isKeyDown(Keyboard.KEY_LMENU))
		{
			float rotY2 = (float) Math.toRadians(-mousedx * .15f);
			float rotX2 = (float) Math.toRadians(mousedy * .15f);

			RotateVector3(rotY2, camera.direction, UP);

			Vector3 mposition = Vector3.cross(camera.direction, UP);

			RotateVector3(rotX2, camera.direction, mposition);
			camera.direction.noramlize();
		}
		else if (Mouse.isButtonDown(1) && !Keyboard.isKeyDown(Keyboard.KEY_LMENU))
		{
			Vector3 right = Vector3.cross(camera.direction, UP);
			Vector3 up = Vector3.cross(right, camera.direction);
			//
			float rotY2 = (float) Math.toRadians(-mousedx * .15f);
			float rotX2 = (float) Math.toRadians(mousedy * .15f);
			//
			up.noramlize();
			right.noramlize();

			Vector3 focus = null;
			focus = Vector3.sub(camera.position, model.GetActiveMatrix().posSize);

			float length = focus.length();

			focus.noramlize();

			RotateVector3(rotY2, focus, up);
			RotateVector3(rotX2, focus, right);

			camera.position = Vector3.mul(focus, length);
			camera.position.add(model.GetActiveMatrix().posSize);

			RotateVector3(rotY2, camera.direction, up);
			RotateVector3(rotX2, camera.direction, right);
			camera.direction.noramlize();
		}
		else if (Mouse.isButtonDown(2))
		{
			Vector3 right = Vector3.cross(camera.direction, UP);

			Vector3 up = Vector3.cross(right, camera.direction);
			right.mul(mousedx);
			up.mul(mousedy);

			right.add(up);

			camera.position.x += -right.x * .1f;
			camera.position.z += -right.z * .1f;
			camera.position.y += -right.y * .04f;
		}
		else if (wheel != 0 && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
		{
			if (wheel > 0)
			{
				camera.position.add(Vector3.mul(camera.direction, 1f));
			}
			else
			{
				camera.position.add(Vector3.mul(camera.direction, -1f));
			}
		}

		// hacks
		if (wheel != 0 && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
		{
			int direction = (int) Math.signum((float) -wheel);

			int value = ColorOption.lastOption.ID + direction;
			if (value > GUI.coloroptionStartID + 9)
				value = GUI.coloroptionStartID;
			if (value < GUI.coloroptionStartID)
				value = GUI.coloroptionStartID + 9;

			ColorOption c = (ColorOption) GUI.get(value);
			c.mouseClick(0);
			c.select();
		}

		while (Mouse.next())
		{
			int button = Mouse.getEventButton();
			boolean buttonState = Mouse.getEventButtonState();

			GUI.handleMouseInput(button, buttonState);

			if (button == 2 && buttonState && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
			{
				ColorOption.lastOption.select();
				ColorOption.lastOption.mouseClick(0);
			}
		}

		while (Keyboard.next())
		{
			int key = Keyboard.getEventKey();
			boolean keyState = Keyboard.getEventKeyState();
			lastkey = key;
			lastkeystate = keyState;
			if (GUI.handleKeyboardInput(key, keyState))
			{
				lastkey = -1;
				continue;
			}
			if (KeyboardUtil.handleKeyboardInput(key, keyState))
			{
				lastkey = -1;
				continue;
			}

			if (currentTool != null)
				currentTool.handelInput(key, keyState);

			if (key == Keyboard.KEY_P && keyState)
			{
				PaletteUtil.WritePalette();
			}

			if (key == Keyboard.KEY_O && keyState)
			{
				QbUtil.writeOBJ();
			}

			if (key == Keyboard.KEY_UP && keyState)
			{
				if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
				{
					if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
						model.GetActiveMatrix().hack_shift_model_upwards();
					else
						for (QbMatrix m : model.matrixList)
						{
							m.hack_shift_model_upwards();
						}
				}
				else if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
					model.GetActiveMatrix().hack_shift_model_backwards();
				else
					for (QbMatrix m : model.matrixList)
					{
						m.hack_shift_model_backwards();
					}
			}
			if (key == Keyboard.KEY_DOWN && keyState)
			{
				if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
				{
					if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
						model.GetActiveMatrix().hack_shift_model_downward();
					else
						for (QbMatrix m : model.matrixList)
						{
							m.hack_shift_model_downward();
						}
				}
				else if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
					model.GetActiveMatrix().hack_shift_model_forwards();
				else
					for (QbMatrix m : model.matrixList)
					{
						m.hack_shift_model_forwards();
					}
			}
			if (key == Keyboard.KEY_LEFT && keyState)
			{
				if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
					model.GetActiveMatrix().hack_shift_model_left();
				else
					for (QbMatrix m : model.matrixList)
					{
						m.hack_shift_model_left();
					}
			}

			if (key == Keyboard.KEY_RIGHT && keyState)
			{
				if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
					model.GetActiveMatrix().hack_shift_model_right();
				else
					for (QbMatrix m : model.matrixList)
					{
						m.hack_shift_model_right();
					}
			}

			if (key == Keyboard.KEY_ADD && keyState)
			{
				for (QbMatrix m : model.matrixList)
				{
					Vector3 size = m.size;

					m.reSize((int) size.x + 1, (int) size.y + 1, (int) size.z + 1);
				}
			}

			if (key == Keyboard.KEY_SUBTRACT && keyState)
			{
				for (QbMatrix m : model.matrixList)
				{
					Vector3 size = m.size;

					m.reSize((int) size.x - 1, (int) size.y - 1, (int) size.z - 1);
				}
			}
		}

		// hacks
		if (lastkey != -1)
		{
			if (Keyboard.isKeyDown(Keyboard.KEY_W))
			{
				camera.position.add(Vector3.mul(camera.direction, .01f * delta));
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_S))
			{
				camera.position.add(Vector3.mul(camera.direction, -.01f * delta));
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_D))
			{
				Vector3 mposition = Vector3.cross(camera.direction, UP);

				camera.position.x -= -mposition.x * .03f * delta;
				camera.position.z -= -mposition.z * .03f * delta;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_A))
			{
				Vector3 mposition = Vector3.cross(camera.direction, UP);

				camera.position.x += -mposition.x * .03f * delta;
				camera.position.z += -mposition.z * .03f * delta;
			}
		}
	}

	// junkyard below

	public static void LoadQB(String path)
	{
		rayCaster.disableRaycaster();

		if (model != null)
		{
			model.dispose();
		}

		try
		{
			model = QbUtil.readQB(path);
			model.generateMeshs();
			model.GetActiveMatrix().clean();

			camera.LookAtModel();
			rayCaster.enableRaycaster();
			floor.updatemesh();

			GUI.Broadcast(GUI.MESSAGE_QB_LOADED, path, 10000);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void LoadPal(String path)
	{
		try
		{
			DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(path)));

			System.out.print("Beginning read .scp \n");

			for (int i = 0; i < 10; i++)
			{
				ColorOption co = (ColorOption) GUI.get(GUI.coloroptionStartID + i);

				Color color = new Color(in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat());
				Color hue = new Color(in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat());
				Vector3 csl = new Vector3(in.readFloat(), in.readFloat(), in.readFloat());

				co.setColor(color);
				co.huecolor = hue;
				co.colorsquarelocation = csl;
			}

			System.out.print(String.format("read : %s \n", "success"));

			in.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void setTool(Tool tool)
	{
		if (currentTool != tool)
		{
			lastTool = currentTool;
			if (lastTool != null)
				lastTool.deactivate();
			currentTool = tool;
			if (currentTool != null)
				currentTool.activate();
		}
	}

	public static long getTime()
	{
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}

	boolean setDisplayMode()
	{

		try
		{
			int t = org.lwjgl.opengl.Display.getDisplayMode().getBitsPerPixel();

			DisplayMode[] dm = org.lwjgl.util.Display.getAvailableDisplayModes(width, height, -1, -1, -1, -1, 60, 60);

			org.lwjgl.util.Display.setDisplayMode(dm, new String[]
			{
					"width=" + width, "height=" + height, "freq=" + 60,
					"bpp=" + org.lwjgl.opengl.Display.getDisplayMode().getBitsPerPixel()
			});
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public static void ready2D()
	{
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(-1f, 1f, -1f, 1f, 1, -1);

		camera.ortho2d = Matrix.CreateOrthographicOffCenter(-1f, 1f, -1f, 1f, 1f, -1f);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}

	public static void ready2DText()
	{
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();

		GLU.gluOrtho2D(0.0f, (float) width, (float) height, 0.0f);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}

	static Matrix4f rtransMat;
	static Vector4f rvecPosMod = new Vector4f();
	static Vector3 UP = new Vector3(0, 1, 0);
	static Vector3 FORWARD = new Vector3(0, 0, 1);
	static Vector3 RIGHT = new Vector3(1, 0, 0);
	static Vector3 DOWN = new Vector3(0, -1, 0);

	Vector3 RotateVector3(float angle, Vector3 vec, Vector3 up)
	{
		rtransMat = new Matrix4f();
		rtransMat.rotate(angle, up.tovecf());

		rvecPosMod.x = vec.x;
		rvecPosMod.y = vec.y;
		rvecPosMod.z = vec.z;
		Matrix4f.transform(rtransMat, rvecPosMod, rvecPosMod);

		vec.x = rvecPosMod.x;
		vec.y = rvecPosMod.y;
		vec.z = rvecPosMod.z;

		return new Vector3(rvecPosMod.x, rvecPosMod.y, rvecPosMod.z);
	}

	// double w = width;
	// double h = height;
	// double ar = w / h;
	// GL11.glOrtho(-1f * ar, 1f * ar, -1f * ar, 1f * ar, 1, -1);

	// void fengInputLogic()
	// {
	// int x = Mouse.getX();
	// int y = Mouse.getY();
	// if (mousedx != 0 || mousedy != 0)
	// {
	// feng.fireMouseMovedEvent(Mouse.getX(), Mouse.getY(),
	// EventHelper.getMouseButton(-1), 1);
	//
	// if (Mouse.isButtonDown(lastbuttondown))
	// feng.fireMouseDraggedEvent(x, y,
	// EventHelper.getMouseButton(lastbuttondown), 1);
	// }
	//
	// if (lastbuttondown != -1 && !Mouse.isButtonDown(lastbuttondown))
	// {
	// feng.fireMouseReleasedEvent(x, y,
	// EventHelper.getMouseButton(lastbuttondown), 1);
	// lastbuttondown = -1;
	// }
	//
	// // feng event firing
	// while (Mouse.next())
	// {
	// if (Mouse.getEventButton() != -1 && Mouse.getEventButtonState())
	// {
	// lastbuttondown = Mouse.getEventButton();
	// feng.fireMousePressedEvent(x, y,
	// EventHelper.getMouseButton(lastbuttondown), 1);
	// }
	// int wheel = Mouse.getEventDWheel();
	// if (wheel != 0)
	// {
	// feng.fireMouseWheel(x, y, wheel > 0, 1, 1);
	// }
	// }
	//
	// Keyboard.poll();
	//
	// while (Keyboard.next())
	// {
	// if (Keyboard.getEventKeyState()) // if pressed
	// {
	// feng.fireKeyPressedEvent(EventHelper.mapKeyChar(),
	// EventHelper.mapEventKey());
	//
	// feng.fireKeyTypedEvent(EventHelper.mapKeyChar());
	// }
	// else
	// {
	// feng.fireKeyReleasedEvent(EventHelper.mapKeyChar(),
	// EventHelper.mapEventKey());
	// }
	// }
	//
	// }
}