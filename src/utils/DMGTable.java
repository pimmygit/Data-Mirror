package utils;

import javax.swing.*;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: GB031042
 * Date: 29-Sep-2006
 * Time: 15:41:28
 * To change this template use File | Settings | File Templates.
 */
public class DMGTable extends JTable {

    private DMGTableModel _dmgTableModel;
    private Vector _rowData;

    public DMGTable() {
        super();
    }

    public DMGTable(DMGTableModel dmgTableModel) {
        _dmgTableModel = dmgTableModel;
    }

}
