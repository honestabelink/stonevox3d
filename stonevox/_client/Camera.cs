using OpenTK;
using OpenTK.Input;
using stonevox;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
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

        bool dotransition;
        Vector3 startpos;
        Vector3 startdir;
        Vector3 _goto;
        Vector3 centerposition;
        float time = 0;


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
                        if (!Singleton<ClientGUI>.INSTANCE.OverWidget)
                        {
                            if (e.Delta < 0)
                            {
                                position -= direction * 6 * 1f;
                            }
                            else if (e.Delta > 0)
                            {
                                position += direction * 6 * 1f;
                            }
                        }
                        else if (Singleton<ClientGUI>.INSTANCE.lastWidgetOver.Drag)
                        {
                            if (e.Delta < 0)
                            {
                                position -= direction * 6 * 1f;
                            }
                            else if (e.Delta > 0)
                            {
                                position += direction * 6 * 1f;
                            }
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
            else if (Singleton<ClientGUI>.INSTANCE.lastWidgetOver.Drag)
            {
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

            if (dotransition)
            {
                time += delta;
                if ((_goto - position).Length < 1f)
                {
                    position = _goto;
                    direction = (centerposition - position).Normalized();
                    dotransition = false;
                    time = 0;
                }
                else
                {
                    position = Vector3.Lerp(startpos, _goto, time / .5f);
                    direction = Vector3.Lerp(startdir, (centerposition - position).Normalized(), time / .5f);
                    direction.Normalize();
                }
            }

            view = Matrix4.LookAt(position, position + direction, Vector3.UnitY);
            modelviewprojection = view * projection;
        }

        public void LookAtModel()
        {
            int minx = 10000;
            int miny = 10000;
            int minz = 10000;
            int maxx = 0;
            int maxy = 0;
            int maxz = 0;
            int sizex = 0;
            int sizey = 0;
            int sizez = 0;

            foreach (var matrix in Client.window.model.matrices)
            {
                if (matrix.minx < minx)
                    minx = matrix.minx;
                if (matrix.maxx > maxx)
                    maxx = matrix.maxx;

                if (matrix.miny < miny)
                    miny = matrix.miny;
                if (matrix.maxy > maxy)
                    maxy = matrix.maxy;

                if (matrix.minz < minz)
                    minz = matrix.minz;
                if (matrix.maxz > maxz)
                    maxz = matrix.maxz;
            }

            sizex = maxx - minx;
            sizey = maxy - miny;
            sizez = maxz - minz;

            float backup = 0;

            if (sizey * 1.5f > 20)
                backup = sizey * 1.5f;
            else if (sizex * 1.5f > 20)
                backup = sizex * 1.5f;
            else backup = 20;

            var centerpos = new Vector3((minx + ((maxx - minx) / 2)), (miny + ((maxy - miny) / 2)), (minz + ((maxz - minz) / 2)));
            position = centerpos + new Vector3(.5f, sizey * .65f, backup);

            Vector3.Subtract(ref centerpos, ref position, out direction);
            direction.Normalize();

            view = Matrix4.LookAt(position, position + direction, cameraup);
            modelviewprojection = Matrix4.CreateScale(.1f) * projection * view;
        }

        public void LookAtMatrix()
        {
            startpos = position;
            startdir = direction;

            var mat = QbManager.getactivematrix();
            _goto = mat.centerposition;
            centerposition = mat.centerposition;

            float height = (mat.maxy - mat.miny);
            float width = (mat.maxx - mat.minx);
            float length = (mat.maxz - mat.minz);

            float distance;

            if (height < 18 && width < 18 && length < 18)
            {
                height = (mat.maxy - mat.miny) * 3.5f;
                width = (mat.maxx - mat.minx) * 3.5f;
                length = (mat.maxz - mat.minz) * 3.5f;

                distance = Math.Max(Math.Max(height, width), length);
            }
            else
            {
                height = (mat.maxy - mat.miny) * 1.5f;
                width = (mat.maxx - mat.minx) * 1.5f;
                length = (mat.maxz - mat.minz) * 1.5f;

                distance = Math.Max(Math.Max(height, width), length);
            }

            Vector3 offset = direction;
            if (Math.Abs(offset.X) > .5f)
                offset.X = 1f * -Math.Sign(direction.X);
            else
                offset.X = 0;
            if (Math.Abs(offset.Z) > .5f)
                offset.Z = 1f * -Math.Sign(direction.Z);
            else
                offset.Z = 0;
            if (Math.Abs(offset.Y) > .5f)
                offset.Y = 1f * -Math.Sign(direction.Y);
            else
                offset.Y = 0;

            _goto += offset * distance;
            dotransition = true;
        }
    }
}
