module fr.karabodjan.jarvis {
    requires javafx.controls;
    requires javafx.fxml;


    opens fr.karabodjan.jarvis to javafx.fxml;
    exports fr.karabodjan.jarvis;
}