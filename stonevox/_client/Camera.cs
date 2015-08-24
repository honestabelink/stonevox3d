using OpenTK;
using OpenTK.Input;
using stonevox;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public class Camera : Singleton<Camera>
    {
        public Vector3 position;
        public Vector3 direction;

        public Matrix4 projection;
        public Matrix4 view;
        public Matrix4 modelviewprojection;

        private ClientInput input;

        public Vector3 cameraright { get { return Vector3.Cross(direction, VectorUtils.UP); } }
        public Vector3 cameraup { get { return Vector3.Cross(cameraright, direction); } }

        float fov = 45f;
        float nearPlane = 1f;
        float farPlane = 300;

        public Camera(GLWindow window, ClientInput input)
            : base()
        {
            this.input = input;

            window.Resize += (e, s) =>
                {
                    projection = Matrix4.CreatePerspectiveFieldOfView(MathHelper.DegreesToRadians(fov), (float)window.Width / (float)window.Height, nearPlane, farPlane);
                };

            position = new Vector3(0f, 0f, 10f);
            direction = new Vector3(0f, 0f, 1f);
            direction.Normalize();

            projection = Matrix4.CreatePerspectiveFieldOfView(MathHelper.DegreesToRadians(fov), (float)window.Width / (float)window.Height, nearPlane, farPlane);
            view = Matrix4.LookAt(position, position + direction, VectorUtils.UP);
            modelviewprojection = projection * view;

            InputHandler handler = new InputHandler()
            {
                mousewheelhandler = (e) =>
                    {
                        if (Singleton<ClientGUI>.INSTANCE.OverWidget) return;

                        if (e.Delta < 0)
                        {
                            position -= direction * 6 * 1f;
                        }
                        else if (e.Delta > 0)
                        {
                            position += direction * 6 * 1f;
                        }
                    }
            };
            input.AddHandler(handler);
        }

        public void update(float delta)
        {
            if (!Singleton<ClientGUI>.INSTANCE.OverWidget)
            {
                if (input.Keydown(Key.W) || input.Keydown(Key.Up))
                {
                    position += direction * 25f * delta;
                }

                if (input.Keydown(Key.S) || input.Keydown(Key.Down))
                {
                    position -= direction * 25f * delta;
                }

                if (input.Keydown(Key.A) || input.Keydown(Key.Left))
                {
                    Vector3 camerar = cameraright;

                    position.X -= camerar.X * 25f * delta;
                    position.Z -= camerar.Z * 25f * delta;
                }

                if (input.Keydown(Key.D) || input.Keydown(Key.Right))
                {
                    Vector3 camerar = cameraright;

                    position.X += camerar.X * 25f * delta;
                    position.Z += camerar.Z * 25f * delta;
                }

                if (input.mousedown(MouseButton.Right))
                {
                    Vector3 camright = cameraright;
                    Vector3 camup = cameraup;

                    float rotz2 = (float)MathHelper.DegreesToRadians(-input.mousedx * .15f);
                    float rotX2 = (float)MathHelper.DegreesToRadians(-input.mousedy * .15f);

                    camright.Normalize();
                    camup.Normalize();

                    Vector3 focus = position - QbManager.getactivematrix().centerposition;
                    float length = focus.Length;
                    focus.Normalize();

                    focus = Vector3.Transform(focus, Quaternion.FromAxisAngle(camup, rotz2));
                    focus = Vector3.Transform(focus, Quaternion.FromAxisAngle(camright, rotX2));

                    position = focus * length + QbManager.getactivematrix().centerposition;

                    direction = Vector3.Transform(direction, Quaternion.FromAxisAngle(camup, rotz2));
                    direction = Vector3.Transform(direction, Quaternion.FromAxisAngle(camright, rotX2));
                    direction.Normalize();
                }

                if (input.mousedown(MouseButton.Middle))
                {
                    Vector3 camright = Vector3.Cross(direction, Vector3.UnitY);

                    camright.Normalize();

                    Vector3 camup = Vector3.Cross(camright, direction);

                    camup.Normalize();

                    camright *= -input.mousedx;
                    camup *= input.mousedy;

                    camright += camup;

                    position.X += camright.X * .06f;
                    position.Y += camright.Y * .06f;
                    position.Z += camright.Z * .06f;
                }
            }

            view = Matrix4.LookAt(position, position + direction, Vector3.UnitY);
            modelviewprojection = view * projection;
        }

        public void LookAtModel()
        {
            position =
                    new Vector3(QbManager.getactivematrix().size.X * .5f - .5f,
                            QbManager.getactivematrix().size.Y * .5f * 3.0f,
                            QbManager.getactivematrix().size.Z * .5f * 3.5f);

            Vector3.Subtract(ref QbManager.getactivematrix().centerposition, ref position, out direction);
            direction.Normalize();

            view = Matrix4.LookAt(position, position + direction, cameraup);
            modelviewprojection = Matrix4.CreateScale(.1f) * projection * view;
        }
    }
}
