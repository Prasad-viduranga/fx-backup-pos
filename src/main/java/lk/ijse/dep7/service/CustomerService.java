package lk.ijse.dep7.service;

import lk.ijse.dep7.dbutils.SingleConnectionDataSource;
import lk.ijse.dep7.dto.CustomerDTO;
import lk.ijse.dep7.exception.DuplicateIdentifierException;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerService {

    private Connection connection = SingleConnectionDataSource.getInstance().getConnection();

    public CustomerService(Connection connection) {

    }

    public void saveCustomer(CustomerDTO customer) throws DuplicateIdentifierException, FailedOperationException {
        try {
            if (existCustomer(customer.getId())) {
                throw new DuplicateIdentifierException(customer.getId() + " already exists");
            }

            PreparedStatement pstmSaveCustomer = connection.prepareStatement("INSERT INTO customer(id,name,address) VALUES (?,?,?)");
            pstmSaveCustomer.setString(1, customer.getId());
            pstmSaveCustomer.setString(2, customer.getName());
            pstmSaveCustomer.setString(3, customer.getAddress());
            int affectedRow = pstmSaveCustomer.executeUpdate();
            System.out.println(affectedRow);

        } catch (SQLException e) {
            throw new FailedOperationException("Failed to save the customer");
        }
    }

    private boolean existCustomer(String id) throws SQLException {

        PreparedStatement pstmExistCustomer = connection.prepareStatement("SELECT id FROM customer WHERE id=?");
        pstmExistCustomer.setString(1, id);
        return pstmExistCustomer.executeQuery().next();
    }

    public void updateCustomer(CustomerDTO customer) throws FailedOperationException, NotFoundException {
        try {
            if (!existCustomer(customer.getId())) {
                throw new NotFoundException("There is no such customer associated with the id" + customer.getId());
            }

            PreparedStatement pstmUpdateCustomer = connection.prepareStatement("UPDATE customer SET name=?,address=? WHERE id=?");
            pstmUpdateCustomer.setString(1, customer.getName());
            pstmUpdateCustomer.setString(2, customer.getAddress());
            pstmUpdateCustomer.setString(3, customer.getId());
            pstmUpdateCustomer.executeUpdate();
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to update the customer" + customer.getId());
        }
    }

    public void deleteCustomer(String id) throws NotFoundException, FailedOperationException {
        try {
            if (!existCustomer(id)) {
                throw new NotFoundException("There is no such customer associated with the id" + id);
            }

            PreparedStatement pstmDeleteCustomer = connection.prepareStatement("DELETE FROM customer WHERE id=?");
            pstmDeleteCustomer.setString(1, id);
            pstmDeleteCustomer.executeUpdate();
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to delete the customer" + id);
        }
    }

    public CustomerDTO findCustomer(String id) throws NotFoundException, FailedOperationException {
        try {
            if (!existCustomer(id)) {
                throw new NotFoundException("There is no such customer associated with the id" + id);
            }
            PreparedStatement pstmFindCustomer = connection.prepareStatement("SELECT * FROM customer WHERE id=?");
            pstmFindCustomer.setString(1, id);
            ResultSet rst = pstmFindCustomer.executeQuery();
            rst.next();
            return new CustomerDTO(rst.getString(1), rst.getString(2), rst.getString(3));

        } catch (SQLException e) {
            throw new FailedOperationException("Failed to find the customer" + id);
        }

    }

    public List<CustomerDTO> findAllCustomer() throws FailedOperationException {
        List<CustomerDTO> customerList = new ArrayList<>();

        try {
            PreparedStatement pstmFindAll = connection.prepareStatement("SELECT * FROM customer");
            ResultSet rst = pstmFindAll.executeQuery();
            while (rst.next()) {
                customerList.add(new CustomerDTO(rst.getString(1), rst.getString(2), rst.getString(3)));
            }
            return customerList;

        } catch (SQLException e) {
            throw new FailedOperationException("Failed to find the customers");

        }
    }
}
