package com.ducknife.project.modules.invoice.export;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ducknife.project.modules.invoice.Invoice;
import com.ducknife.project.modules.orderdetail.OrderDetail;
import com.ducknife.project.modules.product.Product;

@Component
public class DetailedInvoiceExporter implements InvoiceExporter {

    @Override
    public InvoiceExportType getType() {
        return InvoiceExportType.DETAILED;
    }

    @Override
    public String export(Invoice invoice) {
        StringBuilder result = new StringBuilder();
        List<OrderDetail> orderDetails = invoice.getOrder().getOrderDetails();
        result.append("Invoice #").append(invoice.getId()).append("\n");
        result.append("Order details:").append("\n");
        int cnt = 1;
        for (OrderDetail od : orderDetails) {
            Product product = od.getProduct();
            result.append("[#")
                    .append(cnt)
                    .append("] ")
                    .append(product.getName())
                    .append("-")
                    .append(String.format("%.2f", product.getPrice()))
                    .append(" x")
                    .append(od.getQuantity())
                    .append("\n");
            cnt++;
        }
        result.append(String.format("%.2f", invoice.getTotalPrice()));
        return result.toString();
    }
}
