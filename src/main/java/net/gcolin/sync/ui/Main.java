package net.gcolin.sync.ui;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle.Control;
import java.util.function.Consumer;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    BorderPane outerRoot;
    MainPanel mainPanel = new MainPanel();
    SettingPanel settingPanel = new SettingPanel();
    static Stage stage;
    
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle(Messages.getString("Main.title")); //$NON-NLS-1$
        
        Consumer<Boolean> listener = (Boolean valid)->{
            mainPanel.getAction().setDisable(!valid);
            mainPanel.getDoSettings().setVisible(!valid);
            mainPanel.getGoToSettings().setVisible(!valid);
        };
        
        mainPanel.getGoToSettings().setOnAction((ActionEvent e)->outerRoot.setCenter(settingPanel));
        
        settingPanel.setOnChange(listener);
        
        stage.getIcons().add(new Image(this.getClass().getClassLoader().getResourceAsStream("logo.png"))); //$NON-NLS-1$
        Main.stage = stage;
        
        // build Menu Bar
        outerRoot = new BorderPane();
        outerRoot.setTop(buildMenuBar());
        outerRoot.setCenter(mainPanel);
        stage.setWidth(600.0);
        stage.setHeight(400.0);
        Scene scene = new Scene(outerRoot, 600, 400);
        stage.setScene(scene);
        stage.setOnHidden((WindowEvent e)->System.exit(0));
        stage.show();  
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    private MenuBar buildMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);
        Menu fileItem = new Menu(Messages.getString("Main.filemenu")); //$NON-NLS-1$
        MenuItem backup = new MenuItem(Messages.getString("Main.new")); //$NON-NLS-1$
        backup.setOnAction((ActionEvent e)->outerRoot.setCenter(mainPanel));
        fileItem.getItems().add(backup);
        MenuItem settings = new MenuItem(Messages.getString("Main.settings")); //$NON-NLS-1$
        settings.setOnAction((ActionEvent e)->outerRoot.setCenter(settingPanel));
        fileItem.getItems().add(settings);
        MenuItem quit = new MenuItem(Messages.getString("Main.exit")); //$NON-NLS-1$
        fileItem.getItems().add(new SeparatorMenuItem());
        fileItem.getItems().add(quit);
        quit.setOnAction((ActionEvent e)->System.exit(0));
        menuBar.getMenus().add(fileItem);
        Menu helpItem = new Menu(Messages.getString("Main.help"));
        MenuItem contents = new MenuItem(Messages.getString("Main.helpcontent"));
        helpItem.getItems().add(contents);
        contents.setOnAction((ActionEvent e)->{
            WebView w = new WebView();
            Control c = Control.getControl(Control.FORMAT_DEFAULT);
            URL url = null;
            for(Locale loc:c.getCandidateLocales("help", Locale.getDefault()))
            {
                url = this.getClass().getClassLoader().getResource(c.toResourceName(c.toBundleName("help", loc),"html"));
                if(url!=null)
                {
                    break;
                }
            }
            w.getEngine().load(url.toString());
            outerRoot.setCenter(w);
            });
        MenuItem about = new MenuItem(Messages.getString("Main.about"));
        helpItem.getItems().add(about);
        about.setOnAction((ActionEvent e)->outerRoot.setCenter(new About()));
        menuBar.getMenus().add(helpItem);
        return menuBar;
    }

}
