package application;
	
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;


public class Ui extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		try {
			WebView browser = new WebView();
			WebEngine webEngine = browser.getEngine();
			webEngine.getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>() {
			    @Override
			    public void changed(ObservableValue<? extends Throwable> ov, Throwable t, Throwable t1) {
			        System.out.println("Received exception: "+t1.getMessage());
			    }
			});
			
			URL indexUrl = Ui.class.getClassLoader().getResource("index.html");
			StringBuilder str = new StringBuilder();
			BufferedReader br = new BufferedReader(
					new InputStreamReader(indexUrl.openStream(), Charset.forName("utf8")));
			String line = null;
			while((line = br.readLine())!=null) {
				str.append(line).append('\n');
			}
			String exturl = indexUrl.toExternalForm();
			String content = str.toString().replaceAll("@path@", exturl.substring(0, exturl.length()-10));
			webEngine.loadContent(content);
			JSObject jsobj = (JSObject) webEngine.executeScript("window");
			jsobj.setMember("controller", new Controller(primaryStage, webEngine));	
			Scene scene = new Scene(browser,400,400);
			primaryStage.setTitle("Sync");
			primaryStage.setScene(scene);
			primaryStage.setOnCloseRequest(e -> System.exit(0));
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
