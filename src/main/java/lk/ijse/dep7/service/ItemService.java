package lk.ijse.dep7.service;

import lk.ijse.dep7.dbutils.SingleConnectionDataSource;
import lk.ijse.dep7.dto.ItemDTO;
import lk.ijse.dep7.exception.DuplicateIdentifierException;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItemService {
    private Connection connection = SingleConnectionDataSource.getInstance().getConnection();

    public ItemService(Connection connection) {

    }

    private boolean existItem(String code) throws SQLException {
        PreparedStatement pstm = connection.prepareStatement("SELECT code FROM item WHERE code=?");
        pstm.setString(1, code);
        ResultSet rst = pstm.executeQuery();
        return (rst.next());
    }

    public void saveItem(ItemDTO itemDTO) throws DuplicateIdentifierException {
        try {
            if (existItem(itemDTO.getCode())) {
                throw new DuplicateIdentifierException("Invalid Item code" + itemDTO.getCode());
            }
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO item(code,description,qty_on_hand,unit_price) VALUES (?,?,?,?)");
            pstm.setString(1, itemDTO.getCode());
            pstm.setString(2, itemDTO.getDescription());
            pstm.setString(3, String.valueOf(itemDTO.getQty()));
            pstm.setString(4, String.valueOf(itemDTO.getUnitPrice()));
            pstm.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void updateItem(ItemDTO itemDTO) throws FailedOperationException {

        try {
            PreparedStatement pstm = connection.prepareStatement("UPDATE item SET description=?,qty_on_hand=?,unit_price=? WHERE code=?");
            pstm.setString(1, itemDTO.getDescription());
            pstm.setInt(2, itemDTO.getQty());
            pstm.setBigDecimal(3, itemDTO.getUnitPrice());
            pstm.setString(4, itemDTO.getCode());
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to update the item" + itemDTO.getCode());
        }
    }


    public void deleteItem(String code) throws FailedOperationException, NotFoundException {
        try {
            if (!existItem(code)) {
                throw new NotFoundException("There is no such item associated with the id" + code);
            }
            PreparedStatement pstm = connection.prepareStatement("DELETE FROM item WHERE code=?");
            pstm.setString(1, code);
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to delete the item" + code);
        }

    }

    public ItemDTO findItem(String code) throws FailedOperationException, NotFoundException {
        try {
            if (!existItem(code)) {
                throw new NotFoundException("There is no such item with " + code);
            }
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM item WHERE code=?");
            pstm.setString(1, code);
            ResultSet rst = pstm.executeQuery();
            rst.next();
            return new ItemDTO(rst.getString(1), rst.getString(2), rst.getInt(4), rst.getBigDecimal(3));


        } catch (SQLException e) {
            throw new FailedOperationException("Failed to Find the Item " + code);
        }

    }

    public List<ItemDTO> findAllItem() throws FailedOperationException {
        List<ItemDTO> allItem = new ArrayList<>();
        try {
            PreparedStatement pstmAllItem = connection.prepareStatement("SELECT * FROM item");
            ResultSet rst = pstmAllItem.executeQuery();
            while (rst.next()) {

                allItem.add(new ItemDTO(rst.getString(1), rst.getString(2), rst.getInt(4), rst.getBigDecimal(3)));
            }
            return allItem;
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to load items");
        }

    }

}
