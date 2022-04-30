/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package edu.wright.airviewer2;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquareCircle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.testfx.framework.junit.ApplicationTest;
import static org.mockito.Mockito.*;


/**
 *
 * @author hilary
 */
public class AIRViewerControllerIT extends ApplicationTest {
    private AIRViewerModel model = Mock();

    private ImageView currentPageImageView;
    
    @FXML
    private MenuItem openMenuItem;

    @FXML
    private MenuItem saveAsMenuItem;

    @FXML
    private MenuItem closeMenuItem;

    @FXML
    private MenuItem extractTextMenuItem;

    @FXML
    private MenuItem undoMenuItem;

    @FXML
    private MenuItem redoMenuItem;

    @FXML
    private MenuItem addBoxAnnotationMenuItem;

    @FXML
    private MenuItem addEllipseAnnotationMenuItem;

    @FXML
    private MenuItem addTextAnnotationMenuItem;

    @FXML
    private MenuItem deleteAnnotationMenuItem;
    
    private List<PDAnnotation> candidate = new ArrayList<>();
    private float lowerLeftX=34;
    private float lowerLeftY=34;
    private float height=70;
    private float width=70;
    private String contents = "This is the dawn"; 
    private PDAnnotationSquareCircle aSquare;
    
    public AIRViewerControllerIT() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
        model = Mock(AIRViewerModel.class);
         PDPage page = document.getPage(0);
        PDRectangle position = new PDRectangle();
        position.setLowerLeftX(lowerLeftX);
        position.setLowerLeftY(lowerLeftY);
        position.setUpperRightX(lowerLeftX + width);
        position.setUpperRightY(lowerLeftY + height);

        aSquare = new PDAnnotationSquareCircle(
                PDAnnotationSquareCircle.SUB_TYPE_SQUARE);
        aSquare.setAnnotationName(UUID.randomUUID().toString());
        aSquare.setContents(contents);
        PDColor fillColor = new PDColor(new float[]{.8f, .8f, .8f}, PDDeviceRGB.INSTANCE);
        aSquare.setInteriorColor(fillColor);
        aSquare.setRectangle(position);
        aSquare.setContent("This is the inititial content");
        page.getAnnotations().add(aSquare);
        
    }
    
    @AfterEach
    public void tearDown() {
        model = null;
        aSquare = null;
    }

    /**
     * Test of initialize method, of class AIRViewerController.
     */
    @Test
    public void testInitialize() {
        System.out.println("initialize");
        URL url = null;
        ResourceBundle rb = null;
        AIRViewerController instance = new AIRViewerController();
        instance.initialize(url, rb);
       
    }
    
    @Test
    public void moveAnnotation(){
        model.executeDocumentCommandWithNameAndArgs("MoveAnnotation", 

                    candidate.getRectangle().getLowerLeftY()+"", 
                    90+"",90+"");
        
         PDAnnotation currentAnnotation = model
                 .getLastAnnotationOnPageAtPoint(0, lowerLeftX, lowerLeftY);
     
        // position of annotation should have changed
        assertNull(currentAnnotation);
        
    }
    
      @Test
    public void extractAnnoation(){
        PDAnnotation currentAnnotation = model.getLastAnnotationOnPageAtPoint(0, lowerLeftX, 
                             lowerLeftY);
        // confirm the contents of the annotation are still the same
        assertEquals("This is the inititial content",currentAnnotation.getContents());
    
    }
    
      @Test
    public void resizeAnnotation(){
        
       
        if(null != candidate){
         model.executeDocumentCommandWithNameAndArgs("ResizeAnnotation",
                              new String[]{Integer.toString(0), candidate.getRectangle().getLowerLeftX()+"",
                                  candidate.getRectangle().getLowerLeftY()+"", "90", "90"});

        }
        PDAnnotation currentAnnotation = model.getLastAnnotationOnPageAtPoint(0, lowerLeftX, 
                              lowerLeftY);
        // assert that the widhth or with or the annotation has changed
        assertEquals("150", currentAnnotation.getRectangle().getWidth());
    }
    
      @Test
    public void editAnnotation(){
       
     
    model.executeDocumentCommandWithNameAndArgs("DeleteSelectedAnnotation",
     new String[]{Integer.toString(pageIndex)});
    model.executeDocumentCommandWithNameAndArgs("AddTextAnnotation",
     new String[]{Integer.toString(pageIndex), lowerLeftX+"", lowerLeftY+"",width+"", height+"", "We are about to edit this text"});
                       
    PDAnnotation currentAnnotation = model.getLastAnnotationOnPageAtPoint(0, lowerLeftX, 
                             lowerLeftY);
        // confirm the contents of the annotation are still the same
    assertEquals("TWe are about to edit this text",currentAnnotation.getContents())}
}
