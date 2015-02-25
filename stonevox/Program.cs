using OpenTK;
using OpenTK.Graphics.OpenGL4;
using OpenTK.Input;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Reflection.Emit;
using System.Runtime.InteropServices;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace stonevox
{
    class Program
    {
        //[DllImport("userid.dll", CallingConvention = CallingConvention.Cdecl)]
        //[return: MarshalAs(UnmanagedType.BStr)]
        //static extern string encrypt(string ip);

        //[DllImport("userid.dll", CallingConvention = CallingConvention.Cdecl)]
        //[return: MarshalAs(UnmanagedType.BStr)]
        //static extern string decrypt(string ip);

        static string lol = "i_made_onion_games_in_the_past";

        public static string Encrypt(string ip)
        {
            string e = EncryptOrDecrypt(ip, lol);
            string re = "";

            foreach (var c in e)
                re += ((int)c).ToString() + '-';

            re = re.Remove(re.Length - 1);

            return re;
        }

        public static string Decrypt(string ip)
        {
            string d = "";

            string[] chars = ip.Split('-');

            foreach (var c in chars)
                d += (char)(Convert.ToInt32(c));

            return EncryptOrDecrypt(d, lol);
        }

        public static string EncryptOrDecrypt(string text, string key)
        {
            var result = new StringBuilder();

            for (int c = 0; c < text.Length; c++)
                result.Append((char)((uint)text[c] ^ (uint)key[c % key.Length]));

            return result.ToString();
        }

        public static Thread serverthread;
        public static Thread clientthread;
        public static DragDropTarget dnd;

        [STAThread()]
        static void Main(string[] args)
        {
            Console.Title = "StoneVox 3D";

            if (args.Length == 0)
            {
                args = new string[] { "/startserver", "/startclient", "/connectlocal" };
            }

            Regex r = new Regex("(?<match>[^\\s\"]+)|(?<match>\"[^\"]*\")");
            var cmds = typeof(ConsoleCommands).GetMethods(BindingFlags.Static | BindingFlags.Public);

            for (int i = 0; i < args.Length; i++)
            {
                string arg = args[i];

                while (true)
                {
                    if (i + 1 < args.Length && !args[i + 1].Contains('/'))
                    {
                        arg += " " + args[i + 1];
                        i++;
                    }
                    else
                    {
                        MatchCollection matches = r.Matches(arg);

                        List<string> splits = new List<string>();

                        foreach (Match m in matches)
                            splits.Add(m.Value.Replace("\"", ""));

                        int argcount = splits.Count;

                        foreach (var c in cmds)
                        {
                            ConsoleCommand command = (ConsoleCommand)c.GetCustomAttribute(typeof(ConsoleCommand));
                            if (arg.Contains(command.name) && argcount - 1 == command.argcount)
                            {
                                List<object> cmdargs = new List<object>();
                                if (argcount > 1)
                                    for (int ii = 1; ii < argcount; ii++)
                                        cmdargs.Add(splits[ii]);
                                c.Invoke(null, cmdargs.ToArray());
                            }
                        }
                        break;
                    }
                }
            }
        
            string read = "";

            while (true)
            {
                ConsoleKeyInfo ki = Console.ReadKey(true);
                if ((ki.Key == ConsoleKey.V) && (ki.Modifiers == ConsoleModifiers.Control))
                {
                    string s = Clipboard.GetText();
                    if (!string.IsNullOrEmpty(s))
                        s = s.Replace("\r", "").Replace("\n", "");

                    Console.Write(s);
                    read += s;
                }
                else if (ki.Key == ConsoleKey.Enter)
                {
                    Console.Write("\r\n");
                    read = read.ToLower();
                    read = read.Trim();

                    if (!string.IsNullOrEmpty(read) && read[0] == '/')
                    {
                        MatchCollection matches = r.Matches(read);

                        List<string> splits = new List<string>();

                        foreach (Match m in matches)
                            splits.Add(m.Value.Replace("\"", ""));

                        int argcount = splits.Count;

                        foreach (var c in cmds)
                        {
                            ConsoleCommand command = (ConsoleCommand)c.GetCustomAttribute(typeof(ConsoleCommand));
                            if (read.Contains(command.name) && argcount - 1 == command.argcount)
                            {
                                List<object> cmdargs = new List<object>();
                                if (argcount > 1)
                                    for (int i = 1; i < argcount; i++)
                                        cmdargs.Add(splits[i]);
                                c.Invoke(null, cmdargs.ToArray());
                            }
                        }
                    }
                    else
                    {
                        var packet = PacketWriter.write<Packet_Chat>(NetEndpoint.CLIENT);
                        packet.outgoingmessage.Write(Client.ID);
                        packet.outgoingmessage.Write(read);
                        packet.send();
                    }

                    read = "";
                }
                else if (ki.Key == ConsoleKey.Backspace)
                {

                    if (read.Length > 0)
                        read = read.Remove(read.Length - 1);

                    Console.Write("\b \b");
                }
                else
                {
                    read += ki.KeyChar;
                    Console.Write(ki.KeyChar);

                    if (read == "/exit")
                    {
                        break;
                    }
                }
            }

            Server.net.Shutdown("shutting down");
            Client.net.Shutdown("shutting down");
        }

        public static void startClient()
        {
            Client.defaultConfigure();
            Client.start();
            Client.localconnect();
            Client.beginstonevox();
        }

        public static void startServer()
        {
            Server.defaultConfigure();
            Server.start();
        }
    }
}
