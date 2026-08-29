package com.ducknife.project.modules.invoice.export;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ducknife.project.common.exception.InvalidRequestException;

@Component
public class InvoiceExporterFactory {

    private final Map<InvoiceExportType, InvoiceExporter> exporters;

    public InvoiceExporterFactory(List<InvoiceExporter> list) {
        this.exporters = list.stream()
                .collect(Collectors.toMap(InvoiceExporter::getType, Function.identity()));
    }

    public InvoiceExporter get(InvoiceExportType type) {
        InvoiceExporter exporter = exporters.get(type);
        if (exporter == null) {
            throw new InvalidRequestException("Không hỗ trợ export này");
        }
        return exporter;
    }
}
