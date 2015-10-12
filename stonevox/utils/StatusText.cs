using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace stonevox
{
    public static class StatusText
    {
        public const string button_target = "Click and Drag over\nMatrix to select it\nShortcut - Space";
        public const string button_eyedrop = "Click and Drag over\nanything to color pick\nShortcut - Shift";
        public const string button_gridoptions = "Voxel Outlining : $(type)\nShortcut - Shift + G\nEnable/Disable - G";

        public const string button_add = "Add Voxel Tool\nClick and Drag to fill by volume\nTab - cycle tools";
        public const string button_remove= "Remove Voxel Tool\nClick and Drag to remove by volume\nTab - cycle tools";
        public const string button_recolor= "Recolor Voxel Tool\nClick and Drag to recolor by volume\nTab - cycle tools";

        public const string picture_colorpicker_header = "Click and Drag to move color picker";
        public const string button_colorpallete = "Click to Set Active Color\nClick again to open Color Picker";

        public const string textbox_screenshot_width = "Set's .png Width";
        public const string textbox_screenshot_height = "Set's .png Height";

        public const string button_save_screenshot = "Save .png";
        public const string button_reset_screeshot_view = "Resets the Camera view";
        public const string button_screenshot_open = "Take a Screenshot";

        public const string button_undo = "Undo\nShortcut - Z";
        public const string button_redo = "Redo\nShortcut - Y";

        public const string button_backgroundcolor = "Set Background Color";
        public const string button_floorcolor = "Set Floor Color";

        public const string label_matrixlistbox = "Click - Sets Active Matrix\nDouble Click - Camera Focus\nClick then F2 - rename Matrix";
        public const string button_matrixvisibiliy = "Toggle Matrix Visibility";

        public const string button_removematrix = "Removes Currently Active Matrix";
        public const string button_addmatrix = "Add new Matrix";

        public const string button_nonactivematrixvisibilitytoggle = "Toggle All Non Active Matrix Visiblity\nShortcut - H";
    }
}
