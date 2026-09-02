package com.ducknife.project.modules.invoice;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ducknife.project.common.ResponseFactory;
import com.ducknife.project.modules.invoice.dto.InvoiceResponse;
import com.ducknife.project.modules.invoice.export.InvoiceExportType;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<ResponseFactory<List<InvoiceResponse>>> getInvoices() {
        return ResponseFactory.ok(invoiceService.getInvoices());
    }

    @GetMapping("/orders/{orderId}/invoice")
    public ResponseEntity<ResponseFactory<InvoiceResponse>> getInvoiceByOrderId(
        @PathVariable Long orderId
    ) {
        return ResponseFactory.ok(invoiceService.getInvoiceByOrderId(orderId));
    }

    @GetMapping("/info")
    public ResponseEntity<ResponseFactory<String>> getInvoiceInfo(
        @RequestParam Long invoiceId,
        @RequestParam(required = false, defaultValue = "SIMPLE") InvoiceExportType type
    ) {
        return ResponseFactory.ok(invoiceService.getInfo(invoiceId, type));
    }
    
}
