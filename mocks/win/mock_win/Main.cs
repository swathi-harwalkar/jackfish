﻿using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace mock_win
{
    public partial class Main : Form
    {
        public Main()
        {
            InitializeComponent();
            fillTable();
            fillListView();
            ComboBox.SelectedIndex = 0;
        }

        private void fillListView()
        {
            List<string[]> list = new List<string[]>();
            list.Add(new String[] { "tr_1_td_1", "tr_1_td_2", "tr_1_td_3" });
            list.Add(new String[] { "tr_2_td_1", "tr_2_td_2", "tr_2_td_3" });
            list.Add(new String[] { "tr_3_td_1", "tr_3_td_2", "tr_3_td_3" });
            list.Add(new String[] { "Green", "", "51" });
            list.Add(new String[] { "Stark", "128 North Street", "35" });

            foreach (String[] item in list)
            {
                ListViewItem lvi = new ListViewItem(item[0]);
                lvi.SubItems.Add(item[1]);
                lvi.SubItems.Add(item[2]);
                Table.Items.Add(lvi);
            }
        }

        private void fillTable()
        {
            for (int i=0; i<5; i++)
            {
                Table1.Rows.Add();
                Table1.Rows[i].Cells["name"].Value = "name_"+i;
                Table1.Rows[i].Cells["pid"].Value = i;
                Table1.Rows[i].Cells["check"].Value = true;
            }
            
        }

        private void saveToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if (saveFileDialog1.ShowDialog() == DialogResult.Cancel) return;
        }

        private void CommonMouseMove(object sender, MouseEventArgs e)
        {
            Control control = (Control)sender;
            string text;
            switch (control.Name)
            {
                case "centralLabel":
                    text = "Label";
                    break;
                default:
                    text = control.Name;
                    break;
            }
            moveLabel.Text = text + "_move";
        }

        private void CommonMouseDown(object sender, MouseEventArgs e)
        {
            writeTextOncentralLabelMouse(writeControlNameOnCentralLabel(sender), e);
        }

        private void writeTextOncentralLabelMouse(string text, MouseEventArgs e)
        {
            if (e.Button == MouseButtons.Left)
            {
                if (e.Clicks == 1)
                {
                    centralLabel.Text = text + "_click";
                }
                else
                {
                    centralLabel.Text = text + "_double_click";
                }
            }
            else
            {
                centralLabel.Text = text + "_rightClick";
            }
        }

        private void CommonKeyDown(object sender, KeyEventArgs e)
        {
            writeTextOncentralLabelKeyboard(writeControlNameOnCentralLabel(sender), e);
        }

        private string writeControlNameOnCentralLabel(object sender)
        {
            Control control = (Control)sender;
            string text;
            switch (control.Name)
            {
                case "centralLabel":
                    text = "Label";
                    break;
                default:
                    text = control.Name;
                    break;
            }
            return text;
        }

        private void writeTextOncentralLabelKeyboard(string text, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.Escape)
            {
                centralLabel.Text = text + "_press_Escape";
            }

            if (Control.ModifierKeys == Keys.Shift)
            {
                centralLabel.Text = text + "_press_Control";
            }
        }

        private void CheckBox_CheckedChanged(object sender, EventArgs e)
        {
            if (CheckBox.Checked)
            {
                centralLabel.Text = CheckBox.Name + "_checked";
            }
            else
            {
                centralLabel.Text = CheckBox.Name + "_unchecked";
            }
        }

        private void ComboBox_SelectedValueChanged(object sender, EventArgs e)
        {
            centralLabel.Text = ComboBox.Name + "_" + ComboBox.SelectedItem;
        }

        private void TextBox_TextChanged(object sender, EventArgs e)
        {
            centralLabel.Text = TextBox.Name + "_" + TextBox.Text;
        }
    }
}