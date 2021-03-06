package lk.ijse.dep7.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lk.ijse.dep7.dbutils.SingleConnectionDataSource;
import lk.ijse.dep7.dto.CustomerDTO;
import lk.ijse.dep7.dto.ItemDTO;
import lk.ijse.dep7.dto.OrderDetailsDTO;
import lk.ijse.dep7.exception.DuplicateIdentifierException;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFoundException;
import lk.ijse.dep7.service.CustomerService;
import lk.ijse.dep7.service.ItemService;
import lk.ijse.dep7.service.OrderService;
import lk.ijse.dep7.util.OrderDetailsTM;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlaceOrderFormController {

    public AnchorPane root;
    public JFXButton btnPlaceOrder;
    public JFXTextField txtCustomerName;
    public JFXTextField txtDescription;
    public JFXTextField txtQtyOnHand;
    public TableView<OrderDetailsTM> tblOrderDetails;
    public JFXTextField txtUnitPrice;
    public JFXComboBox<String> cmbCustomerId;
    public JFXComboBox<String> cmbItemCode;
    public JFXTextField txtQty;
    public Label lblId;
    public Label lblDate;
    public Label lblTotal;
    public JFXButton btnAdd;
    private String orderId;

    private OrderService orderService = new OrderService(SingleConnectionDataSource.getInstance().getConnection());
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

        tblOrderDetails.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("code"));
        tblOrderDetails.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblOrderDetails.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("qty"));
        tblOrderDetails.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        tblOrderDetails.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("total"));
        TableColumn<OrderDetailsTM, Button> lastCol = (TableColumn<OrderDetailsTM, Button>) tblOrderDetails.getColumns().get(5);
        lastCol.setCellValueFactory(param -> {
            Button btnDelete = new Button("Delete");
            btnDelete.setOnAction(event -> {
                tblOrderDetails.getItems().remove(param.getValue());
                initUI();
                setOrDisablePlaceOrder();
            });
            return new ReadOnlyObjectWrapper<>(btnDelete);
        });

        orderId = orderService.generateOrderId();
        lblId.setText("Order ID: " + orderId);
        lblDate.setText(LocalDate.now().toString());
        btnPlaceOrder.setDisable(true);
        loadAllCustomer();
        loadAllItem();
        txtCustomerName.setEditable(false);
        txtDescription.setFocusTraversable(false);
        txtUnitPrice.setFocusTraversable(false);
        txtQtyOnHand.setFocusTraversable(false);
        txtDescription.setEditable(false);
        txtQtyOnHand.setEditable(false);
        txtUnitPrice.setEditable(false);
        txtQty.setOnAction(event -> btnAdd.fire());
        txtQty.setDisable(true);
        btnAdd.setDisable(true);

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
            }else{
                txtCustomerName.clear();
            }
            setOrDisablePlaceOrder();
        });

        cmbItemCode.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            txtQty.setDisable(newValue == null);
            btnAdd.setDisable(newValue == null);

            if (newValue != null) {
//                Method 1
//                int orderedQty = 0;
//                for (OrderDetailsTM orderDetailsTM : tblOrderDetails.getItems()) {
//                    if (orderDetailsTM.getCode().equals(newValue)) {
//                        orderedQty = orderDetailsTM.getQty();
//                    }
//                }

                try {
                    txtDescription.setText(itemService.findItem(newValue).getDescription());
                    txtUnitPrice.setText(String.valueOf(itemService.findItem(newValue).getUnitPrice().setScale(2)));
//                    Method 1
//                    txtQtyOnHand.setText(String.valueOf(itemService.findItem(newValue).getQty()-orderedQty));

//                    Method 2
                    Optional<OrderDetailsTM> optOrderDetail = tblOrderDetails.getItems().stream().filter(details -> details.getCode().equals(newValue)).findFirst();
                    txtQtyOnHand.setText((optOrderDetail.isPresent() ? itemService.findItem(newValue).getQty() - optOrderDetail.get().getQty() : itemService.findItem(newValue).getQty()) + "");

                } catch (NotFoundException e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to load the item description.").show();

                } catch (FailedOperationException e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to load the item description.").show();
                    throw new RuntimeException(e);
                }
            }
        });

        tblOrderDetails.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                btnAdd.setText("Update");
                cmbItemCode.setDisable(true);
                cmbItemCode.setValue(newValue.getCode());
                txtQtyOnHand.setText(Integer.parseInt(txtQtyOnHand.getText()) + newValue.getQty() + "");
                txtQty.setText(newValue.getQty() + "");

            } else {
                btnAdd.setText("Add");
            }
        });

    }

    private void total() {

        BigDecimal total = tblOrderDetails.getItems().stream().map(details -> details.getTotal()).reduce((bigDecimal, bigDecimal2) -> bigDecimal.add(bigDecimal2)).orElse(new BigDecimal(0)).setScale(2);
        lblTotal.setText(total.toString());
        /*        BigDecimal total = new BigDecimal(0);
        for (OrderDetailsTM item : tblOrderDetails.getItems()) {
            total = total.add(item.getTotal());
        }
        lblTotal.setText(total.setScale(2).toString());*/

    }

    private void setOrDisablePlaceOrder() {
        btnPlaceOrder.setDisable(!(cmbCustomerId.getSelectionModel().getSelectedItem() != null && !tblOrderDetails.getItems().isEmpty()));
    }

    private void initUI() {
        cmbItemCode.getSelectionModel().clearSelection();
        txtDescription.clear();
        txtQtyOnHand.clear();
        txtUnitPrice.clear();
        txtQty.clear();
        btnAdd.setDisable(true);
        tblOrderDetails.getSelectionModel().clearSelection();
        cmbCustomerId.requestFocus();
        cmbItemCode.setDisable(false);
        total();
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

        String code = cmbItemCode.getSelectionModel().getSelectedItem();
        String description = txtDescription.getText();
        int qty = Integer.parseInt(txtQty.getText());
        BigDecimal unitPrice = new BigDecimal(txtUnitPrice.getText());
        BigDecimal total = unitPrice.multiply(new BigDecimal(qty)).setScale(2);

        if (!txtQty.getText().matches("\\d+") || Integer.parseInt(txtQty.getText()) <= 0
                || Integer.parseInt(txtQty.getText()) > Integer.parseInt(txtQtyOnHand.getText())) {
            new Alert(Alert.AlertType.ERROR, "Invalid qty").show();
            txtQty.requestFocus();
            txtQty.selectAll();
            return;
        }
        if (btnAdd.getText().equals("Update")) {

            total = unitPrice.multiply(new BigDecimal(qty)).setScale(2);
            tblOrderDetails.getSelectionModel().getSelectedItem().setQty(qty);
            tblOrderDetails.getSelectionModel().getSelectedItem().setTotal(total);
            tblOrderDetails.refresh();
            initUI();
            return;
        }

        for (OrderDetailsTM orderDetailsTM : tblOrderDetails.getItems()) {
            if (orderDetailsTM.getCode().equals(code)) {
                qty = orderDetailsTM.getQty() + qty;
                total = unitPrice.multiply(new BigDecimal(qty)).setScale(2);
                orderDetailsTM.setQty(qty);
                orderDetailsTM.setTotal(total);

                tblOrderDetails.refresh();
                cmbItemCode.requestFocus();
                initUI();
                return;
            }
        }

        tblOrderDetails.getItems().add(new OrderDetailsTM(code, description, qty, unitPrice, total));
        initUI();
        setOrDisablePlaceOrder();
    }

    public void txtQty_OnAction(ActionEvent actionEvent) {
    }

    public void btnPlaceOrder_OnAction(ActionEvent actionEvent) throws FailedOperationException, DuplicateIdentifierException, NotFoundException {
        try {
            orderService.saveOrder(orderId, LocalDate.now(), cmbCustomerId.getValue(), tblOrderDetails.getItems().stream().map(tm -> new OrderDetailsDTO(tm.getCode(), tm.getQty(), tm.getUnitPrice())).collect(Collectors.toList()));
            new Alert(Alert.AlertType.INFORMATION, "Order has been placed successfully").show();

            orderId = orderService.generateOrderId();
            lblId.setText("Order Id " + orderId);
            cmbCustomerId.getSelectionModel().clearSelection();
            cmbItemCode.getSelectionModel().clearSelection();
            tblOrderDetails.getItems().clear();
            txtQty.clear();
            total();

        } catch (FailedOperationException | DuplicateIdentifierException | NotFoundException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            throw e;
        }
    }


}
