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
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;

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

    private int size = 401;
    private Map<String, String> defaultStringMap;
    private Map<TextField, String> valueMemory;

    private double offset = 33000, angle, energy;

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
    private void handleButtonAction(ActionEvent event) {
        updateGraph();
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
        //Defining map for storing field values
        valueMemory = new HashMap<>();
        f1Field.textProperty().addListener(event -> crystal.setF1(testValueWithMemory(0, 1000, f1Field, "14.321", valueMemory)));
        f2Field.textProperty().addListener(event -> crystal.setF2(testValueWithMemory(0, 1000, f2Field, "0.494", valueMemory)));
        dField.textProperty().addListener(event -> crystal.setD(testValueWithMemory(0, 2, dField, "0.192", valueMemory) * 1e-9));
        denField.textProperty().addListener(event -> crystal.setDen(testValueWithMemory(0, 30, denField, "2", valueMemory) * 1e3));
        cutField.textProperty().addListener(event -> crystal.setTheta(testValueWithMemory(0, 90, cutField, "0", valueMemory) * Math.PI / 180));
        thetaField.textProperty().addListener(event -> {
            if (isAngle.get()) {
                angle = testValueWithMemory(0, 1000, thetaField, "60", valueMemory) * Math.PI / 180;
            } else {
                energy = testValueWithMemory(100, 100000, thetaField, "6457.96", valueMemory);
            }
        });
        LField.textProperty().addListener(event -> crystal.setL(testValueWithMemory(0, 1000, LField, "0.25", valueMemory) * 1e-3));
        MField.textProperty().addListener(event -> crystal.setM(testValueWithMemory(1, 300, thetaField, "28.09", valueMemory) * 1e-3));
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

    @FXML
    private void handleSaveButtonAction(ActionEvent event) {

    }

    private void updateGraph() {
        double iter, R, T, step;
        mainChart.getData().clear();
        rSeries = new Series<>();
        tSeries = new Series<>();
        if (isAngle.get()) {
            output.setText("Bragg diffraction. The first order. Energy dependence");
            offset = CONV / (2 * crystal.getD() * Math.cos(angle + crystal.getTheta()));
            step = 5 * crystal.getWavelengthWidth(angle, CONV / offset) * scale.get() / (size - 1) * offset * offset / CONV;
        } else {
            output.setText("Bragg diffraction. The first order. Angualar dependence");
            offset = (Math.acos(CONV / energy / 2 / crystal.getD()) - crystal.getTheta()) * 180 / Math.PI;
            step = 5 * crystal.getAngleWidth(offset, CONV / energy) * scale.get() / (size - 1) * 180 / Math.PI;
        }

        /*
        * Calculating the data
        */
        for (int i = -(size - 1) / 2; i < (size + 1) / 2; i++) {
            iter = offset + i * step;
            if (isAngle.get()) {
                R = crystal.getBraggIReflectivity(angle, CONV / iter);
                T = crystal.getBraggITransmittivity(angle, CONV / iter);
            } else {
                R = crystal.getBraggIReflectivity(iter * Math.PI / 180, CONV / energy);
                T = crystal.getBraggITransmittivity(iter * Math.PI / 180, CONV / energy);
            }
            rSeries.getData().add(new Data<>(iter, R));
            tSeries.getData().add(new Data<>(iter, T));
        }
        /*
        * Adding the data to the chart
        */
        mainChart.getData().add(rSeries);
        mainChart.getData().get(0).setName("Reflectivity");
        mainChart.getData().add(tSeries);
        mainChart.getData().get(1).setName("Transmittivity");
        mainChart.setCreateSymbols(false);
        //Formatting X-axis
        ((NumberAxis) mainChart.getXAxis()).setAutoRanging(false);
        ((NumberAxis) mainChart.getXAxis()).setForceZeroInRange(false);
        ((NumberAxis) mainChart.getXAxis()).setTickUnit(step * (size - 1) / 8);
        if (isAngle.get()) {
            ((NumberAxis) mainChart.getXAxis()).setLabel("Energy, eV");
        } else {
            ((NumberAxis) mainChart.getXAxis()).setLabel("Angle, mrad");
        }
        ((NumberAxis) mainChart.getXAxis()).setUpperBound(offset + size * step / 2);
        ((NumberAxis) mainChart.getXAxis()).setLowerBound(offset - (size - 1) * step / 2);
        //Formatting Y-axis
        ((NumberAxis) mainChart.getYAxis()).setTickUnit(0.2);
        ((NumberAxis) mainChart.getYAxis()).setAutoRanging(false);
        ((NumberAxis) mainChart.getYAxis()).setUpperBound(1);
        ((NumberAxis) mainChart.getYAxis()).setLabel("(Transmi/Reflec)tivity");
    }
    /*
    * Metod that test the entered values for correctness and puts them into the memory
    */
    private static Double testValueWithMemory(double min, double max, TextField field,
            String str, Map<TextField, String> oldStrings) {
        Double value;
        String oldStr=oldStrings.get(field);
        String newStr=field.getText();
        if (newStr.length() == 0) {
            return 0.0;
        }
        if (oldStr==null) {
            oldStr=str;
            oldStrings.put(field, str);
        }
        try {
            value=Double.valueOf(newStr);
        } catch (NumberFormatException e) {
            field.setText(oldStr);
            value=Double.valueOf(oldStr);
            return value;
        }
        if (value < min || value > max ) {
            field.setText(oldStr);
            value=Double.valueOf(oldStr);
            return value;
        }
        oldStrings.replace(field, newStr);
        return value;
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
}
