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
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/**
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private Text output;
    @FXML
    private TextField f1Field, f2Field, dField, denField;
    @FXML
    private Button button;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        output.setText("Calculating! " + "\nf1=" + f1
                + "\nf2=" + f2 + "\nd=" + d + "\nden=" + den);
    }

    /*
     * Real part of the scaterring amplitude inputs
     */
    @FXML
    private void handlef1FieldAction(ActionEvent event) {
        f1 = Float.parseFloat(f1Field.getText());
    }

    /*
     * Imagenery part of the scaterring amplitude inputs
     */
    @FXML
    private void handlef2FieldAction(ActionEvent event) {
        f2 = Float.parseFloat(f2Field.getText());
    }

    /*
     * Interplane distance inputs
     */
    @FXML
    private void handleDFieldAction(ActionEvent event) {
        d = Float.parseFloat(dField.getText());
    }

    /*
     * Density inputs
     */
    @FXML
    private void handleDenFieldAction(ActionEvent event) {
        den = Float.parseFloat(denField.getText());
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        f1Field.textProperty().addListener(event -> f1 = Float.parseFloat(f1Field.getText()));
        f2Field.textProperty().addListener(event -> f2 = Float.parseFloat(f2Field.getText()));
        dField.textProperty().addListener(event -> d = Float.parseFloat(dField.getText()));
        denField.textProperty().addListener(event -> {/*den = Float.parseFloat(denField.getText());*/
        System.out.println(Float.parseFloat(denField.getText()));});
    }

    private double f1, f2, den, d;
}
