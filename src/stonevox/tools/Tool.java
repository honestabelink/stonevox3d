package stonevox.tools;

import stonevox.data.RayHitPoint;

public interface Tool
{
	public void init();

	public boolean isActive();

	public void activate();

	public void deactivate();

	public boolean repeatTest(RayHitPoint hit);

	public void logic();

	public void use(RayHitPoint hit);

	public void undo();

	public void redo();

	public int hotKey();

	public void render();

	public void setState(int id);
}
