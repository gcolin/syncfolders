package net.gcolin.sync.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle.Control;

public class HtmlControl extends Control{

    private final static List<String> FORMAT
            = Collections.unmodifiableList(Arrays.asList("java.html"));
    
    public HtmlControl(){
       
    }
    
    @Override
    public List<String> getFormats(String baseName) {
        if (baseName == null) {
            throw new NullPointerException();
        }
        return FORMAT;
    }
}
