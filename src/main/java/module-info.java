module fr.karabodjan.jarvis {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires java.net.http;


    opens fr.karabodjan.jarvis to javafx.fxml;
    exports fr.karabodjan.jarvis;
    exports fr.karabodjan.jarvis.view;
    opens fr.karabodjan.jarvis.view to javafx.fxml;
    opens fr.karabodjan.jarvis.model to com.fasterxml.jackson.databind;
    opens fr.karabodjan.jarvis.util to com.fasterxml.jackson.databind;
}