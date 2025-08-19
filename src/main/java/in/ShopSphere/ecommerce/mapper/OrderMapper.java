package in.ShopSphere.ecommerce.mapper;

import in.ShopSphere.ecommerce.dto.order.OrderRequest;
import in.ShopSphere.ecommerce.dto.order.OrderResponse;
import in.ShopSphere.ecommerce.model.entity.Order;
import in.ShopSphere.ecommerce.model.entity.ProductVariant;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "shippingAmount", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "shippingAddress", ignore = true)
    @Mapping(target = "billingAddress", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "estimatedDeliveryDate", ignore = true)
    @Mapping(target = "actualDeliveryDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toOrder(OrderRequest request);
    
    @Mapping(target = "user", source = "user")
    @Mapping(target = "shippingAddress", source = "shippingAddress")
    @Mapping(target = "billingAddress", source = "billingAddress")
    @Mapping(target = "items", source = "items")
    OrderResponse toOrderResponse(Order order);
    
    List<OrderResponse> toOrderResponseList(List<Order> orders);
    
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productImage", expression = "java(orderItem.getProduct().getPrimaryImageUrl())")
    @Mapping(target = "variant", expression = "java(toProductVariantInfo(orderItem.getProduct().getVariants().stream().findFirst().orElse(null)))")
    OrderResponse.OrderItemResponse toOrderItemResponse(in.ShopSphere.ecommerce.model.entity.OrderItem orderItem);
    
    List<OrderResponse.OrderItemResponse> toOrderItemResponseList(List<in.ShopSphere.ecommerce.model.entity.OrderItem> orderItems);
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "variantName", source = "variantName")
    @Mapping(target = "variantValue", source = "variantValue")
    @Mapping(target = "priceAdjustment", source = "priceAdjustment")
    OrderResponse.OrderItemResponse.ProductVariantInfo toProductVariantInfo(ProductVariant productVariant);
}
