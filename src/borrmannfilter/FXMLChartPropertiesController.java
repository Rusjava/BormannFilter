/*
 * Copyright (C) 2015 Samsung
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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.DoubleProperty;

/**
 * FXML Controller class
 *
 * @author Samsung
 */
public class FXMLChartPropertiesController implements Initializable {
    @FXML
    private TextField lineThicknessField;
    @FXML
    private TextField fontSizeField;
    @FXML
    private TextField axisThicknessField;
    //Properties
    DoubleProperty lineThicknessProperty, fontSizeProperty, axisThicknessProperty;

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        lineThicknessProperty = new SimpleDoubleProperty(2);
        fontSizeProperty = new SimpleDoubleProperty(10);
        axisThicknessProperty = new SimpleDoubleProperty(1);
    }    
    
}
