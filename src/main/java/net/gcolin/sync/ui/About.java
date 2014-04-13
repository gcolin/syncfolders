package net.gcolin.sync.ui;

import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.effect.Reflection;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class About extends Group{

    public About()
    {
        PerspectiveCamera camera = new PerspectiveCamera(true);
        getChildren().add(camera);
        Text t = new Text();
        t.setX(10.0f);
        t.setY(50.0f);
        t.setCache(true);
        t.setText("by Gaël Colin");
        t.setFont(Font.font(null, FontWeight.BOLD, 30));
         
        Reflection r = new Reflection();
        r.setFraction(0.7f);
         
        t.setEffect(r);

        getChildren().add(t);
    }
}
