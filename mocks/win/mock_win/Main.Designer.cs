﻿namespace mock_win
{
    partial class Main
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Main));
            this.Button = new System.Windows.Forms.Button();
            this.TextBox = new System.Windows.Forms.TextBox();
            this.CheckBox = new System.Windows.Forms.CheckBox();
            this.Table1 = new System.Windows.Forms.DataGridView();
            this.centralLabel = new System.Windows.Forms.Label();
            this.moveLabel = new System.Windows.Forms.Label();
            this.rb1 = new System.Windows.Forms.RadioButton();
            this.rb2 = new System.Windows.Forms.RadioButton();
            this.panel1 = new System.Windows.Forms.Panel();
            this.rb4 = new System.Windows.Forms.RadioButton();
            this.rb3 = new System.Windows.Forms.RadioButton();
            this.ComboBox = new System.Windows.Forms.ComboBox();
            this.Slider = new System.Windows.Forms.TrackBar();
            this.Tree = new System.Windows.Forms.TreeView();
            this.TabPanel = new System.Windows.Forms.TabControl();
            this.tabPage1 = new System.Windows.Forms.TabPage();
            this.label3 = new System.Windows.Forms.Label();
            this.tabPage2 = new System.Windows.Forms.TabPage();
            this.label4 = new System.Windows.Forms.Label();
            this.ListView = new System.Windows.Forms.ListBox();
            this.Table = new System.Windows.Forms.ListView();
            this.header1 = ((System.Windows.Forms.ColumnHeader)(new System.Windows.Forms.ColumnHeader()));
            this.header2 = ((System.Windows.Forms.ColumnHeader)(new System.Windows.Forms.ColumnHeader()));
            this.header3 = ((System.Windows.Forms.ColumnHeader)(new System.Windows.Forms.ColumnHeader()));
            this.ProgressBar = new System.Windows.Forms.ProgressBar();
            this.Panel = new System.Windows.Forms.Panel();
            this.label5 = new System.Windows.Forms.Label();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.menuStrip1 = new System.Windows.Forms.MenuStrip();
            this.menuToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.optionsToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.oneToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.twoToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.threeToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.fourToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.fiveToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.sixToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator1 = new System.Windows.Forms.ToolStripSeparator();
            this.saveToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.openToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator2 = new System.Windows.Forms.ToolStripSeparator();
            this.exitToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.aboutToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.HScrollBar = new System.Windows.Forms.HScrollBar();
            this.VScrollBar = new System.Windows.Forms.VScrollBar();
            this.Splitter = new System.Windows.Forms.SplitContainer();
            this.label6 = new System.Windows.Forms.Label();
            this.label7 = new System.Windows.Forms.Label();
            this.Any = new System.Windows.Forms.Button();
            this.toolTip1 = new System.Windows.Forms.ToolTip(this.components);
            this.ToggleButton = new System.Windows.Forms.CheckBox();
            this.saveFileDialog1 = new System.Windows.Forms.SaveFileDialog();
            this.name = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.pid = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.check = new System.Windows.Forms.DataGridViewCheckBoxColumn();
            this.dateColumn = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.numberColumn = new System.Windows.Forms.DataGridViewTextBoxColumn();
            ((System.ComponentModel.ISupportInitialize)(this.Table1)).BeginInit();
            this.panel1.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.Slider)).BeginInit();
            this.TabPanel.SuspendLayout();
            this.tabPage1.SuspendLayout();
            this.tabPage2.SuspendLayout();
            this.Panel.SuspendLayout();
            this.menuStrip1.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.Splitter)).BeginInit();
            this.Splitter.Panel1.SuspendLayout();
            this.Splitter.Panel2.SuspendLayout();
            this.Splitter.SuspendLayout();
            this.SuspendLayout();
            // 
            // Button
            // 
            resources.ApplyResources(this.Button, "Button");
            this.Button.Name = "Button";
            this.Button.UseVisualStyleBackColor = true;
            this.Button.KeyDown += new System.Windows.Forms.KeyEventHandler(this.CommonKeyDown);
            this.Button.MouseDown += new System.Windows.Forms.MouseEventHandler(this.CommonMouseDown);
            this.Button.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // TextBox
            // 
            resources.ApplyResources(this.TextBox, "TextBox");
            this.TextBox.Name = "TextBox";
            this.TextBox.TextChanged += new System.EventHandler(this.TextBox_TextChanged);
            this.TextBox.KeyDown += new System.Windows.Forms.KeyEventHandler(this.CommonKeyDown);
            this.TextBox.MouseDown += new System.Windows.Forms.MouseEventHandler(this.CommonMouseDown);
            this.TextBox.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // CheckBox
            // 
            resources.ApplyResources(this.CheckBox, "CheckBox");
            this.CheckBox.Name = "CheckBox";
            this.CheckBox.UseVisualStyleBackColor = true;
            this.CheckBox.CheckedChanged += new System.EventHandler(this.CheckBox_CheckedChanged);
            this.CheckBox.KeyDown += new System.Windows.Forms.KeyEventHandler(this.CommonKeyDown);
            this.CheckBox.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            this.CheckBox.MouseUp += new System.Windows.Forms.MouseEventHandler(this.CommonMouseDown);
            // 
            // Table1
            // 
            this.Table1.AllowUserToAddRows = false;
            this.Table1.AllowUserToDeleteRows = false;
            this.Table1.AllowUserToOrderColumns = true;
            this.Table1.ColumnHeadersBorderStyle = System.Windows.Forms.DataGridViewHeaderBorderStyle.Single;
            this.Table1.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.Table1.Columns.AddRange(new System.Windows.Forms.DataGridViewColumn[] {
            this.name,
            this.pid,
            this.check,
            this.dateColumn,
            this.numberColumn});
            resources.ApplyResources(this.Table1, "Table1");
            this.Table1.Name = "Table1";
            this.Table1.RowHeadersBorderStyle = System.Windows.Forms.DataGridViewHeaderBorderStyle.Single;
            this.Table1.MouseDown += new System.Windows.Forms.MouseEventHandler(this.CommonMouseDown);
            this.Table1.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // centralLabel
            // 
            resources.ApplyResources(this.centralLabel, "centralLabel");
            this.centralLabel.Name = "centralLabel";
            this.centralLabel.MouseDown += new System.Windows.Forms.MouseEventHandler(this.CommonMouseDown);
            this.centralLabel.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // moveLabel
            // 
            resources.ApplyResources(this.moveLabel, "moveLabel");
            this.moveLabel.Name = "moveLabel";
            this.moveLabel.MouseDown += new System.Windows.Forms.MouseEventHandler(this.CommonMouseDown);
            this.moveLabel.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // rb1
            // 
            resources.ApplyResources(this.rb1, "rb1");
            this.rb1.Name = "rb1";
            this.rb1.TabStop = true;
            this.rb1.UseVisualStyleBackColor = true;
            this.rb1.KeyDown += new System.Windows.Forms.KeyEventHandler(this.CommonKeyDown);
            this.rb1.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // rb2
            // 
            resources.ApplyResources(this.rb2, "rb2");
            this.rb2.Name = "rb2";
            this.rb2.TabStop = true;
            this.rb2.UseVisualStyleBackColor = true;
            this.rb2.KeyDown += new System.Windows.Forms.KeyEventHandler(this.CommonKeyDown);
            this.rb2.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // panel1
            // 
            this.panel1.Controls.Add(this.rb4);
            this.panel1.Controls.Add(this.rb2);
            this.panel1.Controls.Add(this.rb3);
            this.panel1.Controls.Add(this.rb1);
            resources.ApplyResources(this.panel1, "panel1");
            this.panel1.Name = "panel1";
            this.panel1.MouseDown += new System.Windows.Forms.MouseEventHandler(this.CommonMouseDown);
            this.panel1.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // rb4
            // 
            resources.ApplyResources(this.rb4, "rb4");
            this.rb4.Name = "rb4";
            this.rb4.TabStop = true;
            this.rb4.UseVisualStyleBackColor = true;
            this.rb4.KeyDown += new System.Windows.Forms.KeyEventHandler(this.CommonKeyDown);
            this.rb4.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // rb3
            // 
            resources.ApplyResources(this.rb3, "rb3");
            this.rb3.Name = "rb3";
            this.rb3.TabStop = true;
            this.rb3.UseVisualStyleBackColor = true;
            this.rb3.KeyDown += new System.Windows.Forms.KeyEventHandler(this.CommonKeyDown);
            this.rb3.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // ComboBox
            // 
            this.ComboBox.FormattingEnabled = true;
            this.ComboBox.Items.AddRange(new object[] {
            resources.GetString("ComboBox.Items"),
            resources.GetString("ComboBox.Items1"),
            resources.GetString("ComboBox.Items2"),
            resources.GetString("ComboBox.Items3")});
            resources.ApplyResources(this.ComboBox, "ComboBox");
            this.ComboBox.Name = "ComboBox";
            this.ComboBox.SelectedValueChanged += new System.EventHandler(this.ComboBox_SelectedValueChanged);
            this.ComboBox.KeyDown += new System.Windows.Forms.KeyEventHandler(this.CommonKeyDown);
            this.ComboBox.MouseDown += new System.Windows.Forms.MouseEventHandler(this.CommonMouseDown);
            this.ComboBox.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // Slider
            // 
            resources.ApplyResources(this.Slider, "Slider");
            this.Slider.Name = "Slider";
            this.Slider.KeyDown += new System.Windows.Forms.KeyEventHandler(this.CommonKeyDown);
            this.Slider.MouseDown += new System.Windows.Forms.MouseEventHandler(this.CommonMouseDown);
            this.Slider.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // Tree
            // 
            resources.ApplyResources(this.Tree, "Tree");
            this.Tree.Name = "Tree";
            this.Tree.Nodes.AddRange(new System.Windows.Forms.TreeNode[] {
            ((System.Windows.Forms.TreeNode)(resources.GetObject("Tree.Nodes")))});
            this.Tree.KeyDown += new System.Windows.Forms.KeyEventHandler(this.CommonKeyDown);
            this.Tree.MouseDown += new System.Windows.Forms.MouseEventHandler(this.CommonMouseDown);
            this.Tree.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // TabPanel
            // 
            this.TabPanel.Controls.Add(this.tabPage1);
            this.TabPanel.Controls.Add(this.tabPage2);
            resources.ApplyResources(this.TabPanel, "TabPanel");
            this.TabPanel.Name = "TabPanel";
            this.TabPanel.SelectedIndex = 0;
            this.TabPanel.KeyDown += new System.Windows.Forms.KeyEventHandler(this.CommonKeyDown);
            this.TabPanel.MouseDown += new System.Windows.Forms.MouseEventHandler(this.CommonMouseDown);
            this.TabPanel.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // tabPage1
            // 
            this.tabPage1.Controls.Add(this.label3);
            resources.ApplyResources(this.tabPage1, "tabPage1");
            this.tabPage1.Name = "tabPage1";
            this.tabPage1.UseVisualStyleBackColor = true;
            this.tabPage1.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // label3
            // 
            resources.ApplyResources(this.label3, "label3");
            this.label3.Name = "label3";
            // 
            // tabPage2
            // 
            this.tabPage2.Controls.Add(this.label4);
            resources.ApplyResources(this.tabPage2, "tabPage2");
            this.tabPage2.Name = "tabPage2";
            this.tabPage2.UseVisualStyleBackColor = true;
            // 
            // label4
            // 
            resources.ApplyResources(this.label4, "label4");
            this.label4.Name = "label4";
            // 
            // ListView
            // 
            this.ListView.FormattingEnabled = true;
            this.ListView.Items.AddRange(new object[] {
            resources.GetString("ListView.Items"),
            resources.GetString("ListView.Items1"),
            resources.GetString("ListView.Items2"),
            resources.GetString("ListView.Items3"),
            resources.GetString("ListView.Items4")});
            resources.ApplyResources(this.ListView, "ListView");
            this.ListView.Name = "ListView";
            this.ListView.KeyDown += new System.Windows.Forms.KeyEventHandler(this.CommonKeyDown);
            this.ListView.MouseDown += new System.Windows.Forms.MouseEventHandler(this.CommonMouseDown);
            this.ListView.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // Table
            // 
            this.Table.Activation = System.Windows.Forms.ItemActivation.OneClick;
            this.Table.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
            this.header1,
            this.header2,
            this.header3});
            this.Table.FullRowSelect = true;
            this.Table.GridLines = true;
            this.Table.HoverSelection = true;
            resources.ApplyResources(this.Table, "Table");
            this.Table.Name = "Table";
            this.Table.UseCompatibleStateImageBehavior = false;
            this.Table.View = System.Windows.Forms.View.Details;
            this.Table.KeyDown += new System.Windows.Forms.KeyEventHandler(this.CommonKeyDown);
            this.Table.MouseDown += new System.Windows.Forms.MouseEventHandler(this.CommonMouseDown);
            this.Table.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // header1
            // 
            resources.ApplyResources(this.header1, "header1");
            // 
            // header2
            // 
            resources.ApplyResources(this.header2, "header2");
            // 
            // header3
            // 
            resources.ApplyResources(this.header3, "header3");
            // 
            // ProgressBar
            // 
            resources.ApplyResources(this.ProgressBar, "ProgressBar");
            this.ProgressBar.Name = "ProgressBar";
            this.ProgressBar.Value = 30;
            this.ProgressBar.MouseDown += new System.Windows.Forms.MouseEventHandler(this.CommonMouseDown);
            this.ProgressBar.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // Panel
            // 
            this.Panel.BackColor = System.Drawing.SystemColors.ControlLight;
            this.Panel.Controls.Add(this.label5);
            resources.ApplyResources(this.Panel, "Panel");
            this.Panel.Name = "Panel";
            this.Panel.MouseDown += new System.Windows.Forms.MouseEventHandler(this.CommonMouseDown);
            this.Panel.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // label5
            // 
            resources.ApplyResources(this.label5, "label5");
            this.label5.Name = "label5";
            // 
            // groupBox1
            // 
            resources.ApplyResources(this.groupBox1, "groupBox1");
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.TabStop = false;
            // 
            // menuStrip1
            // 
            this.menuStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.menuToolStripMenuItem,
            this.aboutToolStripMenuItem});
            resources.ApplyResources(this.menuStrip1, "menuStrip1");
            this.menuStrip1.Name = "menuStrip1";
            this.menuStrip1.MouseDown += new System.Windows.Forms.MouseEventHandler(this.CommonMouseDown);
            this.menuStrip1.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // menuToolStripMenuItem
            // 
            this.menuToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.optionsToolStripMenuItem,
            this.toolStripSeparator1,
            this.saveToolStripMenuItem,
            this.openToolStripMenuItem,
            this.toolStripSeparator2,
            this.exitToolStripMenuItem});
            this.menuToolStripMenuItem.Name = "menuToolStripMenuItem";
            resources.ApplyResources(this.menuToolStripMenuItem, "menuToolStripMenuItem");
            // 
            // optionsToolStripMenuItem
            // 
            this.optionsToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.oneToolStripMenuItem,
            this.twoToolStripMenuItem,
            this.threeToolStripMenuItem});
            this.optionsToolStripMenuItem.Name = "optionsToolStripMenuItem";
            resources.ApplyResources(this.optionsToolStripMenuItem, "optionsToolStripMenuItem");
            // 
            // oneToolStripMenuItem
            // 
            this.oneToolStripMenuItem.Name = "oneToolStripMenuItem";
            resources.ApplyResources(this.oneToolStripMenuItem, "oneToolStripMenuItem");
            // 
            // twoToolStripMenuItem
            // 
            this.twoToolStripMenuItem.Name = "twoToolStripMenuItem";
            resources.ApplyResources(this.twoToolStripMenuItem, "twoToolStripMenuItem");
            // 
            // threeToolStripMenuItem
            // 
            this.threeToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.fourToolStripMenuItem,
            this.fiveToolStripMenuItem,
            this.sixToolStripMenuItem});
            this.threeToolStripMenuItem.Name = "threeToolStripMenuItem";
            resources.ApplyResources(this.threeToolStripMenuItem, "threeToolStripMenuItem");
            // 
            // fourToolStripMenuItem
            // 
            this.fourToolStripMenuItem.Name = "fourToolStripMenuItem";
            resources.ApplyResources(this.fourToolStripMenuItem, "fourToolStripMenuItem");
            // 
            // fiveToolStripMenuItem
            // 
            this.fiveToolStripMenuItem.Name = "fiveToolStripMenuItem";
            resources.ApplyResources(this.fiveToolStripMenuItem, "fiveToolStripMenuItem");
            // 
            // sixToolStripMenuItem
            // 
            this.sixToolStripMenuItem.Name = "sixToolStripMenuItem";
            resources.ApplyResources(this.sixToolStripMenuItem, "sixToolStripMenuItem");
            // 
            // toolStripSeparator1
            // 
            this.toolStripSeparator1.Name = "toolStripSeparator1";
            resources.ApplyResources(this.toolStripSeparator1, "toolStripSeparator1");
            // 
            // saveToolStripMenuItem
            // 
            this.saveToolStripMenuItem.Name = "saveToolStripMenuItem";
            resources.ApplyResources(this.saveToolStripMenuItem, "saveToolStripMenuItem");
            this.saveToolStripMenuItem.Click += new System.EventHandler(this.saveToolStripMenuItem_Click);
            // 
            // openToolStripMenuItem
            // 
            this.openToolStripMenuItem.Name = "openToolStripMenuItem";
            resources.ApplyResources(this.openToolStripMenuItem, "openToolStripMenuItem");
            // 
            // toolStripSeparator2
            // 
            this.toolStripSeparator2.Name = "toolStripSeparator2";
            resources.ApplyResources(this.toolStripSeparator2, "toolStripSeparator2");
            // 
            // exitToolStripMenuItem
            // 
            this.exitToolStripMenuItem.Name = "exitToolStripMenuItem";
            resources.ApplyResources(this.exitToolStripMenuItem, "exitToolStripMenuItem");
            // 
            // aboutToolStripMenuItem
            // 
            this.aboutToolStripMenuItem.Name = "aboutToolStripMenuItem";
            resources.ApplyResources(this.aboutToolStripMenuItem, "aboutToolStripMenuItem");
            // 
            // HScrollBar
            // 
            resources.ApplyResources(this.HScrollBar, "HScrollBar");
            this.HScrollBar.Name = "HScrollBar";
            this.HScrollBar.KeyDown += new System.Windows.Forms.KeyEventHandler(this.CommonKeyDown);
            // 
            // VScrollBar
            // 
            resources.ApplyResources(this.VScrollBar, "VScrollBar");
            this.VScrollBar.Name = "VScrollBar";
            // 
            // Splitter
            // 
            resources.ApplyResources(this.Splitter, "Splitter");
            this.Splitter.Name = "Splitter";
            // 
            // Splitter.Panel1
            // 
            this.Splitter.Panel1.BackColor = System.Drawing.SystemColors.ControlLightLight;
            this.Splitter.Panel1.Controls.Add(this.label6);
            this.Splitter.Panel1.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // Splitter.Panel2
            // 
            this.Splitter.Panel2.BackColor = System.Drawing.SystemColors.ButtonShadow;
            this.Splitter.Panel2.Controls.Add(this.label7);
            this.Splitter.Panel2.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            this.Splitter.MouseDown += new System.Windows.Forms.MouseEventHandler(this.CommonMouseDown);
            this.Splitter.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // label6
            // 
            resources.ApplyResources(this.label6, "label6");
            this.label6.Name = "label6";
            // 
            // label7
            // 
            resources.ApplyResources(this.label7, "label7");
            this.label7.Name = "label7";
            // 
            // Any
            // 
            resources.ApplyResources(this.Any, "Any");
            this.Any.Name = "Any";
            this.Any.UseVisualStyleBackColor = true;
            this.Any.KeyDown += new System.Windows.Forms.KeyEventHandler(this.CommonKeyDown);
            this.Any.MouseDown += new System.Windows.Forms.MouseEventHandler(this.CommonMouseDown);
            this.Any.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            // 
            // ToggleButton
            // 
            resources.ApplyResources(this.ToggleButton, "ToggleButton");
            this.ToggleButton.Name = "ToggleButton";
            this.ToggleButton.UseVisualStyleBackColor = true;
            this.ToggleButton.KeyDown += new System.Windows.Forms.KeyEventHandler(this.CommonKeyDown);
            this.ToggleButton.MouseMove += new System.Windows.Forms.MouseEventHandler(this.CommonMouseMove);
            this.ToggleButton.MouseUp += new System.Windows.Forms.MouseEventHandler(this.CommonMouseDown);
            // 
            // name
            // 
            resources.ApplyResources(this.name, "name");
            this.name.Name = "name";
            // 
            // pid
            // 
            resources.ApplyResources(this.pid, "pid");
            this.pid.Name = "pid";
            // 
            // check
            // 
            resources.ApplyResources(this.check, "check");
            this.check.Name = "check";
            // 
            // dateColumn
            // 
            resources.ApplyResources(this.dateColumn, "dateColumn");
            this.dateColumn.Name = "dateColumn";
            this.dateColumn.ReadOnly = true;
            // 
            // numberColumn
            // 
            resources.ApplyResources(this.numberColumn, "numberColumn");
            this.numberColumn.Name = "numberColumn";
            this.numberColumn.ReadOnly = true;
            // 
            // Main
            // 
            resources.ApplyResources(this, "$this");
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Controls.Add(this.ToggleButton);
            this.Controls.Add(this.Any);
            this.Controls.Add(this.Splitter);
            this.Controls.Add(this.VScrollBar);
            this.Controls.Add(this.HScrollBar);
            this.Controls.Add(this.groupBox1);
            this.Controls.Add(this.Panel);
            this.Controls.Add(this.ProgressBar);
            this.Controls.Add(this.Table);
            this.Controls.Add(this.ListView);
            this.Controls.Add(this.TabPanel);
            this.Controls.Add(this.Tree);
            this.Controls.Add(this.Slider);
            this.Controls.Add(this.ComboBox);
            this.Controls.Add(this.panel1);
            this.Controls.Add(this.moveLabel);
            this.Controls.Add(this.centralLabel);
            this.Controls.Add(this.Table1);
            this.Controls.Add(this.CheckBox);
            this.Controls.Add(this.TextBox);
            this.Controls.Add(this.Button);
            this.Controls.Add(this.menuStrip1);
            this.MainMenuStrip = this.menuStrip1;
            this.Name = "Main";
            this.toolTip1.SetToolTip(this, resources.GetString("$this.ToolTip"));
            ((System.ComponentModel.ISupportInitialize)(this.Table1)).EndInit();
            this.panel1.ResumeLayout(false);
            this.panel1.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.Slider)).EndInit();
            this.TabPanel.ResumeLayout(false);
            this.tabPage1.ResumeLayout(false);
            this.tabPage1.PerformLayout();
            this.tabPage2.ResumeLayout(false);
            this.tabPage2.PerformLayout();
            this.Panel.ResumeLayout(false);
            this.Panel.PerformLayout();
            this.menuStrip1.ResumeLayout(false);
            this.menuStrip1.PerformLayout();
            this.Splitter.Panel1.ResumeLayout(false);
            this.Splitter.Panel1.PerformLayout();
            this.Splitter.Panel2.ResumeLayout(false);
            this.Splitter.Panel2.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.Splitter)).EndInit();
            this.Splitter.ResumeLayout(false);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        public System.Windows.Forms.Button Button;
        private System.Windows.Forms.TextBox TextBox;
        private System.Windows.Forms.CheckBox CheckBox;
        private System.Windows.Forms.DataGridView Table1;
        public System.Windows.Forms.Label centralLabel;
        public System.Windows.Forms.Label moveLabel;
        private System.Windows.Forms.RadioButton rb1;
        private System.Windows.Forms.RadioButton rb2;
        private System.Windows.Forms.Panel panel1;
        private System.Windows.Forms.RadioButton rb4;
        private System.Windows.Forms.RadioButton rb3;
        private System.Windows.Forms.ComboBox ComboBox;
        private System.Windows.Forms.TrackBar Slider;
        private System.Windows.Forms.TreeView Tree;
        private System.Windows.Forms.TabControl TabPanel;
        private System.Windows.Forms.TabPage tabPage1;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.TabPage tabPage2;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.ListBox ListView;
        private System.Windows.Forms.ListView Table;
        private System.Windows.Forms.ColumnHeader header1;
        private System.Windows.Forms.ColumnHeader header2;
        private System.Windows.Forms.ColumnHeader header3;
        private System.Windows.Forms.ProgressBar ProgressBar;
        private System.Windows.Forms.Panel Panel;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.MenuStrip menuStrip1;
        private System.Windows.Forms.ToolStripMenuItem menuToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem optionsToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator1;
        private System.Windows.Forms.ToolStripMenuItem oneToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem twoToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem threeToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem saveToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem openToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator2;
        private System.Windows.Forms.ToolStripMenuItem exitToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem aboutToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem fourToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem fiveToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem sixToolStripMenuItem;
        private System.Windows.Forms.HScrollBar HScrollBar;
        private System.Windows.Forms.VScrollBar VScrollBar;
        private System.Windows.Forms.SplitContainer Splitter;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.Label label7;
        private System.Windows.Forms.Button Any;
        private System.Windows.Forms.ToolTip toolTip1;
        private System.Windows.Forms.CheckBox ToggleButton;
        private System.Windows.Forms.SaveFileDialog saveFileDialog1;
        private System.Windows.Forms.DataGridViewTextBoxColumn name;
        private System.Windows.Forms.DataGridViewTextBoxColumn pid;
        private System.Windows.Forms.DataGridViewCheckBoxColumn check;
        private System.Windows.Forms.DataGridViewTextBoxColumn dateColumn;
        private System.Windows.Forms.DataGridViewTextBoxColumn numberColumn;
    }
}
