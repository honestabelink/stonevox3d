using OpenTK.Graphics;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public class QbModelTabs : Widget
    {
        List<QbModelTab> tabs;
        float borderscale = 2.5f;

        // persistent cross UI changes
        static int selected;

        int startindex;
        int lowest;
        int highest;

        WidgetEventHandler tabHandler;

        public QbModelTabs()
            : base()
        {
            float height = Client.window.Qfont.fontData.maxGlyphHeight;
            height = height.ScaleVerticlSize();
            float vBorderOffset = -(3f).ScaleVerticlSize();
            float hBorderOffset = (3f).ScaleHorizontalSize();

            SetBoundsNoScaling(-1 , 1 - height *borderscale + vBorderOffset, 2, height *borderscale);

            appearence.AddAppearence("background", new PlainBackground(new Color4(100, 87, 61, 255)));
            appearence.AddAppearence("border", new PlainBorder(4, new Color4(122, 106, 70, 255)));

            tabs = new List<QbModelTab>();

            foreach (var model in Singleton<QbManager>.INSTANCE.models)
            {
                QbModelTab default_tab = new QbModelTab(model);
                tabHandler = new WidgetEventHandler()
                {
                    mousedownhandler = (e, mouse) =>
                    {
                        if (mouse.IsPressed && mouse.Button == OpenTK.Input.MouseButton.Left)
                        {
                            if (e is QbModelTab)
                            {
                                var tab = e as QbModelTab;
                                selected = tabs.IndexOf(tab);
                                Singleton<QbManager>.INSTANCE.ActiveModel = tab.model;

                            }
                            else if (e is Label)
                            {
                                var tab = e.customData["qbmodeltab"] as QbModelTab;
                                selected = tabs.IndexOf(tab);
                                Singleton<QbManager>.INSTANCE.ActiveModel = tab.model;
                            }
                            UpdateTabs();
                        }
                    }
                };
                default_tab.handler = tabHandler;
                tabs.Add(default_tab);
            }
            UpdateTabs();
        }

        public void UpdateTabs()
        {
            float startX = -1;
            int endindex = 0;

            bool hasselected = false;
            bool firstrun = true;

            while (!hasselected)
            {
                for (int i = startindex; i < tabs.Count; i++)
                {
                    if (i == selected)
                    {
                        hasselected = true;
                        tabs[i].Enable = true;
                        tabs[i].SetSelected(true, ref startX);
                    }
                    else
                    {
                        tabs[i].SetSelected(false, ref startX);
                        tabs[i].Enable = true;
                    }

                    if (startX == -2 || startX >= .85f)
                    {
                        if (i == selected)
                            hasselected = false;
                        tabs[i].Enable = false;
                        startX = -1f;
                        endindex = i;
                        highest = tabs.Count - 1;
                        break;
                    }
                    endindex = i;
                }


                if (hasselected)
                    break;
                else
                    startindex++;
                if (firstrun)
                {
                    firstrun = false;
                    startindex = 0;
                }
            }

            lowest = Math.Min(0, startindex);

            for (int i = 0; i < startindex; i++)
                tabs[i].Enable = false;
            for (int i = endindex+1; i < tabs.Count; i++)
                tabs[i].Enable = false;
        }

        public void AddWidgets(ClientGUI gui)
        {
            gui.widgets.Add(this);

            tabs.ForEach(t => t.AddWidgets(gui));
        }

        public override void HandleMessageRecieved(BroadcastMessage message)
        {
            if (message.messgae == Message.ModelImported)
            {
                var tab = new QbModelTab(message.args[0] as QbModel);
                tab.handler = tabHandler;
                tabs.Add(tab);

                selected = tabs.Count - 1;
                // hacks
                Client.OpenGLContextThread.Add(() => 
                {
                    tab.AddWidgets(Singleton<ClientGUI>.INSTANCE);
                    UpdateTabs();
                });
            }
            else if (message.messgae == Message.ActiveModelChanged)
            {
                Singleton<Camera>.INSTANCE.LookAtModel();
            }

            base.HandleMessageRecieved(message);
        }
    }
}
