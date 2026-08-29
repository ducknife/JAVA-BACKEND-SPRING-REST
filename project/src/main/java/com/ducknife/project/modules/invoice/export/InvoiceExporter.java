package com.ducknife.project.modules.invoice.export;

import com.ducknife.project.modules.invoice.Invoice;

public interface InvoiceExporter {
    InvoiceExportType getType();
    String export(Invoice invoice);
}
