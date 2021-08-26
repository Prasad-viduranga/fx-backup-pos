package lk.ijse.dep7.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lk.ijse.dep7.dbutils.SingleConnectionDataSource;
import lk.ijse.dep7.dto.OrderDTO;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.service.OrderService;
import lk.ijse.dep7.util.SearchOrderTM;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class SearchOrdersFormController {

    private final OrderService orderService = new OrderService(SingleConnectionDataSource.getInstance().getConnection());
    public AnchorPane root;
    public TextField txtSearch;
    public TableView<SearchOrderTM> tblOrders;

    public void initialize() throws FailedOperationException {
        tblOrders.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("orderID"));
        tblOrders.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("date"));
        tblOrders.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("customerID"));
        tblOrders.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("customerName"));
        tblOrders.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("total"));


        loadAllOrders();

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                loadAllOrders();

            } catch (FailedOperationException e) {
                throw new RuntimeException(e);
            }

        });
    }


    public void loadAllOrders() throws FailedOperationException {

        try {
            List<OrderDTO> orderList = orderService.searchOrder(txtSearch.getText());
            tblOrders.getItems().clear();

            orderList.forEach(order -> tblOrders.getItems().add(new SearchOrderTM(order.getOrderId(),
                    order.getOrderDate(),
                    order.getCustomerId(),
                    order.getCustomerName(),
                    order.getOrderTotal().setScale(2))));

        } catch (FailedOperationException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to fetch orders").show();
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

    public void tblOrders_OnMouseClicked(MouseEvent mouseEvent) throws IOException {
        if (tblOrders.getSelectionModel().getSelectedItem() == null) {
            return;
        }
        if ((mouseEvent.getClickCount() == 2) && (tblOrders.getSelectionModel().getSelectedItem() != null)) {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/view/view-order-form.fxml"));
            stage.setScene(new Scene(fxmlLoader.load()));
            stage.show();
            stage.centerOnScreen();
            stage.sizeToScene();
        }
    }
}
