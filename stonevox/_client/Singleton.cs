﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public class Singleton<T> where T : class
    {
        private static T Instance;
        public static T INSTANCE { get { return Singleton<T>.Instance; } }

        public Singleton()
        {
            Instance = this as T;
        }
    }
}
