package stonevox;

import java.awt.Canvas;
import java.awt.event.WindowEvent;
import java.io.File;

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
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import stonevox.data.Keyhook;
import stonevox.data.Matrix;
import stonevox.data.QbModel;
import stonevox.data.Shader;
import stonevox.data.Vector3;
import stonevox.gui.ColorOption;
import stonevox.tools.Tool;
import stonevox.tools.ToolColorPicker;
import stonevox.tools.ToolEdit;
import stonevox.tools.ToolPainter;
import stonevox.tools.ToolSave;
import stonevox.tools.ToolSelection;
import stonevox.tools.ToolSettings;
import stonevox.util.DNDHandler;
import stonevox.util.FontUtil;
import stonevox.util.GUI;
import stonevox.util.KeyboardUtil;
import stonevox.util.QbUtil;
import stonevox.util.Scale;

public class Program
{

	// (float) (720.0 * (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL))

	String							windowtitle					= "StoneVox";
	boolean							running						= true;
	long							lastLoopTime;
	long							lastFpsTime;
	int								mousedx;
	int								mousedy;
	boolean							firstRun					= true;

	public static QbModel			model;
	public static float				fov							= 45f;
	public static float				nearPlane					= 1f;
	public static float				farPlane					= 100f;
	public static int				fps;
	public static int				height						= 800;
	public static int				width						= 800;
	public static long				delta						= 1l;

	public static Camera			camera;
	public static Shader			shader;
	public static Raycaster			rayCaster;
	public static Floor				floor;
	public static boolean			multithreading_raycasting	= true;

	public static ToolPainter		toolpainter;
	public static ToolColorPicker	toolcolorpicker;
	public static ToolSelection		toolselection;
	public static ToolEdit			tooledit;
	public static ToolSave			toolsave;
	public static ToolSettings		toolsettings;

	public static Tool				lastTool;
	public static Tool				currentTool;

	public static String			filepath					= "";

	static Canvas					openglSurface;
	static JFrame					frame;
	static JList<File>				list						= new JList<File>();

	// to get rid of
	boolean							wasLeftClickDown;
	int								lastkey;
	boolean							lastkeystate;

	public static boolean			debug						= false;

	public static void main(String[] args)
	{
		new Program().execute(args);
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
		frame.dispose();
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
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1280, 800);
		frame.add(openglSurface);
		// frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		frame.setTransferHandler(new DNDHandler(list));
		openglSurface.setSize(1280, 800);

		try
		{
			Display.setParent(openglSurface);
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
		}

		setDisplayMode();

		frame.setTitle(windowtitle);

		try
		{
			Display.setFullscreen(false);
			Display.setResizable(true);
			Display.create();

			width = Display.getWidth();
			height = Display.getHeight();

			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0);
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

		shader.vertexshader_path = "/shaders/VertexShader.txt";
		shader.fragmentshader_path = "/shaders/FragmentShader.txt";
		shader.Load();
		shader.InstallShader();
		shader.UseShader();

		shader.CreateUniformAccess("modelview\0");

		toolcolorpicker = new ToolColorPicker();
		tooledit = new ToolEdit();
		toolpainter = new ToolPainter();
		toolsave = new ToolSave();
		toolselection = new ToolSelection();
		toolsettings = new ToolSettings();

		model = QbUtil.GetDefault();

		camera = new Camera();
		camera.projection =
				Matrix.CreatePerspectiveFieldOfView((float) Math.toRadians(fov), (float) width / (float) height,
						nearPlane, farPlane);
		camera.LookAtModel();

		rayCaster = new Raycaster();
		rayCaster.disableRaycaster();
		rayCaster.Setup(camera);

		floor = new Floor();

		FontUtil.loadFont("default", "/data/fonts/Bigfish.ttf");

		GUI.Hacked720pGUI();

		KeyboardUtil.Add(Keyboard.KEY_LMENU, new Keyhook(null)
		{
			public void down()
			{
				if (currentTool != toolcolorpicker)
				{
					setTool(toolcolorpicker);
					toolcolorpicker.activate();
					toolpainter.SetColor(ColorOption.lastOption.color);
				}
				super.down();
			}

			public void up()
			{
				if (currentTool == toolcolorpicker)
				{
					setTool(lastTool);
					toolcolorpicker.deactivate();
					toolpainter.SetColor(ColorOption.lastOption.color);
				}
				super.up();
			}
		});

		Thread t = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				int cores = Runtime.getRuntime().availableProcessors();

				if (multithreading_raycasting && cores > 1)
					rayCaster.Start();
				if (cores <= 1)
				{
					multithreading_raycasting = false;
				}
			}
		});
		t.run();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Display.destroy();
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

		GL11.glClearColor(0, 0, 0, 1);
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
			LoadQB(filepath);
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
			// GUI.BeginWindowSizeChange();

			int w = Display.getWidth();
			int h = Display.getHeight();

			// GUI.RescaleWindowChange((float) width, (float) height, (float) w,
			// (float) h);

			width = w;
			height = h;

			GL11.glViewport(0, 0, width, height);
			Scale.SetHScaling(0, width);
			Scale.SetVScaling(0, height);
			// Scale.SetAspectRatio((float) width, (float) height);

			// GUI.EndWindowSizeChange();

			if (width > 1500)
			{
				GUI.Hacked1080pGUI();
			}
			else
				GUI.Hacked720pGUI();
			// end dirt

			camera.projection =
					Matrix.CreatePerspectiveFieldOfView((float) Math.toRadians(fov), (float) width / (float) height,
							nearPlane, farPlane);
		}
	}

	// to be gutted
	void inputLogic()
	{
		int wheel = Mouse.getDWheel();

		if (Mouse.isButtonDown(1) && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
		{
			float rotY2 = (float) Math.toRadians(-mousedx * .15f);
			float rotX2 = (float) Math.toRadians(mousedy * .15f);

			RotateVector3(rotY2, camera.direction, UP);

			Vector3 mposition = Vector3.cross(camera.direction, UP);

			RotateVector3(rotX2, camera.direction, mposition);
			camera.direction.noramlize();
		}
		else if (Mouse.isButtonDown(1) && !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
		{

			Vector3 right = Vector3.cross(camera.direction, UP);
			Vector3 up = Vector3.cross(right, camera.direction);

			float rotY2 = (float) Math.toRadians(-mousedx * .15f);
			float rotX2 = (float) Math.toRadians(mousedy * .15f);

			up.noramlize();
			right.noramlize();

			Vector3 focus = null;
			focus = Vector3.sub(camera.position, model.GetActiveMatrix().pos_size);

			float length = focus.length();

			focus.noramlize();

			RotateVector3(rotY2, focus, up);
			RotateVector3(rotX2, focus, right);

			Vector3 pos = null;
			pos = model.GetActiveMatrix().pos_size;

			camera.position = Vector3.mul(focus, length);
			// this is the junk line - below
			camera.position.add(model.GetActiveMatrix().pos_size);
			camera.direction = Vector3.sub(pos, camera.position);
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
		else if (wheel != 0)
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

		while (Mouse.next())
		{
			int button = Mouse.getEventButton();
			boolean buttonState = Mouse.getEventButtonState();

			GUI.handleMouseInput(button, buttonState);
		}

		while (Keyboard.next())
		{
			int key = Keyboard.getEventKey();
			boolean keyState = Keyboard.getEventKeyState();

			if (KeyboardUtil.handleKeyboardInput(key, keyState))
				continue;

			if (GUI.handleKeyboardInput(key, keyState))
				continue;

			lastkey = key;
			lastkeystate = keyState;
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
		}
		catch (Exception e)
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

	static Matrix4f	rtransMat;
	static Vector4f	rvecPosMod	= new Vector4f();
	static Vector3	UP			= new Vector3(0, 1, 0);
	static Vector3	FORWARD		= new Vector3(0, 0, 1);
	static Vector3	RIGHT		= new Vector3(1, 0, 0);

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