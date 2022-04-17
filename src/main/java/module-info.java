module edu.wright.airviewer2 {    
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphicsEmpty;
    requires javafx.swing;
    requires pdfbox;

    opens edu.wright.airviewer2 to javafx.fxml;
    exports edu.wright.airviewer2;
  
}
