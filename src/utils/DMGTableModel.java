package utils;

import javax.swing.table.AbstractTableModel;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: GB031042
 * Date: 29-Sep-2006
 * Time: 11:32:28
 * To change this template use File | Settings | File Templates.
 */
public class DMGTableModel extends AbstractTableModel {

    private Vector rowData = null;
    private Vector columnNames = null;

    public DMGTableModel() {
        super();
    }

    public DMGTableModel(Vector matrix, Vector colNames) {
        rowData = matrix;
        columnNames = colNames;
    }

    // Get the name of the specified table
    public String getColumnName(int col) {
        if (col > 3)
            return "Blah "+col;
        else
            return columnNames.get(col).toString();
        //return columnNames.get(col).toString();
    }

    // Number of rows in the table
    public int getRowCount() {
        return rowData.size();
    }

    // Well number of columns will be always 4
    public int getColumnCount() {
        return columnNames.size();
    }

    // DMG table is used only for reporting
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    // Get content of a cell as Object (I suppose it will never be used, which means never tested :-))
    public Object getValueAt(int row, int col) {

        if (rowData.get(row) instanceof Vector) {
            return ((Vector) rowData.get(row)).get(col);
        } else {
            return "";
        }
    }

    // Set content of a cell as Object (I suppose it will never be used, which means never tested :-))
    public void setValueAt(Object value, int row, int col) {

        // First update the value of the required row
        Vector rowVal = (Vector) rowData.get(row);
        rowVal.setElementAt(value, col);

        // Then update the row in the table
        rowData.setElementAt(rowVal, row);

        // Refresh the table
        fireTableCellUpdated(row, col);
    }

    // Add element to the table
    public void addElement(Object filename, Object action, Object location, Object status) {

        Vector newRow = new Vector();
        newRow.removeAllElements();
        newRow.add(filename);
        newRow.add(action);
        newRow.add(location);
        newRow.add(status);

        rowData.add(newRow);

        fireTableDataChanged();
    }

    // Remove all logs from the table
    public void clearTable() {
        rowData.clear();
        fireTableDataChanged();
    }

}
