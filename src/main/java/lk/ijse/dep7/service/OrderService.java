package lk.ijse.dep7.service;

import lk.ijse.dep7.dto.ItemDTO;
import lk.ijse.dep7.dto.OrderDetailsDTO;
import lk.ijse.dep7.exception.DuplicateIdentifierException;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFoundException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class OrderService {

    public final Connection connection;

    public OrderService(Connection connection) {
        this.connection = connection;
    }

    public void saveOrder(String orderId, LocalDate date, String customerId, List<OrderDetailsDTO> orderDetails) throws FailedOperationException, DuplicateIdentifierException, NotFoundException {

        CustomerService customerService = new CustomerService(connection);
        ItemService itemService = new ItemService(connection);
        try {
            PreparedStatement stm = connection.prepareStatement("SELECT id FROM `order` WHERE id=?");
            stm.setString(1, orderId);

            if (stm.executeQuery().next()) {
                throw new DuplicateIdentifierException(orderId + " already exists");
            }
            if (!customerService.existCustomer(customerId)) {
                throw new DuplicateIdentifierException("Customer id doesn't exists");
            }

            connection.setAutoCommit(false);
            stm = connection.prepareStatement("INSERT INTO `order` (id, date, customer_id) VALUES (?,?,?)");
            stm.setString(1, orderId);
            stm.setDate(2, Date.valueOf(date));
            stm.setString(3, customerId);

            if (stm.executeUpdate() != 1) {
                throw new FailedOperationException("Failed to save the order");
            }
            stm = connection.prepareStatement("INSERT INTO order_detail (order_id, item_code, unit_price, qty) VALUES (?,?,?,?)");

            for (OrderDetailsDTO orderDetailsDTO : orderDetails) {
                stm.setString(1, orderId);
                stm.setString(2, orderDetailsDTO.getItemCode());
                stm.setBigDecimal(3, orderDetailsDTO.getUnitPrice());
                stm.setInt(4, orderDetailsDTO.getQty());

                if (stm.executeUpdate() != 1) {
                    throw new FailedOperationException("Failed to save the order");
                }

                ItemDTO item = itemService.findItem(orderDetailsDTO.getItemCode());
                item.setQty(item.getQty() - orderDetailsDTO.getQty());
                itemService.updateItem(item);
            }
            connection.commit();

        } catch (SQLException e) {
            failedOperationExecutionContext(connection::rollback);
        } catch (Throwable t) {
            failedOperationExecutionContext(connection::rollback);
            throw t;
        } finally {
            failedOperationExecutionContext(() -> connection.setAutoCommit(true));
        }
    }

    private void failedOperationExecutionContext(ExecutionContext context) throws FailedOperationException {
        try {
            context.execute();
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to save the order ");
        }
    }

    @FunctionalInterface
    interface ExecutionContext {
        void execute() throws SQLException;
    }
}
