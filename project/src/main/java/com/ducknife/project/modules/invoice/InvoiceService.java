package com.ducknife.project.modules.invoice;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ducknife.project.common.exception.ResourceNotFoundException;
import com.ducknife.project.modules.invoice.dto.InvoiceResponse;
import com.ducknife.project.modules.invoice.export.InvoiceExportType;
import com.ducknife.project.modules.invoice.export.InvoiceExporterFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceExporterFactory invoiceExporterFactory;

    public List<InvoiceResponse> getInvoices() {
        return invoiceRepository.findAll().stream()
                .map(InvoiceResponse::from)
                .collect(Collectors.toList());
    }

    public InvoiceResponse getInvoiceByOrderId(Long orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        return InvoiceResponse.from(invoice);
    }

    public String getInfo(Long invoiceId, InvoiceExportType type) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hoá đơn"));
        return invoiceExporterFactory.get(type).export(invoice);
    }
}
