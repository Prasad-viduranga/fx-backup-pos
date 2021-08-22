package lk.ijse.dep7.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lk.ijse.dep7.dbutils.SingleConnectionDataSource;
import lk.ijse.dep7.dto.ItemDTO;
import lk.ijse.dep7.exception.DuplicateIdentifierException;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFoundException;
import lk.ijse.dep7.service.ItemService;
import lk.ijse.dep7.util.ItemTM;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;

public class ManageItemsFormController {

    public AnchorPane root;
    public JFXTextField txtCode;
    public JFXTextField txtDescription;
    public JFXTextField txtQtyOnHand;
    public JFXButton btnDelete;
    public JFXButton btnSave;
    public TableView<ItemTM> tblItems;
    public JFXTextField txtUnitPrice;
    public JFXButton btnAddNewItem;
    private ItemService itemService = new ItemService(SingleConnectionDataSource.getInstance().getConnection());

    public void initialize() {
        tblItems.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("code"));
        tblItems.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblItems.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("qty"));
        tblItems.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        txtCode.setEditable(false);
        txtCode.setDisable(true);
        txtDescription.setDisable(true);
        txtQtyOnHand.setDisable(true);
        txtUnitPrice.setDisable(true);
        btnDelete.setDisable(true);

        tblItems.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnDelete.setDisable(newValue == null);
            if (newValue != null) {
                txtCode.setText(newValue.getCode());
                txtDescription.setText(newValue.getDescription());
                txtQtyOnHand.setText(newValue.getQty());
                txtUnitPrice.setText(newValue.getUnitPrice());
                btnSave.setText("Update");
                txtCode.setDisable(false);
                txtDescription.setDisable(false);
                txtQtyOnHand.setDisable(false);
                txtUnitPrice.setDisable(false);
                txtDescription.requestFocus();
            } else {
                btnSave.setText("Save");
            }
        });


        loadAllItem();

    }

    private void loadAllItem() {
        try {
            List<ItemDTO> allItem = itemService.findAllItem();
            for (ItemDTO item : allItem) {
                tblItems.getItems().add(new ItemTM(item.getCode(), item.getDescription(), String.valueOf(item.getQty()), String.valueOf(item.getUnitPrice())));
            }

        } catch (FailedOperationException e) {
            e.printStackTrace();
        }
    }

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

    public void btnAddNew_OnAction(ActionEvent actionEvent) {
        txtCode.setText(newItemCode());
        txtDescription.clear();
        txtQtyOnHand.clear();
        txtUnitPrice.clear();
        txtCode.setDisable(false);
        txtDescription.setDisable(false);
        txtQtyOnHand.setDisable(false);
        txtUnitPrice.setDisable(false);
        txtDescription.requestFocus();
        tblItems.getSelectionModel().clearSelection();
    }

    public void btnDelete_OnAction(ActionEvent actionEvent) throws FailedOperationException {

        try {
            itemService.deleteItem(tblItems.getSelectionModel().getSelectedItem().getCode());
        } catch (FailedOperationException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            throw e;
        } catch (NotFoundException e) {
            e.printStackTrace();
        }

        tblItems.getItems().remove(tblItems.getSelectionModel().getSelectedItem());
        btnAddNewItem.fire();
    }

    public void btnSave_OnAction(ActionEvent actionEvent) throws FailedOperationException {
        String code = txtCode.getText();
        String description = txtDescription.getText();
        int qty = Integer.parseInt(txtQtyOnHand.getText());
        BigDecimal unitPrice = BigDecimal.valueOf(Float.parseFloat(txtUnitPrice.getText()));
        try {

            if (btnSave.getText().equals("Save")) {
                itemService.saveItem(new ItemDTO(code, description, qty, unitPrice));
                tblItems.getItems().add(new ItemTM(code, description, String.valueOf(qty), String.valueOf(unitPrice)));
                tblItems.refresh();
                txtDescription.requestFocus();

            } else if (btnSave.getText().equals("Update")) {
                itemService.updateItem(new ItemDTO(code, description, qty, unitPrice));
                tblItems.getSelectionModel().getSelectedItem().setDescription(description);
                tblItems.getSelectionModel().getSelectedItem().setQty(String.valueOf(qty));
                tblItems.getSelectionModel().getSelectedItem().setUnitPrice(String.valueOf(unitPrice));
                tblItems.refresh();

            }
            btnAddNewItem.fire();
        } catch (DuplicateIdentifierException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();

        } catch (FailedOperationException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            throw e;
        }
    }

    private String newItemCode() {
        int lastCode = 0;
        if (tblItems.getItems().isEmpty()) {
            return "I001";
        } else {
            for (int i = 0; i < tblItems.getItems().size(); i++) {
                if (lastCode < Integer.parseInt(tblItems.getItems().get(i).getCode().split("I")[1])) {
                    lastCode = Integer.parseInt(tblItems.getItems().get(i).getCode().split("I")[1]);
                }
            }
            int newId = lastCode + 1;
            return String.format("I%03d", newId);
        }

    }
}