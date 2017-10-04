package com.exactprosystems.jf.common.documentation;

import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionsList;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.documents.matrix.parser.Parser;
import com.exactprosystems.jf.documents.matrix.parser.items.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckItemsAndActions {

    public CheckItemsAndActions() {
    }

    public static void main(String[] args){
        checkItems();
        checkActions();
    }

    private static void checkItems(){
        for (Class<?> clazz : Parser.knownItems)
        {
            MatrixItemAttribute attribute = clazz.getAnnotation(MatrixItemAttribute.class);
            if (attribute == null)
            {
                continue;
            }

            if (!attribute.real() || clazz.equals(ActionItem.class) || clazz.equals(TempItem.class))
            {
                continue;
            }

            checkPartOfAttribute(attribute.description(), clazz.getSimpleName());
            checkPartOfAttribute(attribute.examples(), clazz.getSimpleName());

        }
    }

    private static void checkActions(){
        for (Class<?> clazz : ActionsList.actions)
        {
            ActionAttribute attr = clazz.getAnnotation(ActionAttribute.class);
            if (attr != null){
	            String generalDescription = attr.constantGeneralDescription() == R.DEFAULT ? attr.generalDescription() : attr.constantGeneralDescription().get();
	            checkPartOfAttribute(generalDescription, clazz.getSimpleName());

				String examples = attr.constantExamples() == R.DEFAULT ? attr.examples() : attr.constantExamples().get();
				checkPartOfAttribute(examples, clazz.getSimpleName());

				String additionalDescription = attr.constantAdditionalDescription() == R.DEFAULT ? attr.additionalDescription() : attr.constantAdditionalDescription().get();
                checkPartOfAttribute(additionalDescription, clazz.getSimpleName());

                String outputDescription = attr.constantOutputDescription() == R.DEFAULT_OUTPUT_DESCRIPTION ? attr.outputDescription() : attr.constantOutputDescription().get();
                checkPartOfAttribute(outputDescription, clazz.getSimpleName());
            }
        }
    }

    private static void checkPartOfAttribute(String string, String className){
        if (!Str.IsNullOrEmpty(string)){
            Pattern openPattern = Pattern.compile("(\\{\\{[1|2|3|4|5|$|#|@|\\^|`|_|*|/|&|=|-])");
            Pattern closePattern = Pattern.compile("([1|2|3|4|5|$|#|@|\\^|`|_|*|/|&|=|-]\\}\\})");
            Matcher open = openPattern.matcher(string);
            int openCount = 0;
            int closeCount = 0;
            while (open.find()){
                openCount +=1;
            }
            Matcher close = closePattern.matcher(string);
            while (close.find()){
                closeCount +=1;
            }
            if (openCount != closeCount){
                System.out.println(className);
            }
        }
    }
}
