/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.wright.airviewer2;

import java.awt.Rectangle;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;

/**
 * Instances of this class encapsulate a dictionary mapping String names to
 * Command subclass constructors as well as Undo and Redo stacks that enable
 * undo or redo of operations performed by instances of the Command class.
 *
 * @author erik
 */
public abstract class AbstractDocumentCommandWrapper {

    /**
     *
     */
    public static interface makeCommand {

        public AbstractDocumentCommand make(AbstractDocumentCommandWrapper anOwner, ArrayList<String> args);
    }
    /**
     *
     */
    protected final PDDocument wrappedDocument;

    /**
     *
     */
    private final Stack<AbstractDocumentCommand> undoStack;

    /**
     *
     */
    private final Stack<AbstractDocumentCommand> redoStack;

    /**
     *
     */
    private static HashMap<String, makeCommand> nameToFactoryMap
            = nameToFactoryMap = new HashMap<>();

    /**
     *
     */
    private final List<PDAnnotation> selectedAnnotations;

    /**
     *
     * @param aDocument
     */
    protected AbstractDocumentCommandWrapper(PDDocument aDocument) {
        wrappedDocument = aDocument;
        undoStack = new Stack<>();
        redoStack = new Stack<>();
        selectedAnnotations = new ArrayList<PDAnnotation>();
    }

    public void deselectAll() {
        getSelectedAnnotations().clear();
    }

    public void extendSelectionOnPageAtPoint(int pageIndex, float x, float y) {
        PDAnnotation candidate = getLastAnnotationOnPageAtPoint(pageIndex, x, y);
        if (null != candidate && !selectedAnnotations.contains(candidate)) {
            System.out.println(selectedAnnotations.toArray());
            getSelectedAnnotations().add(candidate);
            System.out.println("Selected: " + selectedAnnotations.toString());

        }
    }

    public List<Rectangle> getSelectedAreas() {
        ArrayList<Rectangle> result = new ArrayList<>();

        for (PDAnnotation a : getSelectedAnnotations()) {
            PDRectangle aBBox = a.getRectangle();
            Rectangle intBBox = new Rectangle((int) aBBox.getLowerLeftX(),
                    (int) aBBox.getLowerLeftY(),
                    (int) aBBox.getWidth(), (int) aBBox.getHeight());
            result.add(intBBox);
        }
        return result;
    }

    public int getSelectionSize() {
        return getSelectedAnnotations().size();
    }

    protected List<PDAnnotation> getSelectedAnnotations() {
        return selectedAnnotations;
    }

    public boolean getCanUndo() {
        return 0 < undoStack.size();
    }

    public boolean getCanRedo() {
        return 0 < redoStack.size();
    }

    public String getSuggestedUndoTitle() {
        if (0 < undoStack.size()) {
            return undoStack.lastElement().getUndoName();
        }

        return "";
    }

    public String getSuggestedRedoTitle() {
        if (0 < redoStack.size()) {
            return redoStack.lastElement().getUndoName();
        }

        return "";
    }

    /**
     *
     * @param command
     * @return
     */
    private boolean executeDocumentCommand(AbstractDocumentCommand command) {
        boolean result = false;
        AbstractDocumentCommand reciprocal = command.execute();
        if (null != reciprocal) {
            reciprocal.setUndoName(command.getName());
            undoStack.push(reciprocal);
            result = true;
        }

        return result;
    }

    /**
     *
     * @param command
     * @param aName
     */
    protected static void registerCommandClassWithName(makeCommand command, String aName) {
        nameToFactoryMap.put(aName, command);
    }

    /**
     *
     * @return
     */
    public static String getCommandNames() {
        String result = "";

        for (String name : nameToFactoryMap.keySet()) {
            result += "\t" + name + "\n";
        }

        return result;
    }

    /**
     *
     * @param aName
     * @param args
     * @return
     */
    public boolean executeDocumentCommandWithNameAndArgs(String aName, ArrayList<String> args) {
        boolean result = false;

        if (nameToFactoryMap.containsKey(aName)) {
            makeCommand makeFunction = nameToFactoryMap.get(aName);
            try {
                AbstractDocumentCommand command = makeFunction.make(this, args);
                executeDocumentCommand(command);

            } catch (IllegalArgumentException ex) {
//                Logger.getLogger(AbstractDocumentCommandWrapper.class
//                        .getName()).log(Level.SEVERE, null, ex);
            }

        } else {
//            Logger.getLogger(AbstractDocumentCommandWrapper.class
//                    .getName()).log(Level.SEVERE, null, aName);
        }

        return result;
    }

    /**
     *
     * @param aName
     * @param args
     * @return
     */
    public boolean executeDocumentCommandWithNameAndArgs(String aName, String[] args) {
        ArrayList<String> commandArgs = new ArrayList<>(Arrays.asList(args));
        return executeDocumentCommandWithNameAndArgs(aName, commandArgs);
    }

    /**
     *
     * @return
     */
    public boolean undo() {
        boolean result = false;

        if (0 < undoStack.size()) {
            ///This function retrieves the parameters for this Application, including any arguments passed on the command line 
            AbstractDocumentCommand command = undoStack.pop();
            AbstractDocumentCommand reciprocal = command.execute();
            if (null != reciprocal) {
                reciprocal.setUndoName(command.getUndoName());
                redoStack.push(reciprocal);
                result = true;
            }
        }

        return result;
    }

    /**
     *
     * @return
     */
    public boolean redo() {
        boolean result = false;

        if (0 < redoStack.size()) {
            ///This function retrieves the parameters for this Application, including any arguments passed on the command line 
            AbstractDocumentCommand command = redoStack.pop();
            AbstractDocumentCommand reciprocal = command.execute();
            if (null != reciprocal) {
                reciprocal.setUndoName(command.getUndoName());
                undoStack.push(reciprocal);
                result = true;
            }
        }

        return result;

    }

    protected PDAnnotation getLastAnnotationOnPageAtPoint(int pageIndex, float x, float y) {
        try {
            PDPage page = wrappedDocument.getPage(pageIndex);
            List<PDAnnotation> annotations = page.getAnnotations();

            return getLastAnnotationAtPoint(annotations, x, y);
        } catch (IOException ex) {
//            Logger.getLogger(AbstractDocumentCommandWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     *
     * @param annotations
     * @param x
     * @param y
     * @return
     */
    protected PDAnnotation getLastAnnotationAtPoint(List<PDAnnotation> annotations, float x, float y) {
        PDAnnotation result = null;

        for (int i = annotations.size() - 1; i >= 0; --i) {
            PDAnnotation candidate = annotations.get(i);
            if (!candidate.isHidden() && !candidate.isReadOnly()) {
                PDRectangle boundingBox = candidate.getRectangle();

                if (boundingBox.contains(x, y)) {
                    return candidate;

                }
            }
        }

        return result;
    }

    /**
     * This class defines the interface, variables, and common operations in
     * order to encapsulates commands that may be executed following the Command
     * Design Pattern. Concrete subclasses must implements execute() When
     * executed to perform operations specific to the concrete subclass and
     * return a reciprocal command to be executed if/when the undo is required.
     * If undo is inappropriate for some reason, concrete implementations of
     * execute() may return null.
     *
     * @author Buck, Saxena
     *
     */
    protected abstract class AbstractDocumentCommand {

        /**
         *
         */
        protected final AbstractDocumentCommandWrapper owner;

        /**
         *
         */
        protected final ArrayList<String> arguments;

        /**
         *
         */
        protected List<PDAnnotation> annotations;

        /**
         *
         */
        protected String undoName;

        /**
         *
         * @param anOwner
         * @param args
         */
        protected AbstractDocumentCommand(AbstractDocumentCommandWrapper anOwner, ArrayList<String> args) {
            owner = anOwner;
            arguments = args;
            annotations = null;
        }

        /**
         *
         * @param anOwner
         * @param someAnnotations
         */
        protected AbstractDocumentCommand(AbstractDocumentCommandWrapper anOwner, List<PDAnnotation> someAnnotations) {
            owner = anOwner;
            arguments = null;
            annotations = someAnnotations;
        }

        /**
         *
         * @param anOwner
         * @param someAnnotations
         * @param args
         */
        protected AbstractDocumentCommand(AbstractDocumentCommandWrapper anOwner, List<PDAnnotation> someAnnotations, ArrayList<String> args) {
            owner = anOwner;
            arguments = args;
            annotations = someAnnotations;
        }

        public void setUndoName(String aName) {
            undoName = aName;
        }

        public String getUndoName() {
            if (null == undoName) {
                undoName = "";
            }
            return undoName;
        }

        /**
         *
         * @return If execute() succeeds, a Command that is the reciprocal of
         * the receiver is returned. Otherwise, null is returned.
         */
        public abstract AbstractDocumentCommand execute();

        /**
         *
         * @return The name of the command as it will appear in a user interface
         * for undo and redo operations e.g. "Undo Delete Annotation" where the
         * string after "Undo " is returned from getName().
         */
        public abstract String getName();

    }

}
