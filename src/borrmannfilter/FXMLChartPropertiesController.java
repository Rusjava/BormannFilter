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

import static TextUtilities.MyTextUtilities.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Map;
import java.util.HashMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

/**
 * FXML controller class for the chart properties box
 * @author Ruslan Feshchenko
 * @version 0.5
 */
public class FXMLChartPropertiesController implements Initializable {

    @FXML
    public TextField lineThicknessField;
    @FXML
    public TextField fontSizeField;
    @FXML
    public TextField axisThicknessField;
    //Properties
    public double lineThickness, axisThickness;
    public int fontSize;
    private Map<TextField, String> formMemory;

    public boolean isChanged = false;
    @FXML
    private Button okButtonBox;
    @FXML
    private Button cancelButtonBox;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        formMemory = new HashMap<>();
        lineThicknessField.textProperty().addListener(event -> lineThickness = TestValueWithMemory(0.1, 10, lineThicknessField, "1.0", formMemory));
        fontSizeField.textProperty().addListener(event -> fontSize = (int) Math.round(TestValueWithMemory(1, 40, fontSizeField, "10", formMemory)));
        axisThicknessField.textProperty().addListener(event -> axisThickness = TestValueWithMemory(0.1, 10, axisThicknessField, "4.0", formMemory));
    }

    @FXML
    private void okButtonBoxAction(ActionEvent event) {
        isChanged = true;
        okButtonBox.getScene().getWindow().hide();
    }

    @FXML
    private void cancelButtonBoxAction(ActionEvent event) {
        cancelButtonBox.getScene().getWindow().hide();
    }

}
