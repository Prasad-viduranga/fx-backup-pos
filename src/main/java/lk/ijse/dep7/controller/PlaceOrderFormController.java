package lk.ijse.dep7.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lk.ijse.dep7.dbutils.SingleConnectionDataSource;
import lk.ijse.dep7.dto.CustomerDTO;
import lk.ijse.dep7.dto.ItemDTO;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFoundException;
import lk.ijse.dep7.service.CustomerService;
import lk.ijse.dep7.service.ItemService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;

public class PlaceOrderFormController {

    public AnchorPane root;
    public JFXButton btnPlaceOrder;
    public JFXTextField txtCustomerName;
    public JFXTextField txtDescription;
    public JFXTextField txtQtyOnHand;
    public JFXButton btnSave;
    public TableView tblOrderDetails;
    public JFXTextField txtUnitPrice;
    public JFXComboBox<String> cmbCustomerId;
    public JFXComboBox<String> cmbItemCode;
    public JFXTextField txtQty;
    public Label lblId;
    public Label lblDate;
    public Label lblTotal;
    private CustomerService customerService = new CustomerService(SingleConnectionDataSource.getInstance().getConnection());
    private ItemService itemService = new ItemService(SingleConnectionDataSource.getInstance().getConnection());

    @FXML
    private void navigateToHome(MouseEvent event) throws IOException {
        URL resource = this.getClass().getResource("/view/main-form.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) (this.root.getScene().getWindow());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        Platform.runLater(() -> primaryStage.sizeToScene());
    }

    public void initialize() throws FailedOperationException {
        /*Todo: Wwe need to generate and set a new order id*/
        lblDate.setText(LocalDate.now().toString());
        btnPlaceOrder.setDisable(true);
        loadAllCustomer();
        loadAllItem();
        txtCustomerName.setEditable(false);
        txtDescription.setEditable(false);

        cmbCustomerId.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {
                    txtCustomerName.setText(customerService.findCustomer(newValue).getName());
                } catch (NotFoundException e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to load the customer name.").show();

                } catch (FailedOperationException e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to load the customer name.").show();
                    throw new RuntimeException(e);
                }
            }
        });

        cmbItemCode.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {
                    txtDescription.setText(itemService.findItem(newValue).getDescription());
                } catch (NotFoundException e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to load the item description.").show();

                } catch (FailedOperationException e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to load the item description.").show();
                }
            }
        });


    }

    private void loadAllItem() {
        try {
            for (ItemDTO itemDTOList : itemService.findAllItem()) {
                cmbItemCode.getItems().add(itemDTOList.getCode());
            }
        } catch (FailedOperationException e) {
            e.printStackTrace();
        }
    }

    private void loadAllCustomer() throws FailedOperationException {
        try {
            for (CustomerDTO customerDTOList : customerService.findAllCustomer()) {
                cmbCustomerId.getItems().add(customerDTOList.getId());
            }
        } catch (FailedOperationException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load customer IDs").show();
            throw e;
        }
    }

    public void btnAdd_OnAction(ActionEvent actionEvent) {
    }

    public void txtQty_OnAction(ActionEvent actionEvent) {
    }

    public void btnPlaceOrder_OnAction(ActionEvent actionEvent) {
    }
}
