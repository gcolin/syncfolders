package net.gcolin.sync.ui;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import net.gcolin.sync.BackupFiles;
import net.gcolin.sync.model.BackupModelIO;
import net.gcolin.sync.model.BackupState;

public class MainPanel extends BorderPane{
    
    Button action = new Button(Messages.getString("MainPanel.start")); //$NON-NLS-1$
    Label remaining = new Label();
    ProgressBar currentFile = new ProgressBar(0.0);
    ProgressBar total = new ProgressBar(0.0);
    Text doSettings = new Text(Messages.getString("MainPanel.invalidSettings")); //$NON-NLS-1$
    Button goToSettings = new Button(Messages.getString("MainPanel.settings")); //$NON-NLS-1$
    
    public MainPanel(){
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        final TextArea log = new TextArea();
        log.setEditable(false);
        setCenter(log);
        setBottom(grid);
        StringRoller s = new StringRoller(100);
        BackupFiles.log.addHandler(new Handler() {
            
            @Override
            public void publish(LogRecord record) {
                Platform.runLater(()->{
                    s.append(record.getMessage()+"\n");
                    log.setText(s.toString());
                });
            }
            
            @Override
            public void flush() {
            }
            
            @Override
            public void close() throws SecurityException {
            }
        });
        
        grid.setMaxWidth(Double.MAX_VALUE);
        currentFile.setMaxWidth(Double.MAX_VALUE);
        total.setMaxWidth(Double.MAX_VALUE);
        currentFile.setMinWidth(300.0);
        total.setMinWidth(300.0);
        
        grid.add(remaining, 0, 0, 3, 1);
        grid.add(currentFile, 0, 1, 3, 1);
        grid.add(total, 0, 2, 3, 1);
        grid.add(action, 0, 3);
        grid.add(doSettings, 1, 3);
        grid.add(goToSettings, 2, 3);
        
        action.setOnAction((ActionEvent e)->{
            action.setDisable(true);
            BackupFiles b = new BackupFiles();
            s.clear();
            log.setText(""); //$NON-NLS-1$
            b.addListener((ObservableValue<? extends BackupState> o,BackupState old,BackupState n)->{
                Platform.runLater(()->{
                    if(n.getState() == BackupState.STATE.END)
                    {
                        remaining.setText(n.getState().name());
                        total.setProgress(1.0);
                        currentFile.setProgress(1.0);
                        action.setDisable(false);
                    }else{
                        currentFile.setProgress(((double)n.getPercentCurrentFile())/100.0);
                        remaining.setText(getPercent(n));
                        total.setProgress(((double)n.getPercent())/100.0);  
                    }
                });
            });
            new Thread(()->b.backup(new BackupModelIO().get())).start();
        });
    }
    
    private String getPercent(BackupState state) {
        long percent = state.getPercent() == 0 ? 1 : state.getPercent();
        long d = System.currentTimeMillis() - state.getTimeInitial();
        long time = ((100 - percent) * d / percent);
        String ts = time + Messages.getString("MainPanel.ms"); //$NON-NLS-1$
        if (time > 1000 * 60 * 60) {
            ts = (time / 1000 / 60 / 60) + Messages.getString("MainPanel.hours"); //$NON-NLS-1$
        } else if (time > 1000 * 60) {
            ts = (time / 1000 / 60) + Messages.getString("MainPanel.mins"); //$NON-NLS-1$
        } else if (time > 1000) {
            ts = (time / 1000) + Messages.getString("MainPanel.secs"); //$NON-NLS-1$
        }
        return state.getState()+" : " + percent + Messages.getString("MainPanel.remaining") + ts + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public Button getAction() {
        return action;
    }

    public Text getDoSettings() {
        return doSettings;
    }

    public Button getGoToSettings() {
        return goToSettings;
    }
}
