using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public static class Native
    {
        [DllImport("gdi32.dll", EntryPoint = "AddFontResourceW", SetLastError = true)]
        static extern int AddFontResource([In][MarshalAs(UnmanagedType.LPWStr)]
                                         string lpFileName);

        public static void AddFont(string filePath)
        {
            var result = AddFontResource(filePath);
            var error = Marshal.GetLastWin32Error();
            if (error != 0)
            {
                Console.WriteLine(new Win32Exception(error).Message);
            }
            else
            {
                Console.WriteLine((result == 0) ? "Font is already installed." :
                                                  "Font installed successfully.");
            }
        }
    }
}
