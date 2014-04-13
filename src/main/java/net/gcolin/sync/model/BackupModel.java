package net.gcolin.sync.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="model")
public class BackupModel {

    private String dir1 = "";
    private String dir2 = "";
    public String getDir1() {
        return dir1;
    }
    public void setDir1(String dir1) {
        this.dir1 = dir1;
    }
    public String getDir2() {
        return dir2;
    }
    public void setDir2(String dir2) {
        this.dir2 = dir2;
    }
}
