package lk.ijse.dep7.service;

import lk.ijse.dep7.dto.ItemDTO;
import lk.ijse.dep7.dto.OrderDTO;
import lk.ijse.dep7.dto.OrderDetailsDTO;
import lk.ijse.dep7.exception.DuplicateIdentifierException;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFoundException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    public final Connection connection;

    public OrderService(Connection connection) {
        this.connection = connection;
    }

    public List<OrderDTO> searchOrder(String query) throws FailedOperationException {

        List<OrderDTO> orderList = new ArrayList<>();

        try {
            String[] searchWord = query.split("\\s");
            String sql = "SELECT o.*, c.name, order_total.total FROM `order` o INNER JOIN customer c on o.customer_id = c.id\n" +
                    "INNER JOIN\n" +
                    "(SELECT order_id, SUM(qty * unit_price) AS total FROM order_detail od GROUP BY  order_id) AS order_total\n" +
                    "ON o.id = order_total.order_id WHERE order_id LIKE ? OR date LIKE ? OR customer_id LIKE ? OR name LIKE ? ";

            for (int i = 1; i < searchWord.length; i++) {
                String condition = "AND (order_id LIKE ? OR date LIKE ? OR customer_id LIKE ? OR name LIKE ? )";
                sql += condition;
            }
            sql += ";";
            System.out.println(searchWord[0]);

            PreparedStatement pstm = connection.prepareStatement(sql);

            int j = 0;

            for (int i = 1; i <= searchWord.length * 4; i++) {
                pstm.setString(i, "%" + searchWord[j] + "%");
                if (i % 4 == 0) j++;
            }


//            pstm.setString(1, "%" + query + "%");
//            pstm.setString(2, "%" + query + "%");
//            pstm.setString(3, "%" + query + "%");
//            pstm.setString(4, "%" + query + "%");
            ResultSet rst = pstm.executeQuery();

            while (rst.next()) {
                orderList.add(new OrderDTO(rst.getString("id"), rst.getDate("date").toLocalDate(),
                        rst.getString("customer_id"), rst.getString("name"), rst.getBigDecimal("total")));
            }

            return orderList;
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to search operation");
        }

    }

    public String generateOrderId() throws FailedOperationException {
//        Method 1
//        int lastId = 0;
//
//        PreparedStatement stm = connection.prepareStatement("SELECT id FROM `order`;");
//        ResultSet rst = stm.executeQuery();
//        while (rst.next()) {
//            if (lastId < Integer.parseInt(rst.getString(1).split("OD")[1])) {
//                lastId = Integer.parseInt(rst.getString(1).split("OD")[1]);
//            }
//        }
//        lastId = lastId + 1;
//        String newOrderID = "OD" + lastId;
//        return newOrderID;

//        Method 2

        try {
            PreparedStatement stm = connection.prepareStatement("SELECT id FROM `order` ORDER BY id DESC LIMIT 1;");
            ResultSet rst = stm.executeQuery();
            return rst.next() ? String.format("OD%03d", (Integer.parseInt(rst.getString("id").replace("OD", "")) + 1)) : "OD001";
            /*if (rst.next()) {
                return String.format("OD%03d", (Integer.parseInt(rst.getString("id").replace("OD", "")) + 1));
            } else {
                return "OD001";
            }*/
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to generate a new order id");
        }
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
