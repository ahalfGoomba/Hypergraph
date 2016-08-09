package com.resinet.util;

import java.awt.datatransfer.*;
import java.io.IOException;

/**
 * Diese Klasse definiert das Verhalten und den Typ der aus dem Graphen kopierten Daten. Im Grunde handelt es sich um
 * Standardverhalten.
 */
class NetPanelTransferable implements Transferable, ClipboardOwner {
    static final DataFlavor DATA_FLAVOR = new DataFlavor(NodeEdgeWrapper.class, "Nodes and Edges");

    private final NodeEdgeWrapper copyData;

    NetPanelTransferable(NodeEdgeWrapper wrapper) {
        copyData = wrapper;
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        //ist egal
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DATA_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DATA_FLAVOR);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(DATA_FLAVOR)) {
            return copyData;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    NodeEdgeWrapper getTransferData() {
        return copyData;
    }
}
