using OpenTK;
using OpenTK.Graphics;
using OpenTK.Platform;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

public class SyncWindow : GameWindow, IGameWindow, INativeWindow, IDisposable
{
    public SyncWindow(int width, int height, GraphicsMode mode)
        :base(width, height, mode)
    {
    }

    public void RunSimple(int targetFps)
    {
        base.Visible = true;
        try
        {
            TargetUpdateFrequency = targetFps;
            TargetRenderFrequency = targetFps;

            FrameEventArgs updateArgs = new FrameEventArgs();
            FrameEventArgs renderArgs = new FrameEventArgs();

            try
            {
                OnLoad(EventArgs.Empty);
                OnResize(EventArgs.Empty);
            }
            catch (Exception e)
            {
                Trace.WriteLine(String.Format("OnLoad failed: {0}", e.ToString()));
                return;
            }

            Debug.Print("Entering main loop.");

            Stopwatch stopWatch = Stopwatch.StartNew();

            int[] sleepTimes = new int[15];
            for (int i = 0; i < sleepTimes.Length; i++) sleepTimes[i] = 1000 / targetFps;
            int frameSleepTime = 0;

            int frames = 0;
            double previousElapsedSeconds = 0;
            while (true)
            {
                frames++;

                double totalElapsedSeconds = stopWatch.Elapsed.TotalSeconds;
                double frameElapsedSeconds = totalElapsedSeconds - previousElapsedSeconds;
                previousElapsedSeconds = totalElapsedSeconds;

                if (totalElapsedSeconds >= 0.25)
                {
                    double fps = frames / totalElapsedSeconds;

                    if (fps < targetFps)
                    {
                        int max = 0;
                        for (int i = 1; i < sleepTimes.Length; i++)
                        {
                            if (sleepTimes[i] > sleepTimes[max]) max = i;
                        }
                        sleepTimes[max] = System.Math.Max(0, sleepTimes[max] - 1);
                    }
                    else
                    {
                        int min = 0;
                        for (int i = 1; i < sleepTimes.Length; i++)
                        {
                            if (sleepTimes[i] < sleepTimes[min]) min = i;
                        }
                        sleepTimes[min] += 1;
                    }

                    //Console.Write(string.Format("FPS:{0} Target:{1} SleepTimes:", fps, targetFps));
                    //for (int i = 0; i < sleepTimes.Length; i++)
                    //{
                    //    Console.Write(string.Format(" {0}", sleepTimes[i]));
                    //}
                    //Console.Write("\n");

                    stopWatch.Reset();
                    stopWatch.Start();
                    frames = 0;
                    previousElapsedSeconds = 0;
                }

                ProcessEvents();

                updateArgs = new FrameEventArgs(frameElapsedSeconds);
                this.OnUpdateFrame(updateArgs);

                renderArgs = new FrameEventArgs(frameElapsedSeconds);
                this.OnRenderFrame(renderArgs);

                System.Threading.Thread.Sleep(sleepTimes[frameSleepTime]);
                frameSleepTime = (frameSleepTime + 1) % sleepTimes.Length;
            }
        }
        catch (Exception ex)
        {
            Debug.WriteLine("Exception Caught - exiting main loop.");
        }
        finally
        {
            Debug.Print("Restoring priority.");
            Thread.CurrentThread.Priority = ThreadPriority.Normal;

            OnUnload(EventArgs.Empty);

            if (Exists)
            {
                Context.Dispose();
            }
            while (this.Exists)
                this.ProcessEvents();
        }
    }
}
