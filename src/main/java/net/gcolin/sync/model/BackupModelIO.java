package net.gcolin.sync.model;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class BackupModelIO {
    
    private File getFile(){
        File f = new File("data.xml");
        if(System.getProperty("os.name").toLowerCase().contains("windows"))
        {
            f = new File(System.getProperty("user.home"),"AppData/Local/Sync folders/app/data.xml");
        }
        return f;
    }

    public BackupModel get(){
        File f = getFile();
        
        if(!f.exists())
        {
           return new BackupModel();
        }
        try {
            return (BackupModel) JAXBContext.newInstance(BackupModel.class).createUnmarshaller().unmarshal(f);
        } catch (JAXBException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,e.getMessage(),e);
            return new BackupModel();
        }
    }
    
    public void set(BackupModel model){
        File f = getFile();
        f.getParentFile().mkdirs();
        try {
            JAXBContext.newInstance(BackupModel.class).createMarshaller().marshal(model,f);
        } catch (JAXBException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,e.getMessage(),e);
        }
    }
    
}
