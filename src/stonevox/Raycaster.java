package stonevox;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import stonevox.data.Matrix;
import stonevox.data.RayHitPoint;
import stonevox.data.Vector3;
import stonevox.util.GUI;
import stonevox.util.RaycastingUtil;

public class Raycaster
{
	private float size = 0.5f;

	public RayHitPoint rayhitpoint = new RayHitPoint();
	private boolean allow_raycasting = true;
	public boolean raycast_dirt = false; // true removal : false
											// adding
	private int vertexobjectarrayid;
	private int vertexbufferid;
	private int indexbufferid;
	private Matrix transform;
	private Camera camera;
	private int index;
	Vector3 projection = new Vector3(0, 0, 0);

	float[] vertexdata;
	int[] indexdata;

	private Thread thread;
	private boolean needsupdate;

	FloatBuffer vertexbuff;
	IntBuffer indexbuff;

	public void Setup(Camera camera)
	{
		this.camera = camera;

		rayhitpoint.cubelocation.y = -10000;
		transform = new Matrix();

		vertexdata = new float[4 * 7];
		indexdata = new int[6];

		indexdata[0] = 0;
		indexdata[1] = 1;
		indexdata[2] = 2;
		indexdata[3] = 0;
		indexdata[4] = 2;
		indexdata[5] = 3;

		updateverts(Vector3.forward);

		vertexbuff = BufferUtils.createFloatBuffer(28);

		vertexbuff.put(vertexdata, 0, 28);
		vertexbuff.flip();

		vertexbufferid = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexbufferid);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexbuff, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		indexbuff = BufferUtils.createIntBuffer(6);

		indexbuff.put(indexdata, 0, 6);
		indexbuff.flip();

		indexbufferid = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbufferid);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbuff, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

		vertexobjectarrayid = GL30.glGenVertexArrays();

		GL30.glBindVertexArray(vertexobjectarrayid);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexbufferid);
		GL20.glEnableVertexAttribArray(0);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 7 << 2, 0l);
		GL20.glEnableVertexAttribArray(1);
		GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 7 << 2, 3 << 2);
		GL20.glEnableVertexAttribArray(2);
		GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, 7 << 2, 6 << 2);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbufferid);
		GL30.glBindVertexArray(0);
	}

	public void Start()
	{
		thread = new Thread(new Runnable()
		{
			public void run()
			{

				while (true)
				{
					if (!allow_raycasting)
					{
						try
						{
							Thread.sleep(5);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
						continue;
					}
					RayHitPoint m = Program.model.rayTest(camera.position, projection);
					RayHitPoint f = Program.floor.rayCast(camera.position, projection);

					if (m != null)
					{
						if ((!rayhitpoint.cubelocation.isEqual(m.cubelocation))
								|| (rayhitpoint.cubelocation.isEqual(m.cubelocation) && !rayhitpoint.cubenormal
										.isEqual(m.cubenormal)))
						{
							rayhitpoint = m;
							needsupdate = true;
						}
					}
					else if (f != null)
					{

						if ((!rayhitpoint.cubelocation.isEqual(f.cubelocation))
								|| (rayhitpoint.cubelocation.isEqual(f.cubelocation) && !rayhitpoint.cubenormal
										.isEqual(f.cubenormal)))
						{
							rayhitpoint = f;
							needsupdate = true;
						}
					}
					else if (rayhitpoint.cubelocation.y != -10000)
					{
						rayhitpoint.cubelocation.y = -10000;
						needsupdate = true;
					}

					try
					{
						Thread.sleep(5);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		});

		thread.start();
	}

	public void stop()
	{
		if (thread != null)
			thread.interrupt();
	}

	public void Update(int fps)
	{
		if (Program.multithreading_raycasting)
		{
			projection = RaycastingUtil.unproject(Mouse.getX(), Mouse.getY());

			if (allow_raycasting && needsupdate)
			{
				updateVisualHit();
			}
		}
		else if (allow_raycasting && fps % 2 == 1)
		{
			raycastTest();
		}
	}

	private void updateVisualHit()
	{
		updateverts(rayhitpoint.cubenormal);

		transform =
				Matrix.CreateTranslation(rayhitpoint.cubelocation.x, rayhitpoint.cubelocation.y,
						rayhitpoint.cubelocation.z);

		vertexbuff.put(vertexdata, 0, 28);
		vertexbuff.flip();

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexbufferid);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexbuff, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		indexbuff.put(indexdata, 0, 6);
		indexbuff.flip();

		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbufferid);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbuff, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		needsupdate = false;

		GUI.setStatus(String.format("Cube : (%s, %s, %s) - %s", rayhitpoint.cubelocation.x, rayhitpoint.cubelocation.y,
				rayhitpoint.cubelocation.z, rayhitpoint.cubenormal.toside().toString()));
	}

	private void raycastTest()
	{
		projection = RaycastingUtil.unproject(Mouse.getX(), Mouse.getY());

		RayHitPoint p = Program.model.rayTest(camera.position, projection);

		if (p != null)
		{
			if ((!rayhitpoint.cubelocation.isEqual(p.cubelocation))
					|| (rayhitpoint.cubelocation.isEqual(p.cubelocation) && !rayhitpoint.cubenormal
							.isEqual(p.cubenormal)))
			{
				rayhitpoint = p;
				updateverts(rayhitpoint.cubenormal);

				transform =
						Matrix.CreateTranslation(rayhitpoint.cubelocation.x, rayhitpoint.cubelocation.y,
								rayhitpoint.cubelocation.z);

				vertexbuff.put(vertexdata, 0, 28);
				vertexbuff.flip();

				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexbufferid);
				GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexbuff, GL15.GL_DYNAMIC_DRAW);
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

				indexbuff.put(indexdata, 0, 6);
				indexbuff.flip();

				GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbufferid);
				GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbuff, GL15.GL_DYNAMIC_DRAW);
				GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

				GUI.setStatus(String.format("Cube : (%s, %s, %s) - %s", rayhitpoint.cubelocation.x,
						rayhitpoint.cubelocation.y, rayhitpoint.cubelocation.z, rayhitpoint.cubenormal.toside()
								.toString()));
			}
		}
		else if (rayhitpoint.cubelocation.y != -10000)
		{
			rayhitpoint.cubelocation.y = -10000;

			updateverts(rayhitpoint.cubenormal);

			transform =
					Matrix.CreateTranslation(rayhitpoint.cubelocation.x, rayhitpoint.cubelocation.y,
							rayhitpoint.cubelocation.z);

			vertexbuff.put(vertexdata, 0, 28);
			vertexbuff.flip();

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexbufferid);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexbuff, GL15.GL_DYNAMIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

			indexbuff.put(indexdata, 0, 6);
			indexbuff.flip();

			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbufferid);
			GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexbuff, GL15.GL_DYNAMIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

			GUI.setStatus(String.format("Cube : (%s, %s, %s) - %s", rayhitpoint.cubelocation.x,
					rayhitpoint.cubelocation.y, rayhitpoint.cubelocation.z, rayhitpoint.cubenormal.toside().toString()));
		}
	}

	private void updateverts(Vector3 normal)
	{
		index = 0;
		if (normal.z == 1)
		{
			index = putbuffer(index, -size, -size, size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
			index = putbuffer(index, size, -size, size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
			index = putbuffer(index, size, size, size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
			index = putbuffer(index, -size, size, size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
		}

		if (normal.z == -1)
		{
			index = putbuffer(index, -size, -size, -size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
			index = putbuffer(index, -size, size, -size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
			index = putbuffer(index, size, size, -size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
			index = putbuffer(index, size, -size, -size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
		}

		if (normal.x == -1)
		{
			index = putbuffer(index, -size, -size, size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
			index = putbuffer(index, -size, size, size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
			index = putbuffer(index, -size, size, -size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
			index = putbuffer(index, -size, -size, -size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
		}

		if (normal.x == 1)
		{
			index = putbuffer(index, size, -size, -size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
			index = putbuffer(index, size, size, -size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
			index = putbuffer(index, size, size, size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
			index = putbuffer(index, size, -size, size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
		}

		if (normal.y == 1)
		{
			index = putbuffer(index, -size, size, size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
			index = putbuffer(index, size, size, size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
			index = putbuffer(index, size, size, -size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
			index = putbuffer(index, -size, size, -size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
		}

		if (normal.y == -1)
		{
			index = putbuffer(index, -size, -size, size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
			index = putbuffer(index, -size, -size, -size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
			index = putbuffer(index, size, -size, -size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
			index = putbuffer(index, size, -size, size);
			index = putbuffer(index, 1, 0, 0);
			index = putbuffer(index, 1);
		}
	}

	private int putbuffer(int index, float v1, float v2, float v3)
	{
		vertexdata[index] = v1;
		vertexdata[index + 1] = v2;
		vertexdata[index + 2] = v3;
		return index + 3;
	}

	private int putbuffer(int index, float v1)
	{
		vertexdata[index] = v1;
		return index + 1;
	}

	public void render()
	{
		if (rayhitpoint.cubelocation.y != -10000)
		{
			Program.shader.WriteUniformMatrix4("modelview\0", Matrix.Multiply(transform, camera.modelview).GetBuffer());

			GL30.glBindVertexArray(vertexobjectarrayid);
			GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0l);
			GL30.glBindVertexArray(0);

			// GL11.glMatrixMode(GL11.GL_MODELVIEW);
			// GL11.glLoadIdentity();
			//
			// GLU.gluLookAt(camera.position.x, camera.position.y,
			// camera.position.z, camera.position.x
			// + camera.direction.x, camera.position.y + camera.direction.y,
			// camera.position.z
			// + camera.direction.z, 0f, 1f, 0f);
			//
			// drawCube(rayhitpoint.cubelocation.x, rayhitpoint.cubelocation.y,
			// rayhitpoint.cubelocation.z, 1, 0, 0,
			// rayhitpoint.cubenormal);
		}
	}

	void drawCube(float x, float y, float z, int cr, int cg, int cb, Vector3 normal)
	{
		float sizex = 0.5f;
		float sizey = 0.5f;
		float sizez = 0.5f;

		// shader.WriteUniformMatrix4("modelview\0",
		// Matrix.Multiply(Matrix.CreateTranslation(x, y, z),
		// camera.modelview).GetBuffer());

		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, z);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glColor3f(cr, cg, cb);

		if (normal.z == 1)
		{
			GL11.glVertex3f(-sizex, -sizey, sizez);
			GL11.glVertex3f(sizex, -sizey, sizez);
			GL11.glVertex3f(sizex, sizey, sizez);
			GL11.glVertex3f(-sizex, sizey, sizez);
		}

		if (normal.z == -1)
		{
			GL11.glVertex3f(-sizex, -sizey, -sizez);
			GL11.glVertex3f(-sizex, sizey, -sizez);
			GL11.glVertex3f(sizex, sizey, -sizez);
			GL11.glVertex3f(sizex, -sizey, -sizez);
		}

		if (normal.x == -1)
		{
			GL11.glVertex3f(-sizex, -sizey, sizez);
			GL11.glVertex3f(-sizex, sizey, sizez);
			GL11.glVertex3f(-sizex, sizey, -sizez);
			GL11.glVertex3f(-sizex, -sizey, -sizez);
		}

		if (normal.x == 1)
		{
			GL11.glVertex3f(sizex, -sizey, -sizez);
			GL11.glVertex3f(sizex, sizey, -sizez);
			GL11.glVertex3f(sizex, sizey, sizez);
			GL11.glVertex3f(sizex, -sizey, sizez);
		}

		if (normal.y == 1)
		{
			GL11.glVertex3f(-sizex, sizey, sizez);
			GL11.glVertex3f(sizex, sizey, sizez);
			GL11.glVertex3f(sizex, sizey, -sizez);
			GL11.glVertex3f(-sizex, sizey, -sizez);
		}

		if (normal.y == -1)
		{
			GL11.glVertex3f(-sizex, -sizey, sizez);
			GL11.glVertex3f(-sizex, -sizey, -sizez);
			GL11.glVertex3f(sizex, -sizey, -sizez);
			GL11.glVertex3f(sizex, -sizey, sizez);
		}

		GL11.glEnd();
		GL11.glPopMatrix();
	}

	public void enableRaycaster()
	{
		this.allow_raycasting = true;
	}

	public void disableRaycaster()
	{
		this.allow_raycasting = false;
		rayhitpoint.cubelocation.y = -10000;
	}

	public boolean getAllowRaycasting()
	{
		return allow_raycasting;
	}
}
