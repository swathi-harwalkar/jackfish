package com.exactprosystems.jf.common.report;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class Marker
{
	protected boolean isWhite = true;

	private static final String OM = "{{";
	private static final String CM = "}}";

	private static final String ALL_SYMBOLS = "(?s).*?";

	private static final String OM_REG = "\\{\\{";
	private static final String CM_REG = "\\}\\}";

	private enum Keys
	{
		A("$", true, "Use it if You want to put some part of code inside current text. Color of inserted code depends on JF color theme: dark or white.", "Simple example: &#123;&#123;&#36;int i = 0&#36;&#125;&#125;", "Simple example: {{$int i = 0$}}"),
		B("#", false, "Put some code into container. Used for highlighting big part of code.  Color of inserted code depends on JF color theme: dark or white.", "Simple example: &#123;&#123;&#35;int i = 0&#35;&#125;&#125;", "Simple example: {{#int i = 0#}}"),
		C("@", false, "Set blue color (#00bfff) for determined text.", "&#123;&#123;&#64;Blue&#64;&#125;&#125; word", "{{@Blue@}} word"),
		D("^", true, "Rotate determined text by 90 degrees.", "&#123;&#123;&#94;rotate&#94;&#125;&#125;", "{{^rotate^}}"),
		E("`", false, "Put text in new paragraph.", "Old paragraph. &#123;&#123;&#96;New paragraph&#96;&#125;&#125;", "Old paragraph. {{`New paragraph`}}"),
		F("_", false, "Underline some part of text.", "&#123;&#123;&#95;Underlined&#95;&#125;&#125; word", "{{_Underlined_}} word"),
		G("*", true, "Highlight some part of text as bolder.", "&#123;&#123;&#42;Bolder&#42;&#125;&#125; word", "{{*Bolder*}} word"),
		H("/", false, "Highlight some part of text as italic.", "&#123;&#123;&#47;Italic&#47;&#125;&#125; word", "{{/Italic/}} word"),
		I("&", false, "Put text in new paragraph.", "Old paragraph. &#123;&#123;&#38;New paragraph&#38;&#125;&#125;", "Old paragraph. {{`New paragraph`}}"),
		M("1", false, "Set current text as name of caption first level.", "&#123;&#123;1Caption1&#125;&#125;", "{{1Caption1}}"),
		N("2", false, "Set current text as name of caption second level.", "&#123;&#123;2Caption2&#125;&#125;", "{{2Caption2}}"),
		O("3", false, "Set current text as name of caption third level.", "&#123;&#123;3Caption3&#125;&#125;", "{{3Caption3}}"),
		P("4", false, "Set current text as name of caption fourth level.", "&#123;&#123;4Caption4&#125;&#125;", "{{4Caption4}}"),
		Q("5", false, "Set current text as name of caption fifth level.", "&#123;&#123;5Caption5&#125;&#125;", "{{5Caption5}}");
		String  key;
		boolean needEscape;
		String description;
		String example;
		String exampleResult;

		Keys(String key, boolean needEscape, String description, String example, String exampleResult)
		{
			this.key = key;
			this.needEscape = needEscape;
			this.description = description;
			this.example = example;
			this.exampleResult = exampleResult;
		}

		String groupName()
		{
			return this.name();
		}

		String key()
		{
			return this.needEscape ? "\\" + this.key : this.key;
		}

		String toRegexp()
		{
			return "(?<" + this.groupName() + ">" + OM_REG + this.key() + ALL_SYMBOLS + this.key() + CM_REG + ")";
		}
	}

	private final String REG_EXP = Arrays.stream(Keys.values()).map(Keys::toRegexp).collect(Collectors.joining("|"));

	public Marker(boolean isWhiteTheme)
	{
		this.isWhite = isWhiteTheme;
	}

	public final String process(String source)
	{
		source = this.beforeProcess(source);
		Pattern pattern = Pattern.compile(REG_EXP);
		Matcher matcher = pattern.matcher(source);
		StringBuffer sb = new StringBuffer(source.length());
		while (matcher.find())
		{
			for (Keys key : Keys.values())
			{
				String name = key.name();
				String group = matcher.group(name);
				if (group != null)
				{
					String value = convertValue(group, key);
					if (value.contains(OM) && value.contains(CM))
					{
						value = process(value);
					}
					matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
					break;
				}
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	protected abstract String convertValue(String group, Keys key);

	protected String beforeProcess(String source)
	{
		return source;
	}

	public static class HTMLMaker extends Marker
	{
		private Map<Keys, Tmp> map = new HashMap<Keys, Tmp>()
		{{
			put(Keys.A, new Tmp("<font color=\"" + (HTMLMaker.super.isWhite ? "#006400" : "#ffe30f") + "\">", "</font>"));
			put(Keys.B, new Tmp("<pre style=\"padding-left: 10px; color:" + (HTMLMaker.super.isWhite ? "#006400" : "#ffe30f") + "; margin: 10px; border-left: 1px solid #CCC;\"><code>", "</code></pre>"));
			put(Keys.C, new Tmp("<a style=\"color:#00bfff\">", "</a>"));
			put(Keys.D, new Tmp("<div class=\"rotate\"><div>", "</div></div>"));
			put(Keys.E, new Tmp("<p>", "</p>"));
			put(Keys.F, new Tmp("<u>", "</u>"));
			put(Keys.G, new Tmp("<b>", "</b>"));
			put(Keys.H, new Tmp("<i>", "</i>"));
			put(Keys.I, new Tmp("<p>", "</p>"));
			put(Keys.M, new Tmp("<h1>", "</h1>"));
			put(Keys.N, new Tmp("<h2>", "</h2>"));
			put(Keys.O, new Tmp("<h3>", "</h3>"));
			put(Keys.P, new Tmp("<h4>", "</h4>"));
			put(Keys.Q, new Tmp("<h5>", "</h5>"));
		}};

		public HTMLMaker(boolean isWhiteTheme)
		{
			super(isWhiteTheme);
		}

		@Override
		protected String convertValue(String group, Keys key)
		{
			Tmp value = this.map.get(key);
			if (value != null)
			{
				return group.replace(OM + key.key, value.begin).replace(key.key + CM, value.end);
			}
			return group;
		}

		public List<String[]> keysDescriptions()
		{
			List<String[]> result = new ArrayList<>();

			for (Keys k: Keys.values())
			{
				result.add(new String[] {k.key, k.description, k.example, k.exampleResult});
			}

			return result;
		}
	}

	/*
	//TODO think about it
	public static class TexMarker extends Marker
	{
		private Map<Keys, Tmp> map = new HashMap<Keys, Tmp>()
		{{
			put(Keys.A, new Tmp("{\\\\color{codecolor} \\\\verb+", "+}"));
			put(Keys.B, new Tmp("\\\\begingroup\n" + "    \\\\fontsize{12pt}{10pt}\\\\selectfont\\\\color{codecolor}\n" + "    \\\\begin{verbatim}  ", "  \\\\end{verbatim}\n" + "\\\\endgroup"));
			put(Keys.C, new Tmp("\\\\superhyperlink{", "}"));
			put(Keys.D, new Tmp("\\\\rotatebox{90}{", "}"));
			put(Keys.E, new Tmp("", "\\\\newline{}"));
			put(Keys.F, new Tmp("\\\\underline{", "}"));
			put(Keys.G, new Tmp("\\\\textbf{", "}"));
			put(Keys.H, new Tmp("\\\\textit{", "}"));
			put(Keys.I, new Tmp("", "\\\\newpage \\\\pagestyle{allpages}"));
			put(Keys.M, new Tmp("\\\\crule[ExactColor]{\\\\textwidth}{3pt} \\\\newline \\\\section{ ", "}"));
			put(Keys.N, new Tmp("\\\\subsection{ ", "}"));
			put(Keys.O, new Tmp("\\\\subsubsection{ ", "}"));
			put(Keys.P, new Tmp("\\\\normalsize{", "}"));
			put(Keys.Q, new Tmp("\\\\large{", "}"));
		}};

		public TexMarker()
		{
			super(true);
		}

		@Override
		protected String convertValue(String group, Keys key)
		{
			Tmp value = this.map.get(key);
			if (value != null)
			{
				return group.replace(OM + key.key, value.begin).replace(key.key + CM, value.end);
			}
			return group;
		}

		@Override
		protected String beforeProcess(String source)
		{
			Pattern patt = Pattern.compile("([^\\\\]_)");
			Matcher m = patt.matcher(source);
			StringBuffer sb = new StringBuffer(source.length());
			while (m.find())
			{
				String text = m.group(1);
				text = text.replace("_", "\\\\_");
				m.appendReplacement(sb, text);
			}
			m.appendTail(sb);
			return sb.toString();
		}
	}
	*/

	private class Tmp
	{
		String begin;
		String end;

		public Tmp(String begin, String end)
		{
			this.begin = begin;
			this.end = end;
		}
	}
}
