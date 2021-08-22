package lk.ijse.dep7.service;

import lk.ijse.dep7.dbutils.SingleConnectionDataSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerServiceTest {
    @Test
    void saveCustomer() {

    }


    @Test
    void existCustomer() throws SQLException {
        Connection connection = SingleConnectionDataSource.getInstance().getConnection();
//        CustomerService customerService = new CustomerService();
        SingleConnectionDataSource.init("jdbc:mysql://localhost:3306/dep7_backup_pos", "root", "prasad");
//        assertTrue(customerService.existCustomer("C001"));
//        assertTrue(customerService.existCustomer("C002"));
    }
}