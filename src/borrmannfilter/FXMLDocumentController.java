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
import java.util.ResourceBundle;
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
import javafx.print.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Formatter;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import static TextUtilities.MyTextUtilities.*;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.BorderFactory;
import java.awt.GridLayout;
import java.io.EOFException;
import java.lang.reflect.InvocationTargetException;
import static javax.swing.SwingUtilities.*;
import shadowfileconverter.ShadowFiles;

/**
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private Label output;
    @FXML
    private TextField f1Field, f2Field, dField, denField, cutField;
    @FXML
    private Button button;

    //Swing elements for graph parameters
    JTextField graphSizeBox, maxEnergyValueBox, minEnergyValueBox,
            maxAngleValueBox, minAngleValueBox;
    JCheckBox isAutomatic;
    //Swing elements for Shadow parameters
    JTextField shadowMaxRayNumberBox;
    JRadioButton sPolButton, pPolButton, spPolButton;
    //Graph parameters
    private int size = 401, maxRayNumber = 10000;
    private boolean sPol = true, pPol = false;
    private double minEnergyValue = 6456, maxEnergyValue = 6460,
            maxAngleValue = 60.00, minAngleValue = 59.99;
    private Map<String, String> defaultStringMap;
    private Map<TextField, String> valueMemory;
    private Map<JTextField, String> valueMemorySwing;
    //Shadow parameters
    private File oldFile = null;
    private PageLayout layout = null;
    private final int MAX_NCOL = 18;

    File rFile = null, wFile = null;

    private double offset, angle, energy, step;

    private final double CONV = 2 * Math.PI * 3.1614e-26 / 1.602e-19;

    private XrayCrystal crystal;

    private DoubleProperty scale;
    private BooleanProperty isAngle;

    private Series<Number, Number> rSeries, tSeries;
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
    private void handleButtonAction(ActionEvent event) {
        double iter, R, T;
        boolean isLaue;
        String graphTitle = " diffraction. The first order. ";
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
                    R = crystal.getBraggIReflectivity(angle, CONV / iter);
                    T = crystal.getBraggITransmittivity(angle, CONV / iter);
                } else {
                    R = crystal.getLaueIReflectivity(angle, CONV / iter);
                    T = crystal.getLaueITransmittivity(angle, CONV / iter);
                }
            } else {
                if (isLaue) {
                    R = crystal.getBraggIReflectivity(iter * Math.PI / 180, CONV / energy);
                    T = crystal.getBraggITransmittivity(iter * Math.PI / 180, CONV / energy);
                } else {
                    R = crystal.getLaueIReflectivity(iter * Math.PI / 180, CONV / energy);
                    T = crystal.getLaueITransmittivity(iter * Math.PI / 180, CONV / energy);
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
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        crystal = new XrayCrystal();
        angle = Math.PI / 3;
        energy = CONV / (2 * crystal.getD() * Math.cos(angle + crystal.getTheta()));
        scale = new SimpleDoubleProperty(1);
        isAngle = new SimpleBooleanProperty(true);
        //Defining map of default values
        defaultStringMap = new HashMap<>();
        defaultStringMap.put("Incidence angle, degree:", "60");
        defaultStringMap.put("Energy, eV:", "6457.96");
        //Defining maps for storing field values
        valueMemory = new HashMap<>();
        valueMemorySwing = new HashMap<>();
        //Defining Swing fields
        graphSizeBox = new JTextField("401");
        isAutomatic = new JCheckBox("Automatic range selection");
        isAutomatic.setSelected(true);
        isAutomatic.addChangeListener(event -> {
            if (isAutomatic.isSelected()) {
                maxEnergyValueBox.setEnabled(false);
                minEnergyValueBox.setEnabled(false);
                maxAngleValueBox.setEnabled(false);
                minAngleValueBox.setEnabled(false);
            } else {
                maxEnergyValueBox.setEnabled(true);
                minEnergyValueBox.setEnabled(true);
                maxAngleValueBox.setEnabled(true);
                minAngleValueBox.setEnabled(true);
            }
        });
        maxEnergyValueBox = new JTextField("6460");
        maxEnergyValueBox.setEnabled(false);
        minEnergyValueBox = new JTextField("6456");
        minEnergyValueBox.setEnabled(false);
        maxAngleValueBox = new JTextField("60.00");
        maxAngleValueBox.setEnabled(false);
        minAngleValueBox = new JTextField("59.99");
        minAngleValueBox.setEnabled(false);
        shadowMaxRayNumberBox = new JTextField("10000");
        sPolButton = new JRadioButton("only s-polarization");
        sPolButton.setSelected(true);
        pPolButton = new JRadioButton("only p-polarization");
        spPolButton = new JRadioButton("both s and p-polarization");

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
            Formatter fm;
            try (PrintWriter pw = new PrintWriter(new FileWriter(file, false))) {
                for (int i = 0; i < size; i++) {
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
                invokeLater(() -> JOptionPane.showMessageDialog(null, "Error while writing to the file", "Error",
                        JOptionPane.ERROR_MESSAGE));
            }
        }
    }

    @FXML
    private void handleCloseMenuAction(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void handleAboutMenuAction(ActionEvent event) {
        javax.swing.SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                "<html>Crystal diffraction calculations. <br>Version: 0.1 <br>Date: June 2015. <br>Author: Ruslan Feshchenko</html>",
                "About Borrmann filter application", 1));
    }

    @FXML
    private void handleGraphParamMenuAction(ActionEvent event) {
        Object[] message = {
            "Graph size", graphSizeBox,
            isAutomatic,
            isAngle.get() ? "Min value, eV" : "Min value, degree", isAngle.get() ? minEnergyValueBox : minAngleValueBox,
            isAngle.get() ? "Max value, eV" : "Max value, degree", isAngle.get() ? maxEnergyValueBox : maxAngleValueBox
        };

        invokeLater(() -> {
            JDialog dialog = new JDialog();
            dialog.setAlwaysOnTop(true);
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
    }

    @FXML
    private void handleShadowParamMenuAction(ActionEvent event) {
        /*
         * Parameters for shadow file filtering: max number of rays and affected polarization
         */
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Affected polarization"));
        panel.setLayout(new GridLayout(3, 1));
        panel.add(sPolButton);
        panel.add(pPolButton);
        panel.add(spPolButton);
        ButtonGroup bGroup = new ButtonGroup();
        bGroup.add(sPolButton);
        bGroup.add(pPolButton);
        bGroup.add(spPolButton);
        Object[] message = {
            "Maximum ray number", shadowMaxRayNumberBox,
            panel
        };

        invokeLater(() -> {
            JDialog dialog = new JDialog();
            dialog.setAlwaysOnTop(true);
            int option = JOptionPane.showConfirmDialog(dialog, message, "Shadow parameters",
                    JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                maxRayNumber = (int) Math.round(TestValueWithMemory(1, 1000000, shadowMaxRayNumberBox, "10000", valueMemorySwing));
                if (sPolButton.isSelected() || spPolButton.isSelected()) {
                    sPol = true;
                }
                if (pPolButton.isSelected() || spPolButton.isSelected()) {
                    pPol = true;
                }
            }
        });
    }

    @FXML
    private void handleFilterMenuAction(ActionEvent event) {
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
                shadowFileWrite.write(ray);
            }
        } catch (EOFException e) {
            invokeLater(() -> JOptionPane.showMessageDialog(null, "The end of file has been reached!", "Error",
                    JOptionPane.ERROR_MESSAGE));
        } catch (IOException e) {
            invokeLater(() -> JOptionPane.showMessageDialog(null, "I/O error during file conversion!", "Error",
                    JOptionPane.ERROR_MESSAGE));
        } catch (ShadowFiles.EndOfLineException e) {
            invokeLater(() -> JOptionPane.showMessageDialog(null, "The number of columns is less than specified on line "
                    + e.rayNumber + " !", "Error", JOptionPane.ERROR_MESSAGE));
        } catch (ShadowFiles.FileIsCorruptedException e) {
            invokeLater(() -> JOptionPane.showMessageDialog(null, "The file is corrupted! (line: "
                    + e.rayNumber + ")", "Error", JOptionPane.ERROR_MESSAGE));
        } catch (ShadowFiles.FileNotOpenedException e) {

        } catch (InterruptedException ex) {

        } catch (InvocationTargetException ex) {

        }
    }

    @FXML
    private void handlePrintMenuAction(ActionEvent event) {
        /*
         * Bringing up printer setup dialog and intitiate printing of the graph
         */
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job.showPrintDialog(null)) {
            if (layout != null) {
                job.getJobSettings().setPageLayout(layout);
            }

            String label = isAngle.get() ? "Energy, eV" : "Angle, degree";
            LineChart<Number, Number> chart = new LineChart<>(new NumberAxis(), new NumberAxis());
            chart.setMinWidth(job.getJobSettings().getPageLayout().getPrintableWidth());
            chart.setMinHeight(job.getJobSettings().getPageLayout().getPrintableHeight());
            createLineChart(rSeries, tSeries, label, offset, step, chart);
            /*double scaleX = layout.getPrintableWidth() / chart.getBoundsInParent().getWidth();
             double scaleY = layout.getPrintableHeight() / chart.getBoundsInParent().getHeight();
             chart.getTransforms().add(new Scale(scaleX, scaleY));*/
            chart.setMaxWidth(job.getJobSettings().getPageLayout().getPrintableWidth());
            chart.setMaxHeight(job.getJobSettings().getPageLayout().getPrintableHeight());
            ((NumberAxis) chart.getXAxis()).setTickLabelFill(Color.BLACK);
            chart.layout();
            if (job.printPage(chart)) {
                job.endJob();
            }
        }
    }

    @FXML
    private void handlePageSetupMenuAction(ActionEvent event) {
        /*
         * Bringing up a dialog that allows to specify page layout settings for printing
         */
        PrinterJob job = PrinterJob.createPrinterJob();
        if (layout != null) {
            job.getJobSettings().setPageLayout(layout);
        }
        if (job.showPageSetupDialog(null)) {
            layout = job.getJobSettings().getPageLayout();
            job.endJob();
        }
    }
    /*
     * Creating LineChart
     */

    private void createLineChart(Series rSeries, Series tSeries, String XLabel, double offset, double step, LineChart<?, ?> chart) {
        chart.getData().clear();
        chart.getData().add(rSeries);
        chart.getData().get(0).setName("Reflectivity");
        chart.getData().add(tSeries);
        chart.getData().get(1).setName("Transmittivity");
        chart.setCreateSymbols(false);
        //Formatting X-axis
        ((NumberAxis) chart.getXAxis()).setAutoRanging(false);
        ((NumberAxis) chart.getXAxis()).setForceZeroInRange(false);
        ((NumberAxis) chart.getXAxis()).setTickUnit(step * (size - 1) / 8);
        ((NumberAxis) chart.getXAxis()).setLabel(XLabel);
        ((NumberAxis) chart.getXAxis()).setUpperBound(offset + size * step / 2);
        ((NumberAxis) chart.getXAxis()).setLowerBound(offset - (size - 1) * step / 2);
        //Formatting Y-axis
        ((NumberAxis) chart.getYAxis()).setTickUnit(0.2);
        ((NumberAxis) chart.getYAxis()).setAutoRanging(false);
        ((NumberAxis) chart.getYAxis()).setUpperBound(1);
        ((NumberAxis) chart.getYAxis()).setLabel("(Transmi/Reflec)tivity");
    }
}
