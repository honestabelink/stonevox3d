package stonevox.data;

public interface GUIdecorator
{
	public boolean isEnabled();

	public void setEnabled(boolean enabled);

	public void paint(float x, float y, float width, float height);

	public void dispose();
}
