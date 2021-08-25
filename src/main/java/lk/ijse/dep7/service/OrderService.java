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

    public Connection connection;

    public void saveOrder(String orderId, LocalDate date, String customerId, List<OrderDetailsDTO> orderDetails) throws FailedOperationException, DuplicateIdentifierException, NotFoundException {

        CustomerService customerService = new CustomerService(connection);
        ItemService itemService = new ItemService(connection);
        try {
            PreparedStatement pstm = connection.prepareStatement("SELECT id FROM `order` WHERE id=?");
            pstm.setString(1, orderId);

            if (pstm.executeQuery().next()) {
                throw new DuplicateIdentifierException(orderId + " already exists");
            }
            if (!customerService.existCustomer(customerId)) {
                throw new DuplicateIdentifierException("Customer id doesn't exists");
            }

            connection.setAutoCommit(false);
            pstm = connection.prepareStatement("INSERT INTO `order` (id, date, customer_id) VALUES (?,?,?)");
            pstm.setString(1, orderId);
            pstm.setDate(2, Date.valueOf(date));
            pstm.setString(3, customerId);

            if (pstm.executeUpdate() != 1) {
                throw new FailedOperationException("Failed to save the order");
            }
            pstm = connection.prepareStatement("INSERT INTO order_detail (order_id, item_code, unit_price, qty) VALUES (?,?,?,?)");

            for (OrderDetailsDTO orderDetailsDTO : orderDetails) {
                pstm.setString(1, orderId);
                pstm.setString(2, orderDetailsDTO.getItemCode());
                pstm.setBigDecimal(3, orderDetailsDTO.getUnitPrice());
                pstm.setInt(4, orderDetailsDTO.getQty());

                if (pstm.executeUpdate() != 1) {
                    throw new FailedOperationException("Failed to save the order");
                }

                ItemDTO item = itemService.findItem(orderDetailsDTO.getItemCode());
                item.setQty(item.getQty() - orderDetailsDTO.getQty());
                itemService.updateItem(item);
            }
            connection.commit();

        } catch (Throwable t) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new FailedOperationException("Failed to rollback the transaction ");
            }
            if (t instanceof DuplicateIdentifierException || t instanceof FailedOperationException || t instanceof NotFoundException) {

                try {
                    throw t;
                } catch (SQLException e) {
                    throw new FailedOperationException("Failed to save the order ");
                }
            } else {
                throw new FailedOperationException("Failed to save the order");
            }

        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new FailedOperationException("Failed to save the order ");
            }
        }
    }

}
