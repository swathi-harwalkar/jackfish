﻿////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary 
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace WindowsInput
{
    /// <summary>
    /// XButton definitions for use in the MouseData property of the <see cref="MOUSEINPUT"/> structure. (See: http://msdn.microsoft.com/en-us/library/ms646273(VS.85).aspx)
    /// </summary>
    public enum XButton : uint
    {
        /// <summary>
        /// Set if the first X button is pressed or released.
        /// </summary>
        XBUTTON1 = 0x0001,

        /// <summary>
        /// Set if the second X button is pressed or released.
        /// </summary>
        XBUTTON2 = 0x0002,
    }
}
