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

    private int size = 41;

    private double step = 0.02, offset = 33000, angle = Math.PI / 3;

    private final double CONV = 2 * Math.PI * 3.1614e-26 / 1.602e-19;
    private double[] data;

    private XrayCrystall crystal;

    private Series<Number, Number> rSeries;
    @FXML
    private LineChart<Number, Number> mainChart;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        double energy, R;
        output.setText("Parameters:" + " f1=" + crystal.getF1()
                + " f2=" + crystal.getF2() + " d=" + crystal.getD()
                + " den=" + crystal.getDen() + " cutting angle =" + crystal.getTheta());
        offset = CONV / (2 * crystal.getD() * Math.cos(angle + crystal.getTheta()));
        for (int i = -(size - 1) / 2; i < (size + 1) / 2; i++) {
            energy = offset + i * step;
            R = crystal.getIReflectivity(angle, CONV / energy);
            rSeries.getData().add(new Data<Number, Number>(energy, R));
            data[i + (size - 1) / 2] = R;
        }
        mainChart.getData().add(rSeries);
        ((NumberAxis) mainChart.getXAxis()).setForceZeroInRange(false);
        mainChart. setCreateSymbols(false);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        crystal = new XrayCrystall();
        rSeries = new Series<>();
        data = new double[size];
        f1Field.textProperty().addListener(event -> crystal.setF1((f1Field.getText().length() == 0)
                ? 0 : Float.parseFloat(f1Field.getText())));
        f2Field.textProperty().addListener(event -> crystal.setF2((f2Field.getText().length() == 0)
                ? 0 : Float.parseFloat(f2Field.getText())));
        dField.textProperty().addListener(event -> crystal.setD((dField.getText().length() == 0)
                ? 0 : Float.parseFloat(dField.getText()) * 1e-9));
        denField.textProperty().addListener(event -> crystal.setDen((denField.getText().length() == 0)
                ? 0 : Float.parseFloat(denField.getText()) * 1e-3));
        cutField.textProperty().addListener(event -> crystal.setTheta((cutField.getText().length() == 0)
                ? 0 : Float.parseFloat(cutField.getText()) * 1e-3));
    }
}
