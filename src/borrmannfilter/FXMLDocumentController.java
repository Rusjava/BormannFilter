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
//import javafx.scene.chart.;

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

    private double step = 0.0025, offset = 33000, angle = Math.PI / 3;

    private final double CONV = 2 * Math.PI * 3.1614e-26 / 1.602e-19;
    private double[] data;

    private XrayCrystall crystal;

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
    private void handleButtonAction(ActionEvent event) {
        double energy, R, T;
        output.setText("Parameters:" + " f1=" + crystal.getF1()
                + " f2=" + crystal.getF2() + " d=" + crystal.getD()
                + " den=" + crystal.getDen() + " cutting angle =" + crystal.getTheta());
        offset = CONV / (2 * crystal.getD() * Math.cos(angle + crystal.getTheta()));
        for (int i = -(size - 1) / 2; i < (size + 1) / 2; i++) {
            energy = offset + i * step;
            R = crystal.getBraggIReflectivity(angle, CONV / energy);
            T = crystal.getBraggITransmitivity(angle, CONV / energy);
            rSeries.getData().add(new Data<>(energy, R));
            tSeries.getData().add(new Data<>(energy, T));
            data[i + (size - 1) / 2] = T;
        }
        mainChart.getData().add(rSeries);
        mainChart.getData().add(tSeries);
        mainChart.setCreateSymbols(false);
        //Formatting X-axis
        ((NumberAxis) mainChart.getXAxis()).setAutoRanging(false);
        ((NumberAxis) mainChart.getXAxis()).setForceZeroInRange(false);
        ((NumberAxis) mainChart.getXAxis()).setTickUnit(step * 50);
        ((NumberAxis) mainChart.getXAxis()).setLabel("Energy, eV");
        ((NumberAxis) mainChart.getXAxis()).setUpperBound(offset + size * step / 2);
        ((NumberAxis) mainChart.getXAxis()).setLowerBound(offset - (size - 1) * step / 2);
        //Formatting Y-axis
        ((NumberAxis) mainChart.getYAxis()).setTickUnit(0.2);
        ((NumberAxis) mainChart.getYAxis()).setAutoRanging(false);
        ((NumberAxis) mainChart.getYAxis()).setUpperBound(1);
        ((NumberAxis) mainChart.getYAxis()).setLabel("(Transmi/Reflec)tivity");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        crystal = new XrayCrystall();
        rSeries = new Series<>();
        tSeries = new Series<>();
        data = new double[size];
        f1Field.textProperty().addListener(event -> crystal.setF1((f1Field.getText().length() == 0)
                ? 0 : Float.parseFloat(f1Field.getText())));
        f2Field.textProperty().addListener(event -> crystal.setF2((f2Field.getText().length() == 0)
                ? 0 : Float.parseFloat(f2Field.getText())));
        dField.textProperty().addListener(event -> crystal.setD((dField.getText().length() == 0)
                ? 0 : Float.parseFloat(dField.getText()) * 1e-9));
        denField.textProperty().addListener(event -> crystal.setDen((denField.getText().length() == 0)
                ? 0 : Float.parseFloat(denField.getText()) * 1e3));
        cutField.textProperty().addListener(event -> crystal.setTheta((cutField.getText().length() == 0)
                ? 0 : Float.parseFloat(cutField.getText()) * Math.PI / 180));
        thetaField.textProperty().addListener(event -> angle = (thetaField.getText().length() == 0)
                ? 0 : Float.parseFloat(thetaField.getText()) * Math.PI / 180);
        LField.textProperty().addListener(event -> crystal.setL((LField.getText().length() == 0)
                ? 0 : Float.parseFloat(LField.getText()) * 1e-3));
        MField.textProperty().addListener(event -> crystal.setM((MField.getText().length() == 0)
                ? 0 : Float.parseFloat(MField.getText()) * 1e-3));
    }
}
