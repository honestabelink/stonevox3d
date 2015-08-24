﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public class GUIAppearenceNameAttribute : Attribute
    {
        private string DisplayeName;
        public string DisplayName { get { return DisplayeName; } }

        public GUIAppearenceNameAttribute(string DisplayName)
        {
            this.DisplayeName = DisplayName;
        }
    }
}