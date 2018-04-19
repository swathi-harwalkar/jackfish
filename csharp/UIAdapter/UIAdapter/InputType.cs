﻿/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace WindowsInput
{
    /// <summary>
    /// Specifies the type of the input event. This member can be one of the following values. 
    /// </summary>
    public enum InputType : uint // UInt32
    {
        /// <summary>
        /// INPUT_MOUSE = 0x00 (The event is a mouse event. Use the mi structure of the union.)
        /// </summary>
        MOUSE = 0,

        /// <summary>
        /// INPUT_KEYBOARD = 0x01 (The event is a keyboard event. Use the ki structure of the union.)
        /// </summary>
        KEYBOARD = 1,

        /// <summary>
        /// INPUT_HARDWARE = 0x02 (Windows 95/98/Me: The event is from input hardware other than a keyboard or mouse. Use the hi structure of the union.)
        /// </summary>
        HARDWARE = 2,
    }
}
