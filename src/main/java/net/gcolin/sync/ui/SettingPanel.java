package net.gcolin.sync.ui;

import java.io.File;
import java.util.function.Consumer;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import net.gcolin.sync.model.BackupModel;
import net.gcolin.sync.model.BackupModelIO;

public class SettingPanel extends GridPane{

    private SimpleStringProperty source = new SimpleStringProperty();
    private SimpleStringProperty destination = new SimpleStringProperty();
    private Button save = new Button(Messages.getString("SettingPanel.save")); //$NON-NLS-1$
    private BackupModel model;
    private Consumer<Boolean> onChange;
    
    public SettingPanel()
    {
        setAlignment(Pos.CENTER);
        setHgap(10);
        setVgap(10);
        setPadding(new Insets(25, 25, 25, 25));
        
        Text title = new Text(Messages.getString("SettingPanel.settings")); //$NON-NLS-1$
        add(title, 0,0,3,1);
        Font f = new Font(18.0);
        title.setFont(f);
        add(new Label(Messages.getString("SettingPanel.source")),0,1); //$NON-NLS-1$
        addChooser(1, 1, source);
        add(new Label(Messages.getString("SettingPanel.destination")),0,2); //$NON-NLS-1$
        addChooser(1, 2, destination);
        
        model = new BackupModelIO().get();
        ChangeListener<String> c = (ObservableValue<? extends String> o,String old,String n)->
        {
            save.setDisable(equals(source.get(),model.getDir1())&&equals(destination.get(),model.getDir2()));
        };
        
        source.addListener(c);
        destination.addListener(c);
        
        source.set(model.getDir1());
        destination.set(model.getDir2());
        
        save.setOnAction((ActionEvent e)->{
            model.setDir1(source.get());
            model.setDir2(destination.get());
            c.changed(null, null, null);
            new BackupModelIO().set(model);
            if(onChange!=null)
            {
                onChange.accept(isModelValid());
            }
        });
        add(save, 1, 3);
    }
    
    private boolean equals(String a,String b)
    {
        if(a==null && b == null)
        {
            return true;
        }
        return a!=null && b!=null && a.equals(b);
    }
    
    private void addChooser(int col,int row, SimpleStringProperty source)
    {
        TextField text = new TextField();
        text.setMinWidth(100.0);
        text.setEditable(false);
        ProgressIndicator progressIndicator = new ProgressIndicator(getPercent(source.get()));
        
        source.addListener((ObservableValue<? extends String> o,String old,String n)->{
            text.setText(n);
            progressIndicator.setProgress(getPercent(source.get()));
        });
        Button b = new Button(Messages.getString("SettingPanel.select")); //$NON-NLS-1$
        add(b, col, row);
        add(text, col+1, row, 2, 1);
        add(progressIndicator, col+3, row);
        b.setOnAction((ActionEvent e)->{
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File f = directoryChooser.showDialog(Main.stage);
            if(f!=null)
            {
                source.set(f.getAbsolutePath());
            }else
            {
                source.set(null);
            }
        });
    }

    private double getPercent(String s) {
        return s!=null && s.length()>0 && new File(s).exists()?100:0;
    }
    
    public boolean isModelValid()
    {
        return getPercent(model.getDir1())==100.0
                && getPercent(model.getDir2())==100.0;
    }

    public void setOnChange(Consumer<Boolean> onChange) {
        this.onChange = onChange;
        onChange.accept(isModelValid());
    }
}
