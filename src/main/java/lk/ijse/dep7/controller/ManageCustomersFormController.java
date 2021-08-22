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
import lk.ijse.dep7.dto.CustomerDTO;
import lk.ijse.dep7.exception.DuplicateIdentifierException;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFoundException;
import lk.ijse.dep7.service.CustomerService;
import lk.ijse.dep7.util.CustomerTM;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ManageCustomersFormController {
    public AnchorPane root;
    public JFXTextField txtCustomerName;
    public JFXTextField txtCustomerId;
    public JFXButton btnDelete;
    public JFXButton btnSave;
    public JFXTextField txtCustomerAddress;
    public TableView<CustomerTM> tblCustomers;
    public JFXButton btnAddNewCustomer;
    private CustomerService customerService = new CustomerService(SingleConnectionDataSource.getInstance().getConnection());

    public void initialize() throws FailedOperationException {
        tblCustomers.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblCustomers.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblCustomers.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));

        initUI();

        tblCustomers.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnDelete.setDisable(newValue == null);
            btnSave.setText(newValue != null ? "Update" : "Save");
            if (newValue != null) {
                txtCustomerId.setText(newValue.getId());
                txtCustomerName.setText(newValue.getName());
                txtCustomerAddress.setText(newValue.getAddress());
                btnSave.setDisable(false);
                txtCustomerName.setDisable(false);
                txtCustomerAddress.setDisable(false);

            }
            tblCustomers.refresh();

        });
        txtCustomerAddress.setOnAction(event -> btnSave.fire());
        loadAllCustomer();
    }

    private void loadAllCustomer() throws FailedOperationException {
        tblCustomers.getItems().clear();
        try {
//            Method 1
//            List<CustomerDTO> allCustomer = customerService.findAllCustomer();
//            for (CustomerDTO customer : allCustomer) {
//                tblCustomers.getItems().add(new CustomerTM(customer.getId(), customer.getName(), customer.getAddress()));
//            }
//            Method 2
//            List<CustomerTM> customers = customerService.findAllCustomer().stream().map(dto -> new CustomerTM(dto.getId(), dto.getName(), dto.getAddress())).collect(Collectors.toList());
//            tblCustomers.setItems(FXCollections.observableList(customers));

//            Method 3
            customerService.findAllCustomer().forEach(dto -> tblCustomers.getItems().add(new CustomerTM(dto.getId(), dto.getName(), dto.getAddress())));

        } catch (FailedOperationException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            throw e;
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
        txtCustomerId.setDisable(false);
        txtCustomerName.setDisable(false);
        txtCustomerAddress.setDisable(false);
        txtCustomerId.clear();
        txtCustomerId.setText(generateNewId());
        txtCustomerName.clear();
        txtCustomerAddress.clear();
        txtCustomerName.requestFocus();
        btnSave.setDisable(false);
        tblCustomers.getSelectionModel().clearSelection();

    }

    public void btnSave_OnAction(ActionEvent actionEvent) throws FailedOperationException {
        String id = txtCustomerId.getText();
        String name = txtCustomerName.getText();
        String address = txtCustomerAddress.getText();
        try {

            if (btnSave.getText().equals("Save")) {

                if (!name.matches("[A-Za-z ]+")) {
                    new Alert(Alert.AlertType.ERROR, "Invalid Name").show();
                    txtCustomerName.requestFocus();
                    return;
                } else if (!address.matches(".{3,}")) {
                    new Alert(Alert.AlertType.ERROR, "Address should  be at least 3 characters long").show();
                    txtCustomerAddress.requestFocus();
                    return;
                }

                try {
                    customerService.saveCustomer(new CustomerDTO(id, name, address));
                } catch (DuplicateIdentifierException e) {
                    new Alert(Alert.AlertType.ERROR, "Invalid ID").show();
                }
                tblCustomers.getItems().add(new CustomerTM(id, name, address));

            } else if (btnSave.getText().equals("Update")) {
                CustomerTM selectedCustomer = tblCustomers.getSelectionModel().getSelectedItem();

                if (!name.matches("[A-Za-z ]+")) {
                    new Alert(Alert.AlertType.ERROR, "Invalid Name").show();
                    txtCustomerName.requestFocus();
                    return;
                } else if (!address.matches(".{3,}")) {
                    new Alert(Alert.AlertType.ERROR, "Address should  be at least 3 characters long").show();
                    txtCustomerAddress.requestFocus();
                    return;
                }

                try {
                    selectedCustomer.setName(name);
                    selectedCustomer.setAddress(address);
                    customerService.updateCustomer(new CustomerDTO(id, name, address));
                    tblCustomers.refresh();
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }

            }
            btnAddNewCustomer.fire();
        } catch (FailedOperationException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            throw e;
        }

    }

    public void btnDelete_OnAction(ActionEvent actionEvent) throws FailedOperationException {
        try {
            customerService.deleteCustomer(tblCustomers.getSelectionModel().getSelectedItem().getId());
            tblCustomers.getItems().remove(tblCustomers.getSelectionModel().getSelectedItem());
            tblCustomers.getSelectionModel().clearSelection();
            tblCustomers.refresh();
            initUI();

        } catch (NotFoundException e) {
            e.printStackTrace(); // Never happend with our UI design
        } catch (FailedOperationException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            throw e;
        }
    }

    private void initUI() {
        txtCustomerId.clear();
        txtCustomerName.clear();
        txtCustomerAddress.clear();
        txtCustomerId.setDisable(true);
        txtCustomerName.setDisable(true);
        txtCustomerAddress.setDisable(true);
        txtCustomerId.setEditable(false);
        btnSave.setDisable(true);
        btnDelete.setDisable(true);
    }

    private String generateNewId() {
        int lastID = 0;
        if (tblCustomers.getItems().isEmpty()) {
            return "C001";
        } else {
            List<Integer> customers = new ArrayList<>();
            for (int i = 0; i < tblCustomers.getItems().size(); i++) {
                if (lastID < Integer.parseInt(tblCustomers.getItems().get(i).getId().split("C")[1])) {
                    lastID = Integer.parseInt(tblCustomers.getItems().get(i).getId().split("C")[1]);
                }
            }
            int newId = lastID + 1;
            return String.format("C%03d", newId);
        }
    }
}