/*
 * Copyright (C) 2015 Ruslan Feshchenko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package borrmannfilter;

import java.net.URL;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
//JavaFX packages and classes
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Slider;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ChoiceBox;
import javafx.stage.FileChooser;
import static javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.*;
import javafx.beans.value.ObservableValue;
import javafx.scene.text.Font;
import javafx.scene.image.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.print.*;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
//Java Input/Output packes and classes
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.EOFException;
//Java AWT packages and classes
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
//Java Swing packages and classes
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import static javax.swing.SwingUtilities.*;
import javax.imageio.ImageIO;

//My packages and classes
import static TextUtilities.MyTextUtilities.*;
import shadowfileconverter.ShadowFiles;

/**
 *
 * @author Ruslan Feshchenko
 * @version 0.5
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private Label output;
    @FXML
    private TextField f1Field, f2Field, dField, denField, cutField;
    @FXML
    private Button button;
    /*
     * Swing components declarations
     */
    //Invisible Swing dialog
    JDialog dialog;
    //Swing components for graph parameters
    JTextField graphSizeBox, maxEnergyValueBox, minEnergyValueBox,
            maxAngleValueBox, minAngleValueBox;
    JCheckBox isAutomatic;
    //Swing components for Shadow parameters
    JTextField shadowMaxRayNumberBox;
    JRadioButton sPolButton, pPolButton, spPolButton;
    //Swing components for image parameters
    JTextField imageWidthJTextField, imageHeightJTextField;
    /*
     * End of Swing components declarations
     */
    //Graph parameters
    private int size = 401;
    private File oldFile = null;
    private double minEnergyValue = 6456, maxEnergyValue = 6460,
            maxAngleValue = 60.00, minAngleValue = 59.99;
    private double offset, angle, energy, step;
    private Series<Number, Number> rSeries = null, tSeries = null;
    //Memory maps
    private Map<String, String> defaultStringMap;
    private Map<TextField, String> valueMemory;
    private Map<JTextField, String> valueMemorySwing;
    //Shadow parameters
    private final int MAX_NCOL = 18;
    private int maxRayNumber = 10000;
    private File wFile = null, rFile = null;
    private boolean sPolShadow = true, pPolShadow = false;
    //Printing parameters
    private PageLayout layout = null;
    //Image parameters
    private File iFile = null;
    private int iWidth = 800, iHeight = 600;

    private final double CONV = 2 * Math.PI * 3.1614e-26 / 1.602e-19;

    private XrayCrystal crystal;
    //Observable properties
    private DoubleProperty scale;
    private BooleanProperty isAngle;
    private BooleanProperty isSPol;
    //Chart parameters
    private double lineThickness = 2, axisThickness = 2;
    private int fontSize = 10;
    //ConextMenu
    ContextMenu chartContextMenu;
    MenuItem printContextMenuItem, saveImageContextMenuItem, propertiesContextMenuItem;

    @FXML
    private LineChart<Number, Number> mainChart;
    @FXML
    private TextField MField;
    @FXML
    private TextField LField;
    @FXML
    private TextField thetaField;
    @FXML
    private Button saveButton;
    @FXML
    private Slider xScaleSlider;
    @FXML
    private ChoiceBox<?> enAngChoiceBox;
    @FXML
    private BorderPane chartPane;
    @FXML
    private ChoiceBox<?> enPolChoiceBox;
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        double iter, R, T;
        boolean isLaue;
        String graphTitle = " diffraction in the first order. "
                + (isSPol.get() ? "S-polarization. " : "P-polarization. ");
        rSeries = new Series<>();
        tSeries = new Series<>();
        if (isAngle.get()) {
            offset = isAutomatic.isSelected()
                    ? CONV / 2 / crystal.getD() / Math.cos(angle + crystal.getTheta())
                    : (minEnergyValue + maxEnergyValue) / 2;
            step = isAutomatic.isSelected()
                    ? 5 * crystal.getWavelengthWidth(angle, CONV / offset) * scale.get() / (size - 1) * offset * offset / CONV
                    : (maxEnergyValue - minEnergyValue) * scale.get() / (size - 1);
            isLaue = Math.cos(angle + 2 * crystal.getTheta()) > 0;
            graphTitle = graphTitle + "Energy dependence";

        } else {
            offset = isAutomatic.isSelected()
                    ? (Math.acos(CONV / energy / 2 / crystal.getD()) - crystal.getTheta()) * 180 / Math.PI
                    : (minAngleValue + maxAngleValue) / 2;
            step = isAutomatic.isSelected()
                    ? 5 * crystal.getAngleWidth(offset, CONV / energy) * scale.get() / (size - 1) * 180 / Math.PI
                    : (maxAngleValue - minAngleValue) * scale.get() / (size - 1);
            isLaue = Math.cos(offset * Math.PI / 180 + 2 * crystal.getTheta()) > 0;
            graphTitle = graphTitle + "Angualar dependence";
        }
        /*
         Setting up graph title
         */
        if (isLaue) {
            output.setText("Bragg" + graphTitle);
        } else {
            output.setText("Laue" + graphTitle);
        }
        /*
         * Calculating the data
         */
        for (int i = -(size - 1) / 2; i < (size + 1) / 2; i++) {
            iter = offset + i * step;
            if (isAngle.get()) {
                if (isLaue) {
                    R = crystal.getBraggIReflectivity(angle, CONV / iter, isSPol.get());
                    T = crystal.getBraggITransmittivity(angle, CONV / iter, isSPol.get());
                } else {
                    R = crystal.getLaueIReflectivity(angle, CONV / iter, isSPol.get());
                    T = crystal.getLaueITransmittivity(angle, CONV / iter, isSPol.get());
                }
            } else {
                if (isLaue) {
                    R = crystal.getBraggIReflectivity(iter * Math.PI / 180, CONV / energy, isSPol.get());
                    T = crystal.getBraggITransmittivity(iter * Math.PI / 180, CONV / energy, isSPol.get());
                } else {
                    R = crystal.getLaueIReflectivity(iter * Math.PI / 180, CONV / energy, isSPol.get());
                    T = crystal.getLaueITransmittivity(iter * Math.PI / 180, CONV / energy, isSPol.get());
                }
            }
            rSeries.getData().add(new Data<>(iter, R));
            tSeries.getData().add(new Data<>(iter, T));
        }
        /*
         * Adding the data to the chart
         */
        String label = isAngle.get() ? "Energy, eV" : "Angle, degree";
        createLineChart(rSeries, tSeries, label, offset, step, mainChart);
        updateLineChart(fontSize, lineThickness, axisThickness, mainChart);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        crystal = new XrayCrystal();
        angle = Math.PI / 3;
        energy = CONV / (2 * crystal.getD() * Math.cos(angle + crystal.getTheta()));
        scale = new SimpleDoubleProperty(1);
        isAngle = new SimpleBooleanProperty(true);
        isSPol = new SimpleBooleanProperty(true);
        //Defining map of default values
        defaultStringMap = new HashMap<>();
        defaultStringMap.put("Incidence angle, degree:", "60");
        defaultStringMap.put("Energy, eV:", "6457.96");
        //Defining maps for storing field values
        valueMemory = new HashMap<>();
        valueMemorySwing = new HashMap<>();
        //Initializing Swing components
        initSwingComponents();

        //Creating default layout
        PrinterJob job = PrinterJob.createPrinterJob();
        layout = job.getPrinter().createPageLayout(Paper.A4, PageOrientation.PORTRAIT,
                job.getJobSettings().getPageLayout().getLeftMargin(), job.getJobSettings().getPageLayout().getRightMargin(),
                job.getJobSettings().getPageLayout().getTopMargin(), job.getJobSettings().getPageLayout().getBottomMargin()
        );

        //Creating context menu
        chartContextMenu = new ContextMenu();
        printContextMenuItem = new MenuItem("Print...");
        printContextMenuItem.setOnAction(this::handlePrintMenuAction);
        saveImageContextMenuItem = new MenuItem("Save as image...");
        saveImageContextMenuItem.setOnAction(this::handleSaveImageMenuAction);
        propertiesContextMenuItem = new MenuItem("Properties...");
        propertiesContextMenuItem.setOnAction(this::handlePropertiesMenuAction);
        chartContextMenu.getItems().addAll(printContextMenuItem,
                saveImageContextMenuItem, new SeparatorMenuItem(), propertiesContextMenuItem);
        mainChart.setOnContextMenuRequested(event -> {
            chartContextMenu.show(scrollPane.getScene().getWindow(), event.getScreenX(), event.getScreenY());
        });

        f1Field.textProperty().addListener(event -> crystal.setF1(TestValueWithMemory(0, 1000, f1Field, "14.321", valueMemory)));
        f2Field.textProperty().addListener(event -> crystal.setF2(TestValueWithMemory(0, 1000, f2Field, "0.494", valueMemory)));
        dField.textProperty().addListener(event -> crystal.setD(TestValueWithMemory(0, 2, dField, "0.192", valueMemory) * 1e-9));
        denField.textProperty().addListener(event -> crystal.setDen(TestValueWithMemory(0, 30, denField, "2", valueMemory) * 1e3));
        cutField.textProperty().addListener(event -> crystal.setTheta(TestValueWithMemory(-90, 90, cutField, "0", valueMemory) * Math.PI / 180));
        thetaField.textProperty().addListener(event -> {
            if (isAngle.get()) {
                angle = TestValueWithMemory(0, 1000, thetaField, "60", valueMemory) * Math.PI / 180;
            } else {
                energy = TestValueWithMemory(100, 100000, thetaField, "6457.96", valueMemory);
            }
        });
        LField.textProperty().addListener(event -> crystal.setL(TestValueWithMemory(0, 1000, LField, "0.25", valueMemory) * 1e-3));
        MField.textProperty().addListener(event -> crystal.setM(TestValueWithMemory(1, 300, thetaField, "28.09", valueMemory) * 1e-3));
        //Binding scale property to X-scale slider
        scale.bind(xScaleSlider.valueProperty());
        //Binging isAngle variable to enAngChoiceBox
        isAngle.bind(Bindings.createBooleanBinding(()
                -> ((String) enAngChoiceBox.getValue()).equals("Incidence angle, degree:"), enAngChoiceBox.valueProperty()));
        //Switching thetaField default value when switching between anergy and angle
        enAngChoiceBox.valueProperty().addListener(event -> {
            thetaField.setText(defaultStringMap.get((String) enAngChoiceBox.getValue()));
            valueMemory.remove(thetaField);
        });
        //Switching polarization
        isSPol.bind(Bindings.createBooleanBinding(()
                -> ((String) enPolChoiceBox.getValue()).equals("s-polarization"), enPolChoiceBox.valueProperty()));
    }

    /*
     * Saving the calculated graphs into a text file
     */
    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
        //Using FX filechooser for file selection
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose a file to save the graphs in");
        fc.setInitialFileName("Graph_data.dat");
        fc.setInitialDirectory(oldFile);
        File file = fc.showSaveDialog(null);
        /*
         * If a file was chosen then proceed
         */
        if (file != null) {
            /* 
             * Storing the file's directory
             */
            oldFile = file.getParentFile();
            /*
             * Writting formatted graph data into the text file
             */
            invokeLater(() -> {
                Formatter fm;
                try (PrintWriter pw = new PrintWriter(new FileWriter(file, false))) {
                    for (int i = 0; i < mainChart.getData().get(0).getData().size(); i++) {
                        fm = new Formatter();
                        fm.format("%f %f %f", (Double) mainChart.getData().get(0)
                                .getData().get(i).getXValue(), (Double) mainChart.getData().get(0)
                                .getData().get(i).getYValue(), (Double) mainChart.getData().get(1)
                                .getData().get(i).getYValue());
                        pw.println(fm);
                    }
                } catch (IOException e) {
                    /*
                     * Using Swing's JOptionPane class to display simple message
                     */
                    JOptionPane.showMessageDialog(dialog, "Error while writing to the data file", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    @FXML
    private void handleCloseMenuAction(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void handleAboutMenuAction(ActionEvent event) {
        javax.swing.SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog,
                "<html>Crystal diffraction calculations. <br>Version: 0.3 <br>Date: June 2015. <br>Author: Ruslan Feshchenko</html>",
                "About Borrmann filter application", 1));
    }

    @FXML
    private void handleGraphParamMenuAction(ActionEvent event) {
        /*
         * Graph parameters: xsize, automatic range selection flag and min and max range values
         */
        final Object[] message = {
            "Graph size", graphSizeBox,
            isAutomatic,
            isAngle.get() ? "Min value, eV" : "Min value, degree", isAngle.get() ? minEnergyValueBox : minAngleValueBox,
            isAngle.get() ? "Max value, eV" : "Max value, degree", isAngle.get() ? maxEnergyValueBox : maxAngleValueBox
        };

        try {
            invokeAndWait(() -> {
                int option = JOptionPane.showConfirmDialog(dialog, message, "Graph parameters",
                        JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    size = (int) Math.round(TestValueWithMemory(2, 10000, graphSizeBox, "401", valueMemorySwing));
                    if (isAngle.get()) {
                        minEnergyValue = TestValueWithMemory(0, 100000, minEnergyValueBox, "6456", valueMemorySwing);
                        maxEnergyValue = TestValueWithMemory(0, 100000, maxEnergyValueBox, "6460", valueMemorySwing);
                    } else {
                        minAngleValue = TestValueWithMemory(0, 90, minAngleValueBox, "59.99", valueMemorySwing);
                        maxAngleValue = TestValueWithMemory(0, 90, maxAngleValueBox, "60.00", valueMemorySwing);
                    }
                }
            });
        } catch (InterruptedException | InvocationTargetException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleShadowParamMenuAction(ActionEvent event) {
        /*
         * Parameters for shadow file filtering: max number of rays and affected polarization
         */
        try {
            invokeAndWait(() -> {
                JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
                panel.add(sPolButton);
                panel.add(pPolButton);
                panel.add(spPolButton);
                JPanel outerPanel = new JPanel();
                outerPanel.setBorder(BorderFactory.createTitledBorder(null,
                        "Affected polarization", TitledBorder.LEFT, TitledBorder.ABOVE_TOP));
                outerPanel.add(panel);
                ButtonGroup bGroup = new ButtonGroup();
                bGroup.add(sPolButton);
                bGroup.add(pPolButton);
                bGroup.add(spPolButton);
                Object[] message = {
                    "Maximum ray number", shadowMaxRayNumberBox,
                    outerPanel
                };
                int option = JOptionPane.showConfirmDialog(dialog, message, "Shadow parameters",
                        JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    maxRayNumber = (int) Math.round(TestValueWithMemory(1, 1000000, shadowMaxRayNumberBox, "10000", valueMemorySwing));
                    if (sPolButton.isSelected() || spPolButton.isSelected()) {
                        sPolShadow = true;
                    }
                    if (pPolButton.isSelected() || spPolButton.isSelected()) {
                        pPolShadow = true;
                    }
                }
            });
        } catch (InterruptedException | InvocationTargetException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleFilterMenuAction(ActionEvent event) {
        /*
         * Filtering a ray set
         */
        invokeLater(() -> {
            int nrays;
            double[] ray;
            try (ShadowFiles shadowFileRead = new ShadowFiles(false, true, MAX_NCOL, maxRayNumber, rFile);
                    ShadowFiles shadowFileWrite = new ShadowFiles(true, true, shadowFileRead.getNcol(), shadowFileRead.getNrays(), wFile)) {
                nrays = shadowFileRead.getNrays();
                ray = new double[shadowFileRead.getNcol()];
                rFile = shadowFileRead.getFile();
                wFile = shadowFileWrite.getFile();
                for (int i = 0; i < nrays; i++) {
                    shadowFileRead.read(ray);
                    ray = crystal.rayConversion(ray, angle, sPolShadow, pPolShadow);
                    shadowFileWrite.write(ray);
                }
            } catch (EOFException e) {
                JOptionPane.showMessageDialog(dialog, "The end of file has been reached!", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(dialog, "I/O error during file conversion!", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (ShadowFiles.EndOfLineException e) {
                JOptionPane.showMessageDialog(dialog, "The number of columns is less than specified on line "
                        + e.rayNumber + " !", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ShadowFiles.FileIsCorruptedException e) {
                JOptionPane.showMessageDialog(dialog, "The file is corrupted! (line: "
                        + e.rayNumber + ")", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ShadowFiles.FileNotOpenedException e) {

            } catch (InterruptedException | InvocationTargetException ex) {

            }
        });
    }

    @FXML
    private void handlePrintMenuAction(ActionEvent event) {
        /*
         * Bringing up printer setup dialog and intitiate printing of the graph
         */
        PrinterJob job = PrinterJob.createPrinterJob();
        job.getJobSettings().setPageLayout(layout);

        if (job.showPrintDialog(null)) {
            String label = isAngle.get() ? "Energy, eV" : "Angle, degree";
            LineChart<Number, Number> chart = new LineChart<>(new NumberAxis(), new NumberAxis());
            chart.setMinWidth(job.getJobSettings().getPageLayout().getPrintableWidth());
            createLineChart(rSeries, tSeries, label, offset, step, chart);
            /*
             double scaleX = layout.getPrintableWidth() / chart.getBoundsInParent().getWidth();
             double scaleY = layout.getPrintableHeight() / chart.getBoundsInParent().getHeight();
             chart.getTransforms().add(new Scale(scaleX, scaleY));
             */
            chart.setMaxWidth(job.getJobSettings().getPageLayout().getPrintableWidth());
            chart.setMaxHeight(job.getJobSettings().getPageLayout().getPrintableHeight());
            chart.setAnimated(false);
            chart.layout();
            if (job.printPage(chart)) {
                job.endJob();
            }
            /*Stage stage = new Stage();
             stage.setScene(new Scene(new FlowPane(new ImageView(image))));
             stage.show();*/
        }
    }

    @FXML
    private void handlePageSetupMenuAction(ActionEvent event) {
        /*
         * Bringing up a dialog that allows to specify page layout settings for printing
         */
        PrinterJob job = PrinterJob.createPrinterJob();
        job.getJobSettings().setPageLayout(layout);
        if (job.showPageSetupDialog(null)) {
            layout = job.getJobSettings().getPageLayout();
            job.endJob();
        }
    }

    private void createLineChart(Series rSeries, Series tSeries, String XLabel, double offset, double step, LineChart<?, ?> chart) {
        /*
         * Creating LineChart
         */
        int xsize = rSeries.getData().size();
        chart.getData().clear();
        chart.getData().add(rSeries);
        chart.getData().get(0).setName("Reflectivity");
        chart.getData().add(tSeries);
        chart.getData().get(1).setName("Transmittivity");
        chart.setCreateSymbols(false);

        //Formatting X-axis
        NumberAxis xaxis = (NumberAxis) chart.getXAxis();
        xaxis.setAutoRanging(false);
        xaxis.setForceZeroInRange(false);
        xaxis.setTickUnit(step * (xsize - 1) / 8);
        xaxis.setLabel(XLabel);
        xaxis.setUpperBound(offset + xsize * step / 2);
        xaxis.setLowerBound(offset - (xsize - 1) * step / 2);

        //Formatting Y-axis
        NumberAxis yaxis = (NumberAxis) chart.getYAxis();
        yaxis.setTickUnit(0.2);
        yaxis.setAutoRanging(false);
        yaxis.setUpperBound(1);
        yaxis.setLabel("(Transmi/Reflec)tivity");
    }

    private void initSwingComponents() {
        /*
         * Initialization of Swing components on EDT
         */
        invokeLater(() -> {
            dialog = new JDialog();
            dialog.setAlwaysOnTop(true);
            graphSizeBox = new JTextField("401");
            isAutomatic = new JCheckBox("Automatic range selection");
            maxEnergyValueBox = new JTextField("6460");
            minEnergyValueBox = new JTextField("6456");
            maxAngleValueBox = new JTextField("60.00");
            maxAngleValueBox.setEnabled(false);
            minAngleValueBox = new JTextField("59.99");
            isAutomatic.addChangeListener(event -> {
                maxEnergyValueBox.setEnabled(!isAutomatic.isSelected());
                minEnergyValueBox.setEnabled(!isAutomatic.isSelected());
                maxAngleValueBox.setEnabled(!isAutomatic.isSelected());
                minAngleValueBox.setEnabled(!isAutomatic.isSelected());
            });
            isAutomatic.setSelected(true);

            shadowMaxRayNumberBox = new JTextField("10000");
            sPolButton = new JRadioButton("only s-polarization");
            sPolButton.setSelected(true);
            pPolButton = new JRadioButton("only p-polarization");
            spPolButton = new JRadioButton("both s and p-polarization");
            imageHeightJTextField = new JTextField("600");
            imageWidthJTextField = new JTextField("800");
        });
    }

    @FXML
    private void handleHelpTopicsMenuAction(ActionEvent event) {
        /*
         * Displaying help html file
         */
        WebView webView = new WebView();
        Stage stage = new Stage();
        URL helpUrl = BorrmannFilter.class.getResource("/borrmannfilterhelp/Help.html");
        WebEngine engine = webView.getEngine();
        engine.load(helpUrl.toString());
        engine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends Worker.State> v, Worker.State os, Worker.State ns) -> {
            if (ns == Worker.State.SUCCEEDED) {
                String title = webView.getEngine().getTitle();
                stage.setTitle(title);
                stage.setScene(new Scene(webView));
                stage.show();
            }
        });
    }

    @FXML
    private void handleSaveImageMenuAction(ActionEvent event) {
        /*
         * Saving chart as image
         */
        if (rSeries == null) {
            return;
        }
        //Selecting file to save the chrt image
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose a file to save the image in");
        fc.setInitialFileName("x-ray diffraction chart.png");
        fc.setInitialDirectory(iFile);
        //File extensions
        fc.getExtensionFilters().add(new ExtensionFilter("PNG image", "*.png"));
        fc.getExtensionFilters().add(new ExtensionFilter("JPG image", "*.jpg"));
        fc.getExtensionFilters().add(new ExtensionFilter("TIF image", "*.tif"));
        fc.getExtensionFilters().add(new ExtensionFilter("GIF image", "*.gif"));
        File file = fc.showSaveDialog(null);
        /*
         * If a file was chosen then proceed
         */
        if (file != null) {
            iFile = file.getParentFile();
            String selectedExtension = fc.getSelectedExtensionFilter().getExtensions().get(0).substring(2);
            //Creating chart
            String label = isAngle.get() ? "Energy, eV" : "Angle, degree";
            LineChart<Number, Number> chart = new LineChart<>(new NumberAxis(), new NumberAxis());
            chart.setAnimated(false);
            createLineChart(rSeries, tSeries, label, offset, step, chart);
            chart.setPrefSize(iWidth, iHeight);
            chart.layout();
            //Capturing image
            WritableImage image = new WritableImage(iWidth, iHeight);
            (new Scene(chart)).snapshot(image);
            //Creating BufferedImage
            invokeLater(() -> {
                BufferedImage bImage = new BufferedImage(iWidth, iHeight, BufferedImage.TYPE_INT_ARGB);
                SwingFXUtils.fromFXImage(image, bImage);
                //Writing image into a file  
                try {
                    ImageIO.write(bImage, selectedExtension, file);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dialog, "I/O error during image file creation!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    @FXML
    private void handleImageParametersMenuAction(ActionEvent event) {/*
         * Image paramters: image size
         */

        try {
            invokeAndWait(() -> {
                Object[] message = {
                    "Image width, px", imageWidthJTextField,
                    "Image height, px", imageHeightJTextField
                };
                int option = JOptionPane.showConfirmDialog(dialog, message, "Image parameters",
                        JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    iWidth = (int) Math.round(TestValueWithMemory(10, 10000, imageWidthJTextField, "800", valueMemorySwing));
                    iHeight = (int) Math.round(TestValueWithMemory(10, 10000, imageHeightJTextField, "600", valueMemorySwing));
                }
            });
        } catch (InterruptedException | InvocationTargetException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void handlePropertiesMenuAction(ActionEvent event) {
        /*
         * Displaying properties menu for the main chart
         */
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("FXMLChartProperties.fxml"));
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        Parent root = loader.getRoot();
        Stage stage = new Stage();
        stage.setTitle("Borrmann filter application");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        final FXMLChartPropertiesController controller
                = (FXMLChartPropertiesController) loader.getController();
        stage.setOnShown(ev -> {
            controller.fontSizeField.setText(new Integer(fontSize).toString());
            controller.lineThicknessField.setText(new Double(lineThickness).toString());
            controller.axisThicknessField.setText(new Double(axisThickness).toString());
        });
        stage.setOnHiding(ev -> {
            if (controller.isChanged) {
                lineThickness = controller.lineThickness;
                fontSize = controller.fontSize;
                axisThickness = controller.axisThickness;
                updateLineChart(fontSize, lineThickness, axisThickness, mainChart);
            }
        });
        stage.show();
    }
    /*
     * Updating line chart after the change in chart properties
     */

    private void updateLineChart(int fSize, double lThickness, double aThickness, LineChart<?, ?> chart) {
        NumberAxis xaxis = (NumberAxis) chart.getXAxis();
        NumberAxis yaxis = (NumberAxis) chart.getYAxis();
        Font font = new Font(fSize);
        xaxis.setTickLabelFont(font);
        yaxis.setTickLabelFont(font);
        chart.getScene().getRoot().setStyle(".chart-series-line {-fx-stroke-width: " + 4 + "px;}");
    }
}
