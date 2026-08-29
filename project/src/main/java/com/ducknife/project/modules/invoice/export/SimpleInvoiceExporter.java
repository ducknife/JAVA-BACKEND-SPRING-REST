package com.ducknife.project.modules.invoice.export;

import org.springframework.stereotype.Component;

import com.ducknife.project.modules.invoice.Invoice;

@Component
public class SimpleInvoiceExporter implements InvoiceExporter {

    @Override
    public InvoiceExportType getType() {
        return InvoiceExportType.SIMPLE;
    }

    @Override
    public String export(Invoice invoice) {
        StringBuilder result = new StringBuilder();
        result.append("Invoice: #").append(invoice.getId()).append("\n");
        result.append("Total price: ").append(String.format("%.2f", invoice.getTotalPrice()));
        return result.toString();
    }
}
