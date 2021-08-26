package lk.ijse.dep7.controller;

import com.jfoenix.controls.JFXTextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import lk.ijse.dep7.dbutils.SingleConnectionDataSource;
import lk.ijse.dep7.dto.OrderDetailsDTO;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFoundException;
import lk.ijse.dep7.service.ItemService;
import lk.ijse.dep7.service.OrderService;
import lk.ijse.dep7.util.ViewOrderTM;

import java.math.BigDecimal;
import java.rmi.AlreadyBoundException;
import java.time.LocalDate;
import java.util.List;

public class ViewOrderFormController {
    public AnchorPane root;
    public JFXTextField txtCustomerName;
    public TableView<ViewOrderTM> tblOrderDetails;
    public Label lblDate;
    public Label lblId;
    public Label lblTotal;
    public JFXTextField txtCustomerID;

    private final OrderService orderService = new OrderService(SingleConnectionDataSource.getInstance().getConnection());
    private final ItemService itemService = new ItemService(SingleConnectionDataSource.getInstance().getConnection());

    public void initialize() {
        tblOrderDetails.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("code"));
        tblOrderDetails.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblOrderDetails.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("qty"));
        tblOrderDetails.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        tblOrderDetails.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("total"));


    }

    public void initWithData(String orderID, LocalDate date, String customerID, String customerName, BigDecimal total) {
        lblId.setText("Order Id : " + orderID);
        lblDate.setText(date.toString());
        txtCustomerID.setText(customerID);
        txtCustomerName.setText(customerName);
        lblTotal.setText("Total: " + total.setScale(2).toString());
        try {
            List<OrderDetailsDTO> orderDetails = orderService.findOrderDetails(orderID);
            orderDetails.forEach(details -> {
                try {
                    tblOrderDetails.getItems().add(new ViewOrderTM(details.getItemCode(),
                            itemService.findItem(details.getItemCode()).getDescription(), details.getQty(), details.getUnitPrice(),
                            details.getUnitPrice().multiply(new BigDecimal(details.getQty())).setScale(2)));

                } catch (FailedOperationException |NotFoundException e) {
                    new Alert(Alert.AlertType.ERROR,e.getMessage()).show();
                    throw new RuntimeException(e);
                }
            });
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (FailedOperationException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            throw new RuntimeException(e);
        }
    }
}