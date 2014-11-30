package stonevox.data;

public interface Tool
{
	public void init();

	public boolean isActive();

	public void activate();

	public void deactivate();

	public boolean repeatTest(RayHitPoint hit);

	public void logic();

	public void use(RayHitPoint hit);

	public int hotKey();

	public void render();

	public void setState(int id);

	public boolean handelInput(int key, boolean state);

	public void resetUndoRedo();
}
