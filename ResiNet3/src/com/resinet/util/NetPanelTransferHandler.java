package com.resinet.util;

import com.resinet.views.NetPanel;

import javax.swing.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Diese Klasse dient dazu, Copy&Paste-Aktionen des NetPanels zu Bearbeiten.
 */
public class NetPanelTransferHandler extends TransferHandler {
    private static final long serialVersionUID = -7944948131884847444L;

    /**
     * Wird etwa beim Einfügen ausgelöst.
     *
     * @param support Die Daten
     * @return Boolean
     * @see javax.swing.TransferHandler.TransferSupport
     */
    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support))
            return false;

        NetPanel netPanel = (NetPanel) support.getComponent();
        NodeEdgeWrapper nodeEdgeWrapper;
        try {
            nodeEdgeWrapper = (NodeEdgeWrapper) support.getTransferable().getTransferData(NetPanelTransferable.DATA_FLAVOR);
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
            return false;
        }

        netPanel.getController().pasteNodesAndEdges(nodeEdgeWrapper);
        return true;
    }

    /**
     * Wird ausgelöst, nachdem Daten kopiert wurden. Entfernt die kopierten Daten, falls sie ausgeschnitten wurden.
     *
     * @param source Die Quellkomponente
     * @param data   Die kopierten Daten
     * @param action Aktion, entweder MOVE oder COPY
     */
    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        if (action != MOVE) {
            return;
        }
        NetPanel netPanel = ((NetPanel) source);
        NetPanelTransferable transferable = (NetPanelTransferable) data;

        netPanel.getController().resetSelection();
        netPanel.getController().removeNodes(transferable.getTransferData().originalNodes);
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return support.isDataFlavorSupported(NetPanelTransferable.DATA_FLAVOR);
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        NetPanel netPanel = ((NetPanel) c);
        NodeEdgeWrapper data = netPanel.getController().getSelectionCopyData();
        if (data == null) {
            return null;
        }
        return new NetPanelTransferable(data);
    }
}
