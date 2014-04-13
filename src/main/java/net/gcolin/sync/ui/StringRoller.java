package net.gcolin.sync.ui;

public class StringRoller {

    private String[] allStrings;
    private int index = 0;
    
    public StringRoller(int n)
    {
        allStrings = new String[n];
    }
    
    public void append(String str)
    {
        allStrings[index] = str;
        index = (index+1)%allStrings.length;
    }
    
    public String toString(){
        StringBuilder b = new StringBuilder();
        for(int i=(index+1)%allStrings.length;i>=0;i--)
        {
            if(allStrings[i]!=null)
            {
                b.append(allStrings[i]);
            }
        }
        for(int i=allStrings.length-1,l=(index+1)%allStrings.length;i>=l;i--)
        {
            if(allStrings[i]!=null)
            {
                b.append(allStrings[i]);
            }
        }
        
        return b.toString();
    }

    public void clear() {
        for(int i=allStrings.length-1;i>=0;i--)
        {
            allStrings[i] = null;
        }
    }
}
