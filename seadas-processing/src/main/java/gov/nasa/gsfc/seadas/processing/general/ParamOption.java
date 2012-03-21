package gov.nasa.gsfc.seadas.processing.general;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 3/12/12
 * Time: 4:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParamOption {
    private String optionName;
    private String optionDescription;
    private OptionType optionType;
    private String optionDefaultValue;
    //private OptionValueRange optionValueRange;

    public enum OptionType{
        FILE,
        INTEGER,
        STRING,
        TEXT
    }
    public String getOptionName(){
        return optionName;
    }

    public String getOptionDescription() {
        return optionDescription;
    }

    public OptionType  getOptionType() {
        return optionType;
    }

    public String getOptionDefaultValue(){
        return optionDefaultValue;
    }



}
