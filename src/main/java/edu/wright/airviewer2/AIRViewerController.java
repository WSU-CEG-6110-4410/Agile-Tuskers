/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
 /// https://github.com/WSU-CEG-6110-4410/Agile-Tuskers/issues/5
/// https://github.com/WSU-CEG-6110-4410/Agile-Tuskers/issues/8
/// https://docs.oracle.com/javafx/2/best_practices/jfxpub-best_practices.htm
package edu.wright.airviewer2;

import edu.wright.airviewer2.AIRViewer;
import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;

/**
 *
 * @author erik
 */
public class AIRViewerController implements Initializable {

    static final String DEFAULT_PATH = "sample.pdf";

    @FXML
    private Pagination pagination;

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

    private AIRViewerModel model;

    private ImageView currentPageImageView;

    private Group pageImageGroup;

      
    private float originalX, originalY; // for tracking the original position of the mouse when it was first pressed
    
    private long lastClick;

    private AIRViewerModel promptLoadModel(String startPath) {

        AIRViewerModel loadedModel = null;
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open PDF File");
            fileChooser.setInitialFileName(startPath);
            Stage stage = (Stage) pagination.getScene().getWindow();
            File file = fileChooser.showOpenDialog(stage);
            if (null != file) {
                String path = file.getCanonicalPath();
                loadedModel = new AIRViewerModel(Paths.get(path));
            }
        } catch (IOException ex) {
//            Logger.getLogger(AIRViewerController.class.getName()).log(
//                    Level.INFO,
//                    "Unable to open <" + ex.getLocalizedMessage() + ">",
//                    "");
            loadedModel = null;
        }

        return loadedModel;
    }
private void synchronizeSelectionKnobs() {
        if (null != model && null != currentPageImageView && null != pageImageGroup) {
            List<java.awt.Rectangle> selectedAreas = model.getSelectedAreas();
            ArrayList<Node> victims = new ArrayList<>(pageImageGroup.getChildren());
            
            /// This function is used to delete everything in the group that isn't currentPageImageView
            victims.stream().filter((n) -> (n != currentPageImageView)).forEach((n) -> {
                pageImageGroup.getChildren().remove(n);
            });
             int pageIndex = pagination.getCurrentPageIndex();
            
            /// This function is used to add knobs to thegroup to indicate selection 
            for (java.awt.Rectangle r : selectedAreas) {
              
                Circle knobA = new Circle(r.getX(),  (int)pageImageGroup.prefHeight(0) - r.getY(), 4);
                knobA.setStroke(Color.YELLOW);
                knobA.setStrokeWidth(2);
                knobA.setOnMouseDragged(new EventHandler<MouseEvent>(){
                    @Override
                    public void handle(MouseEvent t) {
                        System.out.print("Dragging Knobbing knob a");
                        PDAnnotation candidate = model.getLastAnnotationOnPageAtPoint(pageIndex, (float) r.getX(), (float) r.getY());
                        if(null != candidate){
                         model.executeDocumentCommandWithNameAndArgs("ResizeAnnotation",
                                              new String[]{Integer.toString(pageIndex), candidate.getRectangle().getLowerLeftX()+"",
                                                  candidate.getRectangle().getLowerLeftY()+"", "90", "90"});
                         refreshUserInterface();
                        }
                    }
                
                });

                pageImageGroup.getChildren().add(knobA);
                Circle knobB = new Circle(r.getX() + r.getWidth(), (int)pageImageGroup.prefHeight(0) - r.getY(), 4);
                knobB.setStroke(Color.YELLOW);
                knobB.setStrokeWidth(2);
                knobB.setOnMouseDragged(new EventHandler<MouseEvent>(){
                    @Override
                    public void handle(MouseEvent t) {
                      
                        System.out.println("Dragging Knobbing knob b "+r.getX()+" "+r.getY());
                        PDAnnotation candidate = model.getLastAnnotationOnPageAtPoint(pageIndex, (float) r.getX(), (float) r.getY());
                        if(null != candidate){
                         model.executeDocumentCommandWithNameAndArgs("ResizeAnnotation",
                                              new String[]{Integer.toString(pageIndex), candidate.getRectangle().getLowerLeftX()+"",
                                                  candidate.getRectangle().getLowerLeftY()+"", "90", "90"});
                         refreshUserInterface();
                        }
                    }
                
                });
                pageImageGroup.getChildren().add(knobB);
                Circle knobC = new Circle(r.getX() + r.getWidth(),  (int)pageImageGroup.prefHeight(0) - (r.getY() + r.getHeight()), 4);
                knobC.setStroke(Color.YELLOW);
                knobC.setStrokeWidth(2);
                knobC.setOnMouseDragged(new EventHandler<MouseEvent>(){
                    @Override
                    public void handle(MouseEvent t) {
                      
                        System.out.println("Dragging Knobbing knob b "+r.getX()+" "+r.getY());
                        PDAnnotation candidate = model.getLastAnnotationOnPageAtPoint(pageIndex, (float) r.getX(), (float) r.getY());
                        if(null != candidate){
                         model.executeDocumentCommandWithNameAndArgs("ResizeAnnotation",
                                              new String[]{Integer.toString(pageIndex), candidate.getRectangle().getLowerLeftX()+"",
                                                  candidate.getRectangle().getLowerLeftY()+"", "90", "90"});
                         refreshUserInterface();
                        }
                    }
                
                });
                pageImageGroup.getChildren().add(knobC);
                Circle knobD = new Circle(r.getX(),  (int)pageImageGroup.prefHeight(0) - (r.getY() + r.getHeight()), 4);
                knobD.setStroke(Color.YELLOW);
                knobD.setOnMouseDragged(new EventHandler<MouseEvent>(){
                    @Override
                    public void handle(MouseEvent t) {
                      
                        System.out.println("Dragging Knobbing knob b "+r.getX()+" "+r.getY());
                        PDAnnotation candidate = model.getLastAnnotationOnPageAtPoint(pageIndex, (float) r.getX(), (float) r.getY());
                        if(null != candidate){
                         model.executeDocumentCommandWithNameAndArgs("ResizeAnnotation",
                                              new String[]{Integer.toString(pageIndex), candidate.getRectangle().getLowerLeftX()+"",
                                                  candidate.getRectangle().getLowerLeftY()+"", "90", "90"});
                         refreshUserInterface();
                        }
                    }
                
                });
                pageImageGroup.getChildren().add(knobD);
            }
        }

    }
//****************************pre-condition programming by contract***********//
    private void refreshUserInterface() {
        assert pagination != null : "fx:id=\"pagination\" was not injected: check your FXML file 'simple.fxml'.";
        assert openMenuItem != null : "fx:id=\"openMenuItem\" was not injected: check your FXML file 'simple.fxml'.";
        assert saveAsMenuItem != null : "fx:id=\"saveAsMenuItem\" was not injected: check your FXML file 'simple.fxml'.";
        assert closeMenuItem != null : "fx:id=\"closeMenuItem\" was not injected: check your FXML file 'simple.fxml'.";

        assert extractTextMenuItem != null : "fx:id=\"extractTextMenuItem\" was not injected: check your FXML file 'simple.fxml'.";
        assert undoMenuItem != null : "fx:id=\"undoMenuItem\" was not injected: check your FXML file 'simple.fxml'.";
        assert redoMenuItem != null : "fx:id=\"redoMenuItem\" was not injected: check your FXML file 'simple.fxml'.";
        assert addBoxAnnotationMenuItem != null : "fx:id=\"addBoxAnnotationMenuItem\" was not injected: check your FXML file 'simple.fxml'.";
        assert addEllipseAnnotationMenuItem != null : "fx:id=\"addEllipseAnnotationMenuItem\" was not injected: check your FXML file 'simple.fxml'.";
        assert addTextAnnotationMenuItem != null : "fx:id=\"addTextAnnotationMenuItem\" was not injected: check your FXML file 'simple.fxml'.";
        assert deleteAnnotationMenuItem != null : "fx:id=\"deleteAnnotationMenuItem\" was not injected: check your FXML file 'simple.fxml'.";
        // *****************************post-condition programming by contract**********//

        if (null != model) {
            pagination.setPageCount(model.numPages());
            pagination.setDisable(false);
            saveAsMenuItem.setDisable(false);
            extractTextMenuItem.setDisable(false);
            undoMenuItem.setDisable(!model.getCanUndo());
            undoMenuItem.setText("Undo " + model.getSuggestedUndoTitle());
            redoMenuItem.setDisable(!model.getCanRedo());
            redoMenuItem.setText("Redo " + model.getSuggestedRedoTitle());
            addBoxAnnotationMenuItem.setDisable(false);
            addEllipseAnnotationMenuItem.setDisable(false);
            addTextAnnotationMenuItem.setDisable(false);
            deleteAnnotationMenuItem.setDisable(0 >= model.getSelectionSize());
            //**************************end of programming by contract************//
            if (null != currentPageImageView) {
                int pageIndex = pagination.getCurrentPageIndex();
                /// This function is used for painting images loaded with Image class
                currentPageImageView.setImage(model.getImage(pageIndex));
                currentPageImageView.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent me) {
                        long currentClick = System.currentTimeMillis();
                        boolean isDoubleClick = false;
                        long diff=0;
                        if(currentClick!=0 && lastClick != 0)
                            diff = currentClick - lastClick;
                        
                        System.out.println("diff "+diff+" lastClick "+lastClick);
                        if(diff<1000)
                            isDoubleClick = true;
                        
                        float flippedY = (float) currentPageImageView.getBoundsInParent().getHeight() - (float) me.getY();

                        PDAnnotation candidate = model.getLastAnnotationOnPageAtPoint(pageIndex, (float) me.getX(), 
                              flippedY);
                        
                        
                        if(isDoubleClick && null != candidate)
                        {
                           candidate.setContents("");
                           model.executeDocumentCommandWithNameAndArgs("DeleteSelectedAnnotation",
                            new String[]{Integer.toString(pageIndex)});
                           model.executeDocumentCommandWithNameAndArgs("AddTextAnnotation",
                            new String[]{Integer.toString(pageIndex), "36", "576", "144.0", "19.0", "We are about to edit this text"});
                          
                        }
                        isDoubleClick = false;
                        lastClick = currentClick;
                        System.out.println("pressed Y: " + me.getY());
                        System.out.println("Mouse pressed X: " + me.getX()
                                + " Y: " + Float.toString(flippedY));

                        float xInPage = (float) me.getX();
                        float yInPage = flippedY;
                        originalX = xInPage;
                        originalY = yInPage;
                        if (null != model) {
                            int pageIndex = pagination.getCurrentPageIndex();
                            if (!me.isMetaDown() && !me.isShiftDown()) {
                                model.deselectAll();
                            }
                            model.extendSelectionOnPageAtPoint(pageIndex,
                                    xInPage, yInPage);
                            refreshUserInterface();
                        }
                    }
                });
            
               
                
               ///This function is used for painting images loaded with image class 
                currentPageImageView.setOnMouseDragged(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent me) {
                      float x = (float) me.getX();
                      float flippedY = (float) currentPageImageView.getBoundsInParent().getHeight() - (float) me.getY();
                      ///Create the correct annotation from the base COS object.
                      PDAnnotation candidate = model.getLastAnnotationOnPageAtPoint(pageIndex, originalX, 
                              originalY);
                   
                      if (null != candidate) {
                        System.out.println("moving");
                        model.executeDocumentCommandWithNameAndArgs("MoveAnnotation",
                        new String[]{Integer.toString(pageIndex), candidate.getRectangle().getLowerLeftX()+"", 
                            candidate.getRectangle().getLowerLeftY()+"", (x-candidate.getRectangle().getLowerLeftX())+"", (flippedY-candidate.getRectangle().getLowerLeftY())+""});

                      }
                        
                      refreshUserInterface();

                      }
                    
                });
                
                currentPageImageView.setOnMouseReleased(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent me) {
                        /// This function is used to get the mouse position when it is released
                        originalX = (float) me.getX();
                        originalY = (float) currentPageImageView.getBoundsInParent().getHeight()-(float) me.getY();
                        refreshUserInterface();
                    }
                });
            }

            synchronizeSelectionKnobs();

        } else {
            pagination.setPageCount(0);
            pagination.setPageFactory(index -> {
                if (null == pageImageGroup) {
                    pageImageGroup = new Group();
                }
                currentPageImageView = new ImageView();
                pageImageGroup.getChildren().clear();
                pageImageGroup.getChildren().add(currentPageImageView);
                return pageImageGroup;
            });
            pagination.setDisable(true);
            /// By using this function users can select one menu item at time. After a user clicks an item, the menu returns to the hidden mode
            ///https://github.com/WSU-CEG-6110-4410/Agile-Tuskers/issues/12
            saveAsMenuItem.setDisable(true);
             ///This function has a display text property and serves as the base class for the bulk of JavaFX menus API
            extractTextMenuItem.setDisable(true);
            /// This function is intended to be used in conjunction with Menu to provide options to users
            undoMenuItem.setDisable(true);
            ///This function is specifically designed for use within a Menu
            redoMenuItem.setDisable(true);
            addBoxAnnotationMenuItem.setDisable(true);
            /// This function is used for adding ellipse annotation to the menuitem
            addEllipseAnnotationMenuItem.setDisable(true);
            /// This function is used for adding text annotation to the menuitem
            addTextAnnotationMenuItem.setDisable(true);
            /// This function is used for deleting annotation to the menuitem
            deleteAnnotationMenuItem.setDisable(true);

        }
    }

    private AIRViewerModel reinitializeWithModel(AIRViewerModel aModel) {
        assert pagination != null : "fx:id=\"pagination\" was not injected: check your FXML file 'simple.fxml'.";
        assert openMenuItem != null : "fx:id=\"openMenuItem\" was not injected: check your FXML file 'simple.fxml'.";
        assert saveAsMenuItem != null : "fx:id=\"saveAsMenuItem\" was not injected: check your FXML file 'simple.fxml'.";
        assert closeMenuItem != null : "fx:id=\"closeMenuItem\" was not injected: check your FXML file 'simple.fxml'.";

        assert extractTextMenuItem != null : "fx:id=\"extractTextMenuItem\" was not injected: check your FXML file 'simple.fxml'.";
        assert undoMenuItem != null : "fx:id=\"undoMenuItem\" was not injected: check your FXML file 'simple.fxml'.";
        assert redoMenuItem != null : "fx:id=\"redoMenuItem\" was not injected: check your FXML file 'simple.fxml'.";
        assert addBoxAnnotationMenuItem != null : "fx:id=\"addBoxAnnotationMenuItem\" was not injected: check your FXML file 'simple.fxml'.";
        assert addEllipseAnnotationMenuItem != null : "fx:id=\"addEllipseAnnotationMenuItem\" was not injected: check your FXML file 'simple.fxml'.";
        assert addTextAnnotationMenuItem != null : "fx:id=\"addTextAnnotationMenuItem\" was not injected: check your FXML file 'simple.fxml'.";
        /// This function is used for deleting annotations in menu item
        assert deleteAnnotationMenuItem != null : "fx:id=\"deleteAnnotationMenuItem\" was not injected: check your FXML file 'simple.fxml'.";

        model = aModel;

        openMenuItem.setOnAction((ActionEvent e) -> {
            System.out.println("Open ...");
            reinitializeWithModel(promptLoadModel(AIRViewerController.DEFAULT_PATH));
        });
        openMenuItem.setDisable(false);
        closeMenuItem.setOnAction((ActionEvent e) -> {
            System.out.println("closeMenuItem ...");
            Platform.exit();
        });
        closeMenuItem.setDisable(false);

        if (null != model) {
            Stage stage = AIRViewer.getPrimaryStage();
            assert null != stage;

            model.deselectAll();

            pagination.setPageCount(model.numPages());
            pagination.setPageFactory(index -> {
                if (null == pageImageGroup) {
                    pageImageGroup = new Group();
                }
                currentPageImageView = new ImageView(model.getImage(index));
                pageImageGroup.getChildren().clear();
                pageImageGroup.getChildren().add(currentPageImageView);
                model.deselectAll();
                refreshUserInterface();
                return pageImageGroup;
            });
            saveAsMenuItem.setOnAction((ActionEvent event) -> {
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
                fileChooser.getExtensionFilters().add(extFilter);
                File file = fileChooser.showSaveDialog((Stage) pagination.getScene().getWindow());
                if (null != file) {
                    model.save(file);
                }
            });
            extractTextMenuItem.setOnAction((ActionEvent e) -> {
                System.out.println("extractTextMenuItem ...");
                int pageIndex = pagination.getCurrentPageIndex();
                /// Annotation that tags a field or method as accessible to markup
                PDAnnotation candidate = model.getLastAnnotationOnPageAtPoint(pageIndex, originalX, 
                            originalY);
                if (null != candidate) {
                      System.out.println(candidate.getContents());
                }
            });
            undoMenuItem.setOnAction((ActionEvent e) -> {
                model.undo();
                refreshUserInterface();
            });
            redoMenuItem.setOnAction((ActionEvent e) -> {
                model.redo();
                refreshUserInterface();
            });
            addBoxAnnotationMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    int pageIndex = pagination.getCurrentPageIndex();
                    model.executeDocumentCommandWithNameAndArgs("AddBoxAnnotation",
                            new String[]{Integer.toString(pageIndex), "36.0", "36.0", "72.0", "72.0"});
                    refreshUserInterface();
                }
            });
            addEllipseAnnotationMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    int pageIndex = pagination.getCurrentPageIndex();
                    model.executeDocumentCommandWithNameAndArgs("AddCircleAnnotation",
                            new String[]{Integer.toString(pageIndex), "288", "576", "144.0", "72.0", "Sample Text!"});
                    refreshUserInterface();
                }
            });
            addTextAnnotationMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    int pageIndex = pagination.getCurrentPageIndex();
                    model.executeDocumentCommandWithNameAndArgs("AddTextAnnotation",
                            new String[]{Integer.toString(pageIndex), "36", "576", "144.0", "19.0", "A Bit More Sample Text!"});
                    refreshUserInterface();
                }
            });
            deleteAnnotationMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    int pageIndex = pagination.getCurrentPageIndex();
                    model.executeDocumentCommandWithNameAndArgs("DeleteSelectedAnnotation",
                            new String[]{Integer.toString(pageIndex)});
                    refreshUserInterface();
                }
            });
        }

        refreshUserInterface();

        return model;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        assert pagination != null : "fx:id=\"pagination\" was not injected: check your FXML file 'simple.fxml'.";

        Stage stage = AIRViewer.getPrimaryStage();
        stage.addEventHandler(WindowEvent.WINDOW_SHOWING, (WindowEvent window) -> {
            reinitializeWithModel(promptLoadModel(DEFAULT_PATH));
        });

    }

}
