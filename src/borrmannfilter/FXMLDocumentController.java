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
import javafx.scene.text.Text;

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

    @FXML
    private void handleButtonAction(ActionEvent event) {
        output.setText("Parameters:" + " f1=" + f1
                + " f2=" + f2 + " d=" + d + " den=" + den);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        f1Field.textProperty().addListener(event -> f1 = (f1Field.getText().length() == 0) ?
                0 : Float.parseFloat(f1Field.getText()));
        f2Field.textProperty().addListener(event -> f2 = (f2Field.getText().length() == 0) ?
                0 : Float.parseFloat(f2Field.getText()));
        dField.textProperty().addListener(event -> d = (dField.getText().length() == 0) ?
                0 : Float.parseFloat(dField.getText()) *1e-9);
        denField.textProperty().addListener(event -> den = (denField.getText().length() == 0) ?
                0 : Float.parseFloat(denField.getText()) *1e-3);
        cutField.textProperty().addListener(event -> den = (cutField.getText().length() == 0) ?
                0 : Float.parseFloat(cutField.getText()) *1e-3);
    }

    private double f1 = 0.00001, f2 = 0.000001, den = 1, d = 0.1;
}
