package com.ducknife.project.modules.invoice.mapper;

import org.mapstruct.Mapper;

import com.ducknife.project.modules.invoice.Invoice;
import com.ducknife.project.modules.invoice.dto.InvoiceResponse;
import com.ducknife.project.modules.order.mapper.OrderMapper;

@Mapper(componentModel = "spring", uses = { OrderMapper.class })
public interface InvoiceMapper {
    InvoiceResponse toResponse(Invoice invoice);
}
