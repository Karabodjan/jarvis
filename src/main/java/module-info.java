module fr.karabodjan.jarvis {
    requires javafx.controls;
    requires javafx.fxml;


    opens fr.karabodjan.jarvis to javafx.fxml;
    exports fr.karabodjan.jarvis;
    exports fr.karabodjan.jarvis.view;
    opens fr.karabodjan.jarvis.view to javafx.fxml;
}